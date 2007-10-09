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
package net.sourceforge.sqlexplorer;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class LoginProgress implements IRunnableWithProgress {

    private static final Log _logger = LogFactory.getLog(LoginProgress.class);

    private class ConnectionThread extends Thread {
        public void run() {
        	try {
        		long start = System.currentTimeMillis();
        		session = user.createSession();
        		SQLConnection connection = null;
        		try {
                	connection = session.grabConnection();
                } finally {
                	if (connection != null)
                		session.releaseConnection(connection);
                }
               _logger.debug("# " + (System.currentTimeMillis() - start) + " ms to open interactive connection.");
            } catch (Exception e) {
                exception = e;
                SQLExplorerPlugin.error(e);
            }
        }
    }
    
    // User to establish a connection for
    private User user;

    // Connection established by background thread
    private Session session;
    
    // Exception raised by background thread
    private Exception exception;

    public LoginProgress(User user) {
		super();
		this.user = user;
	}

	/**
     * Returns the established connection
     * @return
     */
    public Session getSession() {
        return session;
    }

    /**
     * Returns true if the connection was established OK
     * @return
     */
    public boolean isOk() {
        return exception == null;
    }

    public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
        monitor.setTaskName(Messages.getString("Logging_to_database..._1"));
        monitor.beginTask(Messages.getString("Logging_to_database..._1"), IProgressMonitor.UNKNOWN);
        
        try {
            long start = System.currentTimeMillis();
            ConnectionThread iThread = new ConnectionThread();
            iThread.start();
            
            while (iThread.isAlive()) {
            	// If it's cancelled, close the connection (if we established one) and quit
                if (monitor.isCanceled()) {                    
                    if (iThread.isAlive())
                        iThread.interrupt();
                    exception = null;
                    
                    if (session != null)
                        session.close();
                    session = null;
                    break;
                }
                
                // Wait until we have a connection and the thread has stopped
                if (session != null && !iThread.isAlive()) {
                    _logger.debug("# " + (System.currentTimeMillis() - start) + " ms to open connection.");
                    break;
                }
                          
                // Snooze
                Thread.sleep(100);
            }
            
            // check for cancellation by user
            if (monitor.isCanceled())
                throw new InterruptedException("Connection cancelled.");
        }catch(Exception e) {
        	exception = e;
        } finally {
            monitor.done();
        }
        if (exception != null)
        	throw new InvocationTargetException(exception);
    }
}
