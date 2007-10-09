package net.sourceforge.sqlexplorer.db2.actions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.TextUtil;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

public class ReactiveViewAction extends AbstractDBTreeContextAction {
	private static final Log _logger = LogFactory
			.getLog(ReactiveViewAction.class);

	public String getText() {
		return Messages.getString("db2.editor.actions.reactive");
	}

	public void run() {
		Statement stmt = null;
		ResultSet rs = null;
		SQLConnection connection = null;
		Session session = null;

		try {
			INode tableNode = _selectedNodes[0];
			if (tableNode != null) {
				boolean confirmed = MessageDialog
						.openConfirm(
								getView().getSite().getShell(),
								Messages
										.getString("db2.editor.actions.reactive.confirm.title"),
								Messages
										.getString("db2.editor.actions.reactive.confirm.text1")
										+ tableNode.getQualifiedName()
										+ Messages
												.getString("db2.editor.actions.reactive.confirm.text2"));

				if (confirmed) {
					// get the currently selected node

					_logger.debug("tableNode: " + tableNode.getType());
					_logger.debug("tableNode: " + tableNode.getQualifiedName());

					// get the view source
					String sqlViewSource = "select text from syscat.views where viewschema = '"
							+ tableNode.getSchemaOrCatalogName()
							+ "' and viewname = '" + tableNode.getName() + "'";

					_logger.debug("SQL to query view source: " + sqlViewSource);
					session = tableNode.getSession();
					connection = session.grabConnection();
					stmt = connection.createStatement();
					rs = stmt.executeQuery(sqlViewSource);
					String viewSource = null;
					while (rs.next()) {
						viewSource = rs.getString(1);
					}

					_logger.debug("viewSource: " + viewSource);
					_logger.debug("removeLineBreaks(viewSouce)"
							+ TextUtil.removeLineBreaks(viewSource));

					// recreate view
					stmt.execute(TextUtil.removeLineBreaks(viewSource));

					// refresh the nodes in the database structure tree
					// starting from the table's parent node.
					tableNode.getParent().refresh();

					// TODO If we could refresh view node to reflect the change
					// it will be better.
					_treeViewer.refresh(tableNode.getParent());
					/*
					 * INode iNode[] =
					 * tableNode.getParent().getParent().getChildNodes();
					 * for(int i=0;i<iNode.length;i++) { _logger.debug("iNode[" +
					 * i + "]: " + iNode[i].getName() + " " + iNode[i].getType() + " " +
					 * iNode[i].getLabelText());
					 * if("Views".equals(iNode[i].getName())){
					 * iNode[i].refresh(); } }
					 */
				}
			}
		} catch (Exception e) {
			// something went wrong, so we display an error message.
			MessageDialog.openError(getView().getSite().getShell(), Messages
					.getString("db2.editor.actions.reactive.error"), e
					.getMessage());

		} finally {

			if (rs != null)
				try {
					rs.close();
				} catch(SQLException e) {
					SQLExplorerPlugin.error("Error closing result set", e);
				}
			// close our statement
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					// fail silently, but log the error.
					SQLExplorerPlugin.error("Error closing statement.", e);
				}
			}
			if (connection != null)
				session.releaseConnection(connection);
		}
	}
}
