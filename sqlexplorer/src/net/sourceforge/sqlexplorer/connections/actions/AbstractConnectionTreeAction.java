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
package net.sourceforge.sqlexplorer.connections.actions;

import net.sourceforge.sqlexplorer.plugin.views.ConnectionsView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Abstract implementation for a context menu action in the connection view.
 * Extend this class to add actions.
 * 
 * @author Davy Vanherbergen
 */
public abstract class AbstractConnectionTreeAction extends Action {

    protected TreeViewer _treeViewer;

    protected ConnectionsView _view;

    /**
     * Store treeViewer for use in the actions
     * 
     * @param treeViewer
     */
    public void setTreeViewer(TreeViewer treeViewer) {
        _treeViewer = treeViewer;
    }


    /**
     * Store view for use in the actions
     */ 
    public void setView(ConnectionsView view) {    
        _view = view;
    }


    /**
     * Implement this method to return true when your action is available for
     * the selected node(s). When true, the action will be included in the
     * context menu, when false it will be ignored.
     * 
     * 
     * @return true if the action should be included in the context menu
     */
    public boolean isAvailable() {
        return true;
    }
}
