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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sourceforge.sqlexplorer.AliasModel;
import net.sourceforge.sqlexplorer.ApplicationFiles;
import net.sourceforge.sqlexplorer.DataCache;
import net.sourceforge.sqlexplorer.DriverModel;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.sessiontree.model.RootSessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModel;
import net.sourceforge.sqlexplorer.sqleditor.ISQLColorConstants;
import net.sourceforge.sqlexplorer.util.SQLString;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDriver;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import net.sourceforge.squirrel_sql.fw.sql.SQLDriverManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class SQLExplorerPlugin extends AbstractUIPlugin {

    private static final Log _logger = LogFactory.getLog(SQLExplorerPlugin.class);

    private int count = 0;

    public final static String PLUGIN_ID = "net.sourceforge.sqlexplorer";

    public SessionTreeModel stm = new SessionTreeModel();

    // The shared instance.
    private static SQLExplorerPlugin plugin;

    // Resource bundle.
    private ResourceBundle resourceBundle;

    private SQLDriverManager _driverMgr;

    private DataCache _cache;

    private AliasModel aliasModel;

    private DriverModel driverModel;

    private ListenerList listeners = new ListenerList();

    private ArrayList sqlHistory = new ArrayList();

    private static final String NEWLINE_SEPARATOR = System.getProperty("line.separator");

    private static final String NEWLINE_REPLACEMENT = "#LF#";
    
    private static final String SESSION_HINT_MARKER = "#SH#";


    public ArrayList getSQLHistory() {
        return sqlHistory;
    }


    public void addListener(SqlHistoryChangedListener listener) {
        listeners.add(listener);
    }


    public void removeListener(SqlHistoryChangedListener listener) {
        listeners.remove(listener);
    }


    private void sqlHistoryChanged() {
        Object[] ls = listeners.getListeners();
        for (int i = 0; i < ls.length; ++i) {
            try {
                ((SqlHistoryChangedListener) ls[i]).changed();
            } catch (Throwable e) {
            }

        }
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
     * Add a query string to the sql history. New queries are added to the start
     * of the list, so that the most recent entry is always located on the top
     * of the history list
     * 
     * @param newSql sql query string
     */
    public void addSQLtoHistory(SQLString newSql) {

        for (int i = 0; i < sqlHistory.size(); i++) {
            SQLString sql = (SQLString) sqlHistory.get(i);
            if (sql.equals(newSql)) {
                sqlHistory.remove(i);
                break;
            }
        }
        sqlHistory.add(0, newSql);
        sqlHistoryChanged();
    }


    public SQLDriverManager getSQLDriverManager() {
        
        if (_driverMgr == null) {
            _driverMgr = new SQLDriverManager();
        }
        
        return _driverMgr;
    }


    public AliasModel getAliasModel() {
        return aliasModel;
    }


    public DriverModel getDriverModel() {
        return driverModel;
    }


    /**
     * The constructor. Moved previous logic to the start method.
     */
    public SQLExplorerPlugin() {
        super();
        plugin = this;
    }
    
    
    public void start(BundleContext context) throws Exception {

        super.start(context);
    
        //FIXME loading preferences here causes
        // thread exception when launching as standalone app
        //initializePreferences();


        _driverMgr = new SQLDriverManager();
        
        _cache = new DataCache(_driverMgr);
        
    
        aliasModel = new AliasModel(_cache);
        driverModel = new DriverModel(_cache);
        Object[] aliases = (Object[]) aliasModel.getElements();
        for (int i = 0; i < aliases.length; i++) {
            final ISQLAlias alias = (ISQLAlias) aliases[i];
            if (alias.isConnectAtStartup()) {
                try {
                    ISQLDriver dv = driverModel.getDriver(alias.getDriverIdentifier());
                    final SQLConnection conn = _driverMgr.getConnection(dv, alias, alias.getUserName(), alias.getPassword());

                    Display.getDefault().asyncExec(new Runnable() {

                        public void run() {

                            try {
                                stm.createSessionTreeNode(conn, alias, null, alias.getPassword());
                            } catch (InterruptedException e) {
                                throw new RuntimeException();
                            }

                        }
                    });

                } catch (Throwable e) {
                    error("Error creating sql connection to " + alias.getName(), e);//$NON-NLS-1$
                }
            }
        }

        try {
            resourceBundle = ResourceBundle.getBundle("net.sourceforge.sqlexplorer.test"); //$NON-NLS-1$
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }

        // load SQL History from previous sessions
        loadSQLHistoryFromFile();
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
        saveSQLHistoryToFile();

        super.stop(context);
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


    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }


    /**
     * @return
     */
    public int getNextElement() {

        return count++;
    }


    protected void initializePreferences() {

        IPreferenceStore store = getPreferenceStore();
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_KEYWORD, new RGB(0, 0, 255));
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_MULTILINE_COMMENT, new RGB(0, 100, 0));
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_SINGLE_LINE_COMMENT, new RGB(0, 100, 0));
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_STRING, new RGB(255, 0, 0));
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_DEFAULT, new RGB(0, 0, 0));
        PreferenceConverter.setDefault(store, IConstants.FONT, JFaceResources.getTextFont().getFontData());//$NON-NLS-1$
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_TABLE, new RGB(0, 100, 255));
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_COLUMS, new RGB(100, 0, 255));

        store.setDefault(IConstants.PRE_ROW_COUNT, 80);
        store.setDefault(IConstants.MAX_SQL_ROWS, 100);

        store.setDefault(IConstants.AUTO_COMMIT, true);
        store.setDefault(IConstants.COMMIT_ON_CLOSE, false);
        store.setDefault(IConstants.SQL_ASSIST, true);
        store.setDefault(IConstants.CLIP_EXPORT_COLUMNS, false);
        store.setDefault(IConstants.CLIP_EXPORT_SEPARATOR, ";");//$NON-NLS-1$
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
     * Load the sql history from previous sessions.
     */
    private void loadSQLHistoryFromFile() {

        try {

            File file = new File(ApplicationFiles.SQLHISTORY_FILE_NAME);

            if (!file.exists()) {
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String currentLine = reader.readLine();
            while (currentLine != null) {
                if (currentLine.trim().length() != 0) {
                    
                    String sessionHint = null;
                    String query = null;
                    
                    int pos = currentLine.indexOf(SESSION_HINT_MARKER); 
                    if (pos != -1) {
                        // split line in session and query
                        
                        sessionHint = currentLine.substring(0, pos);
                        currentLine = currentLine.substring(pos + SESSION_HINT_MARKER.length());                        
                    } 
                    
                    query = currentLine.replaceAll(SQLExplorerPlugin.NEWLINE_REPLACEMENT, SQLExplorerPlugin.NEWLINE_SEPARATOR);
                    sqlHistory.add(new SQLString(query, sessionHint));
                    
                }
                currentLine = reader.readLine();
            }

            reader.close();

        } catch (Exception e) {
            error("Couldn't load sql history.", e);
        }

    }


    /**
     * Save all the used queries into a file, so that we can reuse them next
     * time.
     */
    private void saveSQLHistoryToFile() {

        try {

            File file = new File(ApplicationFiles.SQLHISTORY_FILE_NAME);

            if (file.exists()) {
                // clear old history
                file.delete();
            }

            if (sqlHistory.size() == 0) {
                // nothing to save
                return;
            }

            file.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            Iterator it = sqlHistory.iterator();
            while (it.hasNext()) {

                SQLString tmp = (SQLString) it.next();
                String qry = tmp.getText();
                qry = qry.replaceAll(SQLExplorerPlugin.NEWLINE_SEPARATOR, SQLExplorerPlugin.NEWLINE_REPLACEMENT);
                
                String sessionHint = tmp.getSessionName();
                
                String tmpLine = sessionHint + SESSION_HINT_MARKER + qry;
                
                writer.write(tmpLine, 0, tmpLine.length());
                writer.newLine();
            }

            writer.close();

        } catch (Exception e) {
            error("Couldn't save sql history.", e);
        }

    }

}
