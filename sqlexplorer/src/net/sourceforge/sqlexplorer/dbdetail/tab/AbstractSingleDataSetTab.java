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
 * Splits data of a single query line to rows 
 * 
 * @author Patras Vlad
 */

package net.sourceforge.sqlexplorer.dbdetail.tab;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dataset.DataSetRow;

public abstract class AbstractSingleDataSetTab extends AbstractDataSetTab {

	public DataSet getDataSet() throws Exception {
		
		DataSet single = getSingleDataSet();
		DataSetRow[] rows = single.getRows();
		String [] labels = single.getColumnLabels();
		
		String data[][] = new String[labels.length][2];
		
		for (int i=0; i<labels.length; ++i) {
			
			data[i][0] = labels[i];
			data[i][1] = rows[0].getObjectValue(i).toString();
		}
		
		return new DataSet(new String[] { Messages.getString("DatabaseDetailView.Tab.SingleDataSet.Property"),
										  Messages.getString("DatabaseDetailView.Tab.SingleDataSet.Value") },
						   data, new int[] { DataSet.TYPE_STRING, DataSet.TYPE_STRING } );
	}
	
	public abstract DataSet getSingleDataSet() throws Exception;
	
    public abstract String getLabelText();

    public abstract String getStatusMessage();

}
