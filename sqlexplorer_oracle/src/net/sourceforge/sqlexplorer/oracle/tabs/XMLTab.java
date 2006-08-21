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
package net.sourceforge.sqlexplorer.oracle.tabs;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSourceTab;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;


public class XMLTab extends AbstractSourceTab {

    public Object[] getSQLParameters() {
        return new Object[] {getNode().getSchemaOrCatalogName(), getNode().getName()};
    }

    public String getSource() {
        
        String source = "";
        String objectType = getNode().getType().toUpperCase();
        String sql = "SELECT DBMS_METADATA.GET_XML(?,?,?) FROM dual";
        
        SQLConnection connection = getNode().getSession().getInteractiveConnection();
        ResultSet rs = null;
        PreparedStatement pStmt = null;
        
        try {
              
            // use prepared statement
            pStmt = connection.prepareStatement(sql);
            pStmt.setString(1, objectType);
            pStmt.setString(2, getNode().getName());
            pStmt.setString(3, getNode().getSchemaOrCatalogName());
            rs = pStmt.executeQuery();
        
            source = "";
            if (rs.next()) {
                Clob clob = rs.getClob(1);
                source = clob.getSubString(1, (int) clob.length());
            }
            
            rs.close();
            
        } catch (Exception e) {
            
            SQLExplorerPlugin.error("Couldn't load xml for: " + getNode().getName(), e);
            
        } finally {
            
            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (Exception e) {
                    SQLExplorerPlugin.error("Error closing statement", e);
                }
            }         
        }
        return source;
    }
    
    public String getLabelText() {
        return Messages.getString("oracle.dbdetail.tab.xml");
    }

    public String getStatusMessage() {
        return Messages.getString("oracle.dbdetail.tab.xmlFor") + " " + getNode().getQualifiedName();
    }
    
}
