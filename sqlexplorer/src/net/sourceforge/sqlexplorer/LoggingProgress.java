package net.sourceforge.sqlexplorer;

/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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
import net.sourceforge.squirrel_sql.fw.sql.SQLDriverManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class LoggingProgress implements IRunnableWithProgress {

    private class LocalThread extends Thread {

        public void run() {

               try {
                   
                   long start = System.currentTimeMillis();
                    _interActiveConnection = _driverMgr.getConnection(driver, alias, user, pswd);
                    _logger.debug("# " + (System.currentTimeMillis() - start) + " ms to open interactive connection.");
                    start = System.currentTimeMillis();
                    _backgroundConnection = _driverMgr.getConnection(driver, alias, user, pswd);
                    _logger.debug("# " + (System.currentTimeMillis() - start) + " ms to open background connection.");
                    
                } catch (Throwable e) {
                    th = e;
                    error = e.getMessage();
                    SQLExplorerPlugin.error("Error logging to database", e);

                }
        }
    }
    
    private static final Log _logger = LogFactory.getLog(LoggingProgress.class);
    
    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(IProgressMonitor)
     */
    SQLDriverManager _driverMgr;

    ISQLDriver driver;

    ISQLAlias alias;

    String user;

    String pswd;

    String error;

    private SQLConnection _interActiveConnection;
    
    private SQLConnection _backgroundConnection;

    Throwable th;

    private boolean _isCancelled = false;

    public LoggingProgress(SQLDriverManager dm, ISQLDriver dv, ISQLAlias al, String user, String pswd) {

        _driverMgr = dm;
        driver = dv;
        alias = al;
        this.user = user;
        this.pswd = pswd;
    };


    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.setTaskName(Messages.getString("Logging_to_database..._1"));
        monitor.beginTask(Messages.getString("Logging_to_database..._1"), IProgressMonitor.UNKNOWN);
        try {
            
            LocalThread myThread = new LocalThread();
            myThread.start();
            
            while (true) {
                
                if (monitor.isCanceled()) {
                    if (myThread.isAlive()) {
                        myThread.interrupt();
                    }
                    _isCancelled = true;
                    th = null;
                    if (_interActiveConnection != null) {
                        _interActiveConnection.close();
                    }
                    _interActiveConnection = null;
                    if (_backgroundConnection != null) {
                        _backgroundConnection.close();
                    }
                    _backgroundConnection = null;
                    
                    break;
                }
                
                if (th != null) {
                    _interActiveConnection = null;
                    _backgroundConnection = null;
                    break;
                }
                
                if (_interActiveConnection != null && _backgroundConnection != null && !myThread.isAlive()) {
                    break;
                }
                          
                Thread.sleep(100);
            }
            
            monitor.done();
        } catch (Throwable e) {
            th = e;
            error = e.getMessage();
            SQLExplorerPlugin.error("Error logging to database", e);

        }
    }


    public SQLConnection[] getConnections() {
        return new SQLConnection[] {_interActiveConnection, _backgroundConnection};
    }


    public String getError() {
        return error;
    }


    public boolean isOk() {
        return ((th == null) ? true : false);
    }

    public boolean isCancelled() {
        return _isCancelled;
    }
}
