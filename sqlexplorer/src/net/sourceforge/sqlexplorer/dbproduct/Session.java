/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.dbproduct;

import java.sql.SQLException;
import java.util.LinkedList;

import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbdetail.DetailTabManager;
import net.sourceforge.sqlexplorer.dbstructure.DatabaseModel;
import net.sourceforge.sqlexplorer.dbstructure.nodes.DatabaseNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.DictionaryLoader;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.squirrel_sql.fw.sql.SQLDatabaseMetaData;

/**
 * The SessionTreeNode represents one active database session.
 * 
 * @modified Davy Vanherbergen
 */
public class Session {
	
	/*
	 * A QueuedTask is run once the current connection becomes idle
	 */
	private interface QueuedTask {
		public void run() throws SQLException;
	}

    // User definition we connect as
    private User user;

    // Connection to the database
    private SQLConnection connection;
    
    // Whether the connection is currently "grabbed" by calling code
    private boolean connectionInUse;
    
	// Database Model
    private DatabaseModel dbModel;

    // Meta data
    private SQLDatabaseMetaData _metaData = null;

    // Whether content assist is enabled
    boolean _assistanceEnabled;

    // The dictionary used for content assist
    private Dictionary _dictionary = new Dictionary();
    
    // List of tasks to execute when the current connection is freed up
    private LinkedList<QueuedTask> queuedTasks = new LinkedList<QueuedTask>();

