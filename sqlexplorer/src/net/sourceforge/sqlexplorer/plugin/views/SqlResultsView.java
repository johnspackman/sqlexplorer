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
package net.sourceforge.sqlexplorer.plugin.views;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.sqlpanel.SQLTableSorter;
import net.sourceforge.sqlexplorer.sqlpanel.SqlTableLabelProvider;
import net.sourceforge.sqlexplorer.sqlpanel.SqlTableModel;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Mazzolini
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SqlResultsView extends ViewPart {
	private String[] []ss;
	private int []colCount;
	
	
	CompositeSQLResultsViewer cmp[];
	SqlTableModel mo[];
	
	SQLTableSorter sorter[];
	Composite parent;
	TabFolder tabFolder;
	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		this.parent=parent;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {

	}
	/*protected void handleDoubleClick(MouseEvent evt)
	{
		Table t = (Table) evt.widget;
		int index = t.getSelectionIndex();
		//System.out.println("selection index "+index);
		if (index != -1)
		{
			for (int i = 0; i < t.getColumnCount(); i++)
			{
				if (t.getItem(index).getBounds(i).contains(evt.x, evt.y))
				{
					//System.out.println("selection i "+i);
					//((SqlTableLabelProvider)table.getLabelProvider()).getColumnText()
					//System.out.println("Element "+table.getElementAt(index));
					try{
						TableViewer tableViewer=cmp.getTableViewer();
						tableViewer.editElement(tableViewer.getElementAt(index), i);
					}catch(Exception e){
						JFaceDbcPlugin.error("Error editing the table element ",e); //$NON-NLS-1$
					}
					break;
				}
			}
		}
		// clicked into empty area, add a new row?
		else
		{
		//???
		}
	}*/

	/**
	 * @param reader
	 * @param mo
	 * @param sorter
	 */
	public void setData(SqlTableModel []new_mo) throws Exception{
		if(mo!=null){
			for(int i=0;i<mo.length;i++){
				mo[i].closeResultSet();
			}
			
		}

		if (tabFolder == null) {
			tabFolder = new TabFolder(parent,SWT.NULL);
		}
		
		this.mo=new_mo;
		sorter=new SQLTableSorter[mo.length];
		colCount=new int[mo.length];
		ss=new String[mo.length][];
		cmp=new CompositeSQLResultsViewer[mo.length];
		
		int tabItemNumber = tabFolder.getItemCount();
		
		for(int i=0;i<new_mo.length;i++){
			sorter[i]=mo[i].sorter;
			colCount[i]=mo[i].ss.length;
			ss[i]=mo[i].ss;
			
			TabItem ti=new TabItem(tabFolder,SWT.NULL);
			ti.setText(""+(tabItemNumber+i+1));
			ti.setToolTipText(TextUtil.getWrappedText(mo[i].getSQLStatement().getText()));
			
			//if(cmp!=null &&  !cmp.isDisposed())
			//	cmp.dispose();
			cmp[i]=new CompositeSQLResultsViewer(this,tabFolder,SWT.NULL,i, ti);
			ti.setControl(cmp[i]);
		}		
		refresh();
		tabFolder.setSelection(tabFolder.getItemCount() - 1);
		
	}
	private void refresh()throws Exception{
		for(int jj=0;jj<mo.length;jj++){
			final int ii=jj;
			int count=colCount[ii];
			
			//Composite parent=cmp.getParent();
			//cmp.dispose();
			final Image imgAsc=ImageDescriptor.createFromURL(SqlexplorerImages.getAscOrderIcon()).createImage();
			final Image imgDesc=ImageDescriptor.createFromURL(SqlexplorerImages.getDescOrderIcon()).createImage();
			final TableViewer tableViewer=cmp[ii].getTableViewer();
			tableViewer.getControl().addDisposeListener(new DisposeListener(){
				public void widgetDisposed(DisposeEvent e) {
					imgAsc.dispose();
					imgDesc.dispose();
				}
			});
		
			final Table table=tableViewer.getTable();
			table.removeAll();
		
			if(mo==null)
				return;
			
								
									//tv.setSorter(sorter);
			SelectionListener headerListener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(tableViewer.getSorter()==null)
						tableViewer.setSorter(sorter[ii]);
					int column = table.indexOf((TableColumn) e.widget);
					if (column == sorter[ii].getTopPriority()){
						int k=sorter[ii].reverseTopPriority();
						if(k==SQLTableSorter.ASCENDING)
							((TableColumn) e.widget).setImage(imgAsc);
						else
							((TableColumn) e.widget).setImage(imgDesc);
					}else {
						sorter[ii].setTopPriority(column);
						((TableColumn) e.widget).setImage(imgAsc);
					}
					TableColumn[] tcArr=table.getColumns();
					for(int i=0;i<tcArr.length;i++){
						if(i!=column){
							tcArr[i].setImage(null);
						}
					}
					//updateSortingState();
					tableViewer.refresh();
					cmp[ii].setMessagePanel1("");
				}
			};
			for(int i=0;i<count;i++){
				TableColumn tc=new TableColumn(table,SWT.NULL);
				tc.setText(ss[ii][i]);
				tc.addSelectionListener(headerListener);
			}
			tableViewer.setColumnProperties(ss[ii]);
			CellEditor[] cellEditors = new CellEditor[count];
			for (int i = 0; i < cellEditors.length; i++)
			{
				final int colIndex = i;
				cellEditors[i] = new TextCellEditor(table){
					protected void keyReleaseOccured(KeyEvent keyEvent)
					{
						super.keyReleaseOccured(keyEvent);
						int index = table.getSelectionIndex();
						int newCol = colIndex;
	
						TableItem[] selection = table.getSelection();
						fireApplyEditorValue();
						if (selection != null)
						{
							table.setSelection(selection);
						}
	
						Object element = tableViewer.getElementAt(index);
						tableViewer.reveal(element);
						tableViewer.editElement(element, newCol);
					}
				};
	
			
			}
			tableViewer.setCellEditors(cellEditors);
			tableViewer.setLabelProvider(new SqlTableLabelProvider(mo[ii]));		
			
							
			tableViewer.setInput(mo[ii]);
			tableViewer.refresh();
				//compositeTableViewer.setModel(mo);
			tableViewer.getControl().addDisposeListener(new DisposeListener(){
				public void widgetDisposed(DisposeEvent e){
					//mo.
					//JFaceDbcPlugin.error("widget dispose: close result set",new Exception());
					mo[ii].closeResultSet();		
				}
			});
			cmp[ii].setMessagePanel2(mo[ii].getPartial());
			tableViewer.getTable().addSelectionListener(new SelectionListener(){
				public void widgetDefaultSelected(SelectionEvent e){};
				public void widgetSelected(SelectionEvent e){
					cmp[ii].setMessagePanel1(Messages.getString("Selected_Row__1")+(tableViewer.getTable().getSelectionIndex()+1));    //$NON-NLS-1$
				};
			});
		
			cmp[ii].enableMoreRows(!mo[ii].isFinished());
			for (int i = 0; i < count; i++) {
				table.getColumn(i).pack();
			}
			table.layout();
			parent.layout();
			parent.redraw();
		}	
	}

	public SqlTableModel[] getModel() {
		return mo;
		
	}

	/**
	 * 
	 */
	public  TableViewer getTableViewer(int ii) {
		if(cmp==null)
			return null;
		return cmp[ii].getTableViewer();
		
	}


	public void enableMoreRows(int ii,boolean b) {
		cmp[ii].enableMoreRows(b);
	}

	public void setMessagePanel2(int ii, String string) {
		if(cmp!=null)
			cmp[ii].setMessagePanel2(string);
		
	}


}
