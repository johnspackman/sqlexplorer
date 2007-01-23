/*
 * Copyright (C) 2007 Patrac Vlad Sebastian
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

/**
 * Dual class for AbstractSQLTabl, to display a line of data
 * on rows (for ech column) 
 * 
 * @author Patras Vlad
 */

package net.sourceforge.sqlexplorer.dbdetail.tab;

import net.sourceforge.sqlexplorer.dataset.DataSet;

public abstract class AbstractSingleSQLTab extends AbstractSingleDataSetTab {

	public abstract String getLabelText();

	public DataSet getSingleDataSet() throws Exception {
		
		final String sqlQuery = getSQL();
		final Object[] sqlParameters = getSQLParameters();
		
		AbstractSQLTab translator = new AbstractSQLTab() {

			public String getLabelText() {
				return null;
			}
			public String getSQL() {
				return sqlQuery;
			}
			public String getStatusMessage() {
				return null;
			}
			public Object[] getSQLParameters() {
				return sqlParameters;
			}
		};
		translator.setNode(getNode());
		
		return translator.getDataSet();
	}

	public abstract String getStatusMessage();
	
	public abstract String getSQL();
	
    public Object[] getSQLParameters() {
        return null;
    }

}
