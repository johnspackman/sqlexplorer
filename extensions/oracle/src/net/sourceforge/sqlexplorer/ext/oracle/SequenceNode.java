package net.sourceforge.sqlexplorer.ext.oracle;

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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;



import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.ext.oracle.utility.InfoBuilder;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class SequenceNode implements IDbModel {
	
	static final Object[] keys={"Created","Last DDL Time","TimeStamp","Status"};
	static final Object[] detailKeys={"Min Value","Max Value","Increment By","Cycle Flag", "Order","Cache Size","Last Number"};
	public Composite getComposite(DetailManager detailManager){
		Composite comp=new Composite(detailManager.getComposite(),SWT.NULL);
		comp.setLayout(new FillLayout());
		TabFolder tabFolder=new TabFolder(comp,SWT.NULL);
		final TabItem tabItem1=new TabItem(tabFolder,SWT.NULL);	
		tabItem1.setText("Info"); //$NON-NLS-1$
		tabItem1.setToolTipText("Info");	 //$NON-NLS-1$
		tv=InfoBuilder.createInfoViewer(tabFolder,tabItem1);	
		final TabItem tabItem2=new TabItem(tabFolder,SWT.NULL);
		tabItem2.setText("Detail"); //$NON-NLS-1$
		tabItem2.setToolTipText("Detail");	 //$NON-NLS-1$

		tv2=InfoBuilder.createInfoViewer(tabFolder,tabItem2);
		final HashMap map=this.getInfo();		
		tv.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object input) {
				return map.entrySet().toArray();
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object arg1, Object arg2) {}
		});
		final HashMap map2=this.getDetail();
				tv2.setContentProvider(new IStructuredContentProvider(){
					public Object[] getElements(Object input) {
						return map2.entrySet().toArray();
					}
					public void dispose() {}
					public void inputChanged(Viewer viewer, Object arg1, Object arg2) {}
				});
		tv2.setInput(this);
		tv.setInput(this);		
		return comp;
	};

	private TableViewer tv;
	private TableViewer tv2;
	
	IDbModel parent;
	
	private String txt;
	private ArrayList list=new ArrayList(1);
	public Object getParent(){return parent;}
	public Object []getChildren(){return list.toArray();}
	public String toString(){return txt;}
	private SQLConnection conn;

	public SequenceNode(IDbModel s,String name,SQLConnection conn){
		this.conn=conn;
		parent=s;
		txt=name;
	}
	
	private HashMap getInfo(){
		final String sql="SELECT  created,last_ddl_time, timestamp, status FROM sys.all_objects where owner=? and object_type='SEQUENCE' and object_name=?";
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
	
	private HashMap getDetail(){
			final String sql=
		"select min_value,max_value,increment_by,cycle_flag,order_flag, cache_size, last_number from sys.all_sequences "+
		"where sequence_owner=? and sequence_name=?";
			HashMap map=new HashMap();
			ResultSet rs=null;
			try{
				PreparedStatement ps=conn.prepareStatement(sql);
				String owner=((IDbModel)parent).getParent().toString();
				ps.setString(1,owner);
				ps.setString(2,txt);
				rs=ps.executeQuery();

				if(rs.next()){
					map.put(detailKeys[0],rs.getString(1));
					map.put(detailKeys[1],rs.getString(2));
					map.put(detailKeys[2],rs.getString(3));
					map.put(detailKeys[3],rs.getString(4));
					map.put(detailKeys[4],rs.getString(5));
					map.put(detailKeys[5],rs.getString(6));
					map.put(detailKeys[6],rs.getString(7));
				}
				ps.close();
			}catch(Throwable e){
			}
			finally{
				try{
					rs.close();
				}catch(Throwable e){
				}
			}
			return map;

		}


}
