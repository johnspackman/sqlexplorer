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
package net.sourceforge.sqlexplorer.ext.mssql.actions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.ext.mssql.TriggerNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author cjp0tter
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TriggerEdit extends Action {

	SessionTreeNode sessionNode;
	IDbModel node;
	public TriggerEdit(SessionTreeNode sessionTreeNode, IDbModel node) {
		
		this.sessionNode=sessionTreeNode;
		this.node=node;
	}

	public String getText(){
		return "Edit an existing Trigger...";
	}
	public void run(){
		try{
			TriggerNode tn=(TriggerNode) node;
			String dbName=((IDbModel)tn.getParent()).getParent().toString();
			final String sql="SELECT sc.text, sc.id, sc.colid FROM " + dbName + "..sysobjects so, " + dbName + "..syscomments sc WHERE so.type='TR' AND so.name=? AND sc.id=so.id order by 2,3";
			SQLConnection conn=sessionNode.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, tn.toString());
			StringBuffer result1 = new StringBuffer(1000);
			ResultSet rs=stmt.executeQuery();
			while(rs.next()){
				result1.append(rs.getString(1));
			}
			String finalResult=result1.toString();
			rs.close();
			stmt.close();
			SQLEditorInput input = new SQLEditorInput("EDIT TRIGGER ("+tn.toString()+").sql");
			input.setSessionNode(sessionNode);
			IWorkbenchPage page=SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			SQLEditor editorPart= (SQLEditor) page.openEditor(input,IConstants.SQL_EDITOR_CLASS);
			editorPart.setText(finalResult);
		}catch(Throwable e){
			SQLExplorerPlugin.error("Error creating sql editor",e);
		}
	}

}
