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

import java.util.Iterator;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.views.ConnectionsView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @author Davy Vanherbergen
 * 
 */
public class RollbackAction extends AbstractConnectionTreeAction implements IViewActionDelegate {

    private ImageDescriptor _image = ImageUtil.getDescriptor("Images.RollbackIcon");


    public void init(IViewPart view) {
        _treeViewer = ((ConnectionsView) view).getTreeViewer();
    }


    public String getText() {
        return Messages.getString("ConnectionsView.Actions.Rollback");
    }


    public void run(IAction action) {
        run();
    }


    public void run() {
        StructuredSelection sel = (StructuredSelection) _treeViewer.getSelection();

        Iterator it = sel.iterator();
        while (it.hasNext()) {

            Object o = it.next();

            if (o instanceof SessionTreeNode) {
                SessionTreeNode node = (SessionTreeNode) o;
                if (!node.isAutoCommitMode()) {
                    node.rollback();
                }
            }
        }
    }


    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(isAvailable());
    }


    public ImageDescriptor getImageDescriptor() {
        return _image;
    }


    public ImageDescriptor getHoverImageDescriptor() {
        return _image;
    }


    /**
     * Action is available when there is at least one session without autocommit
     * selected
     * 
     * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
     */
    public boolean isAvailable() {

        StructuredSelection sel = (StructuredSelection) _treeViewer.getSelection();

        Iterator it = sel.iterator();
        while (it.hasNext()) {

            Object o = it.next();

            if (o instanceof SessionTreeNode) {
                SessionTreeNode node = (SessionTreeNode) o;
                if (!node.isAutoCommitMode()) {
                    return true;
                }
            }

        }

        return false;
    }
}
