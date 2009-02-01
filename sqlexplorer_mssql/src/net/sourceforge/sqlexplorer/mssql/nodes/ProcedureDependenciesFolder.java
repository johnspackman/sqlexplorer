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

public class ProcedureDependenciesFolder extends AbstractFolderNode {

	protected int _id;

	public ProcedureDependenciesFolder() {
		super(Messages.getString("mssql.dbstructure.procedures.dependenciesfolder"));
	}

	public ProcedureDependenciesFolder(INode parent, int id, MetaDataSession session) {
		super(parent, Messages.getString("mssql.dbstructure.procedures.dependenciesfolder"), session, "FOLDER");
		_id = id;
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
        			"select name from "+
        			getSchemaOrCatalogName() +".dbo.sysobjects " +
        			"where id in ( select depid from "+
        			getSchemaOrCatalogName() +".dbo.sysdepends where id = "+ _id+")" );

            rs = pStmt.executeQuery();

            while (rs.next()) {

            	if (isExcludedByFilter(rs.getString(1))) {
            		continue;
            	}

            	ProcedureDependenciesNode newNode = new ProcedureDependenciesNode(this, rs.getString(1), getSession());

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
