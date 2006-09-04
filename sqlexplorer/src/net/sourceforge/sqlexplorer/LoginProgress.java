package net.sourceforge.sqlexplorer;

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

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDriver;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import net.sourceforge.sqlexplorer.SQLDriverManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class LoginProgress implements IRunnableWithProgress {

    private class BackgroundConnectionThread extends Thread {

        public void run() {

               try {
                   
                   long start = System.currentTimeMillis();
                    _backgroundConnection = _driverMgr.getConnection(_driver, _alias, _user, _pwd);
                    _logger.debug("# " + (System.currentTimeMillis() - start) + " ms to open background connection.");
                    
                } catch (Throwable e) {
                    _backgroundError = e;
                    _backgroundErrorMsg = e.getClass().getName() + ": " + e.getMessage();
                    SQLExplorerPlugin.error("Error logging to database", e);

                }
        }
    }
    
    private class InteractiveConnectionThread extends Thread {

        public void run() {

               try {
                   
                   long start = System.currentTimeMillis();
                    _interActiveConnection = _driverMgr.getConnection(_driver, _alias, _user, _pwd);
                    _logger.debug("# " + (System.currentTimeMillis() - start) + " ms to open interactive connection.");
                     
                } catch (Throwable e) {
                    _interactiveError = e;
                    _interactiveErrorMsg = e.getClass().getName() + ": " + e.getMessage();
                    SQLExplorerPlugin.error("Error logging to database", e);

                }
        }
    }
    
    private ISQLAlias _alias;
    
    private SQLConnection _backgroundConnection;

    private Throwable _backgroundError;

    private String _backgroundErrorMsg;

    private ISQLDriver _driver;

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(IProgressMonitor)
     */
    private SQLDriverManager _driverMgr;

    private SQLConnection _interActiveConnection;
    
    private Throwable _interactiveError;

    private String _interactiveErrorMsg;
    
    private String _pwd;
    
    private String _user;

    private static final Log _logger = LogFactory.getLog(LoginProgress.class);

    public LoginProgress(SQLDriverManager dm, ISQLDriver dv, ISQLAlias al, String user, String pwd) {

        _driverMgr = dm;
        _driver = dv;
        _alias = al;
        _user = user;
        _pwd = pwd;
    };


    public SQLConnection[] getConnections() {
        return new SQLConnection[] {_interActiveConnection, _backgroundConnection};
    }


    public String getError() {
        if (_interactiveErrorMsg == null) {
            return _backgroundErrorMsg;
        }
        return _interactiveErrorMsg;
    }


    public boolean isOk() {
        return ((_interactiveError == null && _backgroundError == null) ? true : false);
    }

    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.setTaskName(Messages.getString("Logging_to_database..._1"));
        monitor.beginTask(Messages.getString("Logging_to_database..._1"), IProgressMonitor.UNKNOWN);
        try {
            
            long start = System.currentTimeMillis();
            
            InteractiveConnectionThread iThread = new InteractiveConnectionThread();
            iThread.start();
            
            BackgroundConnectionThread bgThread = new BackgroundConnectionThread();
            bgThread.start();
            
            while (true) {
                
                if (monitor.isCanceled()) {                    
                    
                    if (iThread.isAlive()) {
                        iThread.interrupt();
                    }
                    _interactiveError = null;
                    
                    if (_interActiveConnection != null) {
                        _interActiveConnection.close();
                    }
                    _interActiveConnection = null;
                    
                    if (bgThread.isAlive()) {
                        bgThread.interrupt();
                    }
                    _backgroundError = null;

                    if (_backgroundConnection != null) {
                        _backgroundConnection.close();
                    }
                    _backgroundConnection = null;
                    
                    break;
                }
                
                if (_interactiveError != null || _backgroundError != null) {
                    _interActiveConnection = null;
                    _backgroundConnection = null;
                    break;
                }
                
                if (_interActiveConnection != null && _backgroundConnection != null 
                        && !iThread.isAlive() && !bgThread.isAlive()) {
                    _logger.debug("# " + (System.currentTimeMillis() - start) + " ms to open both connections.");
                    break;
                }
                          
                Thread.sleep(100);
            }
            
            // check for cancellation by user
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException("Connection cancelled.");
            }
            
            monitor.done();
            
        } catch (Throwable e) {
            _interactiveError = e;
            _interactiveErrorMsg = e.getMessage();
            SQLExplorerPlugin.error("Error logging to database", e);

        } finally {
            monitor.done();
        }
    }
}
