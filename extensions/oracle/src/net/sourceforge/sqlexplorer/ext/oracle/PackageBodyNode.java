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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.eclipse.jface.viewers.Viewer;

import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.ext.oracle.utility.*;
import net.sourceforge.sqlexplorer.sqlpanel.SQLTextViewer;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import java.util.*;

import org.eclipse.jface.viewers.TableViewer;


public class PackageBodyNode implements IDbModel {
	static final Object[] keys={"Created","Last DDL Time","TimeStamp","Status"};
	private IDbModel parent;
	
	private SQLTextViewer viewer;
	
	private String txt;
	private ArrayList list=new ArrayList(1);
	public Object getParent(){return parent;}
	public Object []getChildren(){return list.toArray();}
	public String toString(){return txt;}
	private SQLConnection conn;
	private TableViewer tv;
	
	public Composite getComposite(DetailManager detailManager){
		Composite comp=new Composite(detailManager.getComposite(),SWT.NULL);
		comp.setLayout(new FillLayout());
		TabFolder tabFolder=new TabFolder(comp,SWT.NULL);
		final TabItem tabItem1=new TabItem(tabFolder,SWT.NULL);
		tabItem1.setText("Source"); //$NON-NLS-1$
		tabItem1.setToolTipText("Source");	 //$NON-NLS-1$

		viewer = new SQLTextViewer(tabFolder,SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER|SWT.FULL_SELECTION,detailManager.getStore(),null);
		tabItem1.setControl(viewer.getControl());	

		final TabItem tabItem2=new TabItem(tabFolder,SWT.NULL);
		tabItem2.setText("Info"); //$NON-NLS-1$
		tabItem2.setToolTipText("Info");	 //$NON-NLS-1$
		tv=InfoBuilder.createInfoViewer(tabFolder,tabItem2);
		viewer.setDocument(new Document(getSource()));
		viewer.refresh();
		viewer.setEditable(false);

		final HashMap map=this.getInfo();		
		tv.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object input) {
				return map.entrySet().toArray();
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object arg1, Object arg2) {}
		});
		tv.setInput(this);
		return comp;
	};
	
	
	public PackageBodyNode(IDbModel s,String name,SQLConnection conn){
		this.conn=conn;
		parent=s;
		txt=name;

	}

	private String getSource(){
		StringBuffer buf=new StringBuffer();
		
		final String sql="SELECT text FROM sys.all_source where owner=? and name=? and type='PACKAGE BODY' order by line";
		String delimiter=viewer.getTextWidget().getLineDelimiter();
		ResultSet rs=null;
  		try{
			PreparedStatement ps=conn.prepareStatement(sql);
			String owner=((IDbModel)parent).getParent().toString();
			ps.setString(1,owner);
			ps.setString(2,txt);
			rs=ps.executeQuery();
			while(rs.next()){
				String text=rs.getString(1);
				//System.out.println(text);
				if(text!=null && text.length()>0){
					buf.append(text.substring(0,text.length()-1));
				}
				buf.append(delimiter);
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
		return buf.toString();
		
	}
	
	private HashMap getInfo(){
		final String sql="SELECT  created,last_ddl_time, timestamp, status FROM sys.all_objects where owner=? and object_type='PACKAGE BODY' and object_name=?";
		HashMap map=new HashMap();
		ResultSet rs=null;
  		try{
			PreparedStatement ps=conn.prepareStatement(sql);
			String owner=((IDbModel)parent).getParent().toString();
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
	/**
	 * @return
	 */
	public String getOwner() {
		return ((IDbModel)parent).getParent().toString();
	}

}
