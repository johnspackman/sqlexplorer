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

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbviewer.model.TableObjectTypeNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author cjp0tter
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CreateViewEdit extends Action {
	SessionTreeNode sessionNode;
	TableObjectTypeNode node;
	/**
	 * @param sessionNode
	 */
	public CreateViewEdit(SessionTreeNode sessionNode,TableObjectTypeNode node) {
		this.sessionNode=sessionNode;
		this.node=node;
	}
	public String getText(){
		return "Create a new View..."; //$NON-NLS-1$
	}
	public void run(){
		try{
			String owner=sessionNode.getSQLConnection().getSQLMetaData().getUserName();
			String separator= System.getProperty("line.separator"); //$NON-NLS-1$
			String txt="CREATE VIEW "+node.getParent().toString()+"."+owner+".<view> AS"+separator
			+ "/*"+separator
			+ "   NAME        : "+separator
			+ "   DESCRIPTION : "+separator
				+"   AUTHOR     " +separator
				+"*/"+separator
				+"SELECT column1"+separator
				+"FROM   table1"+separator
				+"WHERE  column1 LIKE '%';"+separator;
		
			SQLEditorInput input = new SQLEditorInput("CREATE VIEW ("+SQLExplorerPlugin.getDefault().getNextElement()+").sql");
			input.setSessionNode(sessionNode);
			IWorkbenchPage page=SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			SQLEditor editorPart= (SQLEditor) page.openEditor(input,IConstants.SQL_EDITOR_CLASS);
			editorPart.setText(txt);
		}catch(Exception e){
			SQLExplorerPlugin.error("Error creating sql editor",e);
		}
	}

	
}
