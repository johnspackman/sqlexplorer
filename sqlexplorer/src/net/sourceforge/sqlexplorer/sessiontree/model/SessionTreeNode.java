package net.sourceforge.sqlexplorer.sessiontree.model;

/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.IdentifierFactory;
import net.sourceforge.sqlexplorer.dbdetail.DetailTabManager;
import net.sourceforge.sqlexplorer.dbstructure.DatabaseModel;
import net.sourceforge.sqlexplorer.dbstructure.nodes.DatabaseNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.DictionaryLoader;
import net.sourceforge.squirrel_sql.fw.id.IIdentifier;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import net.sourceforge.squirrel_sql.fw.sql.SQLDatabaseMetaData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.widgets.Table;

/**
 * The SessionTreeNode represents one active database session.
 * 
 * @modified Davy Vanherbergen
 */
public class SessionTreeNode implements ISessionTreeNode {

    private ISQLAlias _alias;

    boolean _assistanceEnabled;

    private SQLConnection _backgroundConnection;

    private SQLConnection _interactiveConnection;
    
    private boolean _backgroundConnectionInUse = false;

    private List _connectionNumberQueue = new ArrayList();

    private long _created;

    private Dictionary _dictionary = new Dictionary();

    private IIdentifier _id = IdentifierFactory.getInstance().createIdentifier();

    private ListenerList _listeners = new ListenerList();

    private SQLDatabaseMetaData _metaData = null;

    private SessionTreeModel _model;
    
    private int _nextConnectionNumber = 0;

    private RootSessionTreeNode _parent;

    final private String _password;
    
    public DatabaseModel dbModel;
       
    private ArrayList ls = new ArrayList(10);
    
    Table table;
    
    private static final int COMMIT_REQUEST = -1;
    
    private static final int ROLLBACK_REQUEST = -2;
    
    private static final int CATALOG_CHANGE_REQUEST = -3;
    
    private String _newCatalog;
    
    private static final Log _logger = LogFactory.getLog(SessionTreeNode.class);
    
    public SessionTreeNode(final SQLConnection[] conn, ISQLAlias alias, SessionTreeModel md, IProgressMonitor monitor, final String password)
            throws InterruptedException {
        
        _created = System.currentTimeMillis();
        _interactiveConnection = conn[0];
        _backgroundConnection = conn[1];
        _alias = alias;
        dbModel = new DatabaseModel(this);
        _model = md;
        _parent = md.getRoot();
        _parent.add(this);
        _password = password;
        
        _assistanceEnabled = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.SQL_ASSIST);
               
