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
package net.sourceforge.sqlexplorer.ext.oracle;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import net.sourceforge.sqlexplorer.URLUtil;
import net.sourceforge.sqlexplorer.dbviewer.model.DatabaseNode;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.dbviewer.model.SchemaNode;
import net.sourceforge.sqlexplorer.dbviewer.model.TableNode;
import net.sourceforge.sqlexplorer.dbviewer.model.TableObjectTypeNode;
import net.sourceforge.sqlexplorer.ext.DefaultSessionPlugin;
import net.sourceforge.sqlexplorer.ext.IActivablePanel;
import net.sourceforge.sqlexplorer.ext.IEditorPlugin;
import net.sourceforge.sqlexplorer.ext.PluginException;
import net.sourceforge.sqlexplorer.ext.oracle.actions.*;
import net.sourceforge.sqlexplorer.ext.oracle.panels.DependentObjectsPanel;
import net.sourceforge.sqlexplorer.ext.oracle.panels.PrivilegesPanel;
import net.sourceforge.sqlexplorer.ext.oracle.panels.StatusPanel;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;

public class OraclePlugin extends  DefaultSessionPlugin implements IEditorPlugin {
	HashMap classMap=new HashMap();
	public OraclePlugin(){
		classMap.put(SequenceNode.class,"plugins/icons/test.gif");
		classMap.put(PackageNode.class,"plugins/icons/package.gif");
		classMap.put(PackageBodyNode.class,"plugins/icons/package_body.gif");
		classMap.put(ProcNode.class,"plugins/icons/p.gif");
		classMap.put(FunctionNode.class,"plugins/icons/f.gif");
		classMap.put(TriggerNode.class,"plugins/icons/test.gif");
		classMap.put(SchemaSessionsNode.class,"plugins/icons/test.gif");
		classMap.put(JavaSourceNode.class,"plugins/icons/test.gif");
		classMap.put(DBLinkNode.class,"plugins/icons/test.gif");
	}
	/**
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#initialize()
	 */
	public void initialize() throws PluginException {
	}
	
	public void load(){}

	/**
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#unload()
	 */
	public void unload()  {
		super.unload();
	}
	
	public boolean sessionStarted(SessionTreeNode sessionNode){
		
		return true;
	}

	/**
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getInternalName()
	 */
	public String getInternalName() {
		return "oraclePlugin";
	}

	/**
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getDescriptiveName()
	 */
	public String getDescriptiveName() {
		return "Oracle Plugin for JFaceDbc";
	}

	/**
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getAuthor()
	 */
	public String getAuthor() {
		return "andreamazzolini@users.sourceforge.net";
	}

	/**
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getContributors()
	 */
	public String getContributors() {
		return null;
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
		return "1.1";
	}

	private boolean isOracle(SessionTreeNode session)
	{
		final String ORACLE = "oracle";
		String dbms = null;
		try
		{
			dbms = session.getSQLConnection().getSQLMetaData().getDatabaseProductName();
		}
		catch (SQLException ex)
		{
		}
		return dbms != null && dbms.toLowerCase().startsWith(ORACLE);
	}
	private boolean isOracle9(SessionTreeNode session)
	{
		final String ORACLE9 = "oracle9";
		String dbms = null;
		try
		{
			dbms = session.getSQLConnection().getSQLMetaData().getDatabaseProductVersion();
			
		}
		catch (SQLException ex)
		{
		}
		return dbms != null && dbms.toLowerCase().startsWith(ORACLE9);
	}
	public IDbModel [] getSchemaAddedTypes(SchemaNode schemaNode,SessionTreeNode sessionNode){
		if(this.isOracle(sessionNode)){
			ArrayList ls=new ArrayList();
			SQLConnection conn=sessionNode.getConnection();
			ls.add(new SequenceTypeNode(schemaNode, "SEQUENCE", conn));
			ls.add(new PackageTypeNode(schemaNode, "PACKAGE", conn));
			ls.add(new PackageBodyTypeNode(schemaNode, "PACKAGE BODY", conn));
			ls.add(new FunctionTypeNode(schemaNode, "FUNCTION", conn));
			ls.add(new ProcTypeNode(schemaNode, "PROCEDURE", conn));
			ls.add(new TriggerTypeNode(schemaNode, "TRIGGER", conn));
			ls.add(new SchemaSessionsNode(schemaNode,"SESSIONS",conn));
			ls.add(new JavaTypeNode(schemaNode,"JAVA SOURCE",conn));
			ls.add(new DBLinkTypeNode(schemaNode,"DATABASE LINK",conn));
	//		ls.add(new TableObjectTypeNode(schemaNode, "PACKAGE BODY", sessionNode.getConnection(),dm));
			//ls.add(new TableObjectTypeNode(schemaNode, "TRIGGER", sessionNode.getConnection(),dm));
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
			url=net.sourceforge.sqlexplorer.ext.PluginManager.asLocalURL(url);
		}catch(Exception e){
			
		}
		
		
		ImageDescriptor des=ImageDescriptor.createFromURL(url);

		return des;
	}
	
