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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.dbviewer.model.SchemaNode;
import net.sourceforge.sqlexplorer.dbviewer.model.TableNode;
import net.sourceforge.sqlexplorer.dbviewer.model.TableObjectTypeNode;
import net.sourceforge.sqlexplorer.ext.oracle.dialogs.ObjectEditor;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author mazzolini
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ViewEdit extends Action {

	SessionTreeNode sessionNode;
	IDbModel nd;
	public ViewEdit(SessionTreeNode sessionNode,IDbModel nd) {
		this.sessionNode=sessionNode;
		this.nd=nd;
	}
	public String getText(){
		return "View Source"; //$NON-NLS-1$
	}
	public void run(){
		try{
			TableNode tn=(TableNode)nd;
			final String sql="SELECT text FROM sys.all_views where owner =? and view_name=?";
			SQLConnection conn=sessionNode.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			String owner=((SchemaNode)((TableObjectTypeNode)nd.getParent()).getParent()).toString();
			stmt.setString(1,owner);
			stmt.setString(2, tn.toString());
			StringBuffer result = new StringBuffer(1000);
			ResultSet rs=null;
			for(rs = stmt.executeQuery(); rs.next();)
			{
				StringBuffer buf=new StringBuffer();
				InputStream ascii_data =rs.getAsciiStream(1);
				BufferedInputStream bis=new BufferedInputStream(ascii_data);
				int c;
				while ((c = bis.read ()) != -1){
					buf.append((char) c);
				}     			
				bis.close();
				ascii_data.close();
				//result.append("CREATE OR REPLACE VIEW ");
				//if(!rs.getString("description").trim().toUpperCase().substring(0, objectOwner.length()).equals(objectOwner))
				//result.append((tn.getOwner() + "."));
				result.append(buf);
			}

			rs.close();
			stmt.close();
			IPreferenceStore store=SQLExplorerPlugin.getDefault().getPreferenceStore();
			ObjectEditor oe=new ObjectEditor(null,store,sessionNode.getDictionary(),result.toString(),"View "+tn.toString(),conn,"VIEW",owner,tn.toString());
			oe.hideSaveButton();
			oe.open();
		}catch(Throwable e){
			SQLExplorerPlugin.error("Error opening view source",e);
		}
	}

}
