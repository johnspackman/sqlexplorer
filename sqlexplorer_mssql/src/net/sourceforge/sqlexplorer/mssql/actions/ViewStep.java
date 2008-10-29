package net.sourceforge.sqlexplorer.mssql.actions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.mssql.nodes.JobNodeStep;
import net.sourceforge.sqlexplorer.mssql.nodes.JobsNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

public class ViewStep extends AbstractDBTreeContextAction {

	public ViewStep() {
	}

	/**
     * Set the text for the menu entry.
     *
     * @see org.eclipse.jface.action.IAction#getText()
     */
    public String getText() {

        return Messages.getString("mssql.Actions.ViewJobStep");
    }

    /**
     * Action is only available for a Job Step.
     *
     * @see net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction#isAvailable()
     */
    public boolean isAvailable() {

    	if( _selectedNodes.length == 1 ) {
    		return true;
    	}

        return false;
    }

    /**
	 * Generate SQL for Editing Procedure
	 */
	public void run() {

        ResultSet rs = null;
        PreparedStatement pStmt = null;
        StringBuffer query = new StringBuffer();

        // Only shows if only one Node is selected

		try {
			INode currNode = _selectedNodes[0];
			SQLConnection connection = currNode.getSession().grabConnection();

			// use prepared statement
			pStmt = connection.prepareStatement("SELECT command FROM msdb.dbo.sysjobsteps " +
					"where job_id = ? and step_id = ? ");

			pStmt.setString(1, ((JobsNode)currNode.getParent()).getID());
			pStmt.setInt(2, ((JobNodeStep)currNode).getID());
			rs = pStmt.executeQuery();
			currNode.getSession().releaseConnection(connection);

            while (rs.next()) {
            	query.append(rs.getString(1));
            }

            rs.close();

			SQLEditorInput input = new SQLEditorInput( "View Job Step " + currNode.getName()+"(" + SQLExplorerPlugin.getDefault().getEditorSerialNo()
					+ ").sql" );
			input.setUser(currNode.getSession().getUser());
            IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();

            SQLEditor editorPart = (SQLEditor) page.openEditor((IEditorInput) input,
                    "net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");
            editorPart.setText(query.toString());

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Could not generate step view.", e);
        }
	}
}
