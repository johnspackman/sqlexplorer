/*
 * Copyright (C) 2006 Davy Vanherbergen
 * dvanherbergen@users.sourceforge.net
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
package net.sourceforge.sqlexplorer.connections;

import java.sql.SQLException;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.plugin.views.DatabaseStructureView;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

public class OpenConnectionJob extends AbstractConnectJob {

    public OpenConnectionJob(User user, Shell shell) {
		super(user, shell);
	}

    @Override
	protected void connectionEstablished(Session session) {
        // after opening connection, open editor if preference is set.
        boolean openEditor = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.AUTO_OPEN_EDITOR);
        if (openEditor) {
            SQLEditorInput input = new SQLEditorInput("SQL Editor (" + SQLExplorerPlugin.getDefault().getEditorSerialNo() + ").sql");
            input.setSessionNode(session);
            IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
            try {
            	page.openEditor(input, "net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");
            }catch(PartInitException e) {
            	SQLExplorerPlugin.error("Failed to open an editor", e);
            }
        }
        DatabaseStructureView dbView = SQLExplorerPlugin.getDefault().getDatabaseStructureView();
        if (dbView != null) {
        	if (!dbView.isConnectedToUser(session.getUser()))
        		try {
        			dbView.addSession(session.getUser().createSession());
        		}catch(SQLException e) {
        			SQLExplorerPlugin.error(e);
        		}
        }
	}
}
