/*
 * Copyright (C) 2006 Davy Vanherbergen
 * dvanherbergen@users.sourceforge.net
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
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.plugin.actions.OpenPasswordConnectDialogAction;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import org.eclipse.jface.resource.ImageDescriptor;


/**
 * @author Davy Vanherbergen
 *
 */
public class ConnectNewUserAction extends AbstractConnectionTreeAction {

    private ImageDescriptor _image = ImageUtil.getDescriptor("Images.ConnectSessionIcon");

    public ImageDescriptor getHoverImageDescriptor() {
        return _image;
    }

    public ImageDescriptor getImageDescriptor() {
        return _image;
    };
    
    public String getText() {
        return Messages.getString("ConnectionsView.Actions.ConnectNewUser");
    }
    
    public String getToolTipText() {
        return Messages.getString("ConnectionsView.Actions.ConnectNewUserToolTip");
    }

    public void run() {
    	Set<Alias> aliases = getView().getSelectedAliases(true);
    	for (Alias alias : aliases) {
            OpenPasswordConnectDialogAction openDlgAction = new OpenPasswordConnectDialogAction(alias, alias.getDefaultUser(), true);
            openDlgAction.run();
        }
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
    	return getView().getSelectedAliases(false) != null;
    }
}