        if (_assistanceEnabled) {
            // schedule job to load dictionary for this session
            DictionaryLoader dictionaryLoader = new DictionaryLoader(this);
            dictionaryLoader.schedule(500);
            
        }
            
    }

    public void add(ISessionTreeNode n) {
        ls.add(n);
    }
    
    
    
    /**
     * Returns an SQLConnection. This connection should only
     * be used to execute statements in the UI thread.
     */
    public SQLConnection getInteractiveConnection() {
    
        return _interactiveConnection;
    }

    public void addListener(ISessionTreeClosedListener listener) {
        _listeners.add(listener);
    }
    
    public void close() {

        // store dictionary
        _dictionary.store();
        
        // clear detail tab cache
        DetailTabManager.clearCacheForSession(this);

        _parent.remove(this);

        Object[] ls = _listeners.getListeners();

        for (int i = 0; i < ls.length; ++i) {
            try {
                ((ISessionTreeClosedListener) ls[i]).sessionTreeClosed();
            } catch (Throwable e) {
            }

        }
        _model.modelChanged(null);
        try {
            _interactiveConnection.close();
        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error closing interactive database connection", e);
        }
        try {
            _backgroundConnection.close();
        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error closing background database connection", e);
        }

    }
    
   
    
    public synchronized void commit() {
        try {
            
            if (_connectionNumberQueue.size() == 0 && !_backgroundConnectionInUse) {
                // nothing is happening, so we can commit immediately
                _backgroundConnection.commit();
            } else {
                // there are still queries in the queue, so we add the commit
                // request to the end of the queue.
                _connectionNumberQueue.add(new Integer(COMMIT_REQUEST));
            }
            
        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error committing ", e);
        }

    }


    public ISQLAlias getAlias() {
        return _alias;
    }


    public String getCatalog() {
        String cat = "";
        try {
            cat = _interactiveConnection.getCatalog();
        } catch (Throwable e) {
        }
        return cat;
    }


    public String[] getCatalogs() {

        List catalogs = ((DatabaseNode) dbModel.getChildNodes()[0]).getCatalogs();
        String[] catalogNames = new String[catalogs.size()];

        Iterator it = catalogs.iterator();
        int i = 0;

        while (it.hasNext()) {
            INode node = (INode) it.next();
            if (node != null) {
                catalogNames[i] = node.toString();
                i++;
            }
        }

        return catalogNames;
    }


    /**
     * @see org.gnu.amaz.ISessionTreeNode#getChildren()
     */
    public Object[] getChildren() {
        return ls.toArray();
    }


    /**
     * @return time this session was created
     */
    public long getCreated() {
        return _created;
    }


    public String getCurrentConnectionPassword() {
        return _password;
    }


    public Dictionary getDictionary() {
        return _dictionary;
    }


    public IIdentifier getIdentifier() {
        return _id;
    }


    public SQLDatabaseMetaData getMetaData() {
        
        if (_metaData == null) {
            _metaData = _interactiveConnection.getSQLMetaData();
        }
        
        return _metaData;
    }


    /**
     * @see org.gnu.amaz.ISessionTreeNode#getParent()
     */
    public Object getParent() {
        return _parent;
    }


    /**
     * Get the connection with queue number 'number'.  This method will
     * return null until the queue number has been reached and the connection 
     * is available.
     */
    public synchronized SQLConnection getQueuedConnection(Integer number) {
        
        if (_backgroundConnectionInUse || number == null) {
            return null;
        }
        
        Integer currentNumber = (Integer) _connectionNumberQueue.get(0);
        
        if (currentNumber.intValue() == number.intValue()) {
            _backgroundConnectionInUse = true;
            _logger.debug("Connection " + number + " acquired.");
            return _backgroundConnection;
        }

        return null;
        
    }


    public synchronized Integer getQueuedConnectionNumber() {
        
        Integer number = new Integer(_nextConnectionNumber);
        _connectionNumberQueue.add(number);
        _nextConnectionNumber++;
        return number;
        
    }



    public DatabaseNode getRoot() {
        return dbModel.getRoot();
    }


    public boolean isAutoCommitMode() {
        boolean result = false;
        try {
            result = _interactiveConnection.getAutoCommit();
        } catch (Throwable e) {
        }
        return result;
    }


    /**
     * Release the currently active connection so we can move on to the next one.
     */
    public synchronized void releaseQueuedConnection(Integer number) {
        
        if (number == null) {
            return;
        }
        

        if (_connectionNumberQueue.indexOf(number) == 0) {
            // release current connection
            _backgroundConnectionInUse = false;
            _connectionNumberQueue.remove(0);
            _logger.debug("Connection " + number + " released.");
            
            // check for pending commit or rollback requests

            while (_connectionNumberQueue.size() > 0) {
                int nextNumber = ((Integer) _connectionNumberQueue.get(0)).intValue();
                try {
                    if (nextNumber == COMMIT_REQUEST) {
                        _logger.debug("Committing.");
                        _connectionNumberQueue.remove(0);
                        _backgroundConnection.commit();
                    } else if (nextNumber == ROLLBACK_REQUEST) {
                        _logger.debug("Rolling back.");
                        _connectionNumberQueue.remove(0);
                        _backgroundConnection.rollback();                        
                    } else if (nextNumber == CATALOG_CHANGE_REQUEST) {
                        _logger.debug("Changing catalog.");
                        _connectionNumberQueue.remove(0);
                        _backgroundConnection.setCatalog(_newCatalog);
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    _logger.error("Couldn't perform commit/rollback or catalog change.", e);
                }
            }
            
        } else {
            // remove pending queue number
            _connectionNumberQueue.remove(number);
            _logger.debug("Connection request " + number + " removed from queue.");
        }
     
    }


    public void remove(ISessionTreeNode n) {
        ls.remove(n);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */

    public synchronized void rollback() {
        try {
            
            if (_connectionNumberQueue.size() == 0 && !_backgroundConnectionInUse) {
                // nothing is happening, so we can rollback immediately
                _backgroundConnection.rollback();
            } else {
                // there are still queries in the queue, so we add the rollback
                // request to the end of the queue.
                _connectionNumberQueue.add(new Integer(ROLLBACK_REQUEST));
            }
            
        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error rollbacking ", e);

        }
    }


    public synchronized void setCatalog(String cat) throws SQLException {
        
        _interactiveConnection.setCatalog(cat);
        
        if (_connectionNumberQueue.size() == 0 && !_backgroundConnectionInUse) {
            // nothing is happening, so we can change immediately
            _backgroundConnection.setCatalog(cat);
        } else {
            // there are still queries in the queue, so we add the rollback
            // request to the end of the queue.
            _newCatalog = cat;
            _connectionNumberQueue.add(new Integer(CATALOG_CHANGE_REQUEST));
        }

    }


    public boolean supportsCatalogs() {
        return getRoot().supportsCatalogs();
    }


    
    /**
     * Returns connection alias name
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        try {
            return _alias.getName();

        } catch (java.lang.Throwable e) {
            SQLExplorerPlugin.error("Error getting the alias name ", e);
            return "";
        }
    }

    
}
