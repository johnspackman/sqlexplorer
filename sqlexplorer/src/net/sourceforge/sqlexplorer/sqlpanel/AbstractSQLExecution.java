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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.ResultsTab;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.util.TextUtil;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Base class for SQL Executions.
 * 
 * The AbstractSQLExecution now operates on a QueryTokenizer directly instead of being passed the
 * SQL to execute; this is so that individual queries from a single SQLEditor can run synchronously -
 * this is essential for DDL queries. 
 * 
 * This has been decoupled slightly from SQLEditor (which is now refactored to include result tabs)
 * such that an AbstractSQLExecution can simply be told to run by calling startExecute().  The
 * constructor is given the SQLEditor instance that fired it off - and when a results tab is
 * required, SQLEditor.createResultsTab() is called to get one.  The old _composite and _parentTab
 * have been replaced by the accessor methods getParentComposite() and getParentTab() respectively
 * which now allocate a tab JIT.  The purpose behind this change is that the execution can spark
 * off several tabs and add entries to the Messages tab.
 *
 * @modified John Spackman
 */
public abstract class AbstractSQLExecution {
	
	// Maximum size of the files used to log queries for debugging
	private static final long MAX_DEBUG_LOG_SIZE = 64 * 1024;
	
	// Maximum length of the caption for query results windows when the preference
	//	IConstants.USE_LONG_CAPTIONS_ON_RESULTS is true
	public static final int MAX_CAPTION_LENGTH = 25;

	/*
	 * LocalThread is used to execute the query in the background by calling doExecute()
	 * in AbstractSQLExecution.
	 */
	private class LocalThread extends Thread {

