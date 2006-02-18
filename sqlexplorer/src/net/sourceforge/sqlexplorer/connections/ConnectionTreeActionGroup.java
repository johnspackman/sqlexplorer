/*
 * Copyright (C) 2006 Davy Vanherbergen
 * dvanherbergen@users.sourceforge.net
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

import net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction;
import net.sourceforge.sqlexplorer.connections.actions.ChangeAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.ConnectAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.CopyAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.DeleteAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.NewAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.NewEditorAction;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.ActionGroup;

/**
 * ActionGroup for Database Structure View. This group controls what context
 * menu actions are being shown for which node.
 * 
 * @author Davy Vanherbergen
 */
public class ConnectionTreeActionGroup extends ActionGroup {

    private AbstractConnectionTreeAction _changeAliasAction;

    private AbstractConnectionTreeAction _copyAliasAction;

    private AbstractConnectionTreeAction _deleteAliasAction;

    private AbstractConnectionTreeAction _connectAliasAction;

    private AbstractConnectionTreeAction _newAliasAction;
    
    private AbstractConnectionTreeAction _newEditorAction;

    private TreeViewer _treeViewer;


    /**
     * Construct a new action group for a given database structure outline.
     * 
     * @param treeViewer TreeViewer used for this outline.
     */
    public ConnectionTreeActionGroup(TreeViewer treeViewer) {

        _treeViewer = treeViewer;

        _newAliasAction = new NewAliasAction();
        _changeAliasAction = new ChangeAliasAction();
        _copyAliasAction = new CopyAliasAction();
        _deleteAliasAction = new DeleteAliasAction();
        _connectAliasAction = new ConnectAliasAction();
        _newEditorAction = new NewEditorAction();
        
        _newAliasAction.setTreeViewer(_treeViewer);
        _changeAliasAction.setTreeViewer(_treeViewer);
        _copyAliasAction.setTreeViewer(_treeViewer);
        _deleteAliasAction.setTreeViewer(_treeViewer);
        _connectAliasAction.setTreeViewer(_treeViewer);
        _newEditorAction.setTreeViewer(_treeViewer);

    }


    /**
     * Fill the node context menu with all the correct actions.
     * 
     * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void fillContextMenu(IMenuManager menu) {

        // find our target node..
        IStructuredSelection selection = (IStructuredSelection) _treeViewer.getSelection();

        // check if we have a valid selection
        if (selection == null) {
            addGenericActions(menu);
            return;
        }

        addSessionActions(menu);
        menu.add(new Separator());
        addAliasActions(menu);
        menu.add(new Separator());
        addGenericActions(menu);

    }


    /**
     * Add generic actions to the context menu that do not require a node in the
     * tree to be selected.
     * 
     * @param menu
     */
    private void addGenericActions(IMenuManager menu) {

        menu.add(_newAliasAction);

        if (_newEditorAction.isAvailable()) {
            menu.add(_newEditorAction);
        }
    }


    /**
     * Add actions to the context menu that apply when a session node is
     * selected.
     * 
     * @param menu
     */
    private void addSessionActions(IMenuManager menu) {


        
    }


    /**
     * Add actions to the context menu that apply when an alias node is
     * selected.
     * 
     * @param menu
     */
    private void addAliasActions(IMenuManager menu) {

        if (_connectAliasAction.isAvailable()) {
            menu.add(_connectAliasAction);
        }

        if (_changeAliasAction.isAvailable()) {
            menu.add(_changeAliasAction);
        }

        if (_copyAliasAction.isAvailable()) {
            menu.add(_copyAliasAction);
        }

        if (_deleteAliasAction.isAvailable()) {
            menu.add(_deleteAliasAction);
        }

    }

}
