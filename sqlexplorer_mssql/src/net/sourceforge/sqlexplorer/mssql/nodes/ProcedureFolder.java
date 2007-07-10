package net.sourceforge.sqlexplorer.mssql.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

public class ProcedureFolder extends AbstractFolderNode {

	public ProcedureFolder() {
	}

	public ProcedureFolder(INode parent, SessionTreeNode sessionNode) {
		_type = "FOLDER";
		initialize(parent, null, sessionNode);
	}

	@Override
	public String getName() {
		return Messages.getString("mssql.dbstructure.procedures");
	}

	@Override
	public void loadChildren() {
		SQLConnection connection = getSession().getInteractiveConnection();
        ResultSet rs = null;
        PreparedStatement pStmt = null;

        try {

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

            	ProcedureNode newNode = new ProcedureNode(this, rs.getString(1), rs.getInt(2), _sessionNode);

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
