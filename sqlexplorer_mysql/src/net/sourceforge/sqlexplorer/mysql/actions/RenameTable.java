package net.sourceforge.sqlexplorer.mysql.actions;

import java.sql.Statement;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;


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
        
        Statement stmt = null;
        
        try {
            
            // get the currently selected node
            INode tableNode = _selectedNodes[0];
            
            // create prompt for new name
            InputDialog dialog = new InputDialog(_view.getSite().getShell(), 
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
            stmt = tableNode.getSession().getInteractiveConnection().createStatement();
            
            // do the rename;
            stmt.execute(sql);
            
            // refresh the nodes in the database structure tree
            // starting from the table's parent node.
            tableNode.getParent().refresh();
            _treeViewer.refresh(tableNode.getParent());

            
        } catch (Exception e) {
            // something went wrong, so we display an error message.
            MessageDialog.openError(_view.getSite().getShell(), 
                    Messages.getString("mysql.dbstructure.actions.RenameTable.Error.Title"), 
                    e.getMessage());
            
        } finally {
            
            // close our statement
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    // fail silently, but log the error.
                    SQLExplorerPlugin.error("Error closing statement.", e);
                }
            }
        }
    }

}
