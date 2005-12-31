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

import net.sourceforge.sqlexplorer.MultiLineString;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.SqlHistoryChangedListener;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Andrea Mazzolini
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SQLHistoryView extends ViewPart implements SqlHistoryChangedListener{
	TableViewer tableViewer;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(final Composite parent) {
		SQLExplorerPlugin.getDefault().addListener(this);
		tableViewer=new TableViewer(parent,SWT.V_SCROLL | SWT.H_SCROLL|SWT.FULL_SELECTION);
		Table table=tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tableViewer.setLabelProvider(new LabelProvider());
		tableViewer.setContentProvider(new IStructuredContentProvider(){

			public Object[] getElements(Object inputElement) {
				return SQLExplorerPlugin.getDefault().getSQLHistory().toArray();
			}

			public void dispose() {
					
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				
			}});
		tableViewer.setInput(this);
		TableColumn tc=new TableColumn(table,SWT.NULL);
		tc.setText("previous sql");
		TableLayout tableLayout=new TableLayout();
				
		tableLayout.addColumnData(new ColumnWeightData(1, 100, true));
		table.setLayout(tableLayout);
		table.layout(); 
		final MenuManager  menuMgr= new MenuManager("#HistoryPopupMenu"); //$NON-NLS-1$
		Menu historyContextMenu= menuMgr.createContextMenu(table);
		menuMgr.add(new Action(){
			public String getText(){
				return "Open in editor";
			}
			public void run(){
				try{
					TableItem[] ti=tableViewer.getTable().getSelection();
					if(ti== null || ti.length<1)
						return;

					SQLEditorInput input = new SQLEditorInput("SQL Editor ("+SQLExplorerPlugin.getDefault().getNextElement()+").sql");
					input.setSessionNode(null);
					IWorkbenchPage page=SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
					if(page==null)
						return;
					SQLEditor editorPart= (SQLEditor) page.openEditor((IEditorInput) input,"net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");
					
                    Object data = ti[0].getData();
                    MultiLineString mls = (MultiLineString) data;
                    editorPart.setText(mls.getOriginalText());

				}catch(Throwable e){
					SQLExplorerPlugin.error("Error creating sql editor",e);
				}
			}
		});
		menuMgr.add(new Action(){
			public String getText(){
				return "Remove from history";
			}
			public void run(){
				try{
					int i=tableViewer.getTable().getSelectionIndex();
					if(i>=0){
						SQLExplorerPlugin.getDefault().getSQLHistory().remove(i);
						changed();
					}
					
					
				}catch(Throwable e){
					SQLExplorerPlugin.error("Error removing item from clipboard",e);
				}
			}
		});
		
		menuMgr.add(new Action() {
			
			public String getText() {
				return "Clear history";
			}
					
			public void run() {
			
				try {
							
					SQLExplorerPlugin.getDefault().getSQLHistory().clear();
					changed();
				} catch (Throwable e) {
					SQLExplorerPlugin.error("Error clearing sql history", e);
				}
			}
		});
					
		menuMgr.add(new Action(){
			public String getText(){
				return "Copy to Clipboard";
			}
			public void run(){
				try{
					TableItem[] ti=tableViewer.getTable().getSelection();
					if(ti== null || ti.length<1)
						return;
					Clipboard cb=new Clipboard(Display.getCurrent());
					TextTransfer textTransfer = TextTransfer.getInstance();
                    
                    Object data = ti[0].getData();
                    MultiLineString mls = (MultiLineString) data;
                    
					cb.setContents(new Object[]{mls.getOriginalText()}, new Transfer[]{textTransfer});			
			
				}catch(Throwable e){
					SQLExplorerPlugin.error("Error copying to clipboard",e);
				}
			}
		});
		table.setMenu(historyContextMenu);
		menuMgr.addMenuListener(new IMenuListener(){
			public void menuAboutToShow(IMenuManager manager){
				TableItem[] ti=tableViewer.getTable().getSelection();
				MenuItem[] items=menuMgr.getMenu().getItems();
				if(ti== null || ti.length<1){
					for(int i=0;i<items.length;i++){
						items[i].setEnabled(false);
					}
				}else{
					for(int i=0;i<items.length;i++){
						items[i].setEnabled(true);
					}
				}
				
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		tableViewer.getTable().setFocus();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		SQLExplorerPlugin.getDefault().removeListener(this);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.plugin.SqlHistoryChangedListener#changed()
	 */
	public void changed() {
		tableViewer.getTable().getDisplay().asyncExec(new Runnable(){
			public void run(){
				tableViewer.refresh();
			}
		});
		
	}

}
