/*
 * Copyright (C) 2007 Patrac Vlad Sebastian
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

/**
 * Reprezents a procedure folder (top-level or of a package) in Database Tree View 
 * 
 * @author Patras Vlad
 */

package net.sourceforge.sqlexplorer.oracle.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;


public class ProcedureFolder extends AbstractFolderNode {
	
	public ProcedureFolder() {
	}	
		
	public ProcedureFolder(INode parent, SessionTreeNode sessionNode) {
		_type = "FOLDER";
		initialize(parent, null, sessionNode);
	}
   
    public String getName() {
        return Messages.getString("oracle.dbstructure.procedures");
    }

	public void loadChildren() {
		
        SQLConnection connection = getSession().getInteractiveConnection();
        ResultSet rs = null;
        PreparedStatement pStmt = null;
        
        try {
        	
            // use prepared statement
        	if (getParent().getType().equalsIgnoreCase("package")) {
        		
        		pStmt = connection.prepareStatement(
        				"select procedure_name," +
        				"  CASE WHEN count(*) over(partition by PROCEDURE_NAME) = 1 THEN 0 " +
        				"   ELSE row_number() over(partition by PROCEDURE_NAME order by PROCEDURE_NAME) END" +
        				" from sys.all_procedures where owner = ? AND object_name = ?");
        		pStmt.setString(2, _parent.getName());
        	} else {
        		pStmt = connection.prepareStatement(
        				"select object_name," +
        				"  CASE WHEN count(*) over(partition by object_name) = 1 THEN 0 " +
        				"   ELSE row_number() over(partition by object_name order by object_name) END" +
        				" from sys.all_objects where owner = ? AND object_type = 'PROCEDURE'");
        	}
        	pStmt.setString(1, getSchemaOrCatalogName());
            
            rs = pStmt.executeQuery();

            while (rs.next()) {
            	
            	if (isExcludedByFilter(rs.getString(1))) {
            		continue;
            	}
            	
            	ProcedureNode newNode = new ProcedureNode();
            	newNode.initialize(this, rs.getString(1), _sessionNode);
            	newNode.setOverload(rs.getInt(2));
            	
            	if (getParent().getType().equalsIgnoreCase("package")) {
            		
            		newNode.setPackage(getParent().getName());
            	}
                addChildNode(newNode);
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
