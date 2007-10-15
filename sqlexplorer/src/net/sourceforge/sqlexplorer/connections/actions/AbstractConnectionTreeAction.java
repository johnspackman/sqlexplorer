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

import net.sourceforge.sqlexplorer.connections.ConnectionsView;
import net.sourceforge.sqlexplorer.dbproduct.AliasManager;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;

/**
 * Abstract implementation for a context menu action in the connection view.
 * Extend this class to add actions.
 * 
 * @author Davy Vanherbergen
 */
public abstract class AbstractConnectionTreeAction extends Action {

    public AbstractConnectionTreeAction() {
		super();
	}

	public AbstractConnectionTreeAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public AbstractConnectionTreeAction(String text, int style) {
		super(text, style);
	}

	public AbstractConnectionTreeAction(String text) {
		super(text);
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
    
    public void init(IViewPart view) {
    }
    
    public AliasManager getAliases() {
    	return SQLExplorerPlugin.getDefault().getAliasManager();
    }

	protected ConnectionsView getView() {
		return SQLExplorerPlugin.getDefault().getConnectionsView();
	}
}
