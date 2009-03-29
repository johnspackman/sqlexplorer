/*
 * Copyright (C) 2007 Patrac Vlad Sebastian
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

/**
 * Classes that want to extend Explain plan
 * as context menu actions must implement this class
 * 
 * @author Patras Vlad
 */

package net.sourceforge.sqlexplorer.oracle.actions.explain;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;

public abstract class AbstractExplainPlanContextAction extends Action {
	
    protected TreeViewer _tableView;
    
    protected ExplainNode _node;

    /**
     * Store table for use in the actions.
     * @param table
     */
    public final void setTableView(TreeViewer tableView) {
        _tableView = tableView;        
    }

    
    /**
     * Implement this method to return true when your action is available
     * for the active table.  When true, the action will be included in the
     * context menu, when false it will be ignored.
     * 
     * @return true if the action should be included in the context menu
     */
    public boolean isAvailable() {
        return true;
    }
	
	public void setNode(ExplainNode node) {
		_node = node;;
	}

}
