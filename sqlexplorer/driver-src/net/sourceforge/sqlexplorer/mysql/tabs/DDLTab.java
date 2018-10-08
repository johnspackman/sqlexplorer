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
package net.sourceforge.sqlexplorer.mysql.tabs;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSourceTab;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;


public class DDLTab extends AbstractSourceTab {

    public Object[] getSQLParameters() {
        return new Object[] {getNode().getSchemaOrCatalogName(), getNode().getName()};
    }

    public String getSource() {
        
        StringBuilder source = new StringBuilder();
        String objectType = getNode().getType().toUpperCase();
        boolean isTrigger = "TRIGGER".equals(objectType);
        if("CATALOG".equals(objectType))
        {
        	objectType = "DATABASE";
        }
        
        String separator = ";";
        if("PROCEDURE".equals(objectType) || "FUNCTION".equals(objectType) || isTrigger)
        {
        	source.append("delimiter //\n\n");
        	separator = "//";
        }
        source.append("DROP " + objectType + " IF EXISTS " + getNode().getQualifiedName() + "\n").append(separator).append("\n");
        
    	Session session = getNode().getSession();
    	SQLConnection connection = null;
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	
        try {
        	connection = session.grabConnection();
              
            if(isTrigger)
            {
                String sql = "select * from INFORMATION_SCHEMA.TRIGGERS where TRIGGER_SCHEMA = ? and TRIGGER_NAME = ?";
                
                // use prepared statement
                stmt = connection.prepareStatement(sql);
                stmt.setString(1, getNode().getSchemaOrCatalogName());
                stmt.setString(2, getNode().getName());
                rs = stmt.executeQuery();
                
                if (rs.next()) {
                	String event = rs.getString("EVENT_MANIPULATION");
                	String schema = rs.getString("EVENT_OBJECT_SCHEMA");
                	String table = rs.getString("EVENT_OBJECT_TABLE");
                	String timing = rs.getString("ACTION_TIMING");
                	String definer = rs.getMetaData().getColumnCount() > 18 ? rs.getString("DEFINER") : null;
                    Clob body = rs.getClob("ACTION_STATEMENT");
                    
                    source.append("CREATE");
                    if(definer != null)
                    {
                    	source.append(" DEFINER=").append(definer);
                    }
                    source
                    	.append(" TRIGGER ")
                    	.append(getNode().getQualifiedName())
                    	.append(" ").append(timing)
                    	.append(" ").append(event)
                    	.append(" on `").append(schema).append("`.`").append(table).append("`\n")
                    	.append("FOR EACH ROW\n")
                    	.append(body.getSubString(1, (int) body.length()))
                        .append("\n").append(separator).append("\n");
                }
            	
            }
            else
            {
                String sql = "show create " + objectType +" " + getNode().getQualifiedName();
                
                // use prepared statement
                stmt = connection.prepareStatement(sql);
                rs = stmt.executeQuery();
            
                int resultIndex = 2;
                ResultSetMetaData metaData = rs.getMetaData();
                for(int i=2; i <= metaData.getColumnCount(); i++)
                {
                	if(metaData.getColumnLabel(i).startsWith("Create"))
                	{
                		resultIndex = i;
                		break;
                	}
                }
                if (rs.next()) {
                    Clob clob = rs.getClob(resultIndex);
                    source.append(clob.getSubString(1, (int) clob.length()));
                }
                source.append("\n").append(separator).append("\n");
            	
            }
            
        } catch (Exception e) {
            SQLExplorerPlugin.error("Error creating export script", e);
        	return "Error reading data: " + e.getMessage();
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
        return source.toString();
    }
    
    public String getLabelText() {
        return Messages.getString("mysql.DatabaseDetailView.Tab.ddl");
    }

    public String getStatusMessage() {
        return Messages.getString("mysql.DatabaseDetailView.Tab.ddlFor") + " " + getNode().getQualifiedName();
    }
    
}
