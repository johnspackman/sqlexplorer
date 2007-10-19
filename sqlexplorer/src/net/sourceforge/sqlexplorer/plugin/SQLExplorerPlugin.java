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
package net.sourceforge.sqlexplorer.plugin;

import java.util.ResourceBundle;
import java.util.MissingResourceException;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.connections.ConnectionsView;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.AliasManager;
import net.sourceforge.sqlexplorer.dbproduct.DriverManager;
import net.sourceforge.sqlexplorer.history.SQLHistory;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.plugin.views.DatabaseStructureView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.internal.runtime.RuntimeLog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class SQLExplorerPlugin extends AbstractUIPlugin {

    private AliasManager aliasManager;

    private int count = 0;

    private DriverManager driverManager;

    // Resource bundle.
    private ResourceBundle resourceBundle;

    private SQLHistory _history = null;

    private static final Log _logger = LogFactory.getLog(SQLExplorerPlugin.class);

    // The shared instance.
    private static SQLExplorerPlugin plugin;

    public final static String PLUGIN_ID = "net.sourceforge.sqlexplorer";

    private boolean _defaultConnectionsStarted = false;
    
    // Cached connections view
    private ConnectionsView connectionsView;
    
    // Cached database structure view
    private DatabaseStructureView databaseStructureView;

	/**
     * The constructor. Moved previous logic to the start method.
     */
    public SQLExplorerPlugin() {
        super();
        plugin = this;
    }

    /**
     * Initialises the Plugin
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        try {
        	RuntimeLog.addLogListener(new ILogListener() {
				public void logging(IStatus status, String plugin) {
					System.err.println(status.getMessage());
					Throwable t = status.getException();
					if (t != null) {
						System.err.println(t.getMessage());
						t.printStackTrace(System.err);
					}
				}
        	});
	        driverManager = new DriverManager();
	        driverManager.loadDrivers();
	        
	        aliasManager = new AliasManager();
	        aliasManager.loadAliases();
	
	        try {
	            resourceBundle = ResourceBundle.getBundle("net.sourceforge.sqlexplorer.test"); //$NON-NLS-1$
	        } catch (MissingResourceException x) {
	            resourceBundle = null;
	        }
	
	        // load SQL History from previous sessions
	        _history = new SQLHistory();
        }catch(Exception e) {
        	error("Exception during start", e);
        	throw e;
        }
    }
    
    /**
     * Open all connections that have the 'open on startup property'. This
     * method should be called from within the UI thread!
     */
    public void startDefaultConnections(ConnectionsView connectionsView) {
    	this.connectionsView = connectionsView;
        if (_defaultConnectionsStarted)
            return;

        boolean openEditor = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.AUTO_OPEN_EDITOR);
        
        // Get the database structure view - NOTE: we don't use SQLExplorerPlugin.getDatabaseView()
        //	because it may not have an active page yet
        DatabaseStructureView dbView = null;
        IWorkbenchSite site = connectionsView.getSite();
        if (site.getPage() != null)
        	dbView = (DatabaseStructureView)site.getPage().findView(DatabaseStructureView.class.getName());

        for (Alias alias : aliasManager.getAliases()) {
            if (alias.isConnectAtStartup() && alias.isAutoLogon() && alias.getDefaultUser() != null) {
                if (dbView != null)
                    dbView.addUser(alias.getDefaultUser());

                if (openEditor) {
                    SQLEditorInput input = new SQLEditorInput("SQL Editor (" + SQLExplorerPlugin.getDefault().getEditorSerialNo() + ").sql");
                    input.setUser(alias.getDefaultUser());
                    try {
                    	site.getPage().openEditor(input, SQLEditor.class.getName());
                    }catch(PartInitException e) {
                    	SQLExplorerPlugin.error("Cannot open SQL editor", e);
                    }
                }
            }
        }

        _defaultConnectionsStarted = true;
    }

    /**
     * Game over. End all..
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
    	driverManager.saveDrivers();
        aliasManager.saveAliases();
        aliasManager.closeAllConnections();

        // save SQL History for next session
        _history.save();

        super.stop(context);
    }

    /**
     * @return Returns the next serial number for creating new editors (used in the title of the filename)
     */
    public int getEditorSerialNo() {
        return count++;
    }
    
    /**
     * Returns the DriverModel
     * @return
     */
    public DriverManager getDriverModel() {
        return driverManager;
    }

    /**
     * @return The list of configured Aliases
     */
	public AliasManager getAliasManager() {
		return aliasManager;
	}

    /**
     * @return SQLHistory Instance
     */
    public SQLHistory getSQLHistory() {
        return _history;
    }

    /**
     * @return Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * Get the version number as specified in plugin.xml
     * 
     * @return version number of SQL Explorer plugin
     */
    public String getVersion() {
        String version = (String) plugin.getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
        return version;
    }

    /**
     * Returns the shared instance.
     */
    public static SQLExplorerPlugin getDefault() {
        return plugin;
    }

    /**
     * Global log method.
     * 
     * @param message
     * @param t
     */
    public static void error(String message, Throwable t) {
        getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, String.valueOf(message), t));
        _logger.error(message, t);
    }

    /**
     * Global log method.
     * @param t
     */
    public static void error(Exception e) {
        getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, String.valueOf(e.getMessage()), e));
        _logger.error(e.getMessage(), e);
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = SQLExplorerPlugin.getDefault().getResourceBundle();
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

	public ConnectionsView getConnectionsView() {
		if (connectionsView == null) {
			IWorkbenchPage page = getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (page != null) {
		        connectionsView = (ConnectionsView)page.findView(ConnectionsView.class.getName());
		        if (connectionsView == null)
		        	try {
		        		connectionsView = (ConnectionsView)page.showView(ConnectionsView.class.getName());
		        	} catch(PartInitException e) {
		        		error(e);
		        	}
			}
		}
			
		return connectionsView;
	}
	
	public DatabaseStructureView getDatabaseStructureView() {
		if (databaseStructureView == null) {
			IWorkbenchPage page = getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (page != null) {
				databaseStructureView = (DatabaseStructureView) page.findView(DatabaseStructureView.class.getName());
		        if (databaseStructureView == null)
		        	try {
		        		databaseStructureView = (DatabaseStructureView)page.showView(DatabaseStructureView.class.getName());
		        	} catch(PartInitException e) {
		        		error(e);
		        	}
			}
		}
		return databaseStructureView;
	}
	
	public IWorkbenchSite getSite() {
		if (getConnectionsView() == null)
			return null;
		return connectionsView.getSite();
	}
}
