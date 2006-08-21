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
package net.sourceforge.sqlexplorer.oracle.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;


public class JobsTab extends AbstractSQLTab {

    public String getSQL() {
        return "SELECT a.job \"Job\",            " +
        "               a.log_user \"Log User\",       " +
        "               a.priv_user \"Priv User\",     " +
        "               a.schema_user \"Schema User\",    " +
        "               To_Char(a.last_date,'DD-Mon-YYYY HH24:MI:SS') \"Last Date\",      " +
        "               To_Char(a.this_date,'DD-Mon-YYYY HH24:MI:SS') \"This Date\",      " +
        "               To_Char(a.next_date,'DD-Mon-YYYY HH24:MI:SS') \"Next Date\",      " +
        "               a.total_time \"Total Time\",     " +
        "               a.broken \"Broken\",         " +
        "               a.interval \"Interval\",       " +
        "               a.failures \"Failures\",       " +
        "               a.what \"What\"," +
        "               a.nls_env \"NLS Env\",        " +
        "               a.misc_env \"Misc Env\"          " +
        "        FROM   dba_jobs a" +
        "";
    }
    

    public String getLabelText() {
        return Messages.getString("oracle.dbdetail.tab.jobs");
    }

    public String getStatusMessage() {
        return Messages.getString("oracle.dbdetail.tab.jobsFor") + " " + getNode().getSession().toString();
    }
    
    
}
