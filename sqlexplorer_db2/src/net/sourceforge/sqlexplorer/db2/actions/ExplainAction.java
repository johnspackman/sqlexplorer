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

import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.db2.actions.explain.ExplainExecution;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Explain plan action for DB2 9.1 and higher. This action will show the explain
 * plan for the selected queries.
 * 
 * @author Davy Vanherbergen
 */
public class ExplainAction extends AbstractEditorAction {

	private static final String CHECK_PLAN_SQL = "select queryno from #SCHEMA#.EXPLAIN_STATEMENT";

	private static final String SCHEMA_TAG = "#SCHEMA#";

	private boolean _explainTablesFound = false;

	/**
	 * Check for the existence of explain plan tables in a given schema. Create
	 * them if they don't exist yet.
	 */
	private void checkExplainTables(String schemaName) {

		if (_explainTablesFound) {
			return;
		}

		Session session = getSession();
		if (session == null) {
			return;
		}

		SQLConnection connection = null;
		Statement st = null;
		ResultSet rs = null;

		try {

			connection = session.grabConnection();
			st = connection.createStatement();
			rs = null;
			boolean createPlanTable = false;

			try {
				// if we can query the table, it exists..
				rs = st.executeQuery(CHECK_PLAN_SQL.replaceAll(SCHEMA_TAG, schemaName));
				rs.close();
				_explainTablesFound = true;

			} catch (Throwable e) {
				// no explain tables found
				createPlanTable = MessageDialog.openQuestion(null, Messages.getString("db2.editor.actions.explain.notFound.Title"),
						Messages.getString("db2.editor.actions.explain.notFound"));

			} finally {
				st.close();
			}

			if (!_explainTablesFound && createPlanTable) {
				st = connection.createStatement();

				URL fileURL = FileLocator.toFileURL(Platform.getBundle("net.sourceforge.sqlexplorer.db2").getEntry("/explain.ddl"));
				File explainFile = new File(fileURL.toURI());
				System.out.println(explainFile.getAbsolutePath());
				String ddl = FileUtils.readFileToString(explainFile, "UTF-8");
				String[] ddlStatements = ddl.split(";");

				for (String ddlStatement : ddlStatements) {
					st.execute(ddlStatement.replaceAll(SCHEMA_TAG, schemaName));
				}

				if (!connection.getAutoCommit()) {
					connection.commit();
				}
				_explainTablesFound = true;
			}

		} catch (final Exception e) {
			SQLExplorerPlugin.error("Explain plan table check or creation failed", e);

			_editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(_editor.getSite().getShell(), Messages.getString("db2.editor.actions.explain.createError.Title"),
							"Explain plan table check or creation failed: " + e.getMessage());
				}
			});

		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					SQLExplorerPlugin.error("Cannot close result set", e);
				}
			if (st != null)
				try {
					st.close();
				} catch (SQLException e) {
					SQLExplorerPlugin.error("Cannot close statement", e);
				}
			if (connection != null) {
				session.releaseConnection(connection);
			}
		}

	}

	/**
	 * Define action label.
	 * 
	 * @see net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction#getText()
	 */
	public String getText() {
		return Messages.getString("db2.editor.actions.explain");
	}

	/**
	 * Define tooltip text.
	 * 
	 * @see net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction#getToolTipText()
	 */
	public String getToolTipText() {
		return getText();
	}

	/**
	 * Run DB2 Explain Plan for the selected queries.
	 * 
	 * @see net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction#run()
	 */
	public void run() {

		Session session = getSession();
		if (session == null) {
			return;
		}

		String schemaName = session.getUser().getUserName().toUpperCase();
		checkExplainTables(schemaName);

		if (!_explainTablesFound) {
			return;
		}

		// execute explain plan for all statements
		QueryParser qt = session.getDatabaseProduct().getQueryParser(_editor.getSQLToBeExecuted(), _editor.getSQLLineNumber());
		try {
			qt.parse();
		} catch (final ParserException e) {
			_editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(_editor.getSite().getShell(), Messages.getString("SQLResultsView.Error.Title"), e.getMessage());
				}
			});
		}
		ExplainExecution job = new ExplainExecution(_editor, qt, schemaName);
		job.schedule();
	}

}
