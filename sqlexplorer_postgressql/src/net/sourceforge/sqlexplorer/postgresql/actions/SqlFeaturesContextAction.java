package net.sourceforge.sqlexplorer.postgresql.actions;

import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.postgresql.dataset.tree.ITreeDataSet;
import net.sourceforge.sqlexplorer.postgresql.dataset.tree.SqlTreeDataSet;
import net.sourceforge.sqlexplorer.postgresql.dialogs.TreeDataDialog;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Extension class providing dialog with databse's SQL features.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class SqlFeaturesContextAction extends AbstractDBTreeContextAction {

	@Override
	public String getText() {
		return "Show SQL features";
	}

	@Override
	public void run() {
		if (_selectedNodes == null || _selectedNodes.length != 1)
			return;
		try {
			SQLConnection c = _selectedNodes[0].getSession()
					.getInteractiveConnection();
			String sql = "SELECT "
					+ "    SUBSTR(feature_id,1,1), "
					+ "    SUBSTR(feature_id,1,2), "
					+ "    SUBSTR(feature_id,1,3), "
					+ "    feature_id||': '||feature_name, "
					+ "    CASE WHEN sub_feature_id = '' THEN NULL ELSE sub_feature_id END, "
					+ "    sub_feature_name AS \"Name\", "
					+ "    is_supported AS \"Is supported\", "
					+ "    comments AS \"Comments\" " + "FROM "
					+ "    information_schema.sql_features " + "ORDER BY "
					+ "    1,2,3,4,5";
			ITreeDataSet set = new SqlTreeDataSet(c, sql, new int[] { 1, 2, 3,
					4, 5 }, new int[] { 6, 7, 8 }, "SQL Feature");
			Shell shell = PlatformUI.getWorkbench().getDisplay()
					.getActiveShell();
			String t = _selectedNodes[0].getSession().getAlias().getName();
			TreeDataDialog dlg = new TreeDataDialog(shell,
					"Database SQL features",
					"PostgreSQL database SQL features for " + t, set);
			dlg.open();
		} catch (Exception e) {
			SQLExplorerPlugin.error("Failed to display database features", e);
		}
	}

}
