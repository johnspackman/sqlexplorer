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

import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.ext.IActivablePanel;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class DependentObjectsPanel implements IActivablePanel {

	boolean activated=false;
	

	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.ext.IActivablePanel#activate()
	 */
	public void activate() {
		if(activated){
			return;
		}
		activated=true;
		final String sql=
		"select owner,type,name from sys.ALL_DEPENDENCIES "+
		" where referenced_owner=? and referenced_name=?"+
		" and referenced_type=? order by owner,type,name";

		ResultSet rs=null;
		try{
			PreparedStatement ps=sessionNode.getConnection().prepareStatement(sql);
			IDbModel parentNode=(IDbModel) tn.getParent();
			String owner=((IDbModel)parentNode).getParent().toString();
			ps.setString(1,owner);
			ps.setString(2,tn.toString());
			ps.setString(3,parentNode.toString());
			rs=ps.executeQuery();
			while(rs.next()){
				rs.getString(1);
				TableItem ti=new TableItem(tb,SWT.NULL);
				ti.setText(0,rs.getString(1));	
				ti.setText(1,rs.getString(2));
				ti.setText(2,rs.getString(3));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		for (int i = 0; i < tb.getColumnCount(); i++) {
			tb.getColumn(i).pack();
		}
		tb.layout();
		cmp.layout();
		
	}


	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.ext.IActivablePanel#getText()
	 */
	public String getText() {
		return "Dependent Objects";
	}
	private SessionTreeNode sessionNode;
	Table tb;
	Composite cmp;
	IDbModel tn;
	Composite parent;

	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.ext.IActivablePanel#create(net.sf.jfacedbc.sessiontree.model.SessionTreeNode, net.sf.jfacedbc.dbviewer.model.TableNode, org.eclipse.swt.widgets.TabFolder)
	 */
	public Control create(SessionTreeNode sessionNode, IDbModel tn, Composite parent) {
		this.sessionNode=sessionNode;
		this.tn=tn;
		this.parent=parent;
		cmp=new Composite(parent,SWT.NULL);
		cmp.setLayout(new FillLayout());
		tb=new Table(cmp,SWT.H_SCROLL|SWT.V_SCROLL|SWT.FULL_SELECTION|SWT.BORDER);
		tb.setLinesVisible(true);
		tb.setHeaderVisible(true);
		TableColumn tc=new TableColumn(tb,SWT.NULL);
		tc.setText("Owner");
		tc=new TableColumn(tb,SWT.NULL);
		tc.setText("Type");
		tc=new TableColumn(tb,SWT.NULL);
		tc.setText("Name");
		return cmp;
	}

}
