package net.sourceforge.sqlexplorer.ext.sybase;
/*
 * Copyright (C) 2002-2004 Chris Potter
 * cjp0tter@users.sourceforge.net
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.ext.sybase.utility.InfoBuilder;
import net.sourceforge.sqlexplorer.sqlpanel.SQLTextViewer;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class TriggerNode implements IDbModel {
	static final Object[] keys={"Created"};
	public Composite getComposite(DetailManager detailManager){
		Composite comp=new Composite(detailManager.getComposite(),SWT.NULL);
		comp.setLayout(new FillLayout());
		TabFolder tabFolder=new TabFolder(comp,SWT.NULL);
		final TabItem tabItem1=new TabItem(tabFolder,SWT.NULL);
		tabItem1.setText("Source");
		tabItem1.setToolTipText("Source");

		viewer = new SQLTextViewer(tabFolder,SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER|SWT.FULL_SELECTION,detailManager.getStore(),null);
		tabItem1.setControl(viewer.getControl());	
		final TabItem tabItem2=new TabItem(tabFolder,SWT.NULL);	
		tabItem2.setText("Info");
		tabItem2.setToolTipText("Info");
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
	
	private TableViewer tv;
	IDbModel parent;
	
	
	private SQLTextViewer viewer;
	
	private String txt;
	private ArrayList list=new ArrayList(1);
	public Object getParent(){return parent;}
	public Object []getChildren(){return list.toArray();}
	public String toString(){return txt;}
	private SQLConnection conn;

	public TriggerNode(IDbModel s,String name,SQLConnection conn){
		this.conn=conn;
		parent=s;
		txt=name;

	}

	public String getOwner(){
		String owner=null;
		try{
			owner=conn.getSQLMetaData().getUserName();
  		}catch(Throwable e){
  			
		}
		return owner;
	}
	private String getSource(){
		StringBuffer buf=new StringBuffer();
		
		final String dbName=parent.getParent().toString();
		final String sql="SELECT sc.text, sc.id, sc.colid FROM " + dbName + "..sysobjects so, " + dbName + "..syscomments sc WHERE so.type='TR' AND so.name=? AND sc.id=so.id order by 2,3";
		String delimiter=viewer.getTextWidget().getLineDelimiter();
		ResultSet rs=null;
  		try{
			PreparedStatement ps=conn.prepareStatement(sql);
			ps.setString(1,txt);
			rs=ps.executeQuery();
			while(rs.next()){
				String text=rs.getString(1);
				if(text!=null && text.length()>0){
					buf.append(text.substring(0,text.length()-1));
				}
				buf.append(delimiter);
			}
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
		final String dbName=parent.getParent().toString();
		final String sql="SELECT crdate FROM " + dbName + "..sysobjects WHERE type='TR' AND name=?";
		HashMap map=new HashMap();
		ResultSet rs=null;
  		try{
			PreparedStatement ps=conn.prepareStatement(sql);
			ps.setString(1,txt);
			rs=ps.executeQuery();
			
			if(rs.next()){
				map.put(keys[0],rs.getString(1));
			}
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


}
