package net.sourceforge.sqlexplorer.informix.tabs;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSourceTab;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode;
import net.sourceforge.sqlexplorer.informix.nodes.ChunkNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import java.sql.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.informix.jdbc.*;

public class ChunkTab extends AbstractSourceTab {

    private static final Log _logger = LogFactory.getLog(AbstractNode.class);
	
	public ChunkTab() {
		// TODO Auto-generated constructor stub
	}

    public String getSource() {
		ChunkNode cn = (ChunkNode)getNode();
		String src = "Chunk number: "+cn.getId()+"\n";
		
        SQLConnection connection  = null;
        Connection conn = null;
        ResultSet rs = null;
        Statement stmt = null;
        int timeOut = SQLExplorerPlugin.getIntPref(IConstants.INTERACTIVE_QUERY_TIMEOUT);
        
        try {
        	connection = cn.getSession().grabConnection();

        	try {
	        	conn = connection.getConnection();
	    		CallableStatement cstmt2 = conn.prepareCall("{call informix.explain_sql(?, ?, ?, ?, ?, ?, ?)}");
	
	    		cstmt2.registerOutParameter( 1, Types.INTEGER );
	    		cstmt2.registerOutParameter( 2, Types.INTEGER );
	    		cstmt2.setString(3,null);
	    		cstmt2.setNull( 5, Types.BLOB );                  // Filter
	    		cstmt2.registerOutParameter( 6, Types.BLOB );     // XML_OUTPUT
	    		cstmt2.registerOutParameter( 7, Types.BLOB );     // XML_MESSAGE
	
	    		String sqlIn = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><plist version=\"1.0\"><dict><key>MAJOR_VERSION</key><integer>1</integer><key>MINOR_VERSION</key><integer>0</integer><key>REQUESTED_LOCALE</key><string>en_us.8859-1</string><key>RETAIN</key><string>N</string><key>TRACE</key><string>N</string><key>SQL_TEXT</key><string>SELECT V.row_id, V.val_begin, V.val_end FROM fr:report_row_values V INNER JOIN fr:finance_report R ON R.id=V.id AND R.regcode=\"10256137\" AND R.type=\"AB09A\" ORDER BY V.row_id</string></dict></plist>";
	    		
	    		byte[] buffer = new byte[8000];
	    		buffer = sqlIn.getBytes();

	    		IfxLobDescriptor loDesc = new IfxLobDescriptor(conn);
	    		IfxLocator loPtr = new IfxLocator();
	    		IfxSmartBlob smb = new IfxSmartBlob(conn);
	    		int loFd = smb.IfxLoCreate(loDesc, IfxSmartBlob.LO_RDWR, loPtr);
	
	    		int n = buffer.length;
	    		if (n > 0) n = smb.IfxLoWrite(loFd, buffer);
	    	  
	    		smb.IfxLoClose(loFd);
	    		Blob blb = new IfxBblob(loPtr);
	    		cstmt2.setBlob(4, blb); // set the blob column
	    		rs = cstmt2.executeQuery();

	    		int outmajver = cstmt2.getInt(1);
	    		int outminver = cstmt2.getInt(2);

	    		/* read the xml explain output if there is any */
	    		while (rs.next()) {
	    			byte[] buf = new byte[80000];
	    			IfxBblob b = (IfxBblob) rs.getBlob(1);

	    			if (b != null) {
		    			IfxLocator loptr = b.getLocator();
		    			IfxSmartBlob smbl = new IfxSmartBlob(conn);
		    			int lofd = smbl.IfxLoOpen(loptr, IfxSmartBlob.LO_RDONLY);
		    			int size = smbl.IfxLoRead(lofd, buf, 80000);
		                _logger.debug(new String(buf));
		    			
		    			smbl.IfxLoClose(lofd);
		    			smbl.IfxLoRelease(loptr);
	    			} else _logger.debug("b==null");
    			}

    		 /* get blob out parameters */
	    		IfxBblob outmsg_b = (IfxBblob)cstmt2.getBlob(7);
	    		if (outmsg_b == null) {
	    			_logger.debug("outmsg_b is null");
	    		}
	    		else {
	    			byte[] buf2 = new byte[80000];
	    			
	    			IfxLocator xml_msg_loptr = outmsg_b.getLocator();
    				IfxSmartBlob xml_msg_smbl = new IfxSmartBlob(conn);
    				int msg_out_lofd = xml_msg_smbl.IfxLoOpen(xml_msg_loptr, IfxSmartBlob.LO_RDONLY);
    				int xml_msg_size = xml_msg_smbl.IfxLoRead(msg_out_lofd, buf2, 80000);
    				xml_msg_smbl.IfxLoClose(msg_out_lofd);          
    				xml_msg_smbl.IfxLoRelease(xml_msg_loptr);
	                _logger.debug(new String(buf2));
    		   }			
        	} catch (Exception sqlex) {_logger.debug(sqlex.getMessage()); }
    		
    		
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
