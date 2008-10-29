package net.sourceforge.sqlexplorer.mssql.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

public class ProcedureFolder extends AbstractFolderNode {

	public ProcedureFolder() {
		super(Messages.getString("mssql.dbstructure.procedures"));
	}

	public ProcedureFolder(INode parent, MetaDataSession session) {
		super(parent, Messages.getString("mssql.dbstructure.procedures"), session, "FOLDER");
	}

	@Override
	public void loadChildren() {
        ResultSet rs = null;
        PreparedStatement pStmt = null;

        try {
    		SQLConnection connection = getSession().grabConnection();

            // use prepared statement
        	pStmt = connection.prepareStatement(
        			"select name, id "+
        			"from "+ getSchemaOrCatalogName()+"..sysobjects where xtype='P' and category = 0" +
        			"order by name");

            rs = pStmt.executeQuery();
            getSession().releaseConnection(connection);
        } catch (Exception e) {
        	SQLExplorerPlugin.error("Couldn't execute query for " + getName(), e);
        }
        
        try {
            while (rs.next()) {

            	if (isExcludedByFilter(rs.getString(1))) {
            		continue;
            	}

            	ProcedureNode newNode = new ProcedureNode(this, rs.getString(1), rs.getInt(2), getSession());

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