    /**
     * Constructor; ties this SessionTreeNode to a User configuration but
     * does not allocate a SQL connection until required.
     * @param user
     */
    /*package*/ Session(User user) throws SQLException {
        this.user = user;
        dbModel = new DatabaseModel(this);
        
        _assistanceEnabled = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.SQL_ASSIST);
        if (_assistanceEnabled) {
            // schedule job to load dictionary for this session
            DictionaryLoader dictionaryLoader = new DictionaryLoader(this);
            dictionaryLoader.schedule(500);
        }
    }
    
    /**
     * Returns true if the session is valid
     * @return
     */
    public synchronized boolean isValidSession() {
    	return user != null;
    }
    
    /**
     * Grabs a connection; note that releaseConnection MUST be called to return the connection
     * for use by other code.
     * @return
     * @throws ExplorerException
     */
    public synchronized SQLConnection grabConnection() throws SQLException {
    	if (user == null)
    		throw new IllegalStateException("Session invalid (closed)");
    	if (connectionInUse)
    		throw new IllegalStateException("Cannot grab a new connection - already in use");
    	connectionInUse = true;
    	
    	// If we don't have one yet, get one from the pool
    	if (connection == null)
    		connection = user.getConnection();
    	connection.setAutoCommit(getUser().isAutoCommit());
    	connection.setCommitOnClose(getUser().isCommitOnClose());
    	return connection;
    }
    
    /**
     * Releases the connection; if the connection does NOT have auto-commit, this session
     * will hang on to it for next time, otherwise it is returned to the pool
     * @param toRelease
     * @throws ExplorerException
     */
    public synchronized void releaseConnection(SQLConnection toRelease) {
    	if (!connectionInUse)
    		throw new IllegalStateException("Cannot release connection - not inuse");
    	if (connection != toRelease)
    		throw new IllegalArgumentException("Attempt to release the wrong connection");
    	
    	// Run any queued tasks
    	try {
	    	while (!queuedTasks.isEmpty()) {
	    		QueuedTask task = queuedTasks.removeFirst();
	    		task.run();
	    	}
    	}catch(SQLException e) {
    		SQLExplorerPlugin.error("Failed running queued task", e);
    	}

    	connectionInUse = false;
    	
    	// If it's not auto-commit, then we have to keep the connection
    	try {
	    	if (!connection.getAutoCommit())
	    		return;
	    	if (connection.getCommitOnClose())
	    		connection.commit();
	    	else
	    		connection.rollback();
    	}catch(SQLException e) {
    		SQLExplorerPlugin.error("Cannot commit", e);
    	}
    	
    	// Give it back into the pool
    	try {
    		SQLConnection connection = this.connection;
    		this.connection = null;
	    	user.releaseConnection(connection);
    	}catch(SQLException e) {
    		SQLExplorerPlugin.error("Cannot release connection", e);
    	}
    }
    
    /**
     * Returns whether the connection is in use or not
     * @return
     */
    public synchronized boolean isConnectionInUse() {
    	return connectionInUse;
    }
    
    /**
     * Queues a task to be completed at the end of the current
     * @param task
     * @throws SQLException
     */
    protected void queueTask(QueuedTask task) throws ExplorerException {
    	// If we have a connection and it's not in use, then just run it
    	if (connection != null && !connectionInUse) {
    		try {
    			task.run();
    		}catch(SQLException e) {
    			throw new ExplorerException(e);
    		}
    		return;
    	}
    	
    	// Queue it
    	queuedTasks.add(task);
    	
    	// If the connection's not in use, grab one and release; the grab
    	//	ensures we have a connection, and the release flushes the queue
    	if (!connectionInUse) {
    		try {
    			grabConnection();
    		}catch(SQLException e) {
    			throw new ExplorerException(e);
    		} finally {
    			releaseConnection(connection);
    		}
    	}
    }
    
    /**
     * Closes the session and returns any connection to the pool.  Note that isConnectionInUse() must
     * return false or close() will throw an exception.
     * @throws ExplorerException
     */
    public synchronized void close() {
    	if (connectionInUse)
    		throw new IllegalAccessError("Cannot close session while connection is still in use!");
    	
        // store dictionary
        _dictionary.store();
        
        // clear detail tab cache
        DetailTabManager.clearCacheForSession(this);

        // Disconnection from the user
        if (connection != null) {
        	try {
        		user.releaseConnection(connection);
        	}catch(SQLException e) {
        		SQLExplorerPlugin.error(e);
        	}
        	connection = null;
        }
        user.releaseSession(this);
        user = null;
    }

    /**
     * Returns true if the connection is set to auto-commit
     * @return
     */
    public boolean isAutoCommitMode() {
        boolean result = false;
        try {
            result = connection != null && connection.getAutoCommit();
        } catch (Throwable e) {
        }
        return result;
    }
    
    /**
     * Commits the connection; this will queue if the connection is
     * currently in use
     *
     */
    public synchronized void commit() throws ExplorerException {
    	queueTask(new QueuedTask() {
			public void run() throws SQLException {
				connection.commit();
			}
    	});
    }

    /**
     * Rolls back the connection; this will queue if the connection is
     * currently in use
     *
     */
    public synchronized void rollback() throws ExplorerException {
    	queueTask(new QueuedTask() {
			public void run() throws SQLException {
				connection.rollback();
			}
    	});
    }

    /**
     * Changes the catalog of the connection; this will queue if the connection is
     * currently in use
     *
     */
    public synchronized void setCatalog(final String catalog) throws ExplorerException {
    	queueTask(new QueuedTask() {
			public void run() throws SQLException {
				connection.setCatalog(catalog);
			}
    	});
    }

    /**
     * Returns true if the connection support catalogs
     * @return
     * @throws ExplorerException
     */
    public boolean supportsCatalogs() {
    	try {
    		return getMetaData().supportsCatalogs();
    	}catch(SQLException e) {
    		SQLExplorerPlugin.error("Cannot get meta data", e);
    		return false;
    	}
    }

    /**
     * Gets (and caches) the meta data for this connection
     * @return
     * @throws ExplorerException
     */
    public synchronized SQLDatabaseMetaData getMetaData() throws SQLException {
        if (_metaData == null) {
        	if (connection != null)
        		_metaData = connection.getSQLMetaData();
        	else {
        		grabConnection();
        		try {
            		_metaData = connection.getSQLMetaData();
        		} finally {
        			releaseConnection(connection);
        		}
        	}
        }
        
        return _metaData;
    }

    /**
     * Returns the root database node 
     * @return
     */
    public DatabaseNode getRoot() {
    	return dbModel.getRoot();
    }

    public Dictionary getDictionary() {
        return _dictionary;
    }

	public User getUser() {
		return user;
	}
	
	/*package*/ void setUser(User user) {
		this.user = user;
	}
	
	public AliasManager getAliases() {
		return SQLExplorerPlugin.getDefault().getAliasManager();
	}
    
    public String toString() {
    	return user.toString();
    }
    
    public DatabaseProduct getDatabaseProduct() {
    	return getUser().getAlias().getDriver().getDatabaseProduct();
    }
}
