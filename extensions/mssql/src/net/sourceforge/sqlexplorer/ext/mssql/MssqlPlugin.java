package net.sourceforge.sqlexplorer.ext.mssql;

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

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import net.sourceforge.sqlexplorer.URLUtil;
import net.sourceforge.sqlexplorer.dbviewer.model.CatalogNode;
import net.sourceforge.sqlexplorer.dbviewer.model.DatabaseNode;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.dbviewer.model.TableNode;
import net.sourceforge.sqlexplorer.dbviewer.model.TableObjectTypeNode;
import net.sourceforge.sqlexplorer.ext.DefaultSessionPlugin;
import net.sourceforge.sqlexplorer.ext.PluginManager;
import net.sourceforge.sqlexplorer.ext.mssql.actions.CreateFunctionEdit;
import net.sourceforge.sqlexplorer.ext.mssql.actions.CreateProcedureEdit;
import net.sourceforge.sqlexplorer.ext.mssql.actions.CreateTriggerEdit;
import net.sourceforge.sqlexplorer.ext.mssql.actions.CreateViewEdit;
import net.sourceforge.sqlexplorer.ext.mssql.actions.FunctionEdit;
import net.sourceforge.sqlexplorer.ext.mssql.actions.ProcedureEdit;
import net.sourceforge.sqlexplorer.ext.mssql.actions.TriggerEdit;
import net.sourceforge.sqlexplorer.ext.mssql.actions.ViewEdit;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;


public class MssqlPlugin extends  DefaultSessionPlugin {
	private final static ILogger s_log = LoggerController.createLogger(MssqlPlugin.class);
	HashMap classMap=new HashMap();
	
	public MssqlPlugin(){
		classMap.put(ProcNode.class,"plugins/icons/test.gif");
		classMap.put(UDFNode.class,"plugins/icons/f.gif");
		classMap.put(CatalogSessionsNode.class,"plugins/icons/test.gif");
	}
	
	public void load(){}

	/**
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getInternalName()
	 */
	public String getInternalName() {
		return "mssql Plugin";
	}

	/**
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getDescriptiveName()
	 */
	public String getDescriptiveName() {
		return "Microsoft SQL Server Plugin for JFaceDbc";
	}

	/**
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getAuthor()
	 */
	public String getAuthor() {
		return "andreamazzolini@users.sourceforge.net";
	}

	/**
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getWebSite()
	 */
	public String getWebSite() {
		return "http://jfacedbc.sourceforge.net";
	}

	/**
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getVersion()
	 */
	public String getVersion() {
		return "0.1";
	}
	
	private boolean isMssql(SessionTreeNode session)
	{
		final String MICROSOFT = "microsoft";
		String dbms = null;
		try
		{
			dbms = session.getSQLConnection().getSQLMetaData().getDatabaseProductName();
		}
		catch (SQLException ex)
		{
			s_log.debug("Error in getDatabaseProductName()", ex);
		}
		return dbms != null && dbms.toLowerCase().startsWith(MICROSOFT);
	}
	
	public IDbModel [] getCatalogAddedTypes(CatalogNode catalogNode,SessionTreeNode sessionNode){
		if(this.isMssql(sessionNode)){
			ArrayList ls=new ArrayList();
			SQLConnection conn=sessionNode.getConnection();
			ls.add(new ProcTypeNode(catalogNode, "PROCEDURE", conn));
			ls.add(new TriggerTypeNode(catalogNode, "TRIGGER", conn));
			ls.add(new UDFTypeNode(catalogNode, "FUNCTIONS", conn));
			ls.add(new CatalogSessionsNode(catalogNode,"SESSIONS",conn));
			return (IDbModel[])ls.toArray(new IDbModel[0]);
		}
		return null;
	}
	
	private  ImageDescriptor getIcon(Class clazz){
		
		URL url=null;
		String string=(String)classMap.get(clazz);
		
		URL baseURL=URLUtil.getBaseURL();
		try{
			url = new URL(baseURL, string);
			url=PluginManager.asLocalURL(url);
		}catch(Exception e){
			//e.printStackTrace();
		}
		
		
		ImageDescriptor des=ImageDescriptor.createFromURL(url);

		return des;
	}
	
	public Map getIconMap(){
		HashMap map=new HashMap();
		map.put(ProcNode.class,getIcon(ProcNode.class));
		map.put(UDFNode.class,getIcon(UDFNode.class));
		map.put(CatalogSessionsNode.class,getIcon(CatalogSessionsNode.class));
		return map;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.DefaultSessionPlugin#getTypeActionsAdded(net.sourceforge.jfacedbc.sessiontree.model.SessionTreeNode)
	 */
	public IAction[] getTypeActionsAdded(SessionTreeNode sessionTreeNode, IDbModel node,TreeViewer tv) {
		ArrayList ls=new ArrayList();
		if (node instanceof ProcNode){
			ls.add(new ProcedureEdit(sessionTreeNode,node));
		}else if (node instanceof ProcTypeNode){
			ls.add(new CreateProcedureEdit(sessionTreeNode, node));
		}else if (node instanceof TriggerNode){
			ls.add(new TriggerEdit(sessionTreeNode, node));
		}else if (node instanceof TriggerTypeNode){
			ls.add(new CreateTriggerEdit(sessionTreeNode, node));
		}else if (node instanceof UDFNode){
			ls.add(new FunctionEdit(sessionTreeNode, node));
		}else if (node instanceof UDFTypeNode){
			ls.add(new CreateFunctionEdit(sessionTreeNode, node));
		}
		
		return (IAction[])ls.toArray(new IAction[0]);
	}
	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.ISessionPlugin#getAddedActions(net.sourceforge.jfacedbc.sessiontree.model.SessionTreeNode, net.sourceforge.jfacedbc.sessiontree.model.DatabaseActionGroup, net.sourceforge.jfacedbc.dbviewer.IDbModel)
	 */
	public IAction[] getAddedActions(SessionTreeNode sessionNode, IDbModel node, TreeViewer tv) {
		ArrayList ls=new ArrayList();
		if(isMssql(sessionNode)){
			
			if(node instanceof TableNode){
				TableNode tNode=(TableNode)node;
				if(tNode.getParent().toString().equalsIgnoreCase("VIEW"))
					ls.add(new ViewEdit(sessionNode,node));
			}else if(node instanceof TableObjectTypeNode){

				TableObjectTypeNode tNode=(TableObjectTypeNode)node;
				if(tNode.toString().equalsIgnoreCase("VIEW")){
					ls.add(new CreateViewEdit(sessionNode,tNode));
				}
			}
		}
		return  (IAction[])ls.toArray(new IAction[0]);
	}

	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.ext.ISessionPlugin#getDbRootAddedTypes(net.sf.jfacedbc.dbviewer.model.DatabaseNode, net.sf.jfacedbc.sessiontree.model.SessionTreeNode)
	 */
	public IDbModel[] getDbRootAddedTypes(DatabaseNode root, SessionTreeNode sessionNode) {
		if(isMssql(sessionNode)){
	
			ArrayList ls=new ArrayList();
			SQLConnection conn=sessionNode.getConnection();
			ls.add(new MonitorNode(root,"Monitor", conn));
				//		ls.add(new TableObjectTypeNode(schemaNode, "PACKAGE BODY", sessionNode.getConnection(),dm));
			//ls.add(new TableObjectTypeNode(schemaNode, "TRIGGER", sessionNode.getConnection(),dm));
			return (IDbModel[])ls.toArray(new IDbModel[0]);
		}
		return null;
	};

}
