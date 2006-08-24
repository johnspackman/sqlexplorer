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

import net.sourceforge.sqlexplorer.DriverModel;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.SQLDriverManager;
import net.sourceforge.sqlexplorer.connections.OpenConnectionJob;
import net.sourceforge.sqlexplorer.dialogs.PasswordConnDlg;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDriver;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public class OpenPasswordConnectDialogAction extends Action {

    private Shell _shell;

    private ISQLAlias _alias;

    private DriverModel _driverModel;

    private IPreferenceStore _store;

    private SQLDriverManager _dmgr;

    private IWorkbenchPartSite _site;


    public OpenPasswordConnectDialogAction(IWorkbenchPartSite site, ISQLAlias alias, DriverModel model,
            IPreferenceStore store, SQLDriverManager dmgr) {

        _site = site;
        _shell = site.getShell();
        _alias = alias;
        _driverModel = model;
        _store = store;
        _dmgr = dmgr;

    }


    public void run() {

        String user = _alias.getUserName();
        String pswd = _alias.getPassword();
        boolean autoCommit = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.AUTO_COMMIT);
        boolean commitOnClose = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(
                IConstants.COMMIT_ON_CLOSE);

        if (!_alias.isAutoLogon()) {

            PasswordConnDlg dlg = new PasswordConnDlg(_shell, _alias, _driverModel, _store);
            if (dlg.open() == Window.OK) {
                pswd = dlg.getPassword();
                user = dlg.getUser();
                autoCommit = dlg.getAutoCommit();
                commitOnClose = dlg.getCommitOnClose();
            } else {
                return;
            }
        }

        ISQLDriver dv = _driverModel.getDriver(_alias.getDriverIdentifier());

        OpenConnectionJob bgJob = new OpenConnectionJob(_dmgr, dv, _alias, user, pswd, autoCommit, commitOnClose,
                _shell);

        IWorkbenchSiteProgressService siteps = (IWorkbenchSiteProgressService) _site.getAdapter(IWorkbenchSiteProgressService.class);
        siteps.showInDialog(_shell, bgJob);
        bgJob.schedule();

    }
}
