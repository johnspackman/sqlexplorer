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

import org.eclipse.swt.widgets.Composite;

import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.model.DatabaseNode;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

/**
 * @author mazzolini
 */
public class InstanceNode implements IDbModel{
	public Composite getComposite(DetailManager detailManager){return null;};
	public Object getParent(){return parent;}
	public String toString(){return txt;}
	public String getTitle(){return txt;}

	private IDbModel parent;
	private String txt;
	private ArrayList list=new ArrayList(10);
	private SQLConnection conn;
	
	public InstanceNode(DatabaseNode root,String name,SQLConnection conn){
		txt=name;
		this.conn=conn;
		parent=root;
		list.add(new SysParamsNode(this,"System Parameters",conn));
		list.add(new TableSpacesNode(this,conn));
	}
	public Object[] getChildren() {
		return list.toArray();
	};
}
