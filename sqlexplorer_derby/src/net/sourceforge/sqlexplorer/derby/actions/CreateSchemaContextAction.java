package net.sourceforge.sqlexplorer.derby.actions;

import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;

public class CreateSchemaContextAction extends AbstractDBTreeContextAction {

	private static final Log _logger = LogFactory.getLog(CreateSchemaContextAction.class);
	
	
	public CreateSchemaContextAction() {
	}
	

	public String getText() {
		return "Create New Schema";
	}
	
	
	public void run() {
		
		InputDialog id = new InputDialog(getView().getSite().getShell(), "Create New Schema", "Enter New Schema Name", "", null);
		
		if (id.open() == InputDialog.OK) {
			
			String result = null;
			Statement st = null;

			INode tableNode = _selectedNodes[0];

			Session session = tableNode.getSession();
	        if (session == null)
	            return;

	        SQLConnection connection = null;

			try {
				connection = session.grabConnection();
				st = connection.createStatement();
				st.execute("CREATE SCHEMA " + id.getValue());
				
			} catch (SQLException e) {
				
				result = "(" + e.getErrorCode() + ") " + e.getMessage();
				
				if (_logger.isDebugEnabled()) {
					_logger.debug("Schema creating error.", e);
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
				MessageDialog.openError(getView().getSite().getShell(), "Create New Schema", "Error while creating schema:\n" + result);
			}
			else
			{
				tableNode.refresh();
				_treeViewer.refresh();
			}
		}
		
	}	
	
}
