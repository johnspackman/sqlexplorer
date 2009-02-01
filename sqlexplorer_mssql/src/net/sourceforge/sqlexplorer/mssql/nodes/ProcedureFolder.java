package net.sourceforge.sqlexplorer.mssql.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.ObjectNode;
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
        SQLConnection connection = null;
        try {
    		connection = getSession().grabConnection();

            // use prepared statement
        	pStmt = connection.prepareStatement(
        			"select name, id "+
        			"from "+ getSchemaOrCatalogName()+"..sysobjects where xtype='P' and category = 0" +
        			"order by name");

            rs = pStmt.executeQuery();

            while (rs.next()) {

            	if (isExcludedByFilter(rs.getString(1))) {
            		continue;
            	}

            	ProcedureNode newNode = new ProcedureNode(this, rs.getString(1), rs.getInt(2), getSession());

                addChildNode(newNode);
            }
            
        } 
        catch (Exception e) 
        {
        	
            ObjectNode node = new ObjectNode("Error loading children: " + e.getLocalizedMessage(), "error", this, null);
            addChildNode(node);

            SQLExplorerPlugin.error("Couldn't load children for: " + getName(), e);

        } finally {
            if (rs != null)
            	try {
            		rs.close();
            	}catch(SQLException e) {
            		SQLExplorerPlugin.error("Error closing result set", e);
            	}
            if (pStmt != null)
                try {
                    pStmt.close();
                } catch (SQLException e) {
                    SQLExplorerPlugin.error("Error closing statement", e);
                }
            if (connection != null)
            	getSession().releaseConnection(connection);
        }
        
	}

}
