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

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;


public class PackageFolder extends AbstractFolderNode {
	
	public PackageFolder() {
		_type = "FOLDER"; 
	}
	
    public String getName() {
        return Messages.getString("oracle.dbstructure.packages");
    }

	public void loadChildren() {
		
        SQLConnection connection = getSession().getInteractiveConnection();
        ResultSet rs = null;
        PreparedStatement pStmt = null;

        try {

            // use prepared statement
            pStmt = connection.prepareStatement("select object_name from sys.all_objects where owner = ? and object_type = 'PACKAGE'");
            pStmt.setString(1, _parent.getQualifiedName());

            rs = pStmt.executeQuery();

            while (rs.next()) {
            	if (!isExcludedByFilter(rs.getString(1))) {
            		addChildNode(new PackageNode(this, rs.getString(1), _sessionNode, getParent().getName()));
            	}
            }

            rs.close();

        } catch (Exception e) {

            SQLExplorerPlugin.error("Couldn't load children for: " + getName(), e);

        } finally {

            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (Exception e) {
                    SQLExplorerPlugin.error("Error closing statement", e);
                }
            }
        }
	}
}
