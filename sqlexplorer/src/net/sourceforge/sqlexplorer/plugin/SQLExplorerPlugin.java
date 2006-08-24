package net.sourceforge.sqlexplorer.plugin;

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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sourceforge.sqlexplorer.AliasModel;
import net.sourceforge.sqlexplorer.DataCache;
import net.sourceforge.sqlexplorer.DriverModel;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.SQLDriverManager;
import net.sourceforge.sqlexplorer.connections.OpenConnectionJob;
import net.sourceforge.sqlexplorer.history.SQLHistory;
import net.sourceforge.sqlexplorer.sessiontree.model.RootSessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModel;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDriver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class SQLExplorerPlugin extends AbstractUIPlugin {

    private DataCache _cache;

    private SQLDriverManager _driverMgr;

    private AliasModel aliasModel;

    private int count = 0;

    private DriverModel driverModel;

    // Resource bundle.
    private ResourceBundle resourceBundle;

    public SessionTreeModel stm = new SessionTreeModel();

    private SQLHistory _history = null;

    private static final Log _logger = LogFactory.getLog(SQLExplorerPlugin.class);

    // The shared instance.
    private static SQLExplorerPlugin plugin;

    public final static String PLUGIN_ID = "net.sourceforge.sqlexplorer";

    private boolean _defaultConnectionsStarted = false;


    /**
     * The constructor. Moved previous logic to the start method.
     */
    public SQLExplorerPlugin() {

        super();
        plugin = this;
    }


    public AliasModel getAliasModel() {

        return aliasModel;
    }


    public DriverModel getDriverModel() {

        return driverModel;
    }


    /**
     * @return
     */
    public int getNextElement() {

        return count++;
    }


    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {

        return resourceBundle;
    }


    public SQLDriverManager getSQLDriverManager() {

        if (_driverMgr == null) {
            _driverMgr = new SQLDriverManager();
        }

        return _driverMgr;
    }


    /**
     * @return SQLHistory Instance
     */
    public SQLHistory getSQLHistory() {

        return _history;
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


    public void start(BundleContext context) throws Exception {

        super.start(context);

        _driverMgr = new SQLDriverManager();

        _cache = new DataCache(_driverMgr);
        aliasModel = new AliasModel(_cache);
        driverModel = new DriverModel(_cache);

        try {
            resourceBundle = ResourceBundle.getBundle("net.sourceforge.sqlexplorer.test"); //$NON-NLS-1$
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }

        // load SQL History from previous sessions
        _history = new SQLHistory();
    }


    /**
     * Open all connections that have the 'open on startup property'. This
     * method should be called from within the UI thread!
     */
    public void startDefaultConnections(IWorkbenchSite site) {

        if (_defaultConnectionsStarted) {
            return;
        }

        boolean autoCommit = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.AUTO_COMMIT);
        boolean commitOnClose = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(
                IConstants.COMMIT_ON_CLOSE);

        Object[] aliases = (Object[]) aliasModel.getElements();
        for (int i = 0; i < aliases.length; i++) {
            final ISQLAlias alias = (ISQLAlias) aliases[i];
            if (alias.isConnectAtStartup() && alias.isAutoLogon()) {
                try {

                    ISQLDriver dv = driverModel.getDriver(alias.getDriverIdentifier());

                    OpenConnectionJob bgJob = new OpenConnectionJob(_driverMgr, dv, alias, alias.getUserName(),
                            alias.getPassword(), autoCommit, commitOnClose, site.getShell());

                    IWorkbenchSiteProgressService siteps = (IWorkbenchSiteProgressService) site.getAdapter(
                            IWorkbenchSiteProgressService.class);
                    siteps.showInDialog(site.getShell(), bgJob);
                    bgJob.schedule();

                } catch (Throwable e) {
                    error("Error creating sql connection to " + alias.getName(), e);//$NON-NLS-1$
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

        _cache.save();
        RootSessionTreeNode rstn = stm.getRoot();
        rstn.closeAllConnections();

        // save SQL History for next session
        _history.save();

        super.stop(context);
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
     * Returns the shared instance.
     */
    public static SQLExplorerPlugin getDefault() {

        return plugin;
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

}
