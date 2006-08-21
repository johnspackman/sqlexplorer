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
package net.sourceforge.sqlexplorer.oracle.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;


public class PackageFolder extends AbstractSQLFolderNode {

    public String getChildType() {
        return "PACKAGE";
    }
   
    public String getName() {
        return Messages.getString("oracle.dbstructure.packages");
    }
    
    public String getSQL() {
        return "select object_name from sys.all_objects where owner = ? and object_type = 'PACKAGE'";
    }
    
    public Object[] getSQLParameters() {
        return new Object[] {getParent().getQualifiedName()};
    }
}
