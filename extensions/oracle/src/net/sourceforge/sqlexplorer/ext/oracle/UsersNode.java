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

import java.util.ArrayList;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.ext.oracle.utility.TableConstructor;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;


public class UsersNode implements IDbModel {
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
						
				String sql="SELECT username,user_id,created FROM sys.all_users";
				viewer=TableConstructor.constructTable(conn,sql,comp);
			
			}
		});
		comp.layout();
		return comp;
	};

	
	TableViewer viewer;	

	public UsersNode(IDbModel s,String name,SQLConnection conn){
			parent=s;
			txt=name;
			this.conn=conn;
		};
	public Object[] getChildren() {
		return list.toArray();
	}

	public Object getParent() {
		return parent;
	}


	public String toString(){return txt;}
	

}
