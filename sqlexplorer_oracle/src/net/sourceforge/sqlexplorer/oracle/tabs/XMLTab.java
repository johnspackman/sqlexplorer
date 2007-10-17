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
import java.sql.SQLException;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSourceTab;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;


public class XMLTab extends AbstractSourceTab {

    public Object[] getSQLParameters() {
        return new Object[] {getNode().getSchemaOrCatalogName(), getNode().getName()};
    }

    public String getSource() {
        
        String source = "";
        String objectType = getNode().getType().toUpperCase();
        String sql = "SELECT DBMS_METADATA.GET_XML(?,?,?) FROM dual";
        
        Session session = getNode().getSession();
    	SQLConnection connection = null;
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	
        try {
        	connection = session.grabConnection();
        	
            // use prepared statement
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, objectType);
            stmt.setString(2, getNode().getName());
            stmt.setString(3, getNode().getSchemaOrCatalogName());
            rs = stmt.executeQuery();
        
            source = "";
            if (rs.next()) {
                Clob clob = rs.getClob(1);
                source = clob.getSubString(1, (int) clob.length());
            }
            
        } catch (SQLException e) {
            SQLExplorerPlugin.error("Error loading XML", e);
        } finally {
        	if (rs != null)
        		try {
        			rs.close();
        		} catch(SQLException e) {
        			SQLExplorerPlugin.error("Cannot close result set", e);
        		}
        	if (stmt != null)
        		try {
        			stmt.close();
        		} catch(SQLException e) {
        			SQLExplorerPlugin.error("Cannot close statement", e);
        		}
        	if (connection != null)
       			session.releaseConnection(connection);
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
