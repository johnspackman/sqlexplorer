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
package net.sourceforge.sqlexplorer.ext.oracle.panels;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.dbviewer.model.TableNode;
import net.sourceforge.sqlexplorer.ext.IActivablePanel;
import net.sourceforge.sqlexplorer.ext.oracle.utility.LProvider;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class StatusPanel implements IActivablePanel {
	boolean activated=false;
	static final Object[] keys={"Created","Last DDL Time","TimeStamp","Status"};
	private HashMap getInfo(String owner,String txt, SQLConnection conn,TableNode tn){
		final String sql1="SELECT  created,last_ddl_time, timestamp, status FROM sys.all_objects where owner=? and object_type='TABLE' and object_name=?";
		final String sql2="SELECT  created,last_ddl_time, timestamp, status FROM sys.all_objects where owner=? and object_type='VIEW' and object_name=?";
		String sql=null;
		if(tn.isView())
			sql=sql2;
		else
			sql=sql1;
		HashMap map=new HashMap();
		ResultSet rs=null;
		try{
			PreparedStatement ps=conn.prepareStatement(sql);
			ps.setString(1,owner);
			ps.setString(2,txt);
			rs=ps.executeQuery();
		
			if(rs.next()){
				map.put(keys[0],rs.getString(1));
				map.put(keys[1],rs.getString(2));
				map.put(keys[2],rs.getString(3));
				map.put(keys[3],rs.getString(4));
			}
			rs.close();
			ps.close();
		}catch(Throwable e){
		}finally{
			try{
				rs.close();
			}catch(Throwable e){
			}	
		}
		return map;

	}
	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.ext.IActivablePanel#activate()
	 */
	public void activate() {
		if(activated){
			return;
		}
		String owner=null;
		String txt=null;
		if(tn instanceof TableNode){
			TableNode tb=(TableNode)tn;
			txt=tb.toString();
			owner=((IDbModel)tb.getParent()).getParent().toString();
			final Map map=getInfo(owner,txt,sessionNode.getConnection(),tb);
			activated=true;
			tv.setContentProvider(new IStructuredContentProvider(){
				public Object[] getElements(Object input) {
					return map.entrySet().toArray();
				}
				public void dispose() {}
				public void inputChanged(Viewer viewer, Object arg1, Object arg2) {}
			});
			tv.setInput(this);
		}
		
		//Map mp=getInfo(owner,txt,sessionNode.getConnection());
		
	}


	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.ext.IActivablePanel#getText()
	 */
	public String getText() {
		
		return "Status";
	}
	private SessionTreeNode sessionNode;
	Composite cmp;
	IDbModel tn;
	Composite parent;
	TableViewer tv;

	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.ext.IActivablePanel#create(net.sf.jfacedbc.sessiontree.model.SessionTreeNode, net.sf.jfacedbc.dbviewer.model.TableNode, org.eclipse.swt.widgets.TabFolder)
	 */
	public Control create(SessionTreeNode sessionNode, IDbModel tn, Composite parent) {
		this.sessionNode=sessionNode;
		this.tn=tn;
		this.parent=parent;
		Composite cmp=new Composite(parent,SWT.NULL);
		cmp.setLayout(new FillLayout());
		tv =new TableViewer(cmp,SWT.NULL);
		
		Table table=tv.getTable();
		TableColumn c1=new TableColumn(table,SWT.NULL);
		c1.setText("Property"); //$NON-NLS-1$
		TableColumn c2=new TableColumn(table,SWT.NULL);		
		c2.setText("Value");		 //$NON-NLS-1$
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		TableLayout tableLayout=new TableLayout();
		for(int i=0;i<2;i++)
			tableLayout.addColumnData(new ColumnWeightData(1, 50, true));
		table.setLayout(tableLayout);
		tv.setLabelProvider(new LProvider());
		return cmp;
	}

}
