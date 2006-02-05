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
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.DetailTabManager;
import net.sourceforge.sqlexplorer.dbstructure.DatabaseModel;
import net.sourceforge.sqlexplorer.dbstructure.nodes.DatabaseNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;
import net.sourceforge.squirrel_sql.fw.id.IIdentifier;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.widgets.Table;

/**
 * The SessionTreeNode represents one active database session.
 * 
 * @modified Davy Vanherbergen
 */
public class SessionTreeNode implements ISessionTreeNode {

    private SQLConnection _connection;

    private IIdentifier _id = IdentifierFactory.getInstance().createIdentifier();

    private ISQLAlias _alias;

    boolean _assistanceEnabled;

    public DatabaseModel dbModel;

    private Dictionary _dictionary = new Dictionary();

    private ListenerList _listeners = new ListenerList();

    private ArrayList ls = new ArrayList(10);

    private SessionTreeModel _model;

    private RootSessionTreeNode _parent;

    final private String _password;

    Table table;


    public SessionTreeNode(final SQLConnection conn, ISQLAlias alias, SessionTreeModel md, IProgressMonitor monitor, final String password)
            throws InterruptedException {
        _connection = conn;
        _alias = alias;
        dbModel = new DatabaseModel(this);
        _model = md;
        _parent = md.getRoot();
        _parent.add(this);
        _password = password;

        _assistanceEnabled = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.SQL_ASSIST);
               
        if (monitor != null && monitor.isCanceled()) {
            throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled"));
        }
        
        
        try {

            if (_assistanceEnabled) {
            
                if (monitor != null) {
                    monitor.setTaskName(Messages.getString("Progress.Dictionary.Loading"));
                }
                
                Object[] children = dbModel.getChildNodes();
                DatabaseNode dbNode = ((DatabaseNode) children[0]); 
                
                // check if we can persisted dictionary 
                if (monitor != null) {
                    monitor.subTask(Messages.getString("Progress.Dictionary.Scanning"));
                }
                
                boolean isLoaded = _dictionary.restore(dbNode);
            
                if (!isLoaded) {           
                    
                    // load full dictionary
                    _dictionary.load(dbNode, monitor);
                }                
            }
            
        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error enabling assistance ", e);
        }
    }


    public void add(ISessionTreeNode n) {
        ls.add(n);
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
            _connection.close();
        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error closing database _connection ", e);
        }

    }


    public void commit() {
        try {
            _connection.commit();
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
            cat = _connection.getCatalog();
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


    public SQLConnection getConnection() {
        return _connection;
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


    /**
     * @see org.gnu.amaz.ISessionTreeNode#getParent()
     */
    public Object getParent() {
        return _parent;
    }


    public SQLConnection getSQLConnection() {
        return _connection;
    }


    public boolean isAutoCommitMode() {
        boolean result = false;
        try {
            result = _connection.getAutoCommit();
        } catch (Throwable e) {
        }
        return result;
    }


    public void remove(ISessionTreeNode n) {
        ls.remove(n);
    }


    public void rollback() {
        try {
            _connection.rollback();
        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error rollbacking ", e);

        }
    }


    public void setCatalog(String cat) throws SQLException {
        _connection.getConnection().setCatalog(cat);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */

    public boolean supportsCatalogs() {
        return getRoot().supportsCatalogs();
    }


    public DatabaseNode getRoot() {
        return dbModel.getRoot();
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
