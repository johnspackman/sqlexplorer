package net.sourceforge.sqlexplorer.postgresql.actions.explain;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.SqlResultsView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction;
import net.sourceforge.sqlexplorer.util.QueryTokenizer;

import org.eclipse.core.runtime.Preferences;

/**
 * Class plugging into SQL editor to provide explain support for PostgreSQL.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public abstract class AbstractExplainAction extends AbstractEditorAction {

	protected final int type;

	/**
	 * Constant indicating a normal explain.
	 */
	public static final int EXPLAIN_NORMAL = 0;

	/**
	 * Constant indicating an analyzed (actually executed) explain.
	 */
	public static final int EXPLAIN_ANALYZE = 1;

	/**
	 * Create new explain editor action.
	 * 
	 * @param type
	 *            The explain type
	 * @see #EXPLAIN_ANALYZE
	 * @see #EXPLAIN_NORMAL
	 */
	public AbstractExplainAction(int type) {
		this.type = type;
	}

	@Override
	public String getToolTipText() {
		return getText();
	}

	@Override
	public void run() {
		SessionTreeNode session = _editor.getSessionTreeNode();
		if (session == null)
			return;

		Preferences prefs = SQLExplorerPlugin.getDefault()
				.getPluginPreferences();
		String delimiter = prefs.getString(IConstants.SQL_QRY_DELIMITER);
		String alternateDelimiter = prefs
				.getString(IConstants.SQL_ALT_QRY_DELIMITER);
		String commentDelimiter = prefs
				.getString(IConstants.SQL_COMMENT_DELIMITER);

		QueryTokenizer qt = new QueryTokenizer(_editor.getSQLToBeExecuted(),
				delimiter, alternateDelimiter, commentDelimiter);
		final List<String> queryStrings = new ArrayList<String>();
		while (qt.hasQuery()) {
			final String query = qt.nextQuery().trim();
			if (!query.startsWith("--"))
				queryStrings.add(query);
		}

		try {
			SqlResultsView res = (SqlResultsView) _editor
					.getSite()
					.getPage()
					.showView(
							"net.sourceforge.sqlexplorer.plugin.views.SqlResultsView");
			while (!queryStrings.isEmpty()) {
				String q = queryStrings.remove(0);
				if (q != null && q.trim().length() != 0)
					res.addSQLExecution(new ExplainExecution(_editor, res, q,
							session, type));
			}
		} catch (Exception e) {
			SQLExplorerPlugin.error("Error creating sql execution tab", e);
		}
	}

}
