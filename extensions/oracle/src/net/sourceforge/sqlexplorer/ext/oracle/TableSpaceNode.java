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
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.ext.oracle.utility.LProvider;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class TableSpaceNode implements IDbModel {

	/**
	 * @param node
	 * @param conn
	 */
	String txt;
	SQLConnection conn;
	String q;
	String p;

	public TableSpaceNode(TableSpacesNode node, SQLConnection conn, String name) {
		
		this.parent=node;
		this.txt=name;
		this.conn=conn;
		q="select INITIAL_EXTENT, NEXT_EXTENT, MIN_EXTENTS, MAX_EXTENTS, PCT_INCREASE, MIN_EXTLEN, STATUS, CONTENTS, LOGGING, EXTENT_MANAGEMENT, ALLOCATION_TYPE from USER_TABLESPACES where TABLESPACE_NAME='"+txt+"'";
		p="select INITIAL_EXTENT, NEXT_EXTENT, MIN_EXTENTS, MAX_EXTENTS, PCT_INCREASE, MIN_EXTLEN, STATUS, CONTENTS, LOGGING, EXTENT_MANAGEMENT, ALLOCATION_TYPE from DBA_TABLESPACES where TABLESPACE_NAME='"+txt+"'";

	}
	IDbModel parent;

	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.dbviewer.model.IDbModel#getChildren()
	 */
	public Object[] getChildren() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.dbviewer.model.IDbModel#getParent()
	 */
	public Object getParent() {
		return parent;
	}
	TableViewer viewer;	
	/* (non-Javadoc)
	 * @see net.sf.jfacedbc.dbviewer.model.IDbModel#getComposite(net.sf.jfacedbc.dbviewer.DetailManager)
	 */
	public Composite getComposite(DetailManager detailManager){
		final Composite comp=new Composite(detailManager.getComposite(),SWT.NULL);
		comp.setLayout(new FillLayout());

		Display display=comp.getDisplay();
		BusyIndicator.showWhile(display,new Runnable(){
			public void run(){
				String sql=null;
				if(OraclePlugin.isDba(conn))
					sql=p;
				else
					sql=q;	
				try{
					viewer=new TableViewer(comp,SWT.NULL);						
					Table table=viewer.getTable();
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
					viewer.setLabelProvider(new LProvider());
					final Map map=getInfo(sql);		
					viewer.setContentProvider(new IStructuredContentProvider(){
						public Object[] getElements(Object input) {
							return map.entrySet().toArray();
						}
						public void dispose() {}
						public void inputChanged(Viewer viewer, Object arg1, Object arg2) {}
					});
					viewer.setInput(this);
				}catch(Throwable e){
					
				}
				
			}
		});
		comp.layout();
		return comp;
	};
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return txt;
	}
	private String[] keys={"Initial Extent","Next Extent","Min Extent","Max Extent","PCT Increase","Min ExtLen","Status","Contents","Logging","Extent Management","Allocation Type"};
	Map getInfo(String sql){
		TreeMap map=new TreeMap();
		ResultSet rs=null;
		try{
			PreparedStatement ps=conn.prepareStatement(sql);
			String owner=((IDbModel)parent).getParent().toString();
			rs=ps.executeQuery();
	
			if(rs.next()){
				for(int i=0;i<keys.length;i++){
					map.put(keys[i],rs.getString(i+1));
				}
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
}
