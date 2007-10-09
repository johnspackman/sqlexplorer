package net.sourceforge.sqlexplorer.postgresql.actions;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.postgresql.dataset.tree.ITreeDataSet;
import net.sourceforge.sqlexplorer.postgresql.dataset.tree.SqlTreeDataSet;
import net.sourceforge.sqlexplorer.postgresql.dialogs.TreeDataDialog;

/**
 * Extension class providing dialog with server's database list.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class DiscoverDbContextAction extends AbstractDBTreeContextAction {

	@Override
	public String getText() {
		return Messages.getString("postgresql.discover.menu");
	}

	@Override
	public void run() {
		if (_selectedNodes == null || _selectedNodes.length != 1)
			return;
		try {
			String lOwner = Messages.getString("postgresql.hdr.owner");
			String lEnc = Messages.getString("postgresql.hdr.encoding");
			String lDesc = Messages.getString("postgresql.hdr.description");
			Session session = _selectedNodes[0].getSession();
			String sql = "SELECT "
					+ "    datname,"
					+ "    us.usename AS \"" + lOwner + "\", "
					+ "    pg_encoding_to_char(encoding) AS \"" + lEnc + "\", "
					+ "    des.description AS \"" + lDesc + "\" "
					+ "FROM "
					+ "    pg_database db JOIN pg_user us ON db.datdba = us.usesysid LEFT JOIN pg_description des ON db.oid = des.objoid "
					+ "WHERE " + "    datallowconn " + "ORDER BY "
					+ "    datname";
			ITreeDataSet set = new SqlTreeDataSet(session, sql, new int[] { 1 },
					new int[] { 2, 3, 4 }, "Database");
			Shell shell = PlatformUI.getWorkbench().getDisplay()
					.getActiveShell();
			String title = Messages.getString("postgresql.discover.title");
			String message = Messages.getString("postgresql.discover.message");
			TreeDataDialog dlg = new TreeDataDialog(shell, title, message, set);
			dlg.open();
		} catch (Exception e) {
			SQLExplorerPlugin.error(Messages.getString("postgresql.discover.error"), e);
		}

	}
}
