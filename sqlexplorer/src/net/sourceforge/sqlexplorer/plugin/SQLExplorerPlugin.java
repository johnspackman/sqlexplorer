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
import net.sourceforge.sqlexplorer.MultiLineString;
import net.sourceforge.sqlexplorer.ext.PluginManager;
import net.sourceforge.sqlexplorer.sessiontree.model.RootSessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModel;
import net.sourceforge.sqlexplorer.sqleditor.ISQLColorConstants;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDriver;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import net.sourceforge.squirrel_sql.fw.sql.SQLDriverManager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class SQLExplorerPlugin extends AbstractUIPlugin {

    private int count = 0;

    public final static String PLUGIN_ID = "net.sourceforge.sqlexplorer";

    public SessionTreeModel stm = new SessionTreeModel();

    public PluginManager pluginManager;

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
     * @return version number of SQL Explorer plugin
     */
    public String getVersion() {        
        String version = (String) plugin.getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
        return version;
    }
    
    /**
     * Add a query string to the sql history.
     * New queries are added to the start of the list, so that
     * the most recent entry is always located on the top of
     * the history list
     * 
     * @param newSql sql query string
     */
    public void addSQLtoHistory(MultiLineString newSql) {

        for (int i = 0; i < sqlHistory.size(); i++) {
            MultiLineString sql = (MultiLineString) sqlHistory.get(i);
            if (sql.equals(newSql)) {
                sqlHistory.remove(i);
                break;
            }
        }
        sqlHistory.add(0, newSql);
        sqlHistoryChanged();
    }

    public SQLDriverManager getSQLDriverManager() {
        return _driverMgr;
    }

    public AliasModel getAliasModel() {
        return aliasModel;
    }

    public DriverModel getDriverModel() {
        return driverModel;
    }

    /**
     * The constructor.
     */
    public SQLExplorerPlugin(IPluginDescriptor descriptor) {
        super(descriptor);
        plugin = this;

        try {
            pluginManager = new PluginManager();
            pluginManager.loadPlugins();
            pluginManager.initializePlugins();

        } catch (Throwable e) {
            error("Error loading plugins preference pages", e);//$NON-NLS-1$
        }

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

    public void shutdown() throws CoreException {
        _cache.save();
        RootSessionTreeNode rstn = stm.getRoot();
        rstn.closeAllConnections();

        // save SQL History for next session
        saveSQLHistoryToFile();
        
        super.shutdown();

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

    protected void initializeDefaultPreferences(IPreferenceStore store) {

        super.initializeDefaultPreferences(store);
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_KEYWORD, new RGB(0, 0, 255));
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_MULTILINE_COMMENT, new RGB(0, 100, 0));
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_SINGLE_LINE_COMMENT, new RGB(0, 100, 0));
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_STRING, new RGB(255, 0, 0));
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_DEFAULT, new RGB(0, 0, 0));
        PreferenceConverter.setDefault(store, IConstants.FONT, JFaceResources.getTextFont().getFontData());//$NON-NLS-1$
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_TABLE, new RGB(0, 100, 255));
        PreferenceConverter.setDefault(store, ISQLColorConstants.SQL_COLUMS, new RGB(100, 0, 255));

        store.setDefault(IConstants.PRE_ROW_COUNT, 80);
        store.setDefault(IConstants.MAX_SQL_ROWS, 2000);

        store.setDefault(IConstants.AUTO_COMMIT, true);
        store.setDefault(IConstants.COMMIT_ON_CLOSE, false);
        store.setDefault(IConstants.SQL_ASSIST, true);
        store.setDefault(IConstants.CLIP_EXPORT_COLUMNS, false);
        store.setDefault(IConstants.CLIP_EXPORT_SEPARATOR, ";");//$NON-NLS-1$
    }

    /**
     * Global log method.
     * @param message
     * @param t
     */
    public static void error(String message, Throwable t) {
        getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, String.valueOf(message), t));
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
    				currentLine = currentLine.replaceAll(SQLExplorerPlugin.NEWLINE_REPLACEMENT, SQLExplorerPlugin.NEWLINE_SEPARATOR);
    				sqlHistory.add(new MultiLineString(currentLine));
    			}
    			currentLine = reader.readLine();
    		}
    		
    		reader.close();
    		
    	} catch (Exception e) {
    		error("Couldn't load sql history.", e);
    	}
    	
    }
    
    /**
     * Save all the used queries into a file, so that we
     * can reuse them next time.
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
    			
                MultiLineString tmp = (MultiLineString) it.next();
                String qry = tmp.getOriginalText();
    			qry = qry.replaceAll(SQLExplorerPlugin.NEWLINE_SEPARATOR, SQLExplorerPlugin.NEWLINE_REPLACEMENT);
    			writer.write(qry, 0, qry.length());
    			writer.newLine();
    		}
    		
    		writer.close();
    		
    	} catch (Exception e) {
    		error("Couldn't save sql history.", e);
    	}
    	
    }
}
