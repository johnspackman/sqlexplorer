package net.sourceforge.sqlexplorer.ext;

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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.sqlexplorer.PluginLoader;
import net.sourceforge.sqlexplorer.URLUtil;
import net.sourceforge.sqlexplorer.dbviewer.model.CatalogNode;
import net.sourceforge.sqlexplorer.dbviewer.model.DatabaseNode;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.dbviewer.model.SchemaNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;


//import org.eclipse.core.internal.boot.PlatformURLConnection;
//import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.TreeViewer;


public class PluginManager {
	public PluginManager(){
	}
	/** Logger for this class. */
	

	/**
	 * Contains a <TT>PluginInfo</TT> object for every plugin that we attempted
	 * to load.
	 */
	private List _plugins = new ArrayList();

	/**
	 * Contains all plugins (<TT>IPlugin</TT>) successfully
	 * loaded. Keyed by <TT>IPlugin.getInternalName()</TT>.
	 */
	private Map _loadedPlugins = new HashMap();

	/**
	 * Contains a <TT>SessionPluginInfo</TT> object for evey object in
	 * <TT>_loadedPlugins<TT> that is an instance of <TT>ISessionPlugin</TT>.
	 */
	private List _sessionPlugins = new ArrayList();
	
	/**
		 * Contains a <TT>EditorPluginInfo</TT> object for evey object in
		 * <TT>_loadedPlugins<TT> that is an instance of <TT>IEditorPlugin</TT>.
		 */
	private List _editorPlugins = new ArrayList();

	/**
	 * Collection of active sessions. Keyed by <TT>ISession.getIdentifier()</TT>
	 * and contains a <TT>List</TT> of active <TT>ISessionPlugin</TT> objects
	 * for the session.
	 */
	private Map _activeSessions = new HashMap();


	/**
	 * A new session is starting.
	 *
	 * @param   session	 The new session.
	 *
	 * @throws	IllegalArgumentException
	 * 			Thrown if a <TT>null</TT> ISession</TT> passed.
	 */
	public synchronized void sessionStarted(SessionTreeNode sessionNode)
	{
		if (sessionNode == null)
		{
			throw new IllegalArgumentException("ISession == null"); //$NON-NLS-1$
		}
		List plugins = new ArrayList();
		_activeSessions.put(sessionNode.getIdentifier(), plugins);
		for (Iterator it = _sessionPlugins.iterator(); it.hasNext();)
		{
			SessionPluginInfo spi = (SessionPluginInfo) it.next();
			try
			{
				if (spi.getSessionPlugin().sessionStarted(sessionNode))
				{
					plugins.add(spi);
				}
			}
			catch (Throwable th)
			{
				String msg =
					"Error occured in IPlugin.sessionStarted() for " + spi.getPlugin().getDescriptiveName(); //$NON-NLS-1$
				SQLExplorerPlugin.error(msg, th);
				//_app.showErrorDialog(msg, th);
			}
		}
	}

	/**
	 * A session is ending.
	 *
	 * @param   session	 The session ending.
	 *
	 * @throws	IllegalArgumentException
	 * 			Thrown if a <TT>null</TT> ISession</TT> passed.
	 */
	public synchronized void sessionEnding(SessionTreeNode sessionNode)
	{
		if (sessionNode == null)
		{
			throw new IllegalArgumentException("ISession == null"); //$NON-NLS-1$
		}

		List plugins = (List) _activeSessions.remove(sessionNode.getIdentifier());
		if (plugins != null)
		{
			for (Iterator it = plugins.iterator(); it.hasNext();)
			{
				SessionPluginInfo spi = (SessionPluginInfo) it.next();
				try
				{
					spi.getSessionPlugin().sessionEnding(sessionNode);
				}
				catch (Throwable th)
				{
					String msg =
						"Error occured in IPlugin.sessionEnding() for " + spi.getPlugin().getDescriptiveName(); //$NON-NLS-1$
					SQLExplorerPlugin.error(msg, th);
					//_app.showErrorDialog(msg, th);
				}
			}
		}
	}

	/**
	 * Unload all plugins.
	 */
	public synchronized void unloadPlugins()
	{
		for (Iterator it = _loadedPlugins.values().iterator(); it.hasNext();)
		{
			IPlugin plugin = (IPlugin) it.next();
			try
			{
				plugin.unload();
			}
			catch (Throwable th)
			{
				String msg = "Error ocured unloading plugin: " + plugin.getInternalName(); //$NON-NLS-1$
				SQLExplorerPlugin.error(msg, th);
				//_app.showErrorDialog(msg, th);
			}
		}
	}

