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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dataset.DataSetTable;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.ResultsTab;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Executes one or more SQL statements in sequence, displaying the results in tabs
 * and/or in the Messages tab
 * @modified John Spackman
 *
 */
public class SQLExecution extends AbstractSQLExecution {

	// Maximum number of rows to return
    protected int _maxRows;

    // The Statement being used to execute the current query
    protected Statement _stmt;


    /**
     * Constructor
     * @param _editor
     * @param queryParser
     * @param maxRows
     * @param _session
     */
    public SQLExecution(SQLEditor _editor, QueryParser queryParser, int maxRows, SessionTreeNode _session) {
		super(_editor, queryParser, _session);
    	
        _maxRows = maxRows;
	}


	/**
     * Display SQL Results in result pane
     * @param sqlResult the results of the query
     */
    protected void displayResults(final SQLResult sqlResult) {

    	// Switch to the UI thread to execute this
    	getEditor().getSite().getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {
            	
            	ResultsTab resultsTab = allocateResultsTab(sqlResult.getQuery());

                try {
                    // set initial message
                    setProgressMessage(Messages.getString("SQLResultsView.ConnectionWait"));
                    
                	Composite composite = resultsTab.getParent();

                    GridLayout gLayout = new GridLayout();
                    gLayout.numColumns = 2;
                    gLayout.marginLeft = 0;
                    gLayout.horizontalSpacing = 0;
                    gLayout.verticalSpacing = 0;
                    gLayout.marginWidth = 0;
                    gLayout.marginHeight = 0;
                    composite.setLayout(gLayout);

                    int resultCount = sqlResult.getDataSet().getRows().length;
                    String statusMessage = Messages.getString("SQLResultsView.Time.Prefix") + " "
                            + sqlResult.getExecutionTimeMillis() + " "
                            + Messages.getString("SQLResultsView.Time.Postfix");
                    getEditor().setMessage(statusMessage);
                    
                    if (resultCount > 0)
                        statusMessage = statusMessage + "  " + Messages.getString("SQLResultsView.Count.Prefix") + " " + resultCount;

                    Query sql = sqlResult.getQuery();
                    int lineNo = sql.getLineNo();
                    lineNo = getQueryParser().adjustLineNo(lineNo);
                    getEditor().addMessage(new SQLEditor.Message(true, lineNo, 0, sql.getQuerySql(), statusMessage));
                    new DataSetTable(composite, sqlResult.getDataSet(), statusMessage);

                    composite.setData("parenttab", resultsTab.getTabItem());

                    composite.layout();
                    composite.redraw();

                    // reset to start message in case F5 will be used
                    setProgressMessage(Messages.getString("SQLResultsView.ConnectionWait"));

                } catch (Exception e) {

                    // add message
                	if (resultsTab != null) {
	                    String message = e.getMessage();
	                    Label errorLabel = new Label(resultsTab.getParent(), SWT.FILL);
	                    errorLabel.setText(message);
	                    errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
                	}
                    SQLExplorerPlugin.error("Error creating result tab", e);
                }
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
        int numErrors = 0;
        SQLException lastSQLException = null;

        try {
        	for (Query query : getQueryParser()) {
            	if (_isCancelled)
            		break;
            	
            	// Get the next bit of SQL to run and store it as "current"
            	if (query == null)
            		break;
            	String querySQL = query.getQuerySql().toString();
            	if (querySQL == null)
            		continue;

            	// Initialise
	            setProgressMessage(Messages.getString("SQLResultsView.Executing"));
	            _stmt = _connection.createStatement();
	            _stmt.setMaxRows(_maxRows);
	
	            if (_isCancelled)
	                return;
	            
	            try {
		            boolean hasResults = _stmt.execute(querySQL);
		
		            if (_isCancelled) {
		                closeStatement();
		                return;
		            }
		            
		            if (hasResults) {
		                final ResultSet rs = _stmt.getResultSet();
		                if (rs != null) {
		                    if (_isCancelled) {
		                        closeStatement();
		                        return;
		                    }
		                    
		                    // create new dataset from results
		                    DataSet dataSet = _session.getDatabaseProduct().createDataSet(_session.getAlias(), rs);
		                    final long endTime = System.currentTimeMillis();

		                    // update sql result
		            		SQLResult sqlResult = new SQLResult();
		            		sqlResult.setQuery(query);
		                    sqlResult.setDataSet(dataSet);
		                    sqlResult.setExecutionTimeMillis(endTime - startTime);
		
		                    // save successfull query
		                    SQLExplorerPlugin.getDefault().getSQLHistory().addSQL(querySQL, _session.toString());
		
		                    closeStatement();
		
		                    if (_isCancelled) {
		                        return;
		                    }
		                    
		                    // show results..
		                    displayResults(sqlResult);
		                }

		            // No results, just an update count
		            } else {
		                final long endTime = System.currentTimeMillis();
		                final int updateCount = _stmt.getUpdateCount();
		
                        String message = "" + updateCount + " " + Messages.getString("SQLEditor.Update.Prefix") + " " + 
                    		(int) (endTime - startTime) + " " + Messages.getString("SQLEditor.Update.Postfix");
		                setMessage(message);
                        
                        Collection<SQLEditor.Message> messages = _session.getDatabaseProduct().getErrorMessages(_connection, query);
                        if (messages == null)
                        	messages = new LinkedList<SQLEditor.Message>();
                        else
                        	for (SQLEditor.Message msg : messages)
                        		msg.setLineNo(getQueryParser().adjustLineNo(msg.getLineNo()));
                        if (messages.size() == 0) {
	                        int lineNo = query.getLineNo();
	                        lineNo = getQueryParser().adjustLineNo(lineNo);
                        	messages.add(new SQLEditor.Message(true, lineNo, 0, query.getQuerySql(), message));
                        }
                        addMessages(messages);
		
		                closeStatement();
		
		                if (_isCancelled)
		                    return;
		                
		                // save successfull query
		                SQLExplorerPlugin.getDefault().getSQLHistory().addSQL(querySQL, _session.toString());
		            }
	
		            _stmt = null;
		            debugLogQuery(query, null);
	            }catch(SQLException e) {
		            debugLogQuery(query, e);
	                logException(e, querySQL);
	                closeStatement();
	            	boolean stopOnError = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.STOP_ON_ERROR);
	            	if (stopOnError)
	            		throw e;
	            	numErrors++;
	            	lastSQLException = e;
	            }
            }
        } catch (Exception e) {
            closeStatement();
            throw e;
        }
        if (numErrors == 1)
        	throw lastSQLException;
        else if (numErrors > 1)
            getEditor().getSite().getShell().getDisplay().asyncExec(new Runnable() {
                public void run() {
                	MessageDialog.openError(getEditor().getSite().getShell(), 
                			Messages.getString("SQLExecution.Error.Title"), 
                			Messages.getString("SQLExecution.Error.Message"));
                }
            });
    }


    /**
     * Cancel sql execution and close execution tab.
     */
    public void doStop() {

    	_isCancelled = true;
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
