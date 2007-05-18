package net.sourceforge.sqlexplorer.sybase.actions;

import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;

import java.sql.ResultSet;
import java.sql.Statement;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;

public class ShowProcedureSource extends AbstractDBTreeContextAction {

	public ShowProcedureSource() {
		
	}
	
	public String getText() {
		return Messages.getString("sybase.dbstructure.actions.ShowProcedureSource");
	}
	
	public boolean isAvailable() {
        if (_selectedNodes.length != 0) {
            return true;
        }
        return false;
    }


	public void run() {
         try {
    		 StringBuffer script = new StringBuffer("");
             String queryDelimiter = SQLExplorerPlugin.getDefault().getPluginPreferences().getString(
                     IConstants.SQL_QRY_DELIMITER);

             Statement stmt = _selectedNodes[0].getSession().getInteractiveConnection().createStatement();
             
             try {
                 for (int i = 0; i < _selectedNodes.length; i++) {

                     if (_selectedNodes[i].getType().equalsIgnoreCase("procedure")) {

                         ResultSet rs = null;
                         try {
                        	 
                        	 //TODO: create custom procedure node to save owner (and not append
                        	 //		owner in procedure name 
                        	 
                        	 //script.append("SET USER ");
                        	 //script.append("DROP PROCEDURE ");
                        	                         	 
                        	 String sql = "Select text from " + _selectedNodes[i].getSchemaOrCatalogName() 
                    		 + "..syscomments where id = object_id('" + _selectedNodes[i].getSchemaOrCatalogName() + "." + _selectedNodes[i].getName() + "')";
                        	 
                             rs = stmt.executeQuery(sql);
                             
                             while (rs.next()) {
                                 script.append(rs.getString(1));
                             }
                             script.append(queryDelimiter).append('\n');
                             
                         } catch (Exception e) {
                        	 SQLExplorerPlugin.error("Error retrieving procedure source.", e);
                         } finally {
                             rs.close();
                         }
                     }
                 }
             } finally {
                 try {
                     stmt.close();
                 } catch (Exception e) {
                     SQLExplorerPlugin.error("Error closing statement.", e);
                 }
             }

             if (script.length() == 0) {
                 return;
             }
             
             
             SQLEditorInput input = new SQLEditorInput("SQL Editor (" + SQLExplorerPlugin.getDefault().getNextElement()
                     + ").sql");
             input.setSessionNode(_selectedNodes[0].getSession());
             IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();

             SQLEditor editorPart = (SQLEditor) page.openEditor((IEditorInput) input,
                     "net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");
             editorPart.setText(script.toString());
             
         } catch (Throwable e) {
             SQLExplorerPlugin.error("Error creating export script", e);
         }	
	}

	
}
