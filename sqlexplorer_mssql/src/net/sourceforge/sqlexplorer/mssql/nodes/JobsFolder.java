package net.sourceforge.sqlexplorer.mssql.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

public class JobsFolder extends AbstractFolderNode {

	public JobsFolder() 
	{
		super("mssql.dbstructure.jobs");
	}

	public JobsFolder(INode parent, MetaDataSession session) 
	{
		super(parent, "mssql.dbstructure.jobs", session, "JOBS_FOLDER");
	}


	@Override
	public void loadChildren() {
        ResultSet rs = null;
        PreparedStatement pStmt = null;

        try {
    		SQLConnection connection = getSession().grabConnection();

            // use prepared statement
        	pStmt = connection.prepareStatement(
        			"select name, job_id from msdb..sysjobs");

            rs = pStmt.executeQuery();

            while (rs.next()) {

            	if (isExcludedByFilter(rs.getString(1))) {
            		continue;
            	}

            	JobsNode newNode = new JobsNode(this, rs.getString(1), rs.getString(2), getSession());

                addChildNode(newNode);
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
