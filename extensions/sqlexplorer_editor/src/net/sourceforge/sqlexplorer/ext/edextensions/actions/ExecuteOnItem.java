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
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sqlpanel.actions.ExecSQLAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;

public class ExecuteOnItem extends MenuManager {
	public ExecuteOnItem(final SQLEditor editor){
		super("Execute on...");
		Object[] connections=SQLExplorerPlugin.getDefault().stm.getRoot().getChildren();
		for(int i=0;i<connections.length;i++){
			final SessionTreeNode conn=(SessionTreeNode) connections[i];
			Action act=new Action(){
				public String getText(){
					return conn.toString().replace('@','_');
				}
				public void run(){
					new ExecSQLAction(editor,SQLExplorerPlugin.getDefault().getPreferenceStore().getInt(IConstants.MAX_SQL_ROWS),conn).run();
				}
			};
			this.add(act);
		}
	}
}
