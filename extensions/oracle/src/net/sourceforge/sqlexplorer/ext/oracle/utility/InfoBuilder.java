package net.sourceforge.sqlexplorer.ext.oracle.utility;
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
import org.eclipse.jface.viewers.ColumnWeightData;

import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.swt.SWT;


import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


public class InfoBuilder {
	
	public static TableViewer createInfoViewer(TabFolder tabFolder,TabItem itemTab){
		TableViewer tv =new TableViewer(tabFolder,SWT.NULL);
		itemTab.setControl(tv.getControl());
		
		Table table=tv.getTable();
		TableColumn c1=new TableColumn(table,SWT.NULL);
		c1.setText("Property"); //$NON-NLS-1$
		TableColumn c2=new TableColumn(table,SWT.NULL);		
		c2.setText("Value");		 //$NON-NLS-1$
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		TableLayout tableLayout=new TableLayout();
		for(int i=0;i<2;i++)
			tableLayout.addColumnData(new ColumnWeightData(1, 50, true));
		table.setLayout(tableLayout);
		tv.setLabelProvider(new LProvider());
		return tv;
	}
}