		public void run() {

			try {
				// Wait until we can get a free connection from the queue
				while (_connection == null) {
					if (_isCancelled)
						break;
					_connection = _session .getQueuedConnection(_connectionNumber);

					if (_connection == null)
						sleep(100);
				}

				// Make sure the user hasn't tried to terminate us and then run the SQL
				if ((!_isCancelled) && _connection != null) {
					doExecution();
					checkForMessages();
				}

			} catch (final RuntimeException e) {
				// Switch back into the main thread to report the error
				final Shell shell = getEditor().getSite().getShell();
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(shell, Messages
								.getString("SQLResultsView.Error.Title"), 
								e.getClass().getName() + ":" + e.getMessage());
					}
				});

			} catch (final Exception e) {
				if (!(e instanceof java.sql.SQLException || e instanceof InterruptedException)) {
					// only log non-sql errors
					SQLExplorerPlugin.error("Error executing.", e);
				}

				// Switch back into the main thread to report the error
				final Shell shell = getEditor().getSite().getShell();
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!(e instanceof InterruptedException)) {
							MessageDialog.openError(shell, Messages
									.getString("SQLResultsView.Error.Title"), e
									.getMessage());
						}
					}
				});

			} finally {
				_session.releaseQueuedConnection(_connectionNumber);
				_connection = null;
			}
		}
	}

	private Integer _connectionNumber;

	protected boolean _isCancelled = false;

	private SQLEditor _editor;

	private LocalThread _executionThread;

	protected SessionTreeNode _session;
	
	protected SQLConnection _connection;

    // Query tokenizer to get SQL statements from
	private QueryParser queryParser;
	
	/**
	 * Constructor
	 * @param _editor The SQLEditor that triggered the execution
	 * @param statement the SQL to be executed
	 * @param _session the session
	 */
	public AbstractSQLExecution(SQLEditor _editor, QueryParser queryParser, SessionTreeNode _session) {
		super();
		this._editor = _editor;
		this._session = _session;
		
		this.queryParser = queryParser;
	}
	
	/**
	 * Creates a new tab for the results in SQLEditor
	 * @return
	 */
	protected ResultsTab allocateResultsTab(Query query) {
		ResultsTab resultsTab = _editor.createResultsTab(this);
		if (resultsTab == null)
			return null;
		boolean longCaptions = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.USE_LONG_CAPTIONS_ON_RESULTS);
		if (longCaptions) {
			String caption = resultsTab.getTabItem().getText() + " [" + TextUtil.compressWhitespace(query.getQuerySql(), MAX_CAPTION_LENGTH) + "]";
			resultsTab.getTabItem().setText(caption);
		}
		return resultsTab;
	}

	/**
	 * Checks the database server for messages
	 */
	protected void checkForMessages() throws SQLException {
		try {
			final Collection messages = _session.getDatabaseProduct().getServerMessages(_connection);
			if (messages == null)
				return;
			addMessages(messages);
		}catch(SQLException e) {
			logException(e, "Checking for messages");
			throw e;
		}
	}
	
	/**
	 * Handles a SQLException by parsing the message and populating the messages tab;
	 * where error messages from the server are numbered, they start relative to the
	 * line number of the query that was sent; lineNoOffset is added to each line
	 * number so that they relate to the line in SQLEditor
	 * @param e
	 */
	protected void logException(SQLException e, String sql) throws SQLException {
		final Collection<SQLEditor.Message> messages = _session.getDatabaseProduct().getErrorMessages(_connection, e, 0);
		if (messages == null)
			return;
		for (SQLEditor.Message message : messages) {
			int lineNo = message.getLineNo();
			lineNo = queryParser.adjustLineNo(lineNo);
			message.setLineNo(lineNo);
			message.setSql(sql);
		}
		addMessages(messages);
	}
	
	/**
	 * Handles a SQLException by parsing the message and populating the messages tab;
	 * where error messages from the server are numbered, they start relative to the
	 * line number of the query that was sent; lineNoOffset is added to each line
	 * number so that they relate to the line in SQLEditor
	 * @param e
	 */
	protected void logException(SQLException e, Query query) throws SQLException {
		final Collection<SQLEditor.Message> messages = _session.getDatabaseProduct().getErrorMessages(_connection, e, query.getLineNo() - 1);
		if (messages == null)
			return;
		for (SQLEditor.Message message : messages) {
			int lineNo = message.getLineNo();
			lineNo = queryParser.adjustLineNo(lineNo);
			message.setLineNo(lineNo);
			message.setSql(query.getQuerySql());
		}
		addMessages(messages);
	}
	
	/**
	 * Called to add messages to the message tab
	 * @param messages a collection of SQLEditor.Message objects
	 */
	protected void addMessages(final Collection messages) {
		_editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {
        		Iterator iter = messages.iterator();
        		while (iter.hasNext()) {
        			SQLEditor.Message message = (SQLEditor.Message)iter.next();
        			_editor.addMessage(message);
        		}
            }
        });
	}
	
	/**
	 * Helper method to set the progress message - switches to the UI thread
	 * @param progressMessage
	 */
	public final void setMessage(final String message) {
        getEditor().getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (getEditor() != null)
                	getEditor().setMessage(message);
            }
        });
	}

	/**
	 * Helper method to set the progress message - switches to the UI thread
	 * @param progressMessage
	 */
	public final void setProgressMessage(final String message) {
	}

	/**
	 * Main execution method.  Note that this method is called from a background thread
	 * and therefore many SWT operations will need to be done via Display.[a]syncExec()
	 * @throws Exception
	 */
	protected abstract void doExecution() throws Exception;

	/**
	 * This method will be called from the UI thread when execution is cancelled
	 * and the tab will be disposed. Do any cleanups required in here.  Note that this 
	 * method is called from a background thread and therefore many SWT operations will
	 * need to be done via Display.[a]syncExec()
	 * @throws Exception
	 */
	protected abstract void doStop() throws Exception;

	/**
	 * Start exection
	 */
	public final void startExecution() {
		_connectionNumber = _session.getQueuedConnectionNumber();

		// start execution in seperate thread
		_executionThread = new LocalThread();
		_executionThread.start();
	}

	/**
	 * Cancel execution.
	 */
	public final void stop() {
		try {
			_isCancelled = true;
			doStop();
			
		} catch (final Exception e) {
			// Switch back to the UI thread and tell the user
			final Shell shell = getEditor().getSite().getShell();
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(shell, Messages
							.getString("SQLResultsView.Error.Title"), e
							.getMessage());
				}
			});
		}
	}
	
	/**
	 * Logs the query to the debug log file, but only if the preferences require
	 * it.  If the query failed, the exception should be included too. 
	 * @param query
	 * @param e
	 */
	protected void debugLogQuery(Query query, SQLException sqlException) {
		// Get the logging level
		String level = SQLExplorerPlugin.getDefault().getPreferenceStore().getString(IConstants.QUERY_DEBUG_LOG_LEVEL);
		if (level == null || level.equals(IConstants.QUERY_DEBUG_OFF))
			return;
		if (sqlException == null && level.equals(IConstants.QUERY_DEBUG_FAILED))
			return;
		
		// Get the log files; if the current log is too big, retire it
		File dir = SQLExplorerPlugin.getDefault().getStateLocation().toFile();
		File log = new File(dir.getAbsolutePath() + '/' + "query-debug.log");
		File oldLog = new File(dir.getAbsolutePath() + '/' + "query-debug.old.log");
		
		// Too big?  Then delete the old and archive the current  
		if (log.exists() && log.length() > MAX_DEBUG_LOG_SIZE) {
			oldLog.delete();
			log.renameTo(oldLog);
		}
		
		// Copy it to the output
		PrintWriter writer = null;
		try {
			FileWriter fw = new FileWriter(log, true);
			writer = new PrintWriter(fw);
			try {
				writer.write("==============================================\r\n");
				StringBuffer sb = new StringBuffer(query.toString());
				for (int i = 0; i < sb.length(); i++)
					if (sb.charAt(i) == '\n')
						sb.insert(i++, '\r');
				sb.append("\r\n");
				writer.write(sb.toString());
				if (sqlException != null)
					writer.write("FAILED: " + sqlException.getMessage() + "\r\n");
			} finally {
				writer.flush();
				writer.close();
			}
		} catch(IOException e) {
			SQLExplorerPlugin.error("Failed to log query", e);
		}
	}

	/**
	 * @return the _editor
	 */
	public SQLEditor getEditor() {
		return _editor;
	}

	/**
	 * @return the queryParser
	 */
	public QueryParser getQueryParser() {
		return queryParser;
	}

}
