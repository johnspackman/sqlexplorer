/*
 * Copyright (C) 2007 SQL Explorer Development Team
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
package net.sourceforge.sqlexplorer.connections;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import net.sourceforge.sqlexplorer.LoginProgress;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractConnectJob extends Job {

    private User user;

    private LoginProgress login;

    private Shell shell;

    private static final String ID = "net.sourceforge.sqlexplorer";

    public AbstractConnectJob(User user, Shell shell) {
        super(Messages.getString("Progress.Connection.Title") + " " + user.getAlias().getName() + '/' + user.getUserName());
        this.user = user;
        this.shell = shell;
    }
    
    /**
     * Open connections.
     * 
     */
    protected IStatus run(IProgressMonitor monitor) {
        login = new LoginProgress(user);

        SQLConnection connection = null;
        try {
            login.run(monitor);
            final Session session = login.getSession();
        	
            // load metadata
            monitor.setTaskName(Messages.getString("RetrievingTableDataProgress.Getting_Database_Structure_Data_1"));
            monitor.done();

            shell.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    try {
                    	connectionEstablished(session);
                    } catch (Throwable e) {
                        SQLExplorerPlugin.error("Error updating ui with new session", e);
                    }
                }
            });
            
        } catch (InterruptedException ie) {
            cleanUp(connection);
            return new Status(IStatus.CANCEL, ID, IStatus.CANCEL, Messages.getString("Progress.Connection.Cancelled"), null);
        } catch(InvocationTargetException e) {
            cleanUp(connection);
        	if (e.getTargetException() instanceof SQLException) {
        		SQLException sqlEx = (SQLException)e.getTargetException();
        		SQLExplorerPlugin.error(sqlEx.getMessage(), sqlEx);
            	return new Status(IStatus.ERROR, ID, IStatus.CANCEL, sqlEx.getMessage(), sqlEx);
        	}
        	return new Status(IStatus.ERROR, ID, IStatus.CANCEL, Messages.getString("Progress.Connection.Error"), e);
        } catch (Exception e) {
            cleanUp(connection);
            return new Status(IStatus.ERROR, ID, IStatus.CANCEL, Messages.getString("Progress.Connection.Error"), e);
        }

        return new Status(IStatus.OK, ID, IStatus.OK, "tested ok ", null);
    }
    
    protected abstract void connectionEstablished(Session session);

    /**
     * Close any open connections.
     */
    private void cleanUp(SQLConnection connection) {
        try {
        	if (connection != null)
        		connection.close();
        }catch(SQLException e) {
        	// Nothing
        }
    }
}
