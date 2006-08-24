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

import net.sourceforge.sqlexplorer.AliasModel;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SQLAlias;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;


/**
 * @author Davy Vanherbergen
 *
 */
public class DeleteAliasAction extends AbstractConnectionTreeAction {

    ImageDescriptor _image = ImageUtil.getDescriptor("Images.DeleteAlias");

    public String getToolTipText() {
        return Messages.getString("ConnectionsView.Actions.DeleteAliasToolTip");
    }

    public String getText() {
        return Messages.getString("ConnectionsView.Actions.DeleteAlias");
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

        if (!okToDelete) {
            return;
        }
        
        StructuredSelection sel = (StructuredSelection) _treeViewer.getSelection();
        AliasModel aliasModel = SQLExplorerPlugin.getDefault().getAliasModel();
        
        Iterator it = sel.iterator();
        while (it.hasNext()) {
            
            Object o = it.next();            
            if (o instanceof SQLAlias) {
                aliasModel.removeAlias((SQLAlias) o);
            }
        }
        _treeViewer.refresh();
        
    }
    
    
    /**
     * Only show action when there is at least 1 alias selected
     * 
     * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
     */
    public boolean isAvailable() {

        StructuredSelection sel = (StructuredSelection) _treeViewer.getSelection();

        if (sel.size() == 0) {
            return false;
        }
        
        Iterator it = sel.iterator();
        
        while (it.hasNext()) {
            
            Object o = it.next();
            if (o instanceof ISQLAlias) {
                return true;
            }
            
        }        

        return false;
    }
}
