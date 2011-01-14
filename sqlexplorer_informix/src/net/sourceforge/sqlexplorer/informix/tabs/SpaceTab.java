package net.sourceforge.sqlexplorer.informix.tabs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSourceTab;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.informix.nodes.SpaceNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

public class SpaceTab extends AbstractSourceTab {

	public SpaceTab() {
		// TODO Auto-generated constructor stub
	}

    public String getSource() {
		SpaceNode n = (SpaceNode)getNode();
		String src = "DBSpace number: "+n.getId()+"\n";
		
        SQLConnection connection  = null;
        ResultSet rs = null;
        Statement stmt = null;
        int timeOut = SQLExplorerPlugin.getIntPref(IConstants.INTERACTIVE_QUERY_TIMEOUT);
        
        try {
        	connection = n.getSession().grabConnection();
        	
            stmt = connection.createStatement();
            try {
            	stmt.setQueryTimeout(timeOut);
            }
            catch(Exception ignored) {}
            
            rs = stmt.executeQuery("SELECT owner, pagesize, nchunks FROM sysmaster:sysdbspaces WHERE dbsnum="+n.getId());

    		int pageSize = 0;
            while (rs.next()) {
            	pageSize = rs.getInt(2);
            	src = src + "Owner: " + rs.getString(1)+"\n";
            	src = src + "Page size: " + pageSize+" bytes\n";
            	src = src + "Nr of chunks: " + rs.getString(3)+"\n";
            }
        	try {
        		rs.close();
        	}catch(SQLException e) { SQLExplorerPlugin.error("Error closing result set", e); }
            
        	long tpag = 0;
        	long fpag = 0;
        	DecimalFormat df = new DecimalFormat("#0.00");
        	rs = stmt.executeQuery("SELECT d.dbsnum, SUM(chksize) pages_size, SUM(nfree) pages_free FROM sysmaster:sysdbspaces d, sysmaster:syschunks c WHERE d.dbsnum = c.dbsnum AND d.dbsnum = "+n.getId()+" group by 1");
            while (rs.next()) {
            	tpag = rs.getLong(2);
            	fpag = rs.getLong(3);
            	float free_percent = (fpag * 100) / (float)tpag;
            	src = src + "Total size: " + tpag + " pages (" + df.format((tpag * pageSize) / (double)1073741824)+ " GB)\n";
            	src = src + "Free size: " + fpag + " pages ("+df.format((fpag*pageSize) / (double)1073741824)+" GB, "+df.format(free_percent)+"%)\n";
            }
            
        } 
        catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't load space information for: " + n.getId(), e);

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
            	n.getSession().releaseConnection(connection);
        }
		
		return src;
    }

    public String getLabelText() {
        return "DBSpace information";
    }
    
}
