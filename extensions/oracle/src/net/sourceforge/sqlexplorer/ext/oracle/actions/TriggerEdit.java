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
package net.sourceforge.sqlexplorer.ext.oracle.actions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.ext.oracle.TriggerNode;
import net.sourceforge.sqlexplorer.ext.oracle.dialogs.ObjectEditor;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author mazzolini
 */
public class TriggerEdit extends Action {
	
	IDbModel nd;
	private ImageDescriptor img=ImageDescriptor.createFromURL(SqlexplorerImages.getSqlEditorIcon());
	IPreferenceStore store;
	SessionTreeNode treeNode;
	public TriggerEdit(SessionTreeNode treeNode,IDbModel nd){
		this.treeNode=treeNode;
		this.nd=nd;
		store=SQLExplorerPlugin.getDefault().getPreferenceStore();
	}
	public String getText(){
		return "Trigger Edit"; //$NON-NLS-1$
	}
	public void run(){
		
		try{
			TriggerNode tn=(TriggerNode)nd;
			final String sql="SELECT description,trigger_body FROM sys.all_triggers where owner=? and trigger_name=?";
			SQLConnection conn=treeNode.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, tn.getOwner());
			stmt.setString(2, tn.toString());
			StringBuffer result = new StringBuffer(1000);
			ResultSet rs=null;
			for(rs = stmt.executeQuery(); rs.next();)
			{
				result.append("CREATE OR REPLACE TRIGGER ");
				//if(!rs.getString("description").trim().toUpperCase().substring(0, objectOwner.length()).equals(objectOwner))
				//result.append((tn.getOwner() + "."));
				result.append((rs.getString(1) + rs.getString(2)));
			}

			rs.close();
			stmt.close();
			ObjectEditor oe=new ObjectEditor(null,store,treeNode.getDictionary(),result.toString(),"Editing "+tn.toString(),conn,"TRIGGER",tn.getOwner(),tn.toString());
			oe.open();
		}catch(Throwable e){
			SQLExplorerPlugin.error("Error opening trigger source",e);
		}
	}
	public ImageDescriptor getHoverImageDescriptor(){
		return img;
	}
	public ImageDescriptor getImageDescriptor(){
		return img;            		
	};
}
