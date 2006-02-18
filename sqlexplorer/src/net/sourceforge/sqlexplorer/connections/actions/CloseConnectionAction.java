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
package net.sourceforge.sqlexplorer.connections.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @author Mazzolini
 * 
 */
public class CloseConnectionAction extends Action implements IViewActionDelegate {

    private ImageDescriptor _image = ImageDescriptor.createFromURL(SqlexplorerImages.getCloseConnIcon());
    private ImageDescriptor _disabledImage = ImageDescriptor.createFromURL(SqlexplorerImages.getDisabledCloseConnIcon());

    /**
     * @param node
     */
    public CloseConnectionAction(SessionTreeNode node) {

        _stn = node;
    }

    public CloseConnectionAction() {

    }

    SessionTreeNode _stn;

    public void init(IViewPart view) {

    }

    public void run(IAction action) {
        run();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection iss = (IStructuredSelection) selection;
            Object obj = iss.getFirstElement();
            if (obj instanceof SessionTreeNode) {
                _stn = (SessionTreeNode) obj;
                action.setEnabled(true);
            } else {
                action.setEnabled(false);
                _stn = null;
            }
        }
    }

    public void run() {
        if (_stn != null)
            _stn.close();
    }

    public String getText() {
        return Messages.getString("ConnectionsView.Actions.CloseConnection");
    }
    
    public String getToolTipText() {
        return Messages.getString("ConnectionsView.Actions.CloseConnectionToolTip");
    }

    public ImageDescriptor getImageDescriptor() {
        return _image;
    }

    public ImageDescriptor getDisabledImageDescriptor() {        
        return _disabledImage;
    }

}
