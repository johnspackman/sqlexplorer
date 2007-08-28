package net.sourceforge.sqlexplorer.postgresql.actions.explain;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Iterator;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.ResultsTab;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.postgresql.dataset.tree.TreeDataSet;
import net.sourceforge.sqlexplorer.postgresql.ui.TreeDataSetViewer;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sqlpanel.AbstractSQLExecution;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabItem;

/**
 * Execute an explain.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class ExplainExecution extends AbstractSQLExecution {

	private static final ILogger logger = LoggerController
			.createLogger(ExplainExecution.class);

	private Statement stmt = null;

	private Savepoint start = null;

	private final String prefix;

	private final int type;

	private boolean autoCommit = false;

	private Connection connection;

	/**
	 * Create new executor object.
	 * 
	 * @param _editor
	 *            Our SQL editor.
	 * @param res
	 *            Our results view.
	 * @param query
	 *            The raw SQL query to explain.
	 * @param session
	 *            Our session.
	 * @param type
	 *            explain type
	 * @see AbstractExplainAction#EXPLAIN_ANALYZE
	 * @see AbstractExplainAction#EXPLAIN_NORMAL
	 */
	public ExplainExecution(final SQLEditor _editor, QueryParser queryParser, final SessionTreeNode session, int type) {
		super(_editor, queryParser, session);
		setProgressMessage("SQLResultsView.ConnectionWait");
		prefix = type == AbstractExplainAction.EXPLAIN_ANALYZE ? " ANALYZE "
				: "";
		this.type = type;
	}

	/**
	 * See if execution has been cancelled.
	 * 
	 * @return Cancelled or not.
	 */
	private boolean isCancelled() {
		boolean r = _isCancelled;
		if (r)
			logger.debug("Explain cancelled, stopping processing");
		return r;
	}

	@Override
	protected void doExecution() throws Exception {
		setProgressMessage("SQLResultsView.Executing");

		if (isCancelled())
			return;

		connection = _session.getInteractiveConnection().getConnection();
		ResultSet rs = null;
		Query query = null;
		try {
			autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			if (isCancelled())
				return;
        	for (Iterator<Query> iter = getQueryParser().iterator(); iter.hasNext(); ) {
        		query = iter.next();
    			if (_isCancelled)
    				break;

    			start = connection.setSavepoint("BEFORE_EXPLAIN");
    			stmt = connection.createStatement();
				stmt.execute("EXPLAIN " + prefix + query.getQuerySql());
				if (isCancelled())
					return;
				rs = stmt.getResultSet();
				if (isCancelled()) {
					rs.close();
					return;
				}
				final ResultSet tmp = rs;
				ExplainTreeBuilder b = new ExplainTreeBuilder(type);
				b.parse(new ExplainLineProvider() {
					public String getLine() throws Exception {
						if (!tmp.next())
							return null;
						return tmp.getString(1);
					}
				});
				rs.close();
				if (isCancelled())
					return;
				connection.rollback(start);
				connection.setAutoCommit(autoCommit);
				stmt.close();
				if (isCancelled())
					return;
				b.getRoot().computeStatistics();
				displayResult(b.getRoot(), query);
        	}
		} catch (Exception e) {
			throw e;
		} finally {
			start = null;
			try {
				if (rs != null)
					rs.close();
				rs = null;
			} catch (Exception e) {
			}
			try {
				if (stmt != null)
					stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	@Override
	protected void doStop() throws Exception {
		if (stmt != null) {
			try {
				final Statement p = stmt;
				final Connection c = connection;
				final Savepoint s = start;
				new Thread() {
					@Override
					public void run() {
						try {
							logger.debug("Attempting to cancel EXPLAIN");
							if (s != null) {
								logger.debug("Rolling back transaction");
								c.rollback(s);
								logger.debug("Did roll back transaction");
							}
							connection.setAutoCommit(autoCommit);
							p.cancel();
							logger.debug("Did cancel EXPLAIN");
							p.close();
							logger.debug("Did close statement");
						} catch (Exception e) {
							SQLExplorerPlugin.error(Messages.getString(
									"postgresql.explain.error.cancel"),	e);
						}
					}
				}.start();
			} catch (Exception e) {
				SQLExplorerPlugin
						.error(Messages.getString("postgresql.explain.error.cancel"), e);
				throw e;
			}
		}
	}

	/**
	 * Display the result to the user. This uses a tree viewer to pretty-present
	 * the resulting explain tree.
	 * 
	 * @param node
	 *            The explain tree's root node.
	 */
	private void displayResult(final ExplainNode node, final Query query) {
		getEditor().getSite().getShell().getDisplay().asyncExec(
				new Runnable() {

					public void run() {

		            	ResultsTab resultsTab = allocateResultsTab(query);

						try {
			                Composite composite = resultsTab.getParent();
			                
							TreeDataSetViewer v = new TreeDataSetViewer(composite);
							String[] l = null;
							String lAction = Messages.getString("postgresql.explain.action");
							String lInfo = Messages.getString("postgresql.explain.info");
							if (type == AbstractExplainAction.EXPLAIN_ANALYZE)
								l = new String[] {
									Messages.getString("postgresql.explain.guessSelf"),
									Messages.getString("postgresql.explain.guessTree"),
									Messages.getString("postgresql.explain.actualSelf"),
									Messages.getString("postgresql.explain.actualTree"),
									lInfo };
							else
								l = new String[] {
									Messages.getString("postgresql.explain.guessSelf"),
									Messages.getString("postgresql.explain.guessTree"),
									lInfo };
							v
									.setTreeDataSet(new TreeDataSet(node, l,
											lAction));
							v.getTreeViewer().setLabelProvider(
									new ExplainTreeLabelProvider(type));

							final Composite parent = composite;
							v.getTree().addKeyListener(new KeyAdapter() {
								@Override
								public void keyReleased(KeyEvent e) {
									if (e.keyCode == SWT.F5) {
										try {
											Object o = parent
													.getData("parenttab");
											if (o != null) {
												AbstractSQLExecution sqlExec = (AbstractSQLExecution) ((TabItem) o)
														.getData();
												if (sqlExec != null)
													sqlExec.startExecution();
											}
										} catch (Exception e1) {
											SQLExplorerPlugin.error(
													Messages.getString(
															"postgresql.explain.error.refresh"),
													e1);
										}
									}
								}
							});
							
							composite.layout();
							composite.redraw();
						} catch (Exception e) {
			                Composite composite = resultsTab.getParent();
							String m = e.getMessage();
							Label errorLabel = new Label(composite, SWT.FILL);
							errorLabel.setText(m);
							errorLabel.setLayoutData(new GridData(SWT.FILL,
									SWT.TOP, true, false));
							SQLExplorerPlugin.error(
									Messages.getString("postgresql.explain.error.tab"), e);
						}
					}
				});
	}
}
