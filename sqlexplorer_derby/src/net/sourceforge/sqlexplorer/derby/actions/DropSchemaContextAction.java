package net.sourceforge.sqlexplorer.derby.actions;

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;

public class DropSchemaContextAction extends AbstractDBTreeContextAction {

	private static final Log _logger = LogFactory.getLog(DropSchemaContextAction.class);
	
	
	public DropSchemaContextAction() {
	}


	public String getText() {
		return "Drop Schema";
	}
	
	
	public void run() {
		
		INode tableNode = _selectedNodes[0];
		String schema_name = tableNode.getSchemaOrCatalogName();
		
		boolean confirm = MessageDialog.openQuestion(getView().getSite().getShell(), "Drop Schema", 
				"Are you shure dropping schema " +
				"'" + schema_name + "'?\n" +
				"(Schema must be empty)");
		
		if (confirm) {
			_logger.debug("About to drop schema [" + schema_name + "]");
			
	        Session session = tableNode.getSession();
	        if (session == null)
	            return;

	        SQLConnection connection = null;
			Statement st = null;
			String result = null;

			try {
				connection = session.grabConnection();
				st = connection.createStatement();
				st.execute("DROP SCHEMA " + schema_name + " RESTRICT");
				
			} catch (SQLException e) {

				result = "(" + e.getErrorCode() + ") " + e.getMessage();
				
				if (_logger.isDebugEnabled()) {
					_logger.debug("Drop schema error.", e);
				}

			} finally {
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
			
			// On error
			if (result != null) {
				MessageDialog.openError(getView().getSite().getShell(), "Drop Schema", "Error while dropping schema:\n" + result);
			}
			else
			{
				tableNode.getParent().refresh();
				_treeViewer.refresh();
			}
			
		}
	}	
}
