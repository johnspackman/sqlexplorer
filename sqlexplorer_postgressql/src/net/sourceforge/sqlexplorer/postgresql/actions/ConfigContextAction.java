package net.sourceforge.sqlexplorer.postgresql.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.postgresql.dataset.tree.ITreeDataSet;
import net.sourceforge.sqlexplorer.postgresql.dataset.tree.SqlTreeDataSet;
import net.sourceforge.sqlexplorer.postgresql.dialogs.TreeDataDialog;
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
		return Messages.getString("postgresql.config.menu");
	}

	@Override
	public void run() {
		if (_selectedNodes == null || _selectedNodes.length != 1)
			return;
		try {
			Session session = _selectedNodes[0].getSession();
			String lVal = Messages.getString("postgresql.hdr.value");
			String lMin = Messages.getString("postgresql.hdr.min");
			String lMax = Messages.getString("postgresql.hdr.max");
			String lDesc = Messages.getString("postgresql.hdr.description");
			String lSet = Messages.getString("postgresql.hdr.setting");
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
					+ "setting AS \"" + lVal + "\","
					+ "COALESCE(min_val,'') AS \"" + lMin + "\","
					+ "COALESCE(max_val,'') AS \"" + lMax + "\","
					+ "COALESCE(TRIM(BOTH FROM short_desc||' '||extra_desc),'') AS \"" + lDesc + "\""
					+ "FROM" + "    pg_catalog.pg_settings " + "ORDER BY"
					+ "    1,2,3";
			ITreeDataSet set = new SqlTreeDataSet(session, sql,
					new int[] { 1, 2, 3 }, new int[] { 4, 5, 6, 7 }, lSet);
			Shell shell = PlatformUI.getWorkbench().getDisplay()
					.getActiveShell();
			String t = session.getUser().getAlias().getName();
			String title = Messages.getString("postgresql.config.title");
			String message = Messages.getString("postgresql.config.message", t);
			TreeDataDialog dlg = new TreeDataDialog(shell, title, message, set);
			dlg.open();
		} catch (Exception e) {
			SQLExplorerPlugin.error(Messages.getString("postgresql.config.error"), e);
		}
	}
}
