package net.sourceforge.sqlexplorer.mssql.actions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.mssql.nodes.JobsNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

public class StopJob extends AbstractDBTreeContextAction {

	public StopJob() {
	}

	/**
     * Set the text for the menu entry.
     *
     * @see org.eclipse.jface.action.IAction#getText()
     */
    public String getText() {

        return Messages.getString("mssql.Actions.StopJob");
    }

    /**
     * Action is only available for Stored Procedures.
     *
     * @see net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction#isAvailable()
     */
    public boolean isAvailable() {

    	for ( int i = 0; i < _selectedNodes.length; i++ )
    	{
    		if ( !(_selectedNodes[i] instanceof JobsNode) ) {
    			return false;
    		}
    	}

    	return true;
    }

    /**
	 * Generate SQL for Editing Procedure
	 */
	public void run() {
		ResultSet rs = null;
        PreparedStatement pStmt = null;

        for ( int i = 0; i < _selectedNodes.length; i++ )
        {
        	try {
        		SQLConnection connection = _selectedNodes[i].getSession().grabConnection();

        		// use prepared statement
        		pStmt = connection.prepareStatement("exec msdb.dbo.sp_stop_job @job_name=?");

        		pStmt.setString(1, _selectedNodes[i].getName());
        		rs = pStmt.executeQuery();
        		_selectedNodes[i].getSession().releaseConnection(connection);
        		rs.close();
        	} catch (Throwable e) {
        		SQLExplorerPlugin.error("Could not run job "+_selectedNodes[i].getName()+".", e);
        	}
        }
	}

}
