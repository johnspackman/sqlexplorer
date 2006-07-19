package net.sourceforge.sqlexplorer.sqleditor.actions;

/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.views.SqlResultsView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sqlpanel.SQLResult;
import net.sourceforge.sqlexplorer.util.QueryTokenizer;
import net.sourceforge.sqlexplorer.util.SQLString;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class SqlExecProgress implements IRunnableWithProgress {

    private class LocalThread extends Thread {

        boolean end = false;

        IProgressMonitor monitor;

        Statement stmt;


        public LocalThread(final IProgressMonitor monitor, final Statement stmt) {
            this.monitor = monitor;
            this.stmt = stmt;
        }


        public void endMonitor() {
            ;
            end = true;
        }


        public void run() {
            try {
                while (true) {

                    if (end)
                        break;
                    if (monitor.isCanceled()) {
                        stmt.cancel();
                    }
                    Thread.sleep(100);
                }
            } catch (Throwable e) {
            }

        }
    }

    private String _sql;

    Throwable exception;

    int maxRows;

    private SessionTreeNode sessionTreeNode;

    boolean sqlError;

    SQLEditor txtComp;


    public SqlExecProgress(String sqlString, SQLEditor txtComp, int maxRows, SessionTreeNode sessionTreeNode) {
        _sql = sqlString;
        this.txtComp = txtComp;
        this.maxRows = maxRows;
        this.sessionTreeNode = sessionTreeNode;

    }


    /**
     * @return
     */
    public Throwable getException() {
        return exception;
    }


    /**
     * @return
     */
    public boolean isSqlError() {
        return sqlError;
    }


    private SQLResult processQuery(String sql, final IProgressMonitor monitor) throws SQLException {

        final long startTime = System.currentTimeMillis();

        SQLResult result = new SQLResult();
        result.setSqlStatement(sql);
        
        LocalThread lt = null;
        final Statement stmt = sessionTreeNode.getConnection().createStatement();

        try {

            stmt.setMaxRows(maxRows);
            lt = new LocalThread(monitor, stmt);
            lt.start();
            
            boolean b = stmt.execute(sql);

            if (b) {
                
                final ResultSet rs = stmt.getResultSet();
                if (rs != null) {

                    // create new dataset from results
                    DataSet dataSet = new DataSet(null, rs, null);
                    long endTime = System.currentTimeMillis();
                    
                    // update sql result
                    result.setDataSet(dataSet);
                    result.setExecutionTimeMillis(endTime - startTime);
                    
                    lt.endMonitor();
                    
                    // save successfull query
                    SQLExplorerPlugin.getDefault().addSQLtoHistory(new SQLString(sql, sessionTreeNode.toString()));
                    
                    stmt.close();
                    
                    return result;
                }
                
                stmt.close();
                
            } else {
                
                lt.endMonitor();

                txtComp.getSite().getShell().getDisplay().asyncExec(new Runnable() {

                    public void run() {
                        try {
                            long endTime = System.currentTimeMillis();
                            String message = Messages.getString("Time__1") + " " + (int) (endTime - startTime) + Messages.getString("_ms");
                            txtComp.setMessage(message + Messages.getString("SqlExecProgress._updated_rowcount__5") + stmt.getUpdateCount());
                            stmt.close();
                        } catch (Throwable e) {
                            SQLExplorerPlugin.error("Error displaying data ", e);
                            txtComp.setMessage(e.getMessage());
                        }
                    };
                });

                // save successfull query
                SQLExplorerPlugin.getDefault().addSQLtoHistory(new SQLString(sql, sessionTreeNode.toString()));
            }

        } catch (final Throwable e) {
            
            if (stmt != null) {
                stmt.close();
            }
            
            if (!monitor.isCanceled()) {
                SQLExplorerPlugin.error("Error processing query ", e);
                exception = e;
                sqlError = true;
            }

            return null;
        } finally {
            lt.endMonitor();
        }
        return null;

    }


    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        Preferences prefs = SQLExplorerPlugin.getDefault().getPluginPreferences();

        String queryDelimiter = prefs.getString(IConstants.SQL_QRY_DELIMITER);
        String alternateDelimiter = prefs.getString(IConstants.SQL_ALT_QRY_DELIMITER);
        String commentDelimiter = prefs.getString(IConstants.SQL_COMMENT_DELIMITER);

        final long startTime = System.currentTimeMillis();

        QueryTokenizer qt = new QueryTokenizer(_sql, queryDelimiter, alternateDelimiter, commentDelimiter);
        final List queryStrings = new ArrayList();
        while (qt.hasQuery()) {
            final String querySql = qt.nextQuery();
            // ignore commented lines.
            if (!querySql.startsWith("--")) {
                queryStrings.add(querySql);
            }
        }

        txtComp.getSite().getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {
                try {

                    SqlResultsView resultsView = (SqlResultsView) txtComp.getSite().getPage().showView(
                            "net.sourceforge.sqlexplorer.plugin.views.SqlResultsView");

                    while (!queryStrings.isEmpty()) {

                        String querySql = (String) queryStrings.remove(0);
                        if (querySql != null) {
                            SQLResult result = processQuery(querySql, monitor);
                            if (result != null) {
                                resultsView.addSQLResult(result);
                            }
                        }
                    }

                    long endTime = System.currentTimeMillis();
                    String message = Messages.getString("SQLEditor.TotalTime.Prefix") + " " + (int) (endTime - startTime)
                            + " " +Messages.getString("SQLEditor.TotalTime.Postfix");
                    txtComp.setMessage(message);

                } catch (Throwable e) {

                    SQLExplorerPlugin.error("Error displaying data", e);
                    txtComp.setMessage(e.getMessage());

                }
            }
        });
    }

}
