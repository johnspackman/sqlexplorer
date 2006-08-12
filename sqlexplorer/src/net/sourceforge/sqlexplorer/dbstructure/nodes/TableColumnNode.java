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
package net.sourceforge.sqlexplorer.dbstructure.nodes;

import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;


/**
 * @author Davy Vanherbergen
 *
 */
public class TableColumnNode extends AbstractNode {

    /* (non-Javadoc)
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {
        // noop
    }

    public TableColumnNode(TableNode node, String name, SessionTreeNode session) throws Exception {
        
        _parent = node;
        _sessionNode = session;               
        _name = name;        
        
        _imageKey = "Images.ColumnNodeIcon";
    }

    /* (non-Javadoc)
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getType()
     */
    public String getType() {        
        return "column";
    }

   
    
    /* (non-Javadoc)
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getUniqueIdentifier() {
        return _parent.getQualifiedName() + "." + _name;
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#isEndNode()
     */
    public boolean isEndNode() {       
        return true;
    }
}
