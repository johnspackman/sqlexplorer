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

package net.sourceforge.sqlexplorer.ext.edextensions.actions;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.ext.edextensions.EditorExtensionPlugin;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sqlpanel.actions.ExecSQLAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public class ExecuteSQL extends Action {
	ExecSQLAction embeddedAction;
	SQLEditor editor;
	public ExecuteSQL(SQLEditor editor){
		this.editor=editor;
		if(editor.getSessionTreeNode()==null)
			this.setEnabled(false);
		embeddedAction=new ExecSQLAction(editor,SQLExplorerPlugin.getDefault().getPreferenceStore().getInt(IConstants.MAX_SQL_ROWS));
	}
	public void run(){
		embeddedAction.run();
	}
	public String getText(){
			return "Execute SQL";
	}
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromURL(EditorExtensionPlugin.getUrl("plugins/icons/replace_persp.gif"));
	}
	public String getToolTipText(){
		return "Execute SQL";
	}	
}
