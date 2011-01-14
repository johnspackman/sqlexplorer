package net.sourceforge.sqlexplorer.informix.nodes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.swt.graphics.Image;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;

public class SessionFolder extends AbstractFolderNode {

	public SessionFolder() {
		super("Sessions");
	}

	public SessionFolder(INode parent, String name, MetaDataSession session) 
	{
		super(parent, name, session, "SESSION_FOLDER");
	}

	public Image getImage() {
		return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.informix", Messages.getString("informix.images.sessions"));
	}	
	

	@Override
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
            
//            rs = stmt.executeQuery("select sid, RTRIM(username), RTRIM(hostname) from sysmaster:syssessions");
            rs = stmt.executeQuery("SELECT sid ,RTRIM(username), trim(hostname)||TRIM(decode(length(trim(ttyerr)),0,'',':'||ttyerr)) hostname FROM sysmaster:sysscblst WHERE sid != DBINFO('sessionid') AND hostname <> \"\" ORDER BY sid");

            while (rs.next()) {
                addChildNode(new SessionNode(this, "#" + rs.getString(1) + " " + rs.getString(2) + " ("+rs.getString(3)+")", _session, idx++, rs.getString(1)));
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
