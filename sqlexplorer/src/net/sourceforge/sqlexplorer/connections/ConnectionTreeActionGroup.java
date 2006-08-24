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

import net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction;
import net.sourceforge.sqlexplorer.connections.actions.ChangeAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.CloseAllConnectionsAction;
import net.sourceforge.sqlexplorer.connections.actions.CloseConnectionAction;
import net.sourceforge.sqlexplorer.connections.actions.CommitAction;
import net.sourceforge.sqlexplorer.connections.actions.ConnectAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.CopyAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.DeleteAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.NewAliasAction;
import net.sourceforge.sqlexplorer.connections.actions.NewEditorAction;
import net.sourceforge.sqlexplorer.connections.actions.RollbackAction;
import net.sourceforge.sqlexplorer.plugin.views.ConnectionsView;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;

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

    private AbstractConnectionTreeAction _closeAllConnectionsAction;
    
    private AbstractConnectionTreeAction _closeConnectionAction;
    
    private AbstractConnectionTreeAction _commitAction;
    
    private AbstractConnectionTreeAction _rollBackAction;
    
    private TreeViewer _treeViewer;


    /**
     * Construct a new action group for a given database structure outline.
     * 
     * @param treeViewer TreeViewer used for this outline.
     */
    public ConnectionTreeActionGroup(ConnectionsView view, TreeViewer treeViewer) {

        _treeViewer = treeViewer;

        _newAliasAction = new NewAliasAction();
        _changeAliasAction = new ChangeAliasAction();
        _copyAliasAction = new CopyAliasAction();
        _deleteAliasAction = new DeleteAliasAction();
        _connectAliasAction = new ConnectAliasAction();
        _newEditorAction = new NewEditorAction();
        
        _closeAllConnectionsAction = new CloseAllConnectionsAction();       
        _closeConnectionAction = new CloseConnectionAction();
        _commitAction= new CommitAction();
        _rollBackAction = new RollbackAction();
        
        _newAliasAction.setTreeViewer(_treeViewer);
        _changeAliasAction.setTreeViewer(_treeViewer);
        _copyAliasAction.setTreeViewer(_treeViewer);
        _deleteAliasAction.setTreeViewer(_treeViewer);
        _connectAliasAction.setTreeViewer(_treeViewer);
        _newEditorAction.setTreeViewer(_treeViewer);

        _closeAllConnectionsAction.setTreeViewer(_treeViewer);      
        _closeConnectionAction.setTreeViewer(_treeViewer);
        _commitAction.setTreeViewer(_treeViewer);
        _rollBackAction.setTreeViewer(_treeViewer);
        
        _newAliasAction.setView(view);
        _changeAliasAction.setView(view);
        _copyAliasAction.setView(view);
        _deleteAliasAction.setView(view);
        _connectAliasAction.setView(view);
        _newEditorAction.setView(view);

        _closeAllConnectionsAction.setView(view); 
        _closeConnectionAction.setView(view);
        _commitAction.setView(view);
        _rollBackAction.setView(view);
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


        if (_newEditorAction.isAvailable()) {
            menu.add(_newEditorAction);
            menu.add(new Separator());
        }
        
        if (selection.getFirstElement() instanceof ISQLAlias) {

            addAliasActions(menu);
            menu.add(new Separator());
            addSessionActions(menu);
            menu.add(new Separator());
            
        } else {

            addSessionActions(menu);
            menu.add(new Separator());
            addAliasActions(menu);
            menu.add(new Separator());            
        }
        
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

    }


    /**
     * Add actions to the context menu that apply when a session node is
     * selected.
     * 
     * @param menu
     */
    private void addSessionActions(IMenuManager menu) {

        if (_closeConnectionAction.isAvailable()) {
            menu.add(_closeConnectionAction);
        }

        if (_closeAllConnectionsAction.isAvailable()) {
            menu.add(_closeAllConnectionsAction);
        }

        if (_commitAction.isAvailable()) {
            menu.add(_commitAction);
        }

        if (_rollBackAction.isAvailable()) {
            menu.add(_rollBackAction);
        }
        
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
