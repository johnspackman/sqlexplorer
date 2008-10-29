package net.sourceforge.sqlexplorer.mssql.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

public class ProcedureParametersFolder extends AbstractFolderNode {

	protected int _id;

	public ProcedureParametersFolder(String name) {
		super(Messages.getString("mssql.dbstructure.procedures.parameterfolder"));
	}

	public ProcedureParametersFolder(INode parent, int id, MetaDataSession session) {
		super(parent, Messages.getString("mssql.dbstructure.procedures.parameterfolder"), session, "FOLDER");
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
        			"select name from "+ getSchemaOrCatalogName() +"..syscolumns where id = "+ _id );

            rs = pStmt.executeQuery();
        } catch (Exception e) {
        	SQLExplorerPlugin.error("Couldn't execute query for " + getName(), e);
        }
        
        try {

            while (rs.next()) {

            	if (isExcludedByFilter(rs.getString(1))) {
            		continue;
            	}

            	ProcedureParameterNode newNode = new ProcedureParameterNode(rs.getString(1));
            	newNode.setSession(this.getSession());

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
