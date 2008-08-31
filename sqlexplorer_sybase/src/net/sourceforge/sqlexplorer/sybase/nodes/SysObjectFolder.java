package net.sourceforge.sqlexplorer.sybase.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

public class SysObjectFolder extends AbstractFolderNode {

	public SysObjectFolder() {
		super(null, null, null, "FOLDER");
	}
	
	//public SysObjectFolder(INode parent, SessionTreeNode sessionNode) {
	public SysObjectFolder(INode parent, String name, MetaDataSession session) {
		super(parent, name, session, "FOLDER");
		//_type = "FOLDER";
		//initialize(parent, null, sessionNode);
	}

	public String getChildType() {
		return "";
	}

	public String getName() {
		return "none";
	}
	
	public Class<? extends SysObjectNode> getChildClass() {
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
		
        SQLConnection connection = null;
        ResultSet rs = null;
        PreparedStatement pStmt = null;
        
        try {
        	connection = getSession().grabConnection();
    		pStmt = connection.prepareStatement(getSQL());
            rs = pStmt.executeQuery();

            while (rs.next()) {
            	if (isExcludedByFilter(rs.getString(1))) {
            		continue;
            	}
            	
            	Class<?>[] param = new Class<?>[3];
            	Object[] params = new Object[3];
            	
            	try {
            		//param[0] = this.getClass();
            		param[0] = INode.class;
            		param[1] = "".getClass();
            		param[2] = _session.getClass();
            		params[0] = this;
            		params[1] = rs.getString(1);
            		params[2] = _session;

            		
	            	SysObjectNode newNode = (SysObjectNode) getChildClass().getConstructor(param).newInstance(params);;
            		
            		//SysObjectNode newNode = (SysObjectNode) getChildClass().getConstructor(new Class[0]).newInstance(new Object[0]);;
            		
	            	//newNode.initialize(this, rs.getString(1), _sessionNode);
	            	newNode.setUID(rs.getInt(2));
	            	newNode.setUName(rs.getString(3));
	            	newNode.setID(rs.getInt(4));
	            	
	
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
            
            if (connection != null)
          		getSession().releaseConnection((net.sourceforge.sqlexplorer.dbproduct.SQLConnection) connection);        	
        }            
	}
}
