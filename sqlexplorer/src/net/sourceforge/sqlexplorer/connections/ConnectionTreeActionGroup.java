/*
 * Copyright (C) 2006 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
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
package net.sourceforge.sqlexplorer.connections;

import java.util.HashMap;
import java.util.LinkedHashSet;

import net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction;
import net.sourceforge.sqlexplorer.connections.actions.AutoCommitAction;
import net.sourceforge.sqlexplorer.connections.actions.ChangeAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.CloseAllConnectionsAction;
import net.sourceforge.sqlexplorer.connections.actions.CloseConnectionAction;
import net.sourceforge.sqlexplorer.connections.actions.CommitAction;
import net.sourceforge.sqlexplorer.connections.actions.CommitOnCloseAction;
import net.sourceforge.sqlexplorer.connections.actions.ConnectAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.ConnectNewUserAction;
import net.sourceforge.sqlexplorer.connections.actions.CopyAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.DeleteAction;
import net.sourceforge.sqlexplorer.connections.actions.NewAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.NewEditorAction;
import net.sourceforge.sqlexplorer.connections.actions.RollbackAction;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionGroup;

/**
 * ActionGroup for Database Structure View. This group controls what context
 * menu actions are being shown for which node.
 * 
 * @author Davy Vanherbergen
 */
public class ConnectionTreeActionGroup extends ActionGroup {
	
	private enum Type {
		GENERIC, IF_SELECTION, ALIAS, USER, SESSION
	}
	
	private HashMap<Type, LinkedHashSet<AbstractConnectionTreeAction>> actions = new HashMap<Type, LinkedHashSet<AbstractConnectionTreeAction>>();

    /**
     * Construct a new action group for a given database structure outline.
     * 
     * @param treeViewer TreeViewer used for this outline.
     */
    public ConnectionTreeActionGroup() {
    	LinkedHashSet<AbstractConnectionTreeAction> set;
    	
    	set = new LinkedHashSet<AbstractConnectionTreeAction>();
    	actions.put(Type.GENERIC, set);
    	set.add(new NewAliasAction());

    	set = new LinkedHashSet<AbstractConnectionTreeAction>();
    	actions.put(Type.IF_SELECTION, set);
        set.add(new NewEditorAction());

    	set = new LinkedHashSet<AbstractConnectionTreeAction>();
    	actions.put(Type.SESSION, set);
        set.add(new CloseConnectionAction());
        set.add(new CloseAllConnectionsAction());       
        set.add(new CommitAction());
        set.add(new RollbackAction());

    	set = new LinkedHashSet<AbstractConnectionTreeAction>();
    	actions.put(Type.ALIAS, set);
        set.add(new ChangeAliasAction());
        set.add(new CopyAliasAction());
        set.add(new DeleteAction());
        set.add(new ConnectAliasAction());
        set.add(new ConnectNewUserAction());

    	set = new LinkedHashSet<AbstractConnectionTreeAction>();
    	actions.put(Type.USER, set);
        set.add(new AutoCommitAction());
        set.add(new CommitOnCloseAction());
    }


    /**
     * Fill the node context menu with all the correct actions.
     * 
     * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void fillContextMenu(IMenuManager menu) {

    	ConnectionsView view = SQLExplorerPlugin.getDefault().getConnectionsView();
    	if (view == null) {
            addActions(menu, Type.GENERIC);
            return;
        }
    	
    	// find our target node..
    	Object[] selection = view.getSelected();

        // check if we have a valid selection
        if (selection == null || selection.length != 1) {
            addActions(menu, Type.GENERIC);
            return;
        }

        if (addActions(menu, Type.IF_SELECTION) > 0)
            menu.add(new Separator());
        
        if (selection[0] instanceof Alias) {
            if (addActions(menu, Type.ALIAS) > 0)
                menu.add(new Separator());
            if (addActions(menu, Type.SESSION) > 0)
                menu.add(new Separator());
        } else {
            if (addActions(menu, Type.SESSION) > 0)
                menu.add(new Separator());
            if (addActions(menu, Type.ALIAS) > 0)
                menu.add(new Separator());
        }
        if (selection[0] instanceof User) {
            if (addActions(menu, Type.USER) > 0)
                menu.add(new Separator());
        }
        
        addActions(menu, Type.GENERIC);
    }
    
    /**
     * Adds all actions of a given type to the menu, and returns a count of the
     * number of items added to the menu
     * @param menu
     * @param type
     * @return
     */
    private int addActions(IMenuManager menu, Type type) {
    	LinkedHashSet<AbstractConnectionTreeAction> set = actions.get(type);
    	int numAdded = 0;
    	for (AbstractConnectionTreeAction action : set) 
    		if (action.isAvailable()){
				menu.add(action);
				numAdded++;
			}
    	return numAdded;
    }
}
