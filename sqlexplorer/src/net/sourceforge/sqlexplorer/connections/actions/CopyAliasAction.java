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
import net.sourceforge.sqlexplorer.dialogs.CreateAliasDlg;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;

/**
 * @author Davy Vanherbergen
 * 
 */
public class CopyAliasAction extends AbstractConnectionTreeAction {

    ImageDescriptor _image = ImageUtil.getDescriptor("Images.CopyAlias");

    public String getToolTipText() {
        return Messages.getString("ConnectionsView.Actions.CopyAliasToolTip");
    }

    public String getText() {
        return Messages.getString("ConnectionsView.Actions.CopyAlias");
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return _image;
    }

    public ImageDescriptor getImageDescriptor() {
        return _image;
    };

    public void run() {
    	Alias alias = getView().getSelectedAlias(false);
    	if (alias == null)
    		return;
    	
    	CreateAliasDlg dlg = new CreateAliasDlg(Display.getCurrent().getActiveShell(), CreateAliasDlg.Type.COPY, new Alias(alias));
    	dlg.open();
    	getView().refresh();
    }

    /**
     * Only show action when there is 1 alias selected
     * 
     * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
     */
    public boolean isAvailable() {
    	if (getView() == null)
    		return false;
        return getView().getSelectedAlias(false) != null;
    }
}
