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
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Davy Vanherbergen
 * 
 */
public class NewEditorAction extends AbstractConnectionTreeAction implements IViewActionDelegate {

    ImageDescriptor _image = ImageUtil.getDescriptor("Images.OpenSQLIcon");

    ImageDescriptor _disabledImage = ImageUtil.getDescriptor("Images.AliasIcon");

    public ImageDescriptor getHoverImageDescriptor() {
        return _image;
    }

    public ImageDescriptor getImageDescriptor() {
        return _image;
    };

    public ImageDescriptor getDisabledImageDescriptor() {
        return _disabledImage;
    }

    public void run(IAction action) {
        run();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(isAvailable());
    }

    public String getText() {
        return Messages.getString("ConnectionsView.Actions.NewEditor");
    }

    public String getToolTipText() {
        return Messages.getString("ConnectionsView.Actions.NewEditorToolTip");
    }

    public void run() {
    	Set<User> users = getView().getSelectedUsers(true);
    	for (User user : users)
            try {
                SQLEditorInput input = new SQLEditorInput("SQL Editor (" + SQLExplorerPlugin.getDefault().getEditorSerialNo() + ").sql");
                input.setSessionNode(user.createSession());
                IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
                page.openEditor(input, SQLEditor.class.getName());
            } catch (Throwable e) {
                SQLExplorerPlugin.error("Error creating sql editor", e);
            }
    
        getView().refresh();
    }


    /**
     * Only show action when there is at least 1 item selected
     * 
     * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
     */
    public boolean isAvailable() {
    	if (getView() == null)
    		return false;
    	return getView().getSelectedUsers(true) != null;
    }
}
