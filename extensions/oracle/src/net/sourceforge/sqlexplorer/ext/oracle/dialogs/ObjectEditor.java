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
package net.sourceforge.sqlexplorer.ext.oracle.dialogs;


import net.sourceforge.sqlexplorer.ext.oracle.dialogs.actions.GetErrors;
import net.sourceforge.sqlexplorer.ext.oracle.dialogs.actions.Save;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;
import net.sourceforge.sqlexplorer.sqlpanel.SQLTextViewer;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author mazzolini
 */
public class ObjectEditor extends Dialog {
	ToolBarManager toolBarMgr;
	StatusLineManager  statusMgr;
	SQLTextViewer vw;
	IPreferenceStore store;
	Dictionary dictionary;
	String text;
	String windowCaption;
	private Save saveAction;
	SQLConnection conn;	
	String objectType;
	String owner;
	String objectName;
	StyledText st;
	CTabFolder tabFolder;
	boolean hiddenSaveButton=false;
	public void hideSaveButton(){
		hiddenSaveButton=true;
	}
	public ObjectEditor(Shell parentShell,IPreferenceStore store,Dictionary dictionary, String text, String windowCaption,SQLConnection conn, String objectType, String owner,String objectName){
		super(parentShell);
		this.store=store;
		this.dictionary=dictionary;
		this.text=text;
		this.windowCaption=windowCaption;
		this.conn=conn;
		this.objectType=objectType;
		this.owner=owner;
		this.objectName=objectName;
		
	}
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(windowCaption);  
	}
	protected Point getInitialSize() {
		 return   new  Point(500, 400);
	}
	protected void createButtonsForButtonBar(Composite parent){
		//Button button = 
	}
	protected void setShellStyle(int newShellStyle){
		super.setShellStyle(newShellStyle|SWT.RESIZE);//Make the about dialog resizable
	}
	protected Control createButtonBar(Composite parent) {
		return null;
	}
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite)super.createDialogArea(parent);
		GridLayout layout;
	
				// Define layout.
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 
		layout.horizontalSpacing = layout.verticalSpacing = 0;
		parentComposite.setLayout(layout);

		toolBarMgr = new ToolBarManager(SWT.FLAT);
		ToolBar toolBar = toolBarMgr.createControl(parentComposite);
		GridData gid = new GridData();
		gid.horizontalAlignment = GridData.FILL;
		gid.verticalAlignment = GridData.BEGINNING;
		toolBar.setLayoutData(gid);
		
		
		if(!hiddenSaveButton){
			saveAction=new Save(this);
			toolBarMgr.add(saveAction);
			toolBarMgr.update(true);	
		}
		gid = new GridData();
		gid.grabExcessHorizontalSpace = gid.grabExcessVerticalSpace = true;
		gid.horizontalAlignment = gid.verticalAlignment = GridData.FILL;
		Composite cmp=new Composite(parentComposite,SWT.NULL);
		cmp.setLayout(new FillLayout());
		tabFolder=new CTabFolder(cmp,SWT.BOTTOM);
		
		CTabItem tabItem1=new CTabItem(tabFolder,SWT.NULL);
		tabItem1.setText("Code");
		if(objectName!=null){
			CTabItem tabItem2=new CTabItem(tabFolder,SWT.NULL);
			tabItem2.setText("Errors");
			st=new StyledText(tabFolder,SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL);
			st.setEditable(false);
			tabItem2.setControl(st);
		}
//				vw=new SQLTextViewer(this,SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI |  SWT.FULL_SELECTION,store);
		vw=new SQLTextViewer(tabFolder,SWT.BORDER|SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI |  SWT.FULL_SELECTION,store,dictionary);
		tabItem1.setControl(vw.getControl());
		
		tabFolder.setSelection(0);
		tabFolder.update();
		cmp.setLayoutData(gid);
		statusMgr=new StatusLineManager();
		statusMgr.createControl(parentComposite);
		gid = new GridData();
		gid.horizontalAlignment = GridData.FILL;
		gid.verticalAlignment = GridData.BEGINNING;
		statusMgr.getControl().setLayoutData(gid);

		parentComposite.layout();
		IDocument dc=new Document(text);
		vw.setDocument(dc);

		if(this.getObjectName()!=null){
			GetErrors getErrors=new GetErrors(this.getConn(),this.getOwner(),this.getObjectType(),this.getObjectName());
			getErrors.getError(st);
		}
				
		return parentComposite;
	}

	/**
	 * @return SQLTextViewer
	 */
	public SQLTextViewer getVw() {
		return vw;
	}

	/**
	 * @return SQLConnection
	 */
	public SQLConnection getConn() {
		return conn;
	}

	/**
	 * @return String
	 */
	public String getObjectName() {
		return objectName;
	}

	/**
	 * @return String
	 */
	public String getObjectType() {
		return objectType;
	}

	/**
	 * @return String
	 */
	public String getOwner() {
		return owner;
	}
	public void showErrorPage(){
		tabFolder.setSelection(1);
	}
	/**
	 * @return
	 */
	public StyledText getSt() {
		
		return st;
	}

}
