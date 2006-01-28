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
package net.sourceforge.sqlexplorer.ext.oracle.utility;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sqlpanel.SQLTableSorter;
import net.sourceforge.sqlexplorer.sqlpanel.SqlTableContentProvider;
import net.sourceforge.sqlexplorer.sqlpanel.SqlTableLabelProvider;
import net.sourceforge.sqlexplorer.sqlpanel.SqlTableModel;
import net.sourceforge.sqlexplorer.util.SQLString;
import net.sourceforge.squirrel_sql.fw.sql.ResultSetReader;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @author mazzolini
 */
public class TableConstructor {
		
	
	public static TableViewer constructTable(SQLConnection conn,String sql, Composite parentComposite){
		final Image imgAsc=ImageDescriptor.createFromURL(SqlexplorerImages.getAscOrderIcon()).createImage();
		final Image imgDesc=ImageDescriptor.createFromURL(SqlexplorerImages.getDescOrderIcon()).createImage();
		SqlTableModel stm=null;
		int count=0;
		ResultSet set=null;
		TableViewer viewer=null;
		try{

			Statement st=conn.createStatement();

			set=st.executeQuery(sql);
			ResultSetReader reader=new ResultSetReader(set);
			ResultSetMetaData metaData=set.getMetaData();
			count=metaData.getColumnCount();
			viewer=new TableViewer(parentComposite,SWT.FULL_SELECTION);
			final Table table=viewer.getTable();
			final SQLTableSorter sorter=new SQLTableSorter(count,metaData);
			table.addDisposeListener(new DisposeListener(){
				public void widgetDisposed(DisposeEvent e){
					imgAsc.dispose();
					imgDesc.dispose();
				}
			});
			final String[]ss=new String[count];
			viewer.setSorter(sorter);
			final TableViewer tmpViewer=viewer;
			SelectionListener headerListener = new SelectionAdapter() {
				
				
				public void widgetSelected(SelectionEvent e) {
					// column selected - need to sort
					int column = table.indexOf((TableColumn) e.widget);
					if (column == sorter.getTopPriority()){
						int k=sorter.reverseTopPriority();
						if(k==SQLTableSorter.ASCENDING)
							((TableColumn) e.widget).setImage(imgAsc);
						else
							((TableColumn) e.widget).setImage(imgDesc);
					}else {
						sorter.setTopPriority(column);
						((TableColumn) e.widget).setImage(imgAsc);
					}
					TableColumn[] tcArr=table.getColumns();
					for(int i=0;i<tcArr.length;i++){
						if(i!=column){
							tcArr[i].setImage(null);
						}
					}
					tmpViewer.refresh();
					
				}
			};
			
			
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			viewer.setContentProvider(new SqlTableContentProvider());
			for(int i=0;i<count;i++){
				TableColumn tc=new TableColumn(table,SWT.NULL);
				if(i==0)
					tc.setImage(imgAsc);
				tc.setText(metaData.getColumnLabel(i+1));
				ss[i]=new String(metaData.getColumnLabel(i+1));
				tc.addSelectionListener(headerListener);
			}
			viewer.setColumnProperties(ss);
			
			stm=new SqlTableModel(reader,metaData,1000,conn,ss,sorter, new SQLString(sql));
			SqlTableLabelProvider slp=new SqlTableLabelProvider(stm);
			viewer.setLabelProvider(slp);	
			viewer.setInput(stm);
			for (int i = 0; i < count; i++) {
				table.getColumn(i).pack();
			}
			table.layout();	 
		}catch(java.lang.Throwable e){
			SQLExplorerPlugin.error("Error creating table ",e); //$NON-NLS-1$

		}finally{
			try{
				Statement st=set.getStatement();
				if(st!=null)
					st.close();
				set.close();

			}catch(Throwable e){
				
			}

		}
		return viewer;
	}
}
