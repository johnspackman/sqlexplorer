package net.sourceforge.sqlexplorer.connections;

import org.eclipse.swt.widgets.Shell;

import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.DatabaseStructureView;

public class OpenDatabaseViewJob extends AbstractConnectJob {

	public OpenDatabaseViewJob(User user, Shell shell) {
		super(user, shell);
	}

	@Override
	protected void connectionEstablished(Session session) {
        DatabaseStructureView dbView = SQLExplorerPlugin.getDefault().getDatabaseStructureView();
        if (dbView != null)
            dbView.addSession(session);
	}

}
