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
package net.sourceforge.sqlexplorer.oracle.actions;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.oracle.actions.explain.ExplainExecution;
import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

import org.eclipse.jface.dialogs.MessageDialog;

/**
 * @author Davy Vanherbergen
 * 
 */
public class ExplainAction extends AbstractEditorAction {

    static final String createPlanTableScript = "CREATE TABLE PLAN_TABLE ("
            + "  STATEMENT_ID                    VARCHAR2(30)," + " TIMESTAMP                       DATE,"
            + "  REMARKS                         VARCHAR2(80)," + "  OPERATION                       VARCHAR2(30),"
            + "  OPTIONS                         VARCHAR2(30)," + "  OBJECT_NODE                     VARCHAR2(128),"
            + "  OBJECT_OWNER                    VARCHAR2(30)," + "  OBJECT_NAME                     VARCHAR2(30),"
            + "  OBJECT_INSTANCE                 NUMBER(38)," + "  OBJECT_TYPE                     VARCHAR2(30),"
            + "  OPTIMIZER                       VARCHAR2(255)," + "  SEARCH_COLUMNS                  NUMBER,"
            + "  ID                              NUMBER(38)," + "  PARENT_ID                       NUMBER(38),"
            + "  POSITION                        NUMBER(38)," + "  COST                            NUMBER(38),"
            + "  CARDINALITY                     NUMBER(38)," + "  BYTES                           NUMBER(38),"
            + "  OTHER_TAG                       VARCHAR2(255)," + "  PARTITION_START                 VARCHAR2(255),"
            + "  PARTITION_STOP                  VARCHAR2(255)," + "  PARTITION_ID                    NUMBER(38),"
            + "  OTHER                           LONG," + "  DISTRIBUTION                    VARCHAR2(30)" + ")";


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction#getText()
     */
    public String getText() {

        return Messages.getString("oracle.editor.actions.explain");
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction#getToolTipText()
     */
    public String getToolTipText() {

        return getText();
    }


    /* (non-Javadoc)
     * @see net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction#run()
     */
    public void run() {
        Session session = getSession();
        if (session == null)
            return;
        
    	SQLConnection connection = null;
    	Statement stmt = null;
    	ResultSet rs = null;
    	
        try {
        	connection = session.grabConnection();
            Statement st = connection.createStatement();
            boolean createPlanTable = false;
            boolean notFoundTable = true;
            try {
                rs = st.executeQuery("select statement_id from plan_table");
                notFoundTable = false;
                rs.close();
                rs = null;
            } catch (SQLException e) {
                createPlanTable = MessageDialog.openQuestion(null,
                        Messages.getString("oracle.editor.actions.explain.notFound.Title"),
                        Messages.getString("oracle.editor.actions.explain.notFound"));
            }
            st.close();
            st = null;
            if (notFoundTable && !createPlanTable) {
                return;
            }

            if (notFoundTable && createPlanTable) {
                st = connection.createStatement();
	            st.execute(createPlanTableScript);
                st.close();
                st = null;
            }

            // execute explain plan for all statements
            QueryParser qt = session.getDatabaseProduct().getQueryParser(_editor.getSQLToBeExecuted(), _editor.getSQLLineNumber());
            qt.parse();
            new ExplainExecution(_editor, qt).schedule();
            
        } catch (SQLException e) {
            SQLExplorerPlugin.error("Error creating explain plan", e);
        }catch(ParserException e) {
        	SQLExplorerPlugin.error("Cannot parse query", e);
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
    };
}
