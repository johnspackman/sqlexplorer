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

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;

import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;


public class TableSpacesNode implements IDbModel{

	
	public Composite getComposite(DetailManager detailManager){return null;};
	public Object getParent(){return parent;}
	public String toString(){return txt;}
	public String getTitle(){return txt;}

	private IDbModel parent;
	private String txt="TableSpace";
	private ArrayList list=new ArrayList(10);
	private SQLConnection conn;
	private boolean loaded=false;
	public TableSpacesNode(InstanceNode parent,SQLConnection conn){
		this.conn=conn;
		this.parent=parent;
	}
	public Object[] getChildren() {
		if(!loaded)
			load();
		return list.toArray();
	};
	private void load(){
		boolean isDba=OraclePlugin.isDba(conn);
		String query=null;
		if (isDba)
			query = "SELECT TABLESPACE_NAME from DBA_TABLESPACES order by TABLESPACE_NAME";
		else
			query = "SELECT TABLESPACE_NAME FROM USER_TABLESPACES order by TABLESPACE_NAME";
		Statement st=null; 
		try{
			st = conn.createStatement();
			ResultSet rs=st.executeQuery(query);
	
			while (rs.next())
			{
				String name = rs.getString(1);
				list.add(new TableSpaceNode(this,conn,name));
			}
		}
		catch (Throwable e) {
		}
		finally{
			try{
				st.close();
			}catch(Throwable e){
			}
		}
	}
}
