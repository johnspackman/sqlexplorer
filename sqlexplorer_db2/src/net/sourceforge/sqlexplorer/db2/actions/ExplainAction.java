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
package net.sourceforge.sqlexplorer.db2.actions;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.URLUtil;
import net.sourceforge.sqlexplorer.db2.actions.explain.ExplainExecution;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.SqlResultsView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction;
import net.sourceforge.sqlexplorer.util.QueryTokenizer;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Davy Vanherbergen
 * 
 */
public class ExplainAction extends AbstractEditorAction {

    private static ImageDescriptor _image = ImageDescriptor.createFromURL(URLUtil.getFragmentURL("net.sourceforge.sqlexplorer.db2", "icons/explain.gif"));


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
        return "Show DB2 Explain Plan";
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
            Statement st = runNode.getConnection().createStatement();
            boolean createPlanTable = false;
            boolean notFoundTable = true;
            try {
                ResultSet rs = st.executeQuery("select queryno from SYSTOOLS.EXPLAIN_STATEMENT");
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
                st = runNode.getConnection().createStatement();
                try {
                    st.execute(createPlanScript1);
                    st.execute(createPlanScript2);
                    st.execute(createPlanScript3);
                    st.execute(createPlanScript4);
                    st.execute(createPlanScript5);
                    st.execute(createPlanScript6);
                    st.execute(createPlanScript7);

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
                    resultsView.addSQLExecution(new ExplainExecution(_editor, resultsView, querySql, 0, runNode));
                }
            }

        } catch (Exception e) {
            SQLExplorerPlugin.error("Error creating sql execution tab", e);
        }
    }
    
 
    static final String createPlanScript1 = "CREATE TABLE SYSTOOLS.EXPLAIN_INSTANCE ( EXPLAIN_REQUESTER VARCHAR(128) NOT NULL, "
                                + "EXPLAIN_TIME      TIMESTAMP    NOT NULL, "
                                + "SOURCE_NAME       VARCHAR(128) NOT NULL, "
                                + "SOURCE_SCHEMA     VARCHAR(128) NOT NULL, "
                                + "SOURCE_VERSION    VARCHAR(64)  NOT NULL, "
                                + "EXPLAIN_OPTION    CHAR(1)      NOT NULL, "
                                + "SNAPSHOT_TAKEN    CHAR(1)   NOT NULL, "
                                + "DB2_VERSION       CHAR(7)   NOT NULL, "
                                + "SQL_TYPE          CHAR(1)   NOT NULL, "
                                + "QUERYOPT          INTEGER   NOT NULL, "
                                + "BLOCK             CHAR(1)   NOT NULL, "
                                + "ISOLATION         CHAR(2)   NOT NULL, "
                                + "BUFFPAGE          INTEGER   NOT NULL, "
                                + "AVG_APPLS         INTEGER   NOT NULL, "
                                + "SORTHEAP          INTEGER   NOT NULL, "
                                + "LOCKLIST          INTEGER   NOT NULL, "
                                + "MAXLOCKS          SMALLINT  NOT NULL, "
                                + "LOCKS_AVAIL       INTEGER   NOT NULL, "
                                + "CPU_SPEED         DOUBLE    NOT NULL, "
                                + "REMARKS           VARCHAR(254), "
                                + "DBHEAP            INTEGER   NOT NULL, "
                                + "COMM_SPEED        DOUBLE    NOT NULL, "
                                + "PARALLELISM       CHAR(2)   NOT NULL, "
                                + "DATAJOINER        CHAR(1)   NOT NULL, "
                                + "PRIMARY KEY (EXPLAIN_REQUESTER, EXPLAIN_TIME, SOURCE_NAME,  SOURCE_SCHEMA, SOURCE_VERSION));";
    
    static final String createPlanScript2 = "CREATE TABLE SYSTOOLS.EXPLAIN_STATEMENT ( EXPLAIN_REQUESTER VARCHAR(128) NOT NULL, "
        + "EXPLAIN_TIME      TIMESTAMP    NOT NULL, "
        + "SOURCE_NAME       VARCHAR(128) NOT NULL, "
        + "SOURCE_SCHEMA     VARCHAR(128) NOT NULL, "
        + " SOURCE_VERSION    VARCHAR(64)  NOT NULL, "
        + " EXPLAIN_LEVEL     CHAR(1)      NOT NULL, "
        + "  STMTNO            INTEGER      NOT NULL, "
        + "  SECTNO            INTEGER      NOT NULL, "
        + "  QUERYNO           INTEGER      NOT NULL, "
        + "                      QUERYTAG          CHAR(20)     NOT NULL, "
        + "                      STATEMENT_TYPE    CHAR(2)      NOT NULL, "
        + "                      UPDATABLE         CHAR(1)      NOT NULL, "
        + "                      DELETABLE         CHAR(1)      NOT NULL, "
        + "                      TOTAL_COST        DOUBLE       NOT NULL, "
        + "                      STATEMENT_TEXT    CLOB(2M)     NOT NULL NOT LOGGED, "
        + "                      SNAPSHOT          BLOB(10M)    NOT LOGGED, "
        + "                      QUERY_DEGREE      INTEGER      NOT NULL, "
        + "                         PRIMARY KEY (EXPLAIN_REQUESTER, "
        + "                                      EXPLAIN_TIME, "
        + "                                      SOURCE_NAME, "
        + "                                      SOURCE_SCHEMA, "
        + "                                   SOURCE_VERSION, "
        + "                                      EXPLAIN_LEVEL, "
        + "                                      STMTNO, "
        + "                                      SECTNO), "
        + "                          FOREIGN KEY (EXPLAIN_REQUESTER, "
        + "                                       EXPLAIN_TIME, "
        + "                                       SOURCE_NAME, "
        + "                                       SOURCE_SCHEMA, "
        + "                                       SOURCE_VERSION) "
        + "                          REFERENCES SYSTOOLS.EXPLAIN_INSTANCE "
        + "                          ON DELETE CASCADE);";
    static final String createPlanScript3 = "CREATE TABLE SYSTOOLS.EXPLAIN_ARGUMENT ( EXPLAIN_REQUESTER   VARCHAR(128)  NOT NULL, "
        + "       EXPLAIN_TIME        TIMESTAMP     NOT NULL, "
        + "                     SOURCE_NAME         VARCHAR(128)  NOT NULL, "
        + "                     SOURCE_SCHEMA       VARCHAR(128)  NOT NULL, "
        + "                     SOURCE_VERSION      VARCHAR(64)   NOT NULL, "
        + "                     EXPLAIN_LEVEL       CHAR(1)       NOT NULL, "
        + "                     STMTNO              INTEGER       NOT NULL, "
        + "                     SECTNO              INTEGER       NOT NULL, "
        + "                     OPERATOR_ID         INTEGER       NOT NULL, "
        + "                     ARGUMENT_TYPE       CHAR(8)       NOT NULL, "
        + "                     ARGUMENT_VALUE      VARCHAR(1024), "
        + "                     LONG_ARGUMENT_VALUE CLOB(2M)      NOT LOGGED, "
        + "                        FOREIGN KEY (EXPLAIN_REQUESTER, "
        + "                                     EXPLAIN_TIME, "
        + "                                     SOURCE_NAME, "
        + "                                     SOURCE_SCHEMA, "
        + "                                     SOURCE_VERSION, "
        + "                                     EXPLAIN_LEVEL, "
        + "                                     STMTNO, "
        + "                                     SECTNO) "
        + "                        REFERENCES SYSTOOLS.EXPLAIN_STATEMENT "
        + "                        ON DELETE CASCADE);";
    static final String createPlanScript4 = "CREATE TABLE SYSTOOLS.EXPLAIN_OBJECT ( EXPLAIN_REQUESTER    VARCHAR(128) NOT NULL, "
        + "                    EXPLAIN_TIME         TIMESTAMP    NOT NULL, "
        + "                   SOURCE_NAME          VARCHAR(128) NOT NULL, "
        + "                   SOURCE_SCHEMA        VARCHAR(128) NOT NULL, "
        + "                   SOURCE_VERSION       VARCHAR(64)  NOT NULL, "
        + "                   EXPLAIN_LEVEL        CHAR(1)      NOT NULL, "
        + "                   STMTNO               INTEGER      NOT NULL, "
        + "                   SECTNO               INTEGER      NOT NULL, "
        + "                   OBJECT_SCHEMA        VARCHAR(128) NOT NULL, "
        + "                   OBJECT_NAME          VARCHAR(128) NOT NULL, "
        + "                   OBJECT_TYPE          CHAR(2)      NOT NULL, "
        + "                   CREATE_TIME          TIMESTAMP, "
        + "                   STATISTICS_TIME      TIMESTAMP, "
        + "                   COLUMN_COUNT         SMALLINT     NOT NULL, "
        + "                   ROW_COUNT            BIGINT       NOT NULL, "
        + "                   WIDTH                INTEGER      NOT NULL, "
        + "                   PAGES                INTEGER      NOT NULL, "
        + "                   DISTINCT             CHAR(1)      NOT NULL, "
        + "                   TABLESPACE_NAME      VARCHAR(128), "
        + "                   OVERHEAD             DOUBLE       NOT NULL, "
        + "                   TRANSFER_RATE        DOUBLE       NOT NULL, "
        + "                   PREFETCHSIZE         INTEGER      NOT NULL, "
        + "                   EXTENTSIZE           INTEGER      NOT NULL, "
        + "                   CLUSTER              DOUBLE       NOT NULL, "
        + "                   NLEAF                INTEGER      NOT NULL, "
        + "                   NLEVELS              INTEGER      NOT NULL, "
        + "                   FULLKEYCARD          BIGINT       NOT NULL, "
        + "                   OVERFLOW             INTEGER      NOT NULL, "
        + "                   FIRSTKEYCARD         BIGINT       NOT NULL, "
        + "                   FIRST2KEYCARD        BIGINT       NOT NULL, "
        + "                   FIRST3KEYCARD        BIGINT       NOT NULL, "
        + "                   FIRST4KEYCARD        BIGINT       NOT NULL, "
        + "                   SEQUENTIAL_PAGES     INTEGER      NOT NULL, "
        + "                   DENSITY              INTEGER      NOT NULL, "
        + "                   STATS_SRC            CHAR(1)      NOT NULL, "
        + "                   AVERAGE_SEQUENCE_GAP          DOUBLE  NOT NULL, "
        + "                   AVERAGE_SEQUENCE_FETCH_GAP    DOUBLE  NOT NULL, "
        + "                   AVERAGE_SEQUENCE_PAGES        DOUBLE  NOT NULL, "
        + "                   AVERAGE_SEQUENCE_FETCH_PAGES  DOUBLE  NOT NULL, "
        + "                   AVERAGE_RANDOM_PAGES          DOUBLE  NOT NULL, "
        + "                   AVERAGE_RANDOM_FETCH_PAGES    DOUBLE  NOT NULL, "
        + "                   NUMRIDS                       BIGINT  NOT NULL, "
        + "                   NUMRIDS_DELETED               BIGINT  NOT NULL, "
        + "                   NUM_EMPTY_LEAFS               BIGINT  NOT NULL, "
        + "                   ACTIVE_BLOCKS                 BIGINT  NOT NULL, "
        + "                        FOREIGN KEY (EXPLAIN_REQUESTER, "
        + "                                     EXPLAIN_TIME, "
        + "                                     SOURCE_NAME, "
        + "                                     SOURCE_SCHEMA, "
        + "                                     SOURCE_VERSION, "
        + "                                     EXPLAIN_LEVEL, "
        + "                                     STMTNO, "
        + "                                     SECTNO) "
        + "                        REFERENCES SYSTOOLS.EXPLAIN_STATEMENT "
        + "                        ON DELETE CASCADE);";
    static final String createPlanScript5 = "CREATE TABLE SYSTOOLS.EXPLAIN_OPERATOR ( EXPLAIN_REQUESTER VARCHAR(128) NOT NULL, "
        + "                     EXPLAIN_TIME      TIMESTAMP    NOT NULL, "
        + "                     SOURCE_NAME       VARCHAR(128) NOT NULL, "
        + "                     SOURCE_SCHEMA     VARCHAR(128) NOT NULL, "
        + "                     SOURCE_VERSION    VARCHAR(64)  NOT NULL, "
        + "                     EXPLAIN_LEVEL     CHAR(1)      NOT NULL, "
        + "                     STMTNO            INTEGER      NOT NULL, "
        + "                     SECTNO            INTEGER      NOT NULL, "
        + "                     OPERATOR_ID       INTEGER      NOT NULL, "
        + "                     OPERATOR_TYPE     CHAR(6)      NOT NULL, "
        + "                     TOTAL_COST        DOUBLE       NOT NULL, "
        + "                     IO_COST           DOUBLE       NOT NULL, "
        + "                     CPU_COST          DOUBLE       NOT NULL, "
        + "                     FIRST_ROW_COST    DOUBLE       NOT NULL, "
        + "                     RE_TOTAL_COST     DOUBLE       NOT NULL, "
        + "                     RE_IO_COST        DOUBLE       NOT NULL, "
        + "                     RE_CPU_COST       DOUBLE       NOT NULL, "
        + "                     COMM_COST         DOUBLE       NOT NULL, "
        + "                     FIRST_COMM_COST   DOUBLE       NOT NULL, "
        + "                     BUFFERS           DOUBLE       NOT NULL, "
        + "                     REMOTE_TOTAL_COST DOUBLE       NOT NULL, "
        + "                     REMOTE_COMM_COST  DOUBLE       NOT NULL, "
        + "                        FOREIGN KEY (EXPLAIN_REQUESTER, "
        + "                                     EXPLAIN_TIME, "
        + "                                     SOURCE_NAME, "
        + "                                     SOURCE_SCHEMA, "
        + "                                     SOURCE_VERSION, "
        + "                                     EXPLAIN_LEVEL, "
        + "                                     STMTNO, "
        + "                                     SECTNO) "
        + "                        REFERENCES SYSTOOLS.EXPLAIN_STATEMENT "
        + "                        ON DELETE CASCADE);";
    static final String createPlanScript6 = "CREATE TABLE SYSTOOLS.EXPLAIN_PREDICATE ( EXPLAIN_REQUESTER VARCHAR(128) NOT NULL, "
        + "                      EXPLAIN_TIME      TIMESTAMP    NOT NULL, "
        + "                      SOURCE_NAME       VARCHAR(128) NOT NULL, "
        + "                      SOURCE_SCHEMA     VARCHAR(128) NOT NULL, "
        + "                      SOURCE_VERSION    VARCHAR(64)  NOT NULL, "
        + "                      EXPLAIN_LEVEL     CHAR(1)      NOT NULL, "
        + "                      STMTNO            INTEGER      NOT NULL, "
        + "                      SECTNO            INTEGER      NOT NULL, "
        + "                      OPERATOR_ID       INTEGER      NOT NULL, "
        + "                      PREDICATE_ID      INTEGER      NOT NULL, "
        + "                      HOW_APPLIED       CHAR(5)      NOT NULL, "
        + "                      WHEN_EVALUATED    CHAR(3)      NOT NULL, "
        + "                      RELOP_TYPE        CHAR(2)      NOT NULL, "
        + "                      SUBQUERY          CHAR(1)      NOT NULL, "
        + "                      FILTER_FACTOR     DOUBLE       NOT NULL, "
        + "                      PREDICATE_TEXT    CLOB(2M)     NOT LOGGED, "
        + "                        FOREIGN KEY (EXPLAIN_REQUESTER, "
        + "                                     EXPLAIN_TIME, "
        + "                                     SOURCE_NAME, "
        + "                                     SOURCE_SCHEMA, "
        + "                                     SOURCE_VERSION, "
        + "                                     EXPLAIN_LEVEL, "
        + "                                     STMTNO, "
        + "                                     SECTNO) "
        + "                        REFERENCES SYSTOOLS.EXPLAIN_STATEMENT "
        + "                        ON DELETE CASCADE);";
    static final String createPlanScript7 = "CREATE TABLE SYSTOOLS.EXPLAIN_STREAM ( EXPLAIN_REQUESTER VARCHAR(128) NOT NULL, "
        + "                     EXPLAIN_TIME      TIMESTAMP    NOT NULL, "
        + "                   SOURCE_NAME       VARCHAR(128) NOT NULL, "
        + "                   SOURCE_SCHEMA     VARCHAR(128) NOT NULL, "
        + "                   SOURCE_VERSION    VARCHAR(64)  NOT NULL, "
        + "                   EXPLAIN_LEVEL     CHAR(1)      NOT NULL, "
        + "                   STMTNO            INTEGER      NOT NULL, "
        + "                   SECTNO            INTEGER      NOT NULL, "
        + "                   STREAM_ID         INTEGER      NOT NULL, "
        + "                   SOURCE_TYPE       CHAR(1)      NOT NULL, "
        + "                   SOURCE_ID         INTEGER      NOT NULL, "
        + "                   TARGET_TYPE       CHAR(1)      NOT NULL, "
        + "                   TARGET_ID         INTEGER      NOT NULL, "
        + "                   OBJECT_SCHEMA     VARCHAR(128), "
        + "                   OBJECT_NAME       VARCHAR(128), "
        + "                   STREAM_COUNT      DOUBLE       NOT NULL, "
        + "                   COLUMN_COUNT      SMALLINT     NOT NULL, "
        + "                   PREDICATE_ID      INTEGER      NOT NULL, "
        + "                   COLUMN_NAMES      CLOB(2M)     NOT LOGGED, "
        + "                    PMID              SMALLINT     NOT NULL, "
        + "               SINGLE_NODE       CHAR(5), "
        + "                   PARTITION_COLUMNS CLOB(2M)     NOT LOGGED, "
        + "                        FOREIGN KEY (EXPLAIN_REQUESTER, "
        + "                                     EXPLAIN_TIME, "
        + "                                    SOURCE_NAME, "
        + "                                 SOURCE_SCHEMA, "
        + "                                     SOURCE_VERSION, "
        + "                                     EXPLAIN_LEVEL, "
        + "                                     STMTNO, "
        + "                                     SECTNO) "
        + "                        REFERENCES SYSTOOLS.EXPLAIN_STATEMENT "
        + "                        ON DELETE CASCADE);";
    
  
    
}
    