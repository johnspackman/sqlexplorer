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
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

/**
 * Deletes a selected item; as of 3.5.0.beta2, this is a generic "delete", not just specific to
 * Aliases, hence the change in name
 * @author Davy Vanherbergen
 */
public class DeleteAction extends AbstractConnectionTreeAction {

    ImageDescriptor _image = ImageUtil.getDescriptor("Images.Delete");

    public String getToolTipText() {
        return Messages.getString("ConnectionsView.Actions.DeleteToolTip");
    }

    public String getText() {
        return Messages.getString("ConnectionsView.Actions.Delete");
    }
    
    public ImageDescriptor getHoverImageDescriptor() {
        return _image;
    }

    public ImageDescriptor getImageDescriptor() {
        return _image;
    };

    public void run() {

        boolean okToDelete = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
                Messages.getString("ConnectionsView.ConfirmDelete.WindowTitle"),
                Messages.getString("ConnectionsView.ConfirmDelete.Message"));

        if (!okToDelete)
            return;

        for (User user : getView().getSelectedUsers(false))
        	user.getAlias().removeUser(user);
        for (Alias alias : getView().getSelectedAliases(false))
           	alias.remove();
        getView().refresh();
    }
    
    
    /**
     * Only show action when there is at least 1 alias selected
     * 
     * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
     */
    public boolean isAvailable() {
    	if (getView() == null)
    		return false;
    	return !getView().getSelectedAliases(false).isEmpty() || !getView().getSelectedUsers(false).isEmpty();
    }
}
