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

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.URLUtil;
import net.sourceforge.sqlexplorer.oracle.actions.explain.ExplainExecution;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.SqlResultsView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction;
import net.sourceforge.sqlexplorer.util.QueryTokenizer;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Davy Vanherbergen
 * 
 */
public class ExplainAction extends AbstractEditorAction {

    private static ImageDescriptor _image = ImageDescriptor.createFromURL(URLUtil.getFragmentResourceURL("net.sourceforge.sqlexplorer.oracle", "icons/explain.gif"));


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        
        return _image;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction#getText()
     */
    public String getText() {
        return "Show Explain Plan";
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction#getToolTipText()
     */
    public String getToolTipText() {
        return getText();
    }


    public void run() {
                
        SessionTreeNode runNode = _editor.getSessionTreeNode();
        if (runNode == null) {
            return;
        }

       
        Preferences prefs = SQLExplorerPlugin.getDefault().getPluginPreferences();

        String queryDelimiter = prefs.getString(IConstants.SQL_QRY_DELIMITER);
        String alternateDelimiter = prefs.getString(IConstants.SQL_ALT_QRY_DELIMITER);
        String commentDelimiter = prefs.getString(IConstants.SQL_COMMENT_DELIMITER);

        QueryTokenizer qt = new QueryTokenizer(_editor.getSQLToBeExecuted(), queryDelimiter, alternateDelimiter, commentDelimiter);
        final List queryStrings = new ArrayList();
        while (qt.hasQuery()) {
            final String querySql = qt.nextQuery();
            // ignore commented lines.
            if (!querySql.startsWith("--")) {
                queryStrings.add(querySql);
            }
        }

        // check if we can run explain plans
        try {
            Statement st = runNode.getInteractiveConnection().createStatement();
            boolean createPlanTable = false;
            boolean notFoundTable = true;
            try {
                ResultSet rs = st.executeQuery("select statement_id from plan_table");
                notFoundTable = false;
                rs.close();
            } catch (Throwable e) {
                createPlanTable = MessageDialog.openQuestion(null, "Plan Table not found",
                        "Plan table not found. Do you want to create a new one?");
            } finally {
                try {
                    st.close();
                } catch (Throwable e) {
                }
            }
            if (notFoundTable && !createPlanTable) {
                return;
            }
    
            if (notFoundTable && createPlanTable) {
                
                SQLConnection conn = runNode.getInteractiveConnection(); 
                st = conn.createStatement();
                
                try {
                    st.execute(createPlanTableScript);
                    
                    if (!conn.getAutoCommit()) {
                        conn.commit();
                    }
                    
                } catch (Throwable e) {
                    SQLExplorerPlugin.error("Error creating the plan table", e);
                    MessageDialog.openError(null, "Table not created",
                            "Error creating the plan table. It's impossible do create the explain plan.");
                    try {
                        st.close();
                    } catch (Throwable e1) {
                    }
                    return;
                }
                try {
                    st.close();
                } catch (Throwable e) {
                }
            }

                
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        // execute explain plan for all statements
        
        try {

            SqlResultsView resultsView = (SqlResultsView) _editor.getSite().getPage().showView(
                    "net.sourceforge.sqlexplorer.plugin.views.SqlResultsView");

            while (!queryStrings.isEmpty()) {

                String querySql = (String) queryStrings.remove(0);

                if (querySql != null) {
                    resultsView.addSQLExecution(new ExplainExecution(_editor, resultsView, querySql, runNode));
                }
            }

        } catch (Exception e) {
            SQLExplorerPlugin.error("Error creating sql execution tab", e);
        }
    }
    
 
    static final String createPlanTableScript = "CREATE TABLE PLAN_TABLE (" + "  STATEMENT_ID                    VARCHAR2(30),"
            + " TIMESTAMP                       DATE," + "  REMARKS                         VARCHAR2(80),"
            + "  OPERATION                       VARCHAR2(30)," + "  OPTIONS                         VARCHAR2(30),"
            + "  OBJECT_NODE                     VARCHAR2(128)," + "  OBJECT_OWNER                    VARCHAR2(30),"
            + "  OBJECT_NAME                     VARCHAR2(30)," + "  OBJECT_INSTANCE                 NUMBER(38),"
            + "  OBJECT_TYPE                     VARCHAR2(30)," + "  OPTIMIZER                       VARCHAR2(255),"
            + "  SEARCH_COLUMNS                  NUMBER," + "  ID                              NUMBER(38),"
            + "  PARENT_ID                       NUMBER(38)," + "  POSITION                        NUMBER(38),"
            + "  COST                            NUMBER(38)," + "  CARDINALITY                     NUMBER(38),"
            + "  BYTES                           NUMBER(38)," + "  OTHER_TAG                       VARCHAR2(255),"
            + "  PARTITION_START                 VARCHAR2(255)," + "  PARTITION_STOP                  VARCHAR2(255),"
            + "  PARTITION_ID                    NUMBER(38)," + "  OTHER                           LONG,"
            + "  DISTRIBUTION                    VARCHAR2(30)" + ")";;
}
