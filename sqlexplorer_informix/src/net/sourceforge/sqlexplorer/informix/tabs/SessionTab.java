package net.sourceforge.sqlexplorer.informix.tabs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSourceTab;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.informix.nodes.SessionNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

public class SessionTab extends AbstractSourceTab {

	public SessionTab() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getSource() {
		SessionNode n = (SessionNode)getNode();
		String src = "Session ID: "+n.getId()+"\n";
		
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
            
            rs = stmt.executeQuery("SELECT c.sid, trim(c.username) user, rtrim(decode(length(hostname),0,'localhost',hostname)) hostname, c.uid as userid, c.gid, c.pid, dbinfo('UTC_TO_DATETIME',connected)::DATETIME MONTH TO SECOND as con, format_units(memtotal,'b') as mtotal, format_units(memused,'b') as mused, nfiles ,rtrim(cbl_stmt[1,1000]) AS curr_stmt FROM sysmaster:sysscblst c, sysmaster:sysrstcb r, outer sysmaster:sysconblock b WHERE c.address = r.scb AND cbl_sessionid = c.sid AND sysmaster:bitval(r.flags,'0x80000')>0 AND c.sid = "+n.getId());

            int i = 0;
            while (rs.next()) {
            	if (i++ == 0) {
	            	src = src + "User: " + rs.getString(2)+"\n";
	            	src = src + "Hostname: " + rs.getString(3)+"\n";
	            	src = src + "User ID: " + rs.getString(4)+"\n";
	            	src = src + "GID: " + rs.getString(5)+"\n";
	            	src = src + "PID: " + rs.getString(6)+"\n";
	            	src = src + "Connected: " + rs.getString(7)+"\n";
	            	src = src + "mtotal: " + rs.getString(8)+"\n";
	            	src = src + "mused: " + rs.getString(9)+"\n";
	            	src = src + "nfiles: " + rs.getString(10)+"\n";
            	}
	            	src = src + "SQL: " +rs.getString(11)+"\n";
            }
        	try {
        		rs.close();
        	}catch(SQLException e) { SQLExplorerPlugin.error("Error closing result set", e); }
        } 
        catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't load session information for: " + n.getId(), e);

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
        return "Session Information";
    }
	
}
