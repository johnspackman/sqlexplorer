package net.sourceforge.sqlexplorer.mssql.actions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

public class EditFunction extends AbstractDBTreeContextAction {

	public EditFunction() {
	}

	/**
     * Set the text for the menu entry.
     *
     * @see org.eclipse.jface.action.IAction#getText()
     */
    public String getText() {

        return Messages.getString("mssql.Actions.EditFunction");
    }

    /**
     * Action is only available for Stored Procedures.
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
			pStmt = connection.prepareStatement("SELECT sc.text FROM " + _selectedNodes[0].getSchemaOrCatalogName() +
					".dbo.sysobjects so JOIN " + _selectedNodes[0].getSchemaOrCatalogName() +
					".dbo.syscomments sc ON so.id=sc.id WHERE so.type='FN' AND so.name=?");

			pStmt.setString(1, currNode.getName());
			rs = pStmt.executeQuery();
			currNode.getSession().releaseConnection(connection);

            while (rs.next()) {
            	query.append(rs.getString(1));
            }

            rs.close();

			SQLEditorInput input = new SQLEditorInput( "Edit Function " + _selectedNodes[0].getName()+"(" + SQLExplorerPlugin.getDefault().getEditorSerialNo()
					+ ").sql" );
			input.setUser(_selectedNodes[0].getSession().getUser());
            IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();

            SQLEditor editorPart = (SQLEditor) page.openEditor((IEditorInput) input,
                    "net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");
            editorPart.setText(query.toString().replaceAll("\\b(?i:create\\s*function\\w*)\\b", "alter function "));

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Could not generate alter script.", e);
        }
	}
}
