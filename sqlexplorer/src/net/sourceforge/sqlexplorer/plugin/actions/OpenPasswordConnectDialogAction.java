/*
 * Copyright (C) 2006 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
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
package net.sourceforge.sqlexplorer.plugin.actions;

import java.sql.SQLException;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.connections.OpenConnectionJob;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.dialogs.PasswordConnDlg;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public class OpenPasswordConnectDialogAction extends Action {

	// The Alias to login - this MUST be the same as the user's alias; the
	//	reason we keep this in addition to the User object is in case we are
	//	logging in a new user that we have no initial definition for
	private Alias alias;
	
	// The user to try and login
    private User user;
    
    // Whether to prompt for the password, even if the user is auto-logon etc
    private boolean alwaysPrompt;

    public OpenPasswordConnectDialogAction(Alias alias, User user, boolean alwaysPrompt) {
    	super();
    	if (alias == null)
    		throw new IllegalArgumentException("Alias cannot be null!");
    	if (user != null && alias != user.getAlias())
    		throw new IllegalArgumentException("User is attached the wrong alias");
    	this.alias = alias;
        this.user = user;
        this.alwaysPrompt = alwaysPrompt;
    }

    public void run() {
    	IWorkbenchSite site = SQLExplorerPlugin.getDefault().getSite();

    	/*
    	 * Loop until we can connect to the database or the user cancels; this is done in the
    	 * foreground to simplify error/retry logic (eg invalid password).  Connection should
    	 * not normally take too long, it's getting the schema data that needs to run in
    	 * the background
    	 */
    	while (true) {
    		if (alwaysPrompt || !alias.isAutoLogon() || user == null || !user.equals(alias.getDefaultUser())) { 
	            PasswordConnDlg dlg = new PasswordConnDlg(site.getShell(), alias, user);
	            if (dlg.open() != Window.OK)
	            	return;
	            user = new User(dlg.getUserName(), dlg.getPassword());
	        	user.setAutoCommit(dlg.getAutoCommit());
	        	user.setCommitOnClose(dlg.getCommitOnClose());
	            user = alias.addUser(user);
	        }
    		
            SQLConnection connection = null;
            try {
            	connection = user.getConnection();
            	break;
            }catch(SQLException e) {
            	alwaysPrompt = true;
            	MessageDialog.openError(site.getShell(), Messages.getString("Login.Error.Title"), e.getMessage());
            } finally {
            	if (connection != null)
            		try {
            			user.releaseConnection(connection);
            		} catch(SQLException e) {
            			SQLExplorerPlugin.error(e);
            		}
            	
            }
    	}

        OpenConnectionJob bgJob = new OpenConnectionJob(user, site.getShell());

        IWorkbenchSiteProgressService siteps = (IWorkbenchSiteProgressService) site.getAdapter(IWorkbenchSiteProgressService.class);
        siteps.showInDialog(site.getShell(), bgJob);
        bgJob.schedule();
    }
}
