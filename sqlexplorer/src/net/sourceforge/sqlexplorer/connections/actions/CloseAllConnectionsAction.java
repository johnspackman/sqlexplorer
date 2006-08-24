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

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.ConnectionsView;
import net.sourceforge.sqlexplorer.sessiontree.model.RootSessionTreeNode;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @author Davy Vanherbergen
 * 
 */
public class CloseAllConnectionsAction extends AbstractConnectionTreeAction implements IViewActionDelegate {

    private ImageDescriptor _image = ImageUtil.getDescriptor("Images.CloseAllConnsIcon");

    private ImageDescriptor _disabledImage = ImageUtil.getDescriptor("Images.DisabledCloseAllConnsIcon");


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view) {
        _treeViewer = ((ConnectionsView) view).getTreeViewer();
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        run();
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(isAvailable());
    }


    public String getText() {
        return Messages.getString("ConnectionsView.Actions.CloseAllConnections");
    }


    public String getToolTipText() {
        return Messages.getString("ConnectionsView.Actions.CloseAllConnectionsToolTip");
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {

        // locate open sessions
        RootSessionTreeNode sessionRoot = SQLExplorerPlugin.getDefault().stm.getRoot();
        sessionRoot.closeAllConnections();        
        setEnabled(false);
        _treeViewer.refresh();
    }


    public ImageDescriptor getImageDescriptor() {
        return _image;
    }


    public ImageDescriptor getDisabledImageDescriptor() {
        return _disabledImage;
    }


    /**
     * Action is available when there are open sessions
     */
    public boolean isAvailable() {

        RootSessionTreeNode sessionRoot = SQLExplorerPlugin.getDefault().stm.getRoot();
        Object[] sessions = sessionRoot.getChildren();
        if (sessions != null && sessions.length != 0) {
            return true;
        }

        return false;
    }

}
