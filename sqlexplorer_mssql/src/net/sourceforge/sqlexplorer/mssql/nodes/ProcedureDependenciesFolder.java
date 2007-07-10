package net.sourceforge.sqlexplorer.mssql.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

public class ProcedureDependenciesFolder extends AbstractFolderNode {

	protected int _id;

	public ProcedureDependenciesFolder() {
	}

	public ProcedureDependenciesFolder(INode parent, int id, SessionTreeNode sessionNode) {
		_type = "FOLDER";
		_id = id;
		initialize(parent, null, sessionNode);
	}

	@Override
	public String getName() {
		return Messages.getString("mssql.dbstructure.procedures.dependenciesfolder");
	}

	@Override
	public void loadChildren() {
		SQLConnection connection = getSession().getInteractiveConnection();
        ResultSet rs = null;
        PreparedStatement pStmt = null;

        try {

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

            	ProcedureDependenciesNode newNode = new ProcedureDependenciesNode();
            	newNode.initialize(this, rs.getString(1), _sessionNode);

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
