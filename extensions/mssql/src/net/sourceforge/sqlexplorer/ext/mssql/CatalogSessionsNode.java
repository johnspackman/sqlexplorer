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
package net.sourceforge.sqlexplorer.ext.mssql;

import java.util.ArrayList;



import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.model.CatalogNode;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.ext.mssql.utility.TableConstructor;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


/**
 * @author mazzolini
 */
public class CatalogSessionsNode implements IDbModel {
	IDbModel parent;
	private String txt;
	
	SQLConnection conn;
	private ArrayList list=new ArrayList(1);
	public Composite getComposite(DetailManager detailManager){
		final Composite comp=new Composite(detailManager.getComposite(),SWT.NULL);
		comp.setLayout(new FillLayout());
		if(viewer!=null){
	
			viewer.getTable().dispose();
			viewer=null;
		}
	


		Display display=comp.getDisplay();
		BusyIndicator.showWhile(display,new Runnable(){
			public void run(){
				String owner=((CatalogNode)parent).toString();
				String sql="select ltrim(rtrim(loginame)) as 'User'"+
									"			,spid as 'Process ID',blocked as 'Blocked', ltrim(rtrim(sp.status)) as 'Status',cpu as 'CPU',physical_io"+
									"			as 'Physical IO',"+
									"			memusage as 'Memory Usage',"+
									"			ltrim(rtrim(hostname)) as 'Host',"+
									"			ltrim(rtrim( program_name )) as 'Application',"+
									"			convert(varchar(24),login_time,121) as 'Login Time',"+
									"			convert(varchar(24),last_batch,121) as 'Last Batch'"+
									"			from master..sysprocesses sp with (nolock) inner join master..sysdatabases sd with (nolock) on sp.dbid = sd.dbid"+
									"			where sd.name = '" + owner +"'"+
									"			order by 3 desc,1";				
					
				viewer=TableConstructor.constructTable(conn,sql,comp);
			}	
		});
		comp.layout();
		return comp;
	};
	
	TableViewer viewer;	
		
	public CatalogSessionsNode(IDbModel s,String name,SQLConnection conn){
		parent=s;
		txt=name;
		this.conn=conn;

	};
	public Object[] getChildren() {
		return list.toArray();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.dbviewer.IDbModel#getParent()
	 */
	public Object getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.dbviewer.IDbModel#activate()
	 */
	

	public String toString(){return txt;}

}
