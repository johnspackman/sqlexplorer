package net.sourceforge.sqlexplorer.dbstructure.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;


public abstract class AbstractSQLFolderNode extends AbstractFolderNode {


    public final void loadChildren() {

        SQLConnection connection = getSession().getInteractiveConnection();
        ResultSet rs = null;
        Statement stmt = null;
        PreparedStatement pStmt = null;
        
        try {
                    
            Object[] params = getSQLParameters();
            if (params == null || params.length == 0) {
                
                
                // use normal statement
                stmt = connection.createStatement();
                rs = stmt.executeQuery(getSQL());
                
            } else {
                
                // use prepared statement
                pStmt = connection.prepareStatement(getSQL());
                
                for (int i = 0; i < params.length; i++) {
                    
                    if (params[i] instanceof String) {
                        pStmt.setString(i + 1, (String) params[i]);
                    } else if (params[i] instanceof Integer) {
                        pStmt.setInt(i + 1, ((Integer) params[i]).intValue());
                    } else if (params[i] instanceof String) {
                        pStmt.setLong(i + 1, ((Long) params[i]).longValue());
                    }                     
                }
                
                rs = pStmt.executeQuery();
            }
        
            
            while (rs.next()) {
                
                String name = rs.getString(1);
                ObjectNode node = new ObjectNode(name, getChildType(), this, _image);
                
                addChildNode(node);
            }
            
            rs.close();
            
        } catch (Exception e) {
            
            SQLExplorerPlugin.error("Couldn't load children for: " + getName(), e);
            
        } finally {
            
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    SQLExplorerPlugin.error("Error closing statement", e);
                }
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (Exception e) {
                    SQLExplorerPlugin.error("Error closing statement", e);
                }
            }            
        }
        
    }

    public abstract String getChildType();
    
    public abstract Object[] getSQLParameters();
    
    public abstract String getSQL();
    
    public abstract String getName();
    
}