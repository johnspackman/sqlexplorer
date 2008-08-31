package net.sourceforge.sqlexplorer.sybase.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;

public class ProcedureFolder extends SysObjectFolder {

	public ProcedureFolder() {
	
	}
	
	public ProcedureFolder(INode parent, String name, MetaDataSession session) {
		super(parent, name,session);
	}
	
	public String getChildType() {
		return "procedure";
	}
	
	public Class<? extends SysObjectNode> getChildClass() {
		return ProcedureNode.class;
	}

	public String getName() {
		return Messages.getString("sybase.dbstructure.procedures");
	}

	public String getSQL() {
		return "select a.name, a.uid, b.name, a.id from " + 
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
