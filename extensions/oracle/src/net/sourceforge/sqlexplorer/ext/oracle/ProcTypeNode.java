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

import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.dbviewer.model.SchemaNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import org.eclipse.swt.widgets.Composite;

public class ProcTypeNode extends AbstractTypeNode implements IDbModel {

	public Composite getComposite(DetailManager detailManager){return null;};
	public Object getParent(){return parent;}
	public String toString(){return txt;}
	public String getTitle(){return txt;}
	
	public void load(){
		ResultSet rs=null;
		try{
			final String sql="SELECT  object_name "+
							"FROM sys.all_objects where owner=? "+
							"and object_type='PROCEDURE'";
			PreparedStatement ps=conn.prepareStatement(sql);
			String owner=((SchemaNode)parent).toString();
			ps.setString(1,owner);
			rs=ps.executeQuery();
			while(rs.next()){
				String name=rs.getString(1);
				ProcNode procNode=new ProcNode(this,name,conn);
				list.add(procNode);
			}
			rs.close();
			ps.close();
	
		}catch(Throwable e){
			list.clear();
		}finally{
			try{
				rs.close();
			}catch(Throwable e){
			}	
		}
	}
	

	private IDbModel parent;
	private String txt;
	private SQLConnection conn;
	public ProcTypeNode(IDbModel s,String name,SQLConnection conn){
		parent=s;
		txt=name;
		this.conn=conn;
	};


}
