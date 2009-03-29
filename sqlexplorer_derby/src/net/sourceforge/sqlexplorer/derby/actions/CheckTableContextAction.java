package net.sourceforge.sqlexplorer.derby.actions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;

public class CheckTableContextAction extends AbstractDBTreeContextAction {

	private static final Log _logger = LogFactory.getLog(CheckTableContextAction.class);

	
	public CheckTableContextAction() {
	}


	public String getText() {
		return "Check Table";
	}


	public void run() {
		
		INode tableNode = _selectedNodes[0];
		
		_logger.debug("tableNode: " + tableNode.getType());
		_logger.debug("tableNode: " + tableNode.getQualifiedName());

		String sql = "VALUES SYSCS_UTIL.SYSCS_CHECK_TABLE(" +
					"'" + tableNode.getSchemaOrCatalogName() + "'" +
					", '" + tableNode.getName() + "')" ;
		
        Session session = tableNode.getSession();
        if (session == null)
            return;

        SQLConnection connection = null;
		Statement st = null;
		ResultSet rs = null;
		
		String result = null;
		
		try {
			connection = session.grabConnection();
			st = connection.createStatement();
			
			rs = st.executeQuery(sql);
			
			while(rs.next()) {
				int ret = rs.getInt(1);
				if (ret != 1) {
					result = "It seems that table is inconsistent.";
				}
			}
			
		} catch (SQLException e) {
			
			result = "(" + e.getErrorCode() + ") " + e.getMessage();

			if (_logger.isDebugEnabled()) {
				_logger.debug("Schema creation error.", e);
			}
			
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// Ignore here
				}
			}
			
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					// Ignore here 
				}
			}
			if(connection != null)
			{
				session.releaseConnection(connection);
			}
		}

		
		// Show result
		if (result != null) {
			MessageDialog.openError(getView().getSite().getShell(), "Check Table", "Error while checking table:\n" + result);
		} else {
			MessageDialog.openInformation(getView().getSite().getShell(), "Check Table", "All of indexes are consistent.");
		}
	}

	
	
}
