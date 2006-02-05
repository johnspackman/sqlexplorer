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

package net.sourceforge.sqlexplorer.dbstructure.actions;

import java.util.ArrayList;
import java.util.Iterator;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SQLAlias;
import net.sourceforge.sqlexplorer.SqlexplorerImages;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Refresh this node from the alias metadata filter.
 * 
 * @author Davy Vanherbergen
 * 
 */
public class AddNodeToFilterAction extends AbstractDBTreeContextAction {

    private static final ImageDescriptor _image = ImageDescriptor.createFromURL(SqlexplorerImages.getFilterIcon());


    /**
     * Custom image for action
     * 
     * @see org.eclipse.jface.action.IAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return _image;
    }


    /**
     * Set the text for the menu entry.
     * 
     * @see org.eclipse.jface.action.IAction#getText()
     */
    public String getText() {
        return Messages.getString("DatabaseStructureView.Actions.AddNodeToFilter");
    }


    /**
     * Add selected node to metadatafilter and refresh view
     * 
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {

        SQLAlias alias = (SQLAlias) _selectedNodes[0].getSession().getAlias();
        
        String filter = alias.getMetaFilterExpression();
        String filterElements[] = null;
        
        if (filter == null || filter.trim().length() == 0) {
            filterElements = new String[0];
        } else {
            filterElements = filter.split(",");
        }
        
        ArrayList newElements = new ArrayList();
        
        for (int i = 0; i < _selectedNodes.length; i ++) {
            
            String nodeName = _selectedNodes[i].toString();
            boolean alreadyExists = false;
            
            for (int j = 0; j < filterElements.length; j++) {
                if (filterElements[j].trim().equalsIgnoreCase(nodeName)) {
                    alreadyExists = true;
                    break;
                }
            }
            
            if (!alreadyExists) {
                newElements.add(nodeName);
            }
        }
        
        
        String sep = "";
        
        if (filter != null && filter.trim().length() != 0) {
            sep = ",";
        }

        Iterator it = newElements.iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            filter += sep + name;
            sep = ",";
        }
        
        
        alias.setMetaFilterExpression(filter);
        _selectedNodes[0].getSession().getRoot().refresh();
        _treeViewer.refresh();

    }


    /**
     * Action is availble when a node is selected
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction#isAvailable()
     */
    public boolean isAvailable() {

        // at least one node needs to be selected
        if (_selectedNodes.length == 0) {
            return false;
        }
        
        // and they should all be either catalog or schema nodes..
        for (int i = 0; i < _selectedNodes.length; i ++) {
            if (!_selectedNodes[i].getType().equalsIgnoreCase("catalog") && !_selectedNodes[i].getType().equalsIgnoreCase("schema")) {
                return false;
            }
        }
        
        return true;
    }
}
