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

//import net.sf.jfacedbc.sessiontree.model.SqlEditNode;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.dbviewer.model.SchemaNode;
import net.sourceforge.sqlexplorer.ext.oracle.TriggerTypeNode;
import net.sourceforge.sqlexplorer.ext.oracle.dialogs.ObjectEditor;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;


public class CreateTriggerEdit extends Action {

	SessionTreeNode sessionNode;
	IDbModel node;
	public CreateTriggerEdit(SessionTreeNode sessionTreeNode, IDbModel node) {
		
		this.sessionNode=sessionTreeNode;
		this.node=node;
	}

	public String getText(){
		return "Create a new Trigger..."; //$NON-NLS-1$
	}
	public void run(){
		String owner=((SchemaNode)(((TriggerTypeNode)node).getParent())).toString();
		String separator= System.getProperty("line.separator"); //$NON-NLS-1$
		String txt="CREATE OR REPLACE TRIGGER "+owner+".<trigger> "+separator
		+"BEFORE DELETE OR UPDATE ON "+owner+".<table>"+separator
		+ "FOR EACH ROW\n"+separator
             + "--"+separator
             + "-- NAME        : "+separator
             + "-- DESCRIPTION : "+separator
          
             + "-- AUTHOR      "+separator+separator
		+"BEGIN "+separator
		 +" null;--do something"
		 +separator
		 + "END;"+separator;

		IPreferenceStore store=SQLExplorerPlugin.getDefault().getPreferenceStore();
		SQLConnection conn=sessionNode.getConnection();
		ObjectEditor oe=new ObjectEditor(null,store,sessionNode.getDictionary(),txt,"Creating a trigger ",conn,"TRIGGER",owner,null);
		oe.open();
	}



}
