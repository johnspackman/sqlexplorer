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
package net.sourceforge.sqlexplorer.sqlpanel;

import java.sql.ResultSet;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dataset.DataSetTable;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.views.SqlResultsView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;

public class SQLExecution extends AbstractSQLExecution {

    protected int _maxRows;
    
    protected SQLResult _sqlResult;

    protected Statement _stmt;


    public SQLExecution(SQLEditor editor, SqlResultsView resultsView, String sqlString, int maxRows,
            SessionTreeNode sessionTreeNode) {

        _editor = editor;
        _sqlStatement = sqlString;
        _maxRows = maxRows;
        _session = sessionTreeNode;
        _resultsView = resultsView;
        _sqlResult = new SQLResult();
        _sqlResult.setSqlStatement(_sqlStatement);
        
        // set initial message
        setProgressMessage(Messages.getString("SQLResultsView.ConnectionWait"));
        
    }


    /**
     * Display SQL Results in result pane
     */
    protected void displayResults() {

        _resultsView.getSite().getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {

                clearCanvas();

                GridLayout gLayout = new GridLayout();
                gLayout.numColumns = 2;
                gLayout.marginLeft = 0;
                gLayout.horizontalSpacing = 0;
                gLayout.verticalSpacing = 0;
                gLayout.marginWidth = 0;
                gLayout.marginHeight = 0;
                _composite.setLayout(gLayout);

                try {
                    int resultCount = _sqlResult.getDataSet().getRows().length;
                    String statusMessage = Messages.getString("SQLResultsView.Time.Prefix") + " "
                            + _sqlResult.getExecutionTimeMillis() + " "
                            + Messages.getString("SQLResultsView.Time.Postfix");
                    
                    if (resultCount > 0) {
                        statusMessage = statusMessage + "  " 
                        + Messages.getString("SQLResultsView.Count.Prefix") + " " + resultCount;
                    }
                    new DataSetTable(_composite, _sqlResult.getDataSet(), statusMessage);

                    _composite.setData("parenttab", _parentTab);

                } catch (Exception e) {

                    // add message
                    String message = e.getMessage();
                    Label errorLabel = new Label(_composite, SWT.FILL);
                    errorLabel.setText(message);
                    errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

                    SQLExplorerPlugin.error("Error creating result tab", e);
                }

                _composite.layout();
                _composite.redraw();

                // reset to start message in case F5 will be used
                setProgressMessage(Messages.getString("SQLResultsView.ConnectionWait"));
            };
        });
    }


    private void closeStatement() {
        
        if (_stmt == null) {
            return;
        }
        if (_stmt != null) {
            try {
                _stmt.close();
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error closing statement.", e);
            }
        }
        _stmt = null;
        
    }
    
    protected void doExecution() throws Exception {

        final long startTime = System.currentTimeMillis();

        try {
            
            _stmt = _connection.createStatement();
            
            setProgressMessage(Messages.getString("SQLResultsView.Executing"));
            
            _stmt.setMaxRows(_maxRows);

            if (_isCancelled) {
                return;
            }
            
            boolean b = _stmt.execute(_sqlStatement);

            if (_isCancelled) {
                closeStatement();
                return;
            }
            
            if (b) {

                final ResultSet rs = _stmt.getResultSet();
                if (rs != null) {

                    if (_isCancelled) {
                        closeStatement();
                        return;
                    }
                    
                    // create new dataset from results
                    DataSet dataSet = new DataSet(null, rs, null);
                    final long endTime = System.currentTimeMillis();

                    // update sql result
                    _sqlResult.setDataSet(dataSet);
                    _sqlResult.setExecutionTimeMillis(endTime - startTime);

                    // save successfull query
                    SQLExplorerPlugin.getDefault().getSQLHistory().addSQL(_sqlStatement, _session.toString());

                    closeStatement();

                    if (_isCancelled) {
                        return;
                    }
                    
                    // show results..
                    displayResults();

                    // update text on editor
                    _composite.getDisplay().asyncExec(new Runnable() {

                        public void run() {

                            String message = Messages.getString("SQLEditor.TotalTime.Prefix") + " "
                                    + (int) (endTime - startTime) + " "
                                    + Messages.getString("SQLEditor.TotalTime.Postfix");
                            if (_editor != null) {
                                _editor.setMessage(message);
                            }
                        }
                    });
                }

            } else {

                final long endTime = System.currentTimeMillis();
                final int updateCount = _stmt.getUpdateCount();

                // update text on editor
                _composite.getDisplay().asyncExec(new Runnable() {

                    public void run() {

                        String message = "" + updateCount + " " + Messages.getString("SQLEditor.Update.Prefix") + " "
                                + (int) (endTime - startTime) + " " + Messages.getString("SQLEditor.Update.Postfix");
                        if (_editor != null) {
                            _editor.setMessage(message);
                        }

                        // close tab
                        _parentTab.dispose();
                    }
                });

                closeStatement();

                if (_isCancelled) {
                    return;
                }
                
                // save successfull query
                SQLExplorerPlugin.getDefault().getSQLHistory().addSQL(_sqlStatement, _session.toString());

            }

            _stmt = null;

        } catch (Exception e) {

            closeStatement();
            throw e;
        }

    }


    /**
     * Cancel sql execution and close execution tab.
     */
    public void doStop() {

        if (_stmt != null) {

            try {
                _stmt.cancel();
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error cancelling statement.", e);
            }
            try {
                closeStatement();
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error closing statement.", e);
            }
        }

    }

}
