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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;

public class SQLExecution {

    private class LocalThread extends Thread {

        public void run() {

            final long startTime = System.currentTimeMillis();
            
            try {

                _stmt = _session.getConnection().createStatement();
                _stmt.setMaxRows(_maxRows);

                boolean b = _stmt.execute(_sqlStatement);

                if (b) {

                    final ResultSet rs = _stmt.getResultSet();
                    if (rs != null) {

                        // create new dataset from results
                        DataSet dataSet = new DataSet(null, rs, null);
                        final long endTime = System.currentTimeMillis();

                        // update sql result
                        _sqlResult.setDataSet(dataSet);
                        _sqlResult.setExecutionTimeMillis(endTime - startTime);

                        // save successfull query
                        SQLExplorerPlugin.getDefault().getSQLHistory().addSQL(_sqlStatement, _session.toString());

                        _stmt.close();

                        // show results..
                        displayResults();

                        // update text on editor
                        _composite.getDisplay().asyncExec(new Runnable() {

                            public void run() {
                                String message = Messages.getString("SQLEditor.TotalTime.Prefix") + " " + (int) (endTime - startTime) + " "
                                + Messages.getString("SQLEditor.TotalTime.Postfix");
                                if (_editor != null) {
                                    _editor.setMessage(message);
                                }                         
                            }
                        }
                        );
                    }

                    _stmt.close();

                } else {

                    final long endTime = System.currentTimeMillis();
                    final int updateCount = _stmt.getUpdateCount();
                                                           
                    // update text on editor
                    _composite.getDisplay().asyncExec(new Runnable() {

                        public void run() {
                            String message = "" + updateCount + " " + Messages.getString("SQLEditor.Update.Prefix") + " " + (int) (endTime - startTime) + " "
                            + Messages.getString("SQLEditor.Update.Postfix");
                            if (_editor != null) {
                                _editor.setMessage(message);
                            }
                            
                            // close tab
                            _parentTab.dispose();
                        }
                    }
                    );
                    
                    _stmt.close();

                    // save successfull query
                    SQLExplorerPlugin.getDefault().getSQLHistory().addSQL(_sqlStatement, _session.toString());

                }

                _stmt = null;


                
            } catch (final Exception e) {

                SQLExplorerPlugin.error("Error executing statement.", e);

                if (_stmt != null) {
                    try {
                        _stmt.close();
                    } catch (Exception e1) {
                        SQLExplorerPlugin.error("Error closing statement.", e);
                    }
                }

                _stmt = null;

                final Shell shell = _resultsView.getSite().getShell();
                shell.getDisplay().asyncExec(new Runnable() {

                    public void run() {

                        clearCanvas();
                        MessageDialog.openError(shell, Messages.getString("SQLResultsView.Error.Title"), e.getMessage());
                        if (_parentTab != null) {
                            _parentTab.dispose();
                        }
                    }
                });

            }

        }
    }

    protected Composite _composite;

    protected int _maxRows;

    protected TabItem _parentTab;

    protected SqlResultsView _resultsView;

    protected SessionTreeNode _session;

    protected LocalThread _sqlExecutionThread;

    protected SQLResult _sqlResult;

    protected String _sqlStatement;

    protected Statement _stmt;

    protected SQLEditor _editor;

    public SQLExecution(SQLEditor editor, SqlResultsView resultsView, String sqlString, int maxRows, SessionTreeNode sessionTreeNode) {

        _editor = editor;
        _sqlStatement = sqlString;
        _maxRows = maxRows;
        _session = sessionTreeNode;
        _resultsView = resultsView;
    }


    /**
     * Clear progress bar or results.
     */
    protected void clearCanvas() {

        // restore correct label
        _parentTab.setText((String) _parentTab.getData("tabLabel"));

        Control[] children = _composite.getChildren();

        if (children != null) {

            for (int i = 0; i < children.length; i++) {
                children[i].dispose();
            }
        }

    }


    /**
     * Display progress bar on tab until results are ready.
     */
    protected void displayProgress() {

        clearCanvas();

        // set label to running
        _parentTab.setText(Messages.getString("SQLResultsView.Running"));

        GridLayout gLayout = new GridLayout();
        gLayout.numColumns = 2;
        gLayout.marginLeft = 0;
        gLayout.horizontalSpacing = 0;
        gLayout.verticalSpacing = 0;
        gLayout.marginWidth = 0;
        gLayout.marginHeight = 50;
        _composite.setLayout(gLayout);

        Group group = new Group(_composite, SWT.NULL);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.getString("SQLResultsView.Executing"));

        // add progress bar
        Composite pbComposite = new Composite(group, SWT.FILL);
        FillLayout pbLayout = new FillLayout();
        pbLayout.marginHeight = 2;
        pbLayout.marginWidth = 5;
        pbComposite.setLayout(pbLayout);
        pbComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ProgressBar pb = new ProgressBar(pbComposite, SWT.HORIZONTAL | SWT.INDETERMINATE | SWT.BORDER);
        pb.setVisible(true);
        pb.setEnabled(true);

        pbComposite.layout();
        _composite.layout();

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
                    String statusMessage = Messages.getString("SQLResultsView.Time.Prefix") + " "
                            + _sqlResult.getExecutionTimeMillis() + " "
                            + Messages.getString("SQLResultsView.Time.Postfix");
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

            };
        });
    }



    /**
     * @return Returns the sqlStatement.
     */
    public String getSqlStatement() {

        return _sqlStatement;
    }


    public void setComposite(Composite composite) {

        _composite = composite;
    }


    public void setParentTab(TabItem parentTab) {

        _parentTab = parentTab;
    }


    /**
     * Start exection of sql statement
     */
    public void startExecution() {

        // start progress bar
        displayProgress();

        // start sql in seperate thread
        _sqlResult = new SQLResult();
        _sqlResult.setSqlStatement(_sqlStatement);
        _sqlExecutionThread = new LocalThread();
        _sqlExecutionThread.start();

    }


    /**
     * Cancel sql execution and close execution tab.
     */
    public void stop() {

        if (_stmt != null) {

            try {
                _stmt.cancel();
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error cancelling statement.", e);
            }
            try {
                _stmt.close();
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error closing statement.", e);
            }
        }

        if (_sqlExecutionThread != null && _sqlExecutionThread.isAlive()) {
            _sqlExecutionThread.interrupt();
        }

    }

}