	public Map getIconMap(){
		HashMap map=new HashMap();
		map.put(SequenceNode.class,getIcon(SequenceNode.class));
		map.put(PackageNode.class,getIcon(PackageNode.class));
		map.put(PackageBodyNode.class,getIcon(PackageBodyNode.class));
		map.put(FunctionNode.class,getIcon(FunctionNode.class));
		map.put(ProcNode.class,getIcon(ProcNode.class));
		map.put(TriggerNode.class,getIcon(TriggerNode.class));
		map.put(SchemaSessionsNode.class,getIcon(SchemaSessionsNode.class));
		map.put(JavaSourceNode.class,getIcon(JavaSourceNode.class));
		map.put(DBLinkNode.class,getIcon(DBLinkNode.class));
		return map;
	}
	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.DefaultSessionPlugin#getDbRootAddedTypes(net.sourceforge.jfacedbc.sessiontree.model.SessionTreeNode, net.sourceforge.jfacedbc.dbviewer.DetailManager)
	 */
	public IDbModel[] getDbRootAddedTypes(DatabaseNode root,SessionTreeNode sessionNode) {
		if(this.isOracle(sessionNode)){
			
			ArrayList ls=new ArrayList();
			SQLConnection conn=sessionNode.getConnection();
			ls.add(new MonitorNode(root,"Monitor", conn));
			ls.add(new SecurityNode(root,"Security",conn));
			ls.add(new InstanceNode(root,"Instance",conn));
				//		ls.add(new TableObjectTypeNode(schemaNode, "PACKAGE BODY", sessionNode.getConnection(),dm));
			//ls.add(new TableObjectTypeNode(schemaNode, "TRIGGER", sessionNode.getConnection(),dm));
			return (IDbModel[])ls.toArray(new IDbModel[0]);
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.DefaultSessionPlugin#getTypeActionsAdded(net.sourceforge.jfacedbc.sessiontree.model.SessionTreeNode)
	 */
	public IAction[] getTypeActionsAdded(SessionTreeNode sessionTreeNode, IDbModel node, TreeViewer tv) {
		ArrayList ls=new ArrayList();
		boolean oracle9=isOracle9(sessionTreeNode);
		if(node instanceof TriggerNode){
			
			ls.add(new TriggerEdit(sessionTreeNode,node));
			if(oracle9){
				ls.add(new ExtractDDL(sessionTreeNode,node));
				ls.add(new ExtractXML(sessionTreeNode,node));
			}
				
		}	
		else if (node instanceof ProcNode){
			
			ls.add(new ProcedureEdit(sessionTreeNode,node));
			ls.add(new RunProcedure(sessionTreeNode,(ProcNode)node));
			if(oracle9){
				ls.add(new ExtractDDL(sessionTreeNode,node));
				ls.add(new ExtractXML(sessionTreeNode,node));
			}
		else if (node instanceof SequenceNode){
			
			if(oracle9){
				ls.add(new ExtractDDL(sessionTreeNode,node));
				ls.add(new ExtractXML(sessionTreeNode,node));
			}
		}

				
		}else if(node instanceof FunctionNode){
			ls.add(new FunctionEdit(sessionTreeNode,node));
			ls.add(new RunFunction(sessionTreeNode,(FunctionNode)node));
			if(oracle9){
				ls.add(new ExtractDDL(sessionTreeNode,node));
				ls.add(new ExtractXML(sessionTreeNode,node));
			}
						
		}else if(node instanceof PackageNode){
			ls.add(new PackageEdit(sessionTreeNode,node));
			ls.add(new RunPackage(sessionTreeNode,(PackageNode)node));
			if(oracle9){
				ls.add(new ExtractDDL(sessionTreeNode,node));
				ls.add(new ExtractXML(sessionTreeNode,node));
			}
						
		}else if(node instanceof PackageBodyNode){
			ls.add(new PackageBodyEdit(sessionTreeNode,node));
			ls.add(new RunPackage(sessionTreeNode,(PackageBodyNode)node));
			if(oracle9){
				ls.add(new ExtractDDL(sessionTreeNode,node));
				ls.add(new ExtractXML(sessionTreeNode,node));
			}	
		}else if(node instanceof PackageTypeNode){
			ls.add(new RefreshTypeNode((AbstractTypeNode)node,tv));
			ls.add(new CreatePackageEdit(sessionTreeNode,node));			
		}else if(node instanceof PackageBodyTypeNode){
			ls.add(new RefreshTypeNode((AbstractTypeNode)node,tv));
			ls.add(new CreatePackageBodyEdit(sessionTreeNode,node));			
		}else if (node instanceof FunctionTypeNode){
			ls.add(new RefreshTypeNode((AbstractTypeNode)node,tv));
			ls.add(new CreateFunctionEdit(sessionTreeNode,node));
		}else if (node instanceof ProcTypeNode){
			ls.add(new RefreshTypeNode((AbstractTypeNode)node,tv));
			ls.add(new CreateProcedureEdit(sessionTreeNode, node));
		}else if (node instanceof TriggerTypeNode){
			ls.add(new RefreshTypeNode((AbstractTypeNode)node,tv));
			ls.add(new CreateTriggerEdit(sessionTreeNode,node));
		}else if(node instanceof SequenceTypeNode){
			ls.add(new RefreshTypeNode((AbstractTypeNode)node,tv));
		}
		
		return (IAction[])ls.toArray(new IAction[0]);
	}
	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.ISessionPlugin#getAddedActions(net.sourceforge.jfacedbc.sessiontree.model.SessionTreeNode, net.sourceforge.jfacedbc.sessiontree.model.DatabaseActionGroup, net.sourceforge.jfacedbc.dbviewer.IDbModel)
	 */
	public IAction[] getAddedActions(SessionTreeNode sessionNode, IDbModel node, TreeViewer tv) {
		ArrayList ls=new ArrayList();
		
		if(isOracle(sessionNode)){
			boolean oracle9=isOracle9(sessionNode);			
			if(node instanceof TableNode){
				TableNode tNode=(TableNode)node;
				if(tNode.getParent().toString().equalsIgnoreCase("VIEW"))
					ls.add(new ViewEdit(sessionNode,node));
				if(oracle9){
					ls.add(new ExtractDDL(sessionNode,tNode));
					ls.add(new ExtractXML(sessionNode,tNode));
				}
			}else if(node instanceof TableObjectTypeNode){

				TableObjectTypeNode tNode=(TableObjectTypeNode)node;
				if(tNode.toString().equalsIgnoreCase("VIEW")){
					ls.add(new CreateViewEdit(sessionNode,tNode));
				}
			}
		}
		return  (IAction[])ls.toArray(new IAction[0]);
	};

	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.ext.ISessionPlugin#getAddedPanels(net.sf.jfacedbc.sessiontree.model.SessionTreeNode, net.sf.jfacedbc.dbviewer.model.IDbModel)
	 */
	public IActivablePanel[] getAddedPanels(
		SessionTreeNode sessionNode,
		IDbModel node) {
		if(isOracle(sessionNode)) {
			if(node instanceof TableNode){
				TableNode tn=(TableNode)node;
				if(tn.isTable()|| tn.isView()){	
					return new IActivablePanel[]{new PrivilegesPanel(),new DependentObjectsPanel(),new StatusPanel() };
				}else if (tn.isSynonym()){
					return new IActivablePanel[]{new DependentObjectsPanel() };
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.ext.IEditorPlugin#getContextMenuActions(net.sf.jfacedbc.plugin.editors.SQLEditor)
	 */
	public IContributionItem[] getContextMenuActions(SQLEditor editor) {
		SessionTreeNode session=editor.getSessionTreeNode();
		if(session==null)
			return null;
		if(isOracle(session)){
			return new IContributionItem[]{new ActionContributionItem(new ExplainAction(editor))};
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.ext.IEditorPlugin#getEditorToolbarActions(net.sf.jfacedbc.plugin.editors.SQLEditor)
	 */
	public IAction[] getEditorToolbarActions(SQLEditor editor) {
		return null;
	}
	public static URL getUrl(String path){
		URL url=null;
		URL baseURL=URLUtil.getBaseURL();
		try{
			url = new URL(baseURL, path);
			url=net.sourceforge.sqlexplorer.ext.PluginManager.asLocalURL(url);
		}catch(Exception e){

		}
		return url;
	}
	public static boolean isDba(SQLConnection conn){
		boolean isDba=false;
		Statement st=null; 
		try{
			st = conn.createStatement();
			ResultSet rs=st.executeQuery("select GRANTED_ROLE from USER_ROLE_PRIVS");
		
			while (rs.next())
			{
				String role = rs.getString(1).toUpperCase();
				if (role.equals("DBA")){
					isDba=true;
					break;
				}
			}
		}
		catch (Throwable e) {
		}
		finally{
			try{
				st.close();
			}catch(Throwable e){
			}
		}
		return isDba; 
	}	
}