	public synchronized PluginInfo[] getPluginInformation()
	{
		return (PluginInfo[]) _plugins.toArray(new PluginInfo[_plugins.size()]);
	}


	public IDbModel[] getSchemaAddedTypes(SchemaNode schemaNode, SessionTreeNode sessionNode)
	{
		List objTypesList = new ArrayList();
		List plugins = (List) _activeSessions.get(sessionNode.getIdentifier());
		if (plugins != null)
		{
			for (Iterator it = plugins.iterator(); it.hasNext();)
			{
				SessionPluginInfo spi = (SessionPluginInfo) it.next();
				IDbModel[] objTypes = spi.getSessionPlugin().getSchemaAddedTypes(schemaNode,sessionNode);
				if (objTypes != null)
				{
					for (int i = 0; i < objTypes.length; ++i)
					{
						objTypesList.add(objTypes[i]);
					}
				}
			}
		}
		return (IDbModel[])objTypesList.toArray(new IDbModel[0]);
	}
	public IDbModel[] getCatalogAddedTypes(CatalogNode catalogNode, SessionTreeNode sessionNode)
		{
			List objTypesList = new ArrayList();
			List plugins = (List) _activeSessions.get(sessionNode.getIdentifier());
			if (plugins != null)
			{
				for (Iterator it = plugins.iterator(); it.hasNext();)
				{
					SessionPluginInfo spi = (SessionPluginInfo) it.next();
					IDbModel[] objTypes = spi.getSessionPlugin().getCatalogAddedTypes(catalogNode,sessionNode);
					if (objTypes != null)
					{
						for (int i = 0; i < objTypes.length; ++i)
						{
							objTypesList.add(objTypes[i]);
						}
					}
				}
			}
			return (IDbModel[])objTypesList.toArray(new IDbModel[0]);
		}
	
	public IDbModel[] getDbRootAddedTypes(DatabaseNode root,SessionTreeNode sessionNode)
	{
		List objTypesList = new ArrayList();
		List plugins = (List) _activeSessions.get(sessionNode.getIdentifier());
		if (plugins != null)
		{
			
			for (Iterator it = plugins.iterator(); it.hasNext();)
			{
				
				SessionPluginInfo spi = (SessionPluginInfo) it.next();
				IDbModel[] objTypes = spi.getSessionPlugin().getDbRootAddedTypes(root,sessionNode);
				if (objTypes != null)
				{
					for (int i = 0; i < objTypes.length; ++i)
					{
						objTypesList.add(objTypes[i]);
					}
				}
			}
		}
		return (IDbModel[])objTypesList.toArray(new IDbModel[0]);

	}

	/**
	 * TODO: Clean this mess up!!!!
	 * Load plugins. Load all plugin jars into class loader.
	 */
	public void loadPlugins()
	{
		List pluginUrls = new ArrayList();
		File dir = null;

		URL file1=URLUtil.getPluggableFile("plugins"+File.separator);	 //$NON-NLS-1$
		try{
			dir=new File(asLocalString(file1));
		}catch(Throwable e){			
		}
			
		if(dir==null)
			return;
		
		
		
		if (dir.isDirectory())
		{
			//String[] tab = { IDialogConstants.OK_LABEL };
			
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; ++i)
			{
				final File file = files[i];
				if (file.isFile())
				{
					final String fileName = file.getAbsolutePath();
					if (fileName.toLowerCase().endsWith(".zip") || fileName.toLowerCase().endsWith(".jar")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						try
						{						
							URL url=URLUtil.getResourceURL("plugins/"+file.getName()); //$NON-NLS-1$

							url=file.toURL();
							pluginUrls.add(url);

							
							
							
							// See if plugin has any jars in lib dir.
						/*	final String pluginDirName = Utilities.removeFileNameSuffix(file.getAbsolutePath());
							final File libDir = new File(pluginDirName, "lib");
							if (libDir.exists() && libDir.isDirectory())
							{
								File[] libDirFiles = libDir.listFiles();
								for (int j = 0; j < libDirFiles.length; ++j)
								{
									if (libDirFiles[j].isFile())
									{
										final String fn = libDirFiles[j].getAbsolutePath();
										if (fn.toLowerCase().endsWith(".zip") ||
												fn.toLowerCase().endsWith(".jar"))
										{
											try
											{
												pluginUrls.add(libDirFiles[j].toURL());
											}
											catch (IOException ex)
											{
												String msg = "Unable to load plugin library file: " + fn;
												s_log.error(msg, ex);
												_app.showErrorDialog(msg, ex);
											}
										}
									}
								}
							}*/
						}
						catch (IOException ex)
						{
							String msg = "Unable to load plugin jar: " + fileName; //$NON-NLS-1$
							SQLExplorerPlugin.error(msg, ex);

						}
					}
				}
			}
		}

