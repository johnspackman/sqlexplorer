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


public class SessionsTab extends AbstractSQLTab {

    public String getSQL() {
        return "SELECT Substr(a.username,1,15) \"Username\"," +
        "               a.osuser \"OS User\"," +
        "               a.sid \"Session ID\"," +
        "               a.serial# \"Serial No\"," +
        "               d.spid \"Process ID\"," +
        "               a.lockwait \"LockWait\"," +
        "               a.status \"Status\"," +
        "               Trunc(b.value/1024) \"PGA (Kb)\"," +
        "               Trunc(e.value/1024) \"UGA (Kb)\"," +
        "               a.module \"Module\"," +
        "               Substr(a.machine,1,15) \"Machine\"," +
        "               a.program \"Program\"," +
        "               Substr(To_Char(a.logon_Time,'DD-Mon-YYYY HH24:MI:SS'),1,20) \"Logon Time\"" +
        "        FROM   v$session a," +
        "               v$sesstat b," +
        "               v$statname c," +
        "               v$process d," +
        "               v$sesstat e," +
        "               v$statname f" +
        "        WHERE  a.paddr = d.addr" +
        "        AND    a.sid = b.sid" +
        "        AND    b.statistic# = c.statistic#" +
        "        AND    c.name = 'session pga memory'" +
        "        AND    a.sid = e.sid" +
        "        AND    e.statistic# = f.statistic#" +
        "        AND    f.name = 'session uga memory'";
    }
    

    public String getLabelText() {
        return Messages.getString("oracle.dbdetail.tab.sessions");
    }

    public String getStatusMessage() {
        return Messages.getString("oracle.dbdetail.tab.sessionsFor") + " " + getNode().getSession().toString();
    }
    
    
}
