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

public class SysObjectFolder extends AbstractFolderNode {

	public SysObjectFolder() {
	
	}
	
	public SysObjectFolder(INode parent, SessionTreeNode sessionNode) {
		_type = "FOLDER";
		initialize(parent, null, sessionNode);
	}

	public String getChildType() {
		return "";
	}

	public String getName() {
		return "none";
	}
	
	public Class getChildClass() {
		return null;
	}

	public String getSQL() {
		return ""; 
	}

	public Object[] getSQLParameters() {
		return null;
	}
	
	public void loadChildren() {
		if (getSQL().equals("")) return;
		
        SQLConnection connection = getSession().getInteractiveConnection();
        ResultSet rs = null;
        PreparedStatement pStmt = null;
        
        try {
    		pStmt = connection.prepareStatement(getSQL());
            rs = pStmt.executeQuery();

            while (rs.next()) {
            	if (isExcludedByFilter(rs.getString(1))) {
            		continue;
            	}
            	
            	try {
	            	SysObjectNode newNode = (SysObjectNode) getChildClass().newInstance();
	            	newNode.initialize(this, rs.getString(1), _sessionNode);
	            	newNode.setUID(rs.getInt(2));
	            	newNode.setUName(rs.getString(3));
	
	            	addChildNode(newNode);
            	} catch (InstantiationException ie) {
            		SQLExplorerPlugin.error("Error loading node class for " + getName(), ie);
            	}
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
