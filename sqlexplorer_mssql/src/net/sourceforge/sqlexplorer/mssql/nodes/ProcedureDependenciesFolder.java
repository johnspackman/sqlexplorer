package net.sourceforge.sqlexplorer.mssql.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
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

        try {
    		SQLConnection connection = getSession().grabConnection();
    		
            // use prepared statement
        	pStmt = connection.prepareStatement(
        			"select name from "+
        			getSchemaOrCatalogName() +".dbo.sysobjects " +
        			"where id in ( select depid from "+
        			getSchemaOrCatalogName() +".dbo.sysdepends where id = "+ _id+")" );

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

            	ProcedureDependenciesNode newNode = new ProcedureDependenciesNode(this, rs.getString(1), getSession());

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
