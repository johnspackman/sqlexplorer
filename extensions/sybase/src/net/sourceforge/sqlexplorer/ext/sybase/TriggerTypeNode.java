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

import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import org.eclipse.swt.widgets.Composite;

public class TriggerTypeNode implements IDbModel {

	public Composite getComposite(DetailManager detailManager){return null;};
	public Object getParent(){return parent;}
	public String toString(){return txt;}
	public String getTitle(){return txt;}
	
	public Object[] getChildren(){
		if(!loaded){
			ResultSet rs=null;
			try{
				final String sql="SELECT name FROM " + parent.toString() +"..sysobjects WHERE type='TR' and name is not NULL";
				PreparedStatement ps=conn.prepareStatement(sql);
				rs=ps.executeQuery();
				while(rs.next()){
					String name=rs.getString(1);
					TriggerNode triggerNode=new TriggerNode(this,name,conn);
					list.add(triggerNode);
				}
				rs.close();
				ps.close();
				
			}catch(Throwable e){
				list.clear();
				return null;
			}finally{
				try{
					rs.close();
				}catch(Throwable e){
				}	
			}
			loaded=true;
		}
		return list.toArray();
	}

	private IDbModel parent;
	private String txt;
	private ArrayList list=new ArrayList(10);
	private SQLConnection conn;
	private boolean loaded=false;
	public TriggerTypeNode(IDbModel s,String name,SQLConnection conn){
		parent=s;
		txt=name;
		this.conn=conn;
	};


}
