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
 * Extension class providing dialog with databse configuration.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class ConfigContextAction extends AbstractDBTreeContextAction {

	@Override
	public String getText() {
		return "Show database configuration";
	}

	@Override
	public void run() {
		if (_selectedNodes == null || _selectedNodes.length != 1)
			return;
		try {
			SQLConnection c = _selectedNodes[0].getSession()
					.getInteractiveConnection();
			String sql = "SELECT "
					+ "CASE POSITION('/' IN category)"
					+ "	WHEN 0 THEN category"
					+ "	ELSE SUBSTRING(category FROM 0 FOR POSITION('/' IN category) - 1)"
					+ "    END,"
					+ "CASE POSITION('/' IN category)"
					+ "	WHEN 0 THEN NULL"
					+ "	ELSE SUBSTRING(category FROM POSITION('/' IN category) + 2)"
					+ "END,"
					+ "name,"
					+ "setting AS \"Value\","
					+ "COALESCE(min_val,'') AS \"Minimum\","
					+ "COALESCE(max_val,'') AS \"Maximum\","
					+ "COALESCE(TRIM(BOTH FROM short_desc||' '||extra_desc),'') AS \"Description\""
					+ "FROM" + "    pg_catalog.pg_settings " + "ORDER BY"
					+ "    1,2,3";
			ITreeDataSet set = new SqlTreeDataSet(c, sql,
					new int[] { 1, 2, 3 }, new int[] { 4, 5, 6, 7 }, "Setting");
			Shell shell = PlatformUI.getWorkbench().getDisplay()
					.getActiveShell();
			String t = _selectedNodes[0].getSession().getAlias().getName();
			TreeDataDialog dlg = new TreeDataDialog(shell,
					"Database configuration",
					"PostgreSQL database configuration for " + t, set);
			dlg.open();
		} catch (Exception e) {
			SQLExplorerPlugin.error("Failed to display database config", e);
		}
	}
}
