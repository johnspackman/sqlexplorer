/*
 * Copyright (C) 2006 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.mysql.actions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Generates a create table script for the selected nodes in the editor.
 * 
 */
public class CreateTableScriptAction extends AbstractDBTreeContextAction {

    public CreateTableScriptAction() {

    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#getText()
     */
    public String getText() {

        return Messages.getString("mysql.DatabaseStructureView.Action.CreateTableScript");
    }


    /**
     * Action is availble when a node is selected
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction#isAvailable()
     */
    public boolean isAvailable() {

        if (_selectedNodes.length != 0) {
            return true;
        }
        return false;
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {

    	Session session = _selectedNodes[0].getSession();
    	SQLConnection connection = null;
    	Statement stmt = null;
    	ResultSet rs = null;
    	
        try {
        	connection = session.grabConnection();

            StringBuffer script = new StringBuffer();
            String queryDelimiter = SQLExplorerPlugin.getStringPref(
                    IConstants.SQL_QRY_DELIMITER);

            stmt = connection.createStatement();
            for (int i = 0; i < _selectedNodes.length; i++) {
                if (_selectedNodes[i].getType().equalsIgnoreCase("table")) {
                    rs = stmt.executeQuery("show create table " + _selectedNodes[i].getQualifiedName());
                    if (rs.next()) {
                        script.append(rs.getString(2)).append(queryDelimiter).append('\n');
                    }
                    rs.close();
                    rs = null;
                }
            }

            if (script.length() == 0) {
                return;
            }

            SQLEditorInput input = new SQLEditorInput("SQL Editor (" + SQLExplorerPlugin.getDefault().getEditorSerialNo()
                    + ").sql");
            input.setUser(_selectedNodes[0].getSession().getUser());
            IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();

            SQLEditor editorPart = (SQLEditor) page.openEditor((IEditorInput) input,
                    "net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");
            editorPart.setText(script.toString());

        } catch (SQLException e) {
            SQLExplorerPlugin.error("Error creating export script", e);
        } catch (PartInitException e) {
            SQLExplorerPlugin.error("Error creating export script", e);
        } finally {
        	if (rs != null)
        		try {
        			rs.close();
        		} catch(SQLException e) {
        			SQLExplorerPlugin.error("Cannot close result set", e);
        		}
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
