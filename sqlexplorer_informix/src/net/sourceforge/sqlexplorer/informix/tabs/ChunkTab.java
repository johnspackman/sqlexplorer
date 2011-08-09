package net.sourceforge.sqlexplorer.informix.tabs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSourceTab;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.informix.nodes.ChunkNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

public class ChunkTab extends AbstractSourceTab {

	
	public ChunkTab() {
	}

    public String getSource() {
		ChunkNode cn = (ChunkNode)getNode();
		String src = "Chunk number: "+cn.getId()+"\n";
		
        SQLConnection connection  = null;
        ResultSet rs = null;
        Statement stmt = null;
        int timeOut = SQLExplorerPlugin.getIntPref(IConstants.INTERACTIVE_QUERY_TIMEOUT);
        
        try {
        	connection = cn.getSession().grabConnection();

            stmt = connection.createStatement();
            try {
            	stmt.setQueryTimeout(timeOut);
            }
            catch(Exception ignored) {}
            
            rs = stmt.executeQuery("select name dbspace, is_mirrored, is_blobspace, is_temp, chknum, fname, offset, is_offline, is_recovering, is_blobchunk, is_inconsistent, chksize Pages_size, nfree pages_free, d.pagesize, mfname mirror_device, moffset mirror_offset, mis_offline, mis_recovering from sysmaster:sysdbspaces d, sysmaster:syschunks c where d.dbsnum = c.dbsnum and c.chknum="+cn.getId());
    		int pageSize = 0;
        	long tpag = 0;
        	long fpag = 0;
        	DecimalFormat df = new DecimalFormat("#0.00");
            while (rs.next()) {
            	pageSize = rs.getInt(14);
            	src = src + "Device: " + rs.getString(6)+"\n";
            	src = src + "DBSpace: " + rs.getString(1)+"\n";
            	tpag = rs.getLong(12);
            	fpag = rs.getLong(13);
            	float free_percent = (fpag * 100) / (float)tpag;
            	src = src + "Total size: " + tpag + " pages (" + df.format((tpag * pageSize) / (double)1073741824)+ " GB)\n";
            	src = src + "Free size: " + fpag + " pages ("+df.format((fpag*pageSize) / (double)1073741824)+" GB, "+df.format(free_percent)+"%)\n";
            	
            }
            rs.close();
        } 
        catch (Exception e) {
            SQLExplorerPlugin.error("Couldn't load chunk information for: " + cn.getId(), e);

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
            	cn.getSession().releaseConnection(connection);
        }
		
		return src;
    }

    public String getLabelText() {
        return "Chunk information";
    }
    
}
