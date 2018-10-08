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


public class TopSQLTab extends AbstractSQLTab {

    public String getSQL() {
        return "SELECT Substr(a.sql_text,1,50) \"SQL Text\"," +
        "               Trunc(a.disk_reads/Decode(a.executions,0,1,a.executions)) \"Reads/Execution\", " +
        "               a.buffer_gets \"Buffer Gets\", " +
        "               a.disk_reads \"Disk Reads\", " +
        "               a.executions \"Executions\", " +
        "               a.sorts \"Sorts\"," +
        "               a.address \"Address\"," +
        "               a.hash_value \"Hash Value\"" +
        "        FROM   v$sqlarea a" +
        "        ORDER BY 2 DESC";
    }
    

    public String getLabelText() {
        return Messages.getString("oracle.dbdetail.tab.topSQL");
    }

    public String getStatusMessage() {
        return Messages.getString("oracle.dbdetail.tab.topSQLFor") + " " + getNode().getSession().toString();
    }
    
    
}