    	URL[] urls = (URL[]) pluginUrls.toArray(new URL[pluginUrls.size()]);
		
		
		PluginLoader tl= new PluginLoader(urls);
		try
		{
			
			Class[] classes = tl.getPluginClasses();
		
			
			for (int i = 0; i < classes.length; ++i)
			{
				Class clazz = classes[i];
				try
				{
					
					loadPlugin(clazz);
				}
				catch (Throwable th)
				{
					String msg = "Error occured loading plugin class: " + clazz.getName(); //$NON-NLS-1$
					SQLExplorerPlugin.error(msg, th);
				}
			}
		}
		catch (IOException ex)
		{
			String msg = "Error occured retrieving plugins. No plugins have been loaded."; //$NON-NLS-1$
			SQLExplorerPlugin.error(msg, ex);
		}
	}

	/**
	 * Initialize plugins.
	 */
	public void initializePlugins()
	{
		for (Iterator it = _loadedPlugins.values().iterator(); it.hasNext();)
		{
			IPlugin plugin = (IPlugin) it.next();
			try
			{
				//long now = System.currentTimeMillis();
				plugin.initialize();
				
			}
			catch (Throwable th)
			{
				String msg = "Error occured initializing plugin: " + plugin.getInternalName(); //$NON-NLS-1$
				SQLExplorerPlugin.error(msg, th);
				
			}
		}
	}

	private void loadPlugin(Class pluginClass)
	{
		PluginInfo pi = new PluginInfo(pluginClass.getName());
		try
		{
			//long now = System.currentTimeMillis();
			IPlugin plugin = (IPlugin) pluginClass.newInstance();
			pi.setPlugin(plugin);
			_plugins.add(pi);
			if (validatePlugin(plugin))
			{
				plugin.load();
				pi.setLoaded(true);
				
				_loadedPlugins.put(plugin.getInternalName(), plugin);
				if (ISessionPlugin.class.isAssignableFrom(pluginClass))
				{
					_sessionPlugins.add(new SessionPluginInfo(pi));
				}
				if (IEditorPlugin.class.isAssignableFrom(pluginClass))
				{
					_editorPlugins.add(new EditorPluginInfo(pi));
				}
			}
		}
		catch (Throwable th)
		{
			String msg = "Error occured loading class " + pluginClass.getName() + " from plugin"; //$NON-NLS-1$ //$NON-NLS-2$
			SQLExplorerPlugin.error(msg, th);
		}
	}

	private boolean validatePlugin(IPlugin plugin)
	{
		String pluginInternalName = plugin.getInternalName();
		if (pluginInternalName == null || pluginInternalName.trim().length() == 0)
		{
			SQLExplorerPlugin.error(
				"Plugin " + plugin.getClass().getName() + "doesn't return a valid getInternalName()",new Exception()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}

		if (_loadedPlugins.get(pluginInternalName) != null)
		{
			SQLExplorerPlugin.error(
				"A Plugin with the internal name " + pluginInternalName + " has already been loaded",new Exception()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		//System.out.println("validated");
		return true;
	}
	
	public static String asLocalString(URL url) throws IOException {
		return asLocalURL(url).getFile();
	}
	
	
	public static URL asLocalURL(URL url) throws IOException {
		return Platform.resolve(url);
/*		if (!url.getProtocol().equals(PlatformURLHandler.PROTOCOL))
			return url;
		java.net.URLConnection connection = url.openConnection();
		if (!(connection instanceof PlatformURLConnection))
			return url;
		//String file = connection.getURL().getFile();
		//return file;
		//if (file.endsWith("/") && !file.endsWith(PlatformURLHandler.JAR_SEPARATOR)) //$NON-NLS-1$
		//	throw new IOException();
		return ((PlatformURLConnection) connection).getURLAsLocal();*/
	}
	
	public Map getImageDescriptorsMap(){
		HashMap map=new HashMap();
		for (Iterator it = _loadedPlugins.values().iterator(); it.hasNext();)
		{
			IPlugin plugin = (IPlugin) it.next();
			try
			{
				Map mp=plugin.getIconMap();
				if(mp!=null)//Fixed NPE
					map.putAll(mp);
			}
			catch (Exception e)
			{
				String msg = "Error ocurred calling getImageDescriptorsMap: " + plugin.getInternalName(); //$NON-NLS-1$
				SQLExplorerPlugin.error(msg, e);
				//_app.showErrorDialog(msg, th);
			}
		}
		return map;
	}

	/**
	 * @param sessionNode
	 * @param group
	 * @param model
	 * @return
	 */
	public IAction[] getTypeActions(SessionTreeNode sessionNode, IDbModel node, TreeViewer tv) {
		List plugins = (List) _activeSessions.get(sessionNode.getIdentifier());
		List actionList = new ArrayList();
		if (plugins != null)
		{
			for (Iterator it = plugins.iterator(); it.hasNext();)
			{
				SessionPluginInfo spi = (SessionPluginInfo) it.next();
				IAction[] objActions = spi.getSessionPlugin().getTypeActionsAdded(sessionNode,node,tv);
				if (objActions != null)
				{
					for (int i = 0; i < objActions.length; ++i)
					{
						actionList.add(objActions[i]);
					}
				}
			}
			return (IAction[])actionList.toArray(new IAction[0]);
		}
		return null;	
	}
	public IAction[] getAddedActions(SessionTreeNode sessionNode, IDbModel node, TreeViewer tv) {
		List plugins = (List) _activeSessions.get(sessionNode.getIdentifier());
		List actionList = new ArrayList();
		if (plugins != null)
		{
			for (Iterator it = plugins.iterator(); it.hasNext();)
			{
				SessionPluginInfo spi = (SessionPluginInfo) it.next();
				IAction[] objActions = spi.getSessionPlugin().getAddedActions(sessionNode,node,tv);
				if (objActions != null)
				{
					for (int i = 0; i < objActions.length; ++i)
					{
						actionList.add(objActions[i]);
					}
				}
			}
			return (IAction[])actionList.toArray(new IAction[0]);
		}
		return null;	
	}

	/**
	 * @param editor
	 * @return
	 */
	public IContributionItem[] getEditorContextMenuActions(SQLEditor editor) {
		List plugins = (List) _editorPlugins;
		List actionList = new ArrayList();
		if (plugins != null)
		{
			for (Iterator it = plugins.iterator(); it.hasNext();)
			{
				EditorPluginInfo spi = (EditorPluginInfo) it.next();
				IContributionItem[] objActions = spi.getEditorPlugin().getContextMenuActions(editor);
				if (objActions != null)
				{
					for (int i = 0; i < objActions.length; ++i)
					{
						actionList.add(objActions[i]);
					}
				}
			}
			return (IContributionItem[])actionList.toArray(new IContributionItem[0]);
		}
		return null;
	}

	/**
	 * @return
	 */
	public IAction[] getEditorToolbarActions(SQLEditor editor) {
		List plugins = (List) _editorPlugins;
		List actionList = new ArrayList();
		if (plugins != null)
		{
			for (Iterator it = plugins.iterator(); it.hasNext();)
			{
				EditorPluginInfo spi = (EditorPluginInfo) it.next();
				IAction[] objActions = spi.getEditorPlugin().getEditorToolbarActions(editor);
				if (objActions != null)
				{
					for (int i = 0; i < objActions.length; ++i)
					{
						actionList.add(objActions[i]);
					}
				}
			}
			return (IAction[])actionList.toArray(new IAction[0]);
		}
		return null;
	}

	/**
	 * @return
	 */
	public IActivablePanel[] getAddedPanels(SessionTreeNode sessionNode, IDbModel node) {
		List plugins = (List) _activeSessions.get(sessionNode.getIdentifier());
		List panelsList = new ArrayList();
		if (plugins != null)
		{
			for (Iterator it = plugins.iterator(); it.hasNext();)
			{
				
				SessionPluginInfo spi = (SessionPluginInfo) it.next();
				IActivablePanel[] panels = spi.getSessionPlugin().getAddedPanels(sessionNode,node);
				if (panels != null)
				{
					for (int i = 0; i < panels.length; ++i)
					{
						panelsList.add(panels[i]);
					}
				}
			}
			return (IActivablePanel[])panelsList.toArray(new IActivablePanel[0]);
		}
		return new IActivablePanel[0];	
	}
}
