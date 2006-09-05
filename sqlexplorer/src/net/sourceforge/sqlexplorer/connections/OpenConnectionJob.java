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

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.LoginProgress;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SQLDriverManager;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.plugin.views.DatabaseStructureView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDriver;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;

public class OpenConnectionJob extends Job {

    private ISQLAlias _alias;

    private boolean _autoCommit;

    private boolean _commitOnClose;

    private ISQLDriver _driver;

    private SQLDriverManager _driverMgr;

    private LoginProgress _login;

    private String _pwd;

    private Shell _shell;

    private String _user;

    private static final String ID = "net.sourceforge.sqlexplorer";;


    /**
     * Hidden constructor.
     */
    private OpenConnectionJob() {

        super(null);
    }


    public OpenConnectionJob(SQLDriverManager dm, ISQLDriver dv, ISQLAlias al, String user, String pwd,
            boolean autoCommit, boolean commitOnClose, Shell shell) {

        super(Messages.getString("Progress.Connection.Title") + " " + al.getName());
        _driverMgr = dm;
        _driver = dv;
        _alias = al;
        _user = user;
        _pwd = pwd;
        _autoCommit = autoCommit;
        _commitOnClose = commitOnClose;
        _shell = shell;

    }


    /**
     * Close any open connections.
     */
    private void cleanUp() {

        SQLConnection[] connections = _login.getConnections();
        for (int i = 0; i < connections.length; i++) {
            if (connections[i] != null) {
                try {
                    connections[i].close();
                } catch (Exception e) {
                    SQLExplorerPlugin.error("Couldn't close connection.", e);
                }
            }
        }

    }


    /**
     * Open connections.
     * 
     */
    protected IStatus run(IProgressMonitor monitor) {

        _login = new LoginProgress(_driverMgr, _driver, _alias, _user, _pwd);

        try {

            _login.run(monitor);

        } catch (InterruptedException ie) {
            cleanUp();
            return new Status(IStatus.CANCEL, ID, IStatus.CANCEL, Messages.getString("Progress.Connection.Cancelled"),
                    null);

        } catch (Exception e) {
            cleanUp();
            return new Status(IStatus.ERROR, ID, IStatus.CANCEL, Messages.getString("Progress.Connection.Error"), e);
        }

        
        if(_login.getError() != null) {
            return new Status(IStatus.ERROR, ID, IStatus.CANCEL, _login.getError(), null);
        }
        
        
        // set connection properties

        SQLConnection[] connections = _login.getConnections();
        try {
            for (int i = 0; i < connections.length; i++) {
                connections[i].setAutoCommit(_autoCommit);
                if (_autoCommit == false) {
                    connections[i].setCommitOnClose(_commitOnClose);
                }
            }
        } catch (Exception e) {
            SQLExplorerPlugin.error("Error setting commit properties", e);
            return new Status(IStatus.ERROR, ID, IStatus.CANCEL, Messages.getString("Progress.Connection.Error"), e);
        }

        // load metadata
        SessionTreeNode session = null;
        try {

            monitor.setTaskName(Messages.getString("RetrievingTableDataProgress.Getting_Database_Structure_Data_1"));
            session = SQLExplorerPlugin.getDefault().stm.createSessionTreeNode(connections, _alias, monitor, _pwd);
            monitor.done();
            
        } catch (InterruptedException ie) {
            cleanUp();
            return new Status(IStatus.CANCEL, ID, IStatus.CANCEL, Messages.getString("Progress.Connection.Cancelled"),
                    null);

        } catch (Exception e) {
            cleanUp();
            return new Status(IStatus.ERROR, ID, IStatus.CANCEL, Messages.getString("Progress.Connection.Error"), e);
        }

        // update ui
        final SessionTreeNode newSession = session;
        _shell.getDisplay().asyncExec(new Runnable() {

            public void run() {

                try {

                    // add session to database structure view
                    DatabaseStructureView dbView = (DatabaseStructureView) SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(
                            "net.sourceforge.sqlexplorer.plugin.views.DatabaseStructureView");
                    if (dbView != null) {
                        dbView.addSession(newSession);
                    }

                    // after opening connection, open editor if preference is set.
                    boolean openEditor = SQLExplorerPlugin.getDefault().getPluginPreferences()
                                                    .getBoolean(IConstants.AUTO_OPEN_EDITOR);
                    if (openEditor) {
                    
                        SQLEditorInput input = new SQLEditorInput("SQL Editor ("
                                + SQLExplorerPlugin.getDefault().getNextElement() + ").sql");
                        input.setSessionNode(newSession);
                        IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
    
                        page.openEditor(input, "net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");

                    }
                } catch (Throwable e) {
                    SQLExplorerPlugin.error("Error updating ui with new session", e);
                }
            }
        });

        // everything ended ok..
        return new Status(IStatus.OK, ID, IStatus.OK, "tested ok ", null);
    }

}
