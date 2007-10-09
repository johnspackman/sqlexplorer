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

import java.util.Set;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;

/**
 * @author Davy Vanherbergen
 * 
 */
public class CloseConnectionAction extends AbstractConnectionTreeAction implements IViewActionDelegate {

    private ImageDescriptor _image = ImageUtil.getDescriptor("Images.CloseConnIcon");

    private ImageDescriptor _disabledImage = ImageUtil.getDescriptor("Images.DisabledCloseConnIcon");

    public void run(IAction action) {
        run();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(isAvailable());
    }

    public void run() {
    	for (SQLConnection connection : getView().getSelectedConnections(false))
   			connection.getUser().releaseFromPool(connection);
        getView().refresh();
    }

    /**
     * Action is available when there is at least one session selected.
     * 
     * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
     */
    public boolean isAvailable() {
    	if (getView() == null)
    		return false;
    	Set<SQLConnection> connections = getView().getSelectedConnections(false);
    	for (SQLConnection connection : connections)
    		if (connection.getUser().isInPool(connection))
    			return true;
    	return false;
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
