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
 * Handles extension points for Explain plan
 * Searches valid extensions, fills context menu, initiates actions with data. 
 * 
 * @author Patras Vlad
 */

package net.sourceforge.sqlexplorer.oracle.actions.explain;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.ActionGroup;

public class ExplainPlanActionGroup extends ActionGroup {
	
    private TreeViewer _tableView;

    private ExplainNode _node;
    
    private AbstractExplainPlanContextAction _expandAction;
    private AbstractExplainPlanContextAction _collapseAction;
    
    private static final ImageDescriptor _expandImage   = ImageUtil.getFragmentDescriptor("net.sourceforge.sqlexplorer.oracle", Messages.getString("oracle.images.explainexpand"));
    private static final ImageDescriptor _collapseImage = ImageUtil.getFragmentDescriptor("net.sourceforge.sqlexplorer.oracle", Messages.getString("oracle.images.explaincollapse"));

    
    /**
     * Construct a new action group for a given Table
     * 
     * @param table Table that displays the context menu
     * @param cursor TableCursor that displays the context menu
     */
    public ExplainPlanActionGroup(TreeViewer tableView, ExplainNode node) {
        _tableView = tableView;
        _node = node;
    }


    /**
     * Fill the context menu with all the correct actions.
     * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void fillContextMenu(IMenuManager menu) {
        
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("net.sourceforge.sqlexplorer", "explainPlanContextAction");
        IExtension[] extensions = point.getExtensions();

        // add basic actions
        
        for (int i = 0; i < extensions.length; i++) {

            IExtension e = extensions[i];

            IConfigurationElement[] ces = e.getConfigurationElements();

            for (int j = 0; j < ces.length; j++) {
                try {
                    
                    String group = ces[j].getAttribute("group");
                    if (group == null) {
                    
                        // check if the action thinks it is suitable..
                        AbstractExplainPlanContextAction action = (AbstractExplainPlanContextAction) ces[j].createExecutableExtension("class");
                        action.setTableView(_tableView);
                        action.setNode(_node);
                        
                        if (action.isAvailable()) {
                            menu.add(action);
                        }
                    }
                        
                } catch (Throwable ex) {
                    SQLExplorerPlugin.error("Could not create menu action", ex);
                }
            }
        }
        
        menu.add(new Separator());
        
        _expandAction = new AbstractExplainPlanContextAction() {
        	
        	public String getText() {	
        		return Messages.getString("oracle.explainplan.expand");
        	}
        	public ImageDescriptor getImageDescriptor() {
        		return _expandImage;
        	}
        	public void run() {
        		_tableView.expandAll();
        	}
        };
        _expandAction.setTableView(_tableView);
        
        _collapseAction = new AbstractExplainPlanContextAction() {
        	
        	public String getText() {	
        		return Messages.getString("oracle.explainplan.collapse");
        	}
        	public ImageDescriptor getImageDescriptor() {
        		return _collapseImage;
        	}
        	public void run() {
        		_tableView.collapseAll();
        	}
        };
        _collapseAction.setTableView(_tableView);        
        
        menu.add(_expandAction);
        menu.add(_collapseAction);
        
    }
}
