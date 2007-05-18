package net.sourceforge.sqlexplorer.sybase.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.sybase.nodes.ProcedureNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

public class ProcedureFolder extends SysObjectFolder {

	public ProcedureFolder() {
	
	}
	
	public String getChildType() {
		return "procedure";
	}
	
	public Class getChildClass() {
		return ProcedureNode.class;
	}

	public String getName() {
		return Messages.getString("sybase.dbstructure.procedures");
	}

	public String getSQL() {
		return "select a.name, a.uid, b.name from " + 
			getSchemaOrCatalogName() + "..sysobjects a, " + 
			getSchemaOrCatalogName() + "..sysusers b " +
			" where a.uid = b.uid and a.type = 'P' order by a.name";
	}

	public Object[] getSQLParameters() {
		return null;
	}
	
//	public void loadChildren() {
//        SQLConnection connection = getSession().getInteractiveConnection();
//        ResultSet rs = null;
//        PreparedStatement pStmt = null;
//        
//        try {
//    		pStmt = connection.prepareStatement(getSQL());
//            rs = pStmt.executeQuery();
//
//            while (rs.next()) {
//            	if (isExcludedByFilter(rs.getString(1))) {
//            		continue;
//            	}
//            	
//            	ProcedureNode newNode = new ProcedureNode();
//            	newNode.initialize(this, rs.getString(1), _sessionNode);
//            	newNode.setUID(rs.getInt(2));
//            	newNode.setUName(rs.getString(3));
//
//            	addChildNode(newNode);
//            }
//
//            rs.close();
//
//        } catch (Exception e) {
//
//            SQLExplorerPlugin.error("Couldn't load children for: " + getName(), e);
//
//        } finally {
//
//            if (pStmt != null) {
//                try {
//                    pStmt.close();
//                } catch (Exception e) {
//                    SQLExplorerPlugin.error("Error closing statement", e);
//                }
//            }
//        }
//	}
	
}
