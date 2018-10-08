package net.sourceforge.sqlexplorer.mysql.actions;

import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

import org.eclipse.jface.dialogs.InputDialog;


public class RenameTable extends AbstractDBTreeContextAction {

    public RenameTable() {

    }

    /**
     * Return the text label for the rename action. 
     */
    public String getText() {
        return Messages.getString("mysql.dbstructure.actions.RenameTable");
    }

    /**
     * Execute Rename Action
     */
    public void run() {
        
    	Session session = _selectedNodes[0].getSession();
    	SQLConnection connection = null;
    	Statement stmt = null;
    	
        try {
        	connection = session.grabConnection();
            
            // get the currently selected node
            INode tableNode = _selectedNodes[0];
            
            // create prompt for new name
            InputDialog dialog = new InputDialog(getView().getSite().getShell(), 
                    Messages.getString("mysql.dbstructure.actions.RenameTable.Prompt.Title"), 
                    Messages.getString("mysql.dbstructure.actions.RenameTable.Prompt.Message") 
                    + " " + tableNode.getName(), 
                    tableNode.getName(), null);
                        
            // display prompt and check if the prompt was cancelled
            if (dialog.open() != InputDialog.OK) {
                return;
            }
            
            // get the new table name from the prompt
            String newTableName = dialog.getValue();
            
            // create a new statement to do the rename
            String sql = "RENAME TABLE " + tableNode.getQualifiedName() + " TO "
                + "`" + tableNode.getSchemaOrCatalogName() + "`.`" + newTableName + "`";
            stmt = connection.createStatement();
            
            // do the rename;
            stmt.execute(sql);
            
            // refresh the nodes in the database structure tree
            // starting from the table's parent node.
            tableNode.getParent().refresh();
            _treeViewer.refresh(tableNode.getParent());
            
        } catch (SQLException e) {
            SQLExplorerPlugin.error("Cannot rename table", e);
        } finally {
        	if (stmt != null)
        		try {
        			stmt.close();
        		} catch(SQLException e) {
        			SQLExplorerPlugin.error("Cannot close statement", e);
        		}
        	if (connection != null)
       			session.releaseConnection(connection);
        }
    }

}
