package net.sourceforge.sqlexplorer.informix.nodes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;


public class ChunksFolderNode extends AbstractFolderNode {

    public ChunksFolderNode(INode parent, String name, MetaDataSession session) {
    	super(parent, name, session, "chunks_folder");
    }
       
	public void loadChildren() {
        SQLConnection connection  = null;
        ResultSet rs = null;
        Statement stmt = null;
        int timeOut = SQLExplorerPlugin.getIntPref(IConstants.INTERACTIVE_QUERY_TIMEOUT);
        int idx = 0;
        
        try {
        	connection = getSession().grabConnection();
        	
            stmt = connection.createStatement();
            try {
            	stmt.setQueryTimeout(timeOut);
            }
            catch(Exception ignored) {}
            
            rs = stmt.executeQuery("SELECT chknum, RTRIM(fname) FROM sysmaster:syschunks ORDER BY chknum");

            while (rs.next()) {
                addChildNode(new ChunkNode(this, "#" + rs.getString(1) + " " + rs.getString(2), _session, idx++, rs.getString(1)));
            }
        } 
        catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't load children for: " + getName(), e);

        } finally {
            if (rs != null)
            	try {
            		rs.close();
            	}catch(SQLException e) {
            		SQLExplorerPlugin.error("Error closing result set", e);
            	}
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                    SQLExplorerPlugin.error("Error closing statement", e);
                }
            if (connection != null)
            	getSession().releaseConnection(connection);
        }
	}
}
