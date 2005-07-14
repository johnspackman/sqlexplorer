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

import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sourceforge.sqlexplorer.AliasModel;
import net.sourceforge.sqlexplorer.DataCache;
import net.sourceforge.sqlexplorer.DriverModel;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.ext.PluginManager;
import net.sourceforge.sqlexplorer.sessiontree.model.RootSessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModel;
import net.sourceforge.sqlexplorer.sqleditor.ISQLColorConstants;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDriver;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import net.sourceforge.squirrel_sql.fw.sql.SQLDriverManager;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
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
	
	public static void error(String message, Throwable t){
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, String.valueOf(message), t));
	}
	private int count=0;
	public final static String PLUGIN_ID = "net.sourceforge.sqlexplorer"; //$NON-NLS-1$
	public SessionTreeModel stm=new SessionTreeModel();
	public PluginManager pluginManager;
	//The shared instance.
	private static SQLExplorerPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private SQLDriverManager _driverMgr;
	private DataCache _cache;
	private AliasModel aliasModel;
	private DriverModel driverModel;
	private ListenerList listeners = new ListenerList();
	private ArrayList sqlHistory=new ArrayList();
	public ArrayList getSQLHistory(){
		return sqlHistory;
	}
	public void addListener(SqlHistoryChangedListener listener) {
		listeners.add(listener);
	}
	public void removeListener(SqlHistoryChangedListener listener){
		listeners.remove(listener);
	}
	private void sqlHistoryChanged(){
		Object []ls=listeners.getListeners();
		for(int i=0;i<ls.length;++i){
			try{
				((SqlHistoryChangedListener)ls[i]).changed();
			}catch(Throwable e){
			}
		
		}
	}
	public void addSQLtoHistory(String newSql){
		boolean found=false;
		for(int i=0;i<sqlHistory.size();i++){
			String sql=(String) sqlHistory.get(i);
			if(sql.equals(newSql)){
				found=true;
				break;
			}
		}
		if(!found){
			sqlHistory.add(newSql);
			sqlHistoryChanged();
		}
	}
		
	public SQLDriverManager getSQLDriverManager(){
		return _driverMgr;
	}
	public AliasModel getAliasModel(){
		return aliasModel;
	}
	public DriverModel getDriverModel(){
		return driverModel;
	}
	
	
	/**
	 * The constructor.
	 */
	public SQLExplorerPlugin() {
		plugin = this;

		//if(!earlyStarted){
			try{
				pluginManager=new PluginManager();
				pluginManager.loadPlugins();
				pluginManager.initializePlugins();

			}catch(Throwable e){
				error("Error loading plugins preference pages",e);//$NON-NLS-1$
			}
			try{
				//DriverManager.setLogWriter(new PrintWriter(new BufferedOutputStream(new FileOutputStream("C:\\out1.txt"))));
			}catch(Throwable e){
			}
			
			_driverMgr=new SQLDriverManager();
			_cache = new DataCache(_driverMgr);
			aliasModel=new AliasModel(_cache);
			driverModel=new DriverModel(_cache);
			Object [] aliases= (Object[]) aliasModel.getElements();
			for(int i=0;i<aliases.length;i++){
				final ISQLAlias alias=(ISQLAlias)aliases[i];
				if(alias.isConnectAtStartup()){
					try{
						ISQLDriver dv=driverModel.getDriver(alias.getDriverIdentifier());
						final SQLConnection conn=_driverMgr.getConnection(dv, alias,alias.getUserName(),alias.getPassword());
						
						Display.getDefault().asyncExec(new Runnable(){
							public void run() {
								
									try {
										stm.createSessionTreeNode(conn,alias,null,alias.getPassword());
									} catch (InterruptedException e) {
										throw new RuntimeException();
									}
								
							}
						});
						
						
					}catch(Throwable e){
						error("Error creating sql connection to "+alias.getName(),e);//$NON-NLS-1$
					}
				}
			}
		//}		
		
		try {
			resourceBundle= ResourceBundle.getBundle("net.sourceforge.sqlexplorer.test"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		_cache.save();
		RootSessionTreeNode rstn=stm.getRoot();
		rstn.closeAllConnections();
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static SQLExplorerPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= SQLExplorerPlugin.getDefault().getResourceBundle();
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeDefaultPreferences(org.eclipse.jface.preference.IPreferenceStore)
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		
		super.initializeDefaultPreferences(store);
		PreferenceConverter.setDefault(store,ISQLColorConstants.SQL_KEYWORD,new RGB(0,0,255));
		PreferenceConverter.setDefault(store,ISQLColorConstants.SQL_MULTILINE_COMMENT,new RGB(0,100,0));
		PreferenceConverter.setDefault(store,ISQLColorConstants.SQL_SINGLE_LINE_COMMENT,new RGB(0,100,0));
		PreferenceConverter.setDefault(store,ISQLColorConstants.SQL_STRING,new RGB(255,0,0));
		PreferenceConverter.setDefault(store,ISQLColorConstants.SQL_DEFAULT,new RGB(0,0,0));
		PreferenceConverter.setDefault(store,IConstants.FONT,JFaceResources.getTextFont().getFontData());//$NON-NLS-1$
		PreferenceConverter.setDefault(store,ISQLColorConstants.SQL_TABLE,new RGB(0,100,255));
		PreferenceConverter.setDefault(store,ISQLColorConstants.SQL_COLUMS,new RGB(100,0,255));

		store.setDefault(IConstants.PRE_ROW_COUNT,80);
		store.setDefault(IConstants.MAX_SQL_ROWS,2000);

		store.setDefault(IConstants.AUTO_COMMIT,true);
		store.setDefault(IConstants.COMMIT_ON_CLOSE,false);
		store.setDefault(IConstants.SQL_ASSIST,true);
		store.setDefault(IConstants.CLIP_EXPORT_COLUMNS,false);
		store.setDefault(IConstants.CLIP_EXPORT_SEPARATOR,";");//$NON-NLS-1$
	}
	
}
