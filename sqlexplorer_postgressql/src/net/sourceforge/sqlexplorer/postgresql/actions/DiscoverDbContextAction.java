package net.sourceforge.sqlexplorer.postgresql.actions;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.postgresql.dataset.tree.ITreeDataSet;
import net.sourceforge.sqlexplorer.postgresql.dataset.tree.SqlTreeDataSet;
import net.sourceforge.sqlexplorer.postgresql.dialogs.TreeDataDialog;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

/**
 * Extension class providing dialog with server's database list.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class DiscoverDbContextAction extends AbstractDBTreeContextAction {

	@Override
	public String getText() {
		return "Discover databases";
	}

	@Override
	public void run() {
		if (_selectedNodes == null || _selectedNodes.length != 1)
			return;
		try {
			SQLConnection c = _selectedNodes[0].getSession()
					.getInteractiveConnection();
			String sql = "SELECT "
					+ "    datname,"
					+ "    us.usename AS \"Owner\", "
					+ "    pg_encoding_to_char(encoding) AS \"Encoding\", "
					+ "    des.description AS \"Description\" "
					+ "FROM "
					+ "    pg_database db JOIN pg_user us ON db.datdba = us.usesysid LEFT JOIN pg_description des ON db.oid = des.objoid "
					+ "WHERE " + "    datallowconn " + "ORDER BY "
					+ "    datname";
			ITreeDataSet set = new SqlTreeDataSet(c, sql, new int[] { 1 },
					new int[] { 2, 3, 4 }, "Database");
			Shell shell = PlatformUI.getWorkbench().getDisplay()
					.getActiveShell();
			TreeDataDialog dlg = new TreeDataDialog(shell, "Databases",
					"PostgreSQL database list", set);
			dlg.open();
		} catch (Exception e) {
			SQLExplorerPlugin.error("Failed to display database list", e);
		}

	}
}
