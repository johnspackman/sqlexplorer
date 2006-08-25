package net.sourceforge.sqlexplorer.oracle.nodes;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;


public class TableIndexFolder extends AbstractFolderNode {

    public String getName() {
        return Messages.getString("oracle.dbstructure.indexes");
    }
    
    public String getSQL() {
        return "select index_name from sys.all_indexes where table_name = ? and table_owner = ?";
    }
    
    public Object[] getSQLParameters() {
        return new Object[] {getParent().getName(), getSchemaOrCatalogName()};
    }

    public String getType() {
        return "INDEX_FOLDER";
    }

    public void loadChildren() {

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
                if (!isExcludedByFilter(name)) {
                    TableIndexNode node = new TableIndexNode(this, name, getSession(), (TableNode) getParent());
                    addChildNode(node);
                }
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
    
    
}
