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
package net.sourceforge.sqlexplorer.ext.oracle.actions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Random;

import net.sourceforge.sqlexplorer.ext.oracle.OraclePlugin;
import net.sourceforge.sqlexplorer.ext.oracle.actions.explain.ExplainNode;
import net.sourceforge.sqlexplorer.ext.oracle.actions.explain.ExplainPlanDialog;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.resource.ImageDescriptor;


public class ExplainAction extends Action {
	SQLEditor editor;
	public ExplainAction(SQLEditor editor) {
		this.editor=editor;
	}
	public String getText(){
			return "Show Explain Plan";
	}
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromURL(OraclePlugin.getUrl("plugins/icons/explain.gif"));
	}
	public String getToolTipText(){
		return "Show Explain Plan";
	}
	public void run(){
		try{
			SessionTreeNode stn=editor.getSessionTreeNode();
			String sqlText=editor.sqlTextViewer.getTextWidget().getText();
			Statement st=stn.getConnection().createStatement();
			boolean createPlanTable=false;
			boolean notFoundTable=true;
			try{
				ResultSet rs=st.executeQuery("select statement_id from plan_table");
				notFoundTable=false;
				rs.close();
			}catch(Throwable e){
				createPlanTable=MessageDialog.openQuestion(null,"Plan Table not found","Plan table not found. Do you want to create a new one?");
			}
			finally{
				try{st.close();}catch(Throwable e){}
			}
			if(notFoundTable && !createPlanTable)
			{
				return;
			}
			
			if(notFoundTable && createPlanTable){
				st=stn.getConnection().createStatement();
				try{
					st.execute(createPlanTableScript);
				}catch(Throwable e){
					SQLExplorerPlugin.error("Error creating the plan table",e);
					MessageDialog.openError(null,"Table not created","Error creating the plan table. It's impossible do create the explain plan.");
					try{st.close();}catch(Throwable e1){}
					return;
				}
				try{st.close();}catch(Throwable e){}
			}
			st=stn.getConnection().createStatement();
			String id_=Integer.toHexString(new Random().nextInt()).toUpperCase();
			st.execute("delete plan_table where statement_id='"+id_+"'");
			st.close();
			st=stn.getConnection().createStatement();
			st.execute("EXPLAIN PLAN SET statement_id = '"+id_+"' FOR "+sqlText);
			st.close();
			PreparedStatement ps=stn.getConnection().prepareStatement("select "+ 
			"object_type,operation,options,object_owner,object_name,optimizer,cardinality ,cost,id,parent_id "+                                                           
		  " from "+ 
			" plan_table "+ 
		  " start with id = 0 and statement_id=? "+
		  " connect by prior id=parent_id and statement_id=?");
		  ps.setString(1,id_);
		  ps.setString(2,id_);
		  ResultSet rs=ps.executeQuery();
		  HashMap mp=new HashMap();
		  while(rs.next()){
			String object_type=rs.getString("object_type");
			String operation=rs.getString("operation");
			String options=rs.getString("options");
			String object_owner=rs.getString("object_owner");
			String object_name=rs.getString("object_name");
			String optimizer=rs.getString("optimizer");
			int cardinality=rs.getInt("cardinality");
			if(rs.wasNull()){
				cardinality=-1;
			}
				
			int cost=rs.getInt("cost");
			if(rs.wasNull())
				cost=-1;
		  	int parentID=rs.getInt("parent_id");
		  	int id=rs.getInt("id");
			ExplainNode nd=null;
		  	if(id==0){
		  		ExplainNode dummy=new ExplainNode(null);
				mp.put(new Integer(-1),dummy);
		  		dummy.setId(-1);
		  		nd=new ExplainNode(dummy);
		  		dummy.add(nd);
				nd.setId(0);
				mp.put(new Integer(0),nd);
		  	}else{
		  		ExplainNode nd_parent=(ExplainNode) mp.get(new Integer(parentID));
		  		
		  		nd=new ExplainNode(nd_parent);
		  		nd_parent.add(nd);
		  		mp.put(new Integer(id),nd);
		  	}
			nd.setCardinality(cardinality);
			nd.setCost(cost);
			nd.setObject_name(object_name);
			nd.setObject_owner(object_owner);
			nd.setObject_type(object_type);
			nd.setOperation(operation);
			nd.setOptimizer(optimizer);
			nd.setOptions(options);
		  }
		  rs.close();
		  ps.close();
		  ExplainNode nd_parent=(ExplainNode) mp.get(new Integer(-1));
		  if(nd_parent==null)
		  	System.out.println("Null parent");
		  ExplainPlanDialog dlg=new ExplainPlanDialog(null,nd_parent);
		  dlg.open();
		}catch(Throwable e){
			SQLExplorerPlugin.error("Error executing the explain plan ",e);
			MessageDialog.openError(null,"Error","See the Eclipse error log view for more details");
			
		}
	}
	static final String createPlanTableScript="CREATE TABLE PLAN_TABLE ("+
"  STATEMENT_ID                    VARCHAR2(30),"+
" TIMESTAMP                       DATE,"+
"  REMARKS                         VARCHAR2(80),"+
"  OPERATION                       VARCHAR2(30),"+
"  OPTIONS                         VARCHAR2(30),"+
"  OBJECT_NODE                     VARCHAR2(128),"+
"  OBJECT_OWNER                    VARCHAR2(30),"+
"  OBJECT_NAME                     VARCHAR2(30),"+
"  OBJECT_INSTANCE                 NUMBER(38),"+
"  OBJECT_TYPE                     VARCHAR2(30),"+
"  OPTIMIZER                       VARCHAR2(255),"+
"  SEARCH_COLUMNS                  NUMBER,"+
"  ID                              NUMBER(38),"+
"  PARENT_ID                       NUMBER(38),"+
"  POSITION                        NUMBER(38),"+
"  COST                            NUMBER(38),"+
"  CARDINALITY                     NUMBER(38),"+
"  BYTES                           NUMBER(38),"+
"  OTHER_TAG                       VARCHAR2(255),"+
"  PARTITION_START                 VARCHAR2(255),"+
"  PARTITION_STOP                  VARCHAR2(255),"+
"  PARTITION_ID                    NUMBER(38),"+
"  OTHER                           LONG,"+
"  DISTRIBUTION                    VARCHAR2(30)"+
")";
;
}
