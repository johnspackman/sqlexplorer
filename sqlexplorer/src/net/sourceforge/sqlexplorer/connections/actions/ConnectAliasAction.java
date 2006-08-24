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

import java.util.Iterator;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SQLAlias;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.actions.OpenPasswordConnectDialogAction;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;


/**
 * @author Davy Vanherbergen
 *
 */
public class ConnectAliasAction extends AbstractConnectionTreeAction {

    ImageDescriptor _image = ImageUtil.getDescriptor("Images.ConnectSessionIcon");

    public ImageDescriptor getHoverImageDescriptor() {
        return _image;
    }

    public ImageDescriptor getImageDescriptor() {
        return _image;
    };
    
    public String getText() {
        return Messages.getString("ConnectionsView.Actions.ConnectAlias");
    }
    
    public String getToolTipText() {
        return Messages.getString("ConnectionsView.Actions.ConnectAliasToolTip");
    }

    public void run() {
        
        StructuredSelection sel = (StructuredSelection) _treeViewer.getSelection();
        
        Iterator it = sel.iterator();
        while (it.hasNext()) {
            
            Object o = it.next();            
            if (o instanceof SQLAlias) {
                
                ISQLAlias al = (ISQLAlias) o;                
                OpenPasswordConnectDialogAction openDlgAction = new OpenPasswordConnectDialogAction(_view.getSite(),
                        al, SQLExplorerPlugin.getDefault().getDriverModel(), SQLExplorerPlugin.getDefault().getPreferenceStore(), SQLExplorerPlugin.getDefault().getSQLDriverManager());
                openDlgAction.run();
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
