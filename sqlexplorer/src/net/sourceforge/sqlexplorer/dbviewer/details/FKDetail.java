package net.sourceforge.sqlexplorer.dbviewer.details;

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
 
 import java.sql.ResultSet;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbviewer.actions.CopyTableAction;
import net.sourceforge.sqlexplorer.dbviewer.model.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

  
public class FKDetail implements IDetailLogDisplay{
	TableTreeViewer viewer;
	TableNode node;
	boolean activated=false;
	IDetailLogDisplay detailLog;
	String errorMessage;
		
	public FKDetail(TabItem itemTab,Composite parent,IDetailLogDisplay detailLog){
		this.detailLog=detailLog;
		viewer = new TableTreeViewer(parent,SWT.BORDER|SWT.FULL_SELECTION);
		itemTab.setControl(viewer.getControl());
		TableTree tableTree=viewer.getTableTree();
		Table table=tableTree.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		MenuManager  menuMgr= new MenuManager("#TableMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(false);
		menuMgr.add(new CopyTableAction(table));
		Menu fDbContextMenu= menuMgr.createContextMenu(table);
		table.setMenu(fDbContextMenu);
		TableColumn tc=new TableColumn(table,SWT.NULL);
		tc.setText(Messages.getString("Foreign_Key_1")); //$NON-NLS-1$
		tc=new TableColumn(table,SWT.NULL);
		tc.setText(Messages.getString("Order_2"));		 //$NON-NLS-1$
		tc=new TableColumn(table,SWT.NULL);
		tc.setText(Messages.getString("SQL_Table_1"));	 //$NON-NLS-1$
		tc=new TableColumn(table,SWT.NULL);
		tc.setText(Messages.getString("Primary_Key_1"));	 //$NON-NLS-1$
		
		TableLayout tableLayout=new TableLayout();
		for(int i=0;i<4;i++)
			tableLayout.addColumnData(new ColumnWeightData(1, 25, true));
		table.setLayout(tableLayout);
		viewer.setContentProvider(new FKDetailContentProvider());
	}
	public void setNode(TableNode node){
		this.node=node;
		activated=false;
	}
	public void setMessage(String s){
		errorMessage=s;
		detailLog.setMessage(s);
	}
	public void activate(){
		if(!activated){
			Display display=viewer.getControl().getDisplay();
			BusyIndicator.showWhile(display,new Runnable(){
				public void run(){
					ResultSet rs=null;
					try{
						rs=node.getFK();
					}catch(Throwable e){
						SQLExplorerPlugin.error("Error activating FK view ",e); //$NON-NLS-1$
						errorMessage=e.getMessage();
						detailLog.setMessage(errorMessage);
					}
					FKDetailTableModel idtm=new FKDetailTableModel(rs,FKDetail.this);
					FKDetailLabelProvider idlp=new FKDetailLabelProvider(idtm);
					viewer.setLabelProvider(idlp);	
					viewer.setInput(idtm);
					viewer.refresh();
					activated=true;	
					try{
						Statement st=rs.getStatement();
						if(st!=null)
							st.close();	
						rs.close();				
					}catch(Throwable e){
					}				
					}
			});
		}
		else
			detailLog.setMessage(errorMessage);
	}

}
