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
package net.sourceforge.sqlexplorer.mysql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

public class ProcessTab extends AbstractSQLTab {

    public ProcessTab() {

    }


    /**
     * Tab label.
     */
    public String getLabelText() {

        return Messages.getString("mysql.DatabaseDetailView.Tab.Processes");
    }


    /**
     * SQL statement to populate tab with.
     */
    public String getSQL() {

        return "show full processlist";
    }


    /**
     * Status message for the process list tab.
     */
    public String getStatusMessage() {

        return Messages.getString("mysql.DatabaseDetailView.Tab.Processes.status") + " "
                + getNode().getSession().toString();
    }
}
