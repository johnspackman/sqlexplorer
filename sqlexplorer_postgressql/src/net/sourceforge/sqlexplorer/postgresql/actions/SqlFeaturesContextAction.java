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
 * Extension class providing dialog with databse's SQL features.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class SqlFeaturesContextAction extends AbstractDBTreeContextAction {

	@Override
	public String getText() {
		return Messages.getString("postgresql.feature.menu");
	}

	@Override
	public void run() {
		if (_selectedNodes == null || _selectedNodes.length != 1)
			return;
		try {
			String lName = Messages.getString("postgresql.hdr.name");
			String lSupported = Messages.getString("postgresql.hdr.supported");
			String lComments = Messages.getString("postgresql.hdr.comments");
			String lFeature = Messages.getString("postgresql.hdr.feature");
			Session session = _selectedNodes[0].getSession();
			String sql = "SELECT "
					+ "    SUBSTR(feature_id,1,1), "
					+ "    SUBSTR(feature_id,1,2), "
					+ "    SUBSTR(feature_id,1,3), "
					+ "    feature_id||': '||feature_name, "
					+ "    CASE WHEN sub_feature_id = '' THEN NULL ELSE sub_feature_id END, "
					+ "    sub_feature_name AS \"" + lName + "\", "
					+ "    is_supported AS \"" + lSupported + "\", "
					+ "    comments AS \"" + lComments + "\" " + "FROM "
					+ "    information_schema.sql_features " + "ORDER BY "
					+ "    1,2,3,4,5";
			ITreeDataSet set = new SqlTreeDataSet(session, sql, new int[] { 1, 2, 3,
					4, 5 }, new int[] { 6, 7, 8 }, lFeature);
			Shell shell = PlatformUI.getWorkbench().getDisplay()
					.getActiveShell();
			String t = session.getUser().getAlias().getName();
			String title = Messages.getString("postgresql.feature.title");
			String message = Messages.getString("postgresql.feature.message", t);
			TreeDataDialog dlg = new TreeDataDialog(shell, title, message, set);
			dlg.open();
		} catch (Exception e) {
			SQLExplorerPlugin.error(Messages.getString("postgresql.feature.error"), e);
		}
	}

}
