package net.sourceforge.sqlexplorer.preferences;
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
 
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GeneralPreferencePage extends PreferencePage implements IWorkbenchPreferencePage{
	//public static final String BOLD= "_bold"; //$NON-NLS-1$
	public final OverlayPreferenceStore.OverlayKey[] fKeys= new OverlayPreferenceStore.OverlayKey[] {
				
		
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, IConstants.PRE_ROW_COUNT), 
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT,IConstants.MAX_SQL_ROWS), 
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.AUTO_COMMIT), 
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.COMMIT_ON_CLOSE),
			new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_ASSIST)		
			};


	private IntegerFieldEditor fPreviewRowCountEditor;
	private IntegerFieldEditor fMaxSqlRowEditor;
	//private IPreferenceStore store;
	OverlayPreferenceStore fOverlayStore;
	Button fAutoCommitBox;
	Button fCommitOnCloseBox;
	Button fAssistance;
	public GeneralPreferencePage(OverlayPreferenceStore fOverlayStore){
		this.setTitle(Messages.getString("General_Preferences_1")); //$NON-NLS-1$
		this.fOverlayStore=fOverlayStore;
	};
	public void init(IWorkbench workbench){
	}
	public GeneralPreferencePage(){
		
		fOverlayStore=new OverlayPreferenceStore(SQLExplorerPlugin.getDefault().getPreferenceStore(),fKeys);
		
		fOverlayStore.load();
		fOverlayStore.start();
	}
	
	protected Control createContents(Composite parent) {
		Composite colorComposite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		
		colorComposite.setLayout(layout);
		fPreviewRowCountEditor = new IntegerFieldEditor(IConstants.PRE_ROW_COUNT,Messages.getString("Preview_Max_Rows_3"),colorComposite); //$NON-NLS-1$ //$NON-NLS-2$
		fPreviewRowCountEditor.setValidRange(1,100);
		fPreviewRowCountEditor.setErrorMessage(Messages.getString("Accepted_Range_is__1_-_100_1")); //$NON-NLS-1$
			
		
		fMaxSqlRowEditor = new IntegerFieldEditor(IConstants.MAX_SQL_ROWS,Messages.getString("SQL_Limit_Rows_2"),colorComposite); //$NON-NLS-1$  //$NON-NLS-2$
		fMaxSqlRowEditor.setValidRange(100,5000);
		fMaxSqlRowEditor.setErrorMessage(Messages.getString("Accepted_Range_is__100_-_5000_3")); //$NON-NLS-1$
		
		fAutoCommitBox= new Button(colorComposite, SWT.CHECK);
		fAutoCommitBox.setText(Messages.getString("GeneralPreferencePage.AutoCommit_1"));  //$NON-NLS-1$
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fAutoCommitBox.setLayoutData(gd);
		
		fCommitOnCloseBox= new Button(colorComposite, SWT.CHECK);
		fCommitOnCloseBox.setText(Messages.getString("GeneralPreferencePage.Commit_On_Close_2")); //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fCommitOnCloseBox.setLayoutData(gd);
		
		fAssistance= new Button(colorComposite, SWT.CHECK);
		fAssistance.setText(Messages.getString("GeneralPreferencePage.Tables_and_columns_auto-completing_assistance._Use_only_with_fast_database_connections_1"));  //$NON-NLS-1$
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fAssistance.setLayoutData(gd);
		
		fAutoCommitBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				fOverlayStore.setValue(IConstants.AUTO_COMMIT, fAutoCommitBox.getSelection()); 
				if(fAutoCommitBox.getSelection()){
					fCommitOnCloseBox.setEnabled(false);
				}
				else
					fCommitOnCloseBox.setEnabled(true);
			}
		});
	
		fCommitOnCloseBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				fOverlayStore.setValue(IConstants.COMMIT_ON_CLOSE, fCommitOnCloseBox.getSelection()); 
			}
		});
		
		fAssistance.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				fOverlayStore.setValue(IConstants.SQL_ASSIST, fAssistance.getSelection()); 
			}
		});
		
		initialize();
		
		
		return colorComposite;
	}
	
	private void initialize(){
		fMaxSqlRowEditor.setPreferenceStore(fOverlayStore);
		fMaxSqlRowEditor.setPreferenceName(IConstants.MAX_SQL_ROWS); //$NON-NLS-1$
		fMaxSqlRowEditor.setPage(this);
		fMaxSqlRowEditor.load();
		
		fPreviewRowCountEditor.setPreferenceStore(fOverlayStore);
		fPreviewRowCountEditor.setPreferenceName(IConstants.PRE_ROW_COUNT); //$NON-NLS-1$
		fPreviewRowCountEditor.setPage(this);
		fPreviewRowCountEditor.load();
		
		fAutoCommitBox.getDisplay().asyncExec(new Runnable() {
			public void run() {
				fCommitOnCloseBox.setSelection(fOverlayStore.getBoolean(IConstants.COMMIT_ON_CLOSE));//$NON-NLS-1$
				fAutoCommitBox.setSelection(fOverlayStore.getBoolean(IConstants.AUTO_COMMIT));//$NON-NLS-1$
				if(fAutoCommitBox.getSelection()){
					fCommitOnCloseBox.setEnabled(false);
				}
				else
					fCommitOnCloseBox.setEnabled(true);
			}
		});
		fAssistance.getDisplay().asyncExec(new Runnable(){
			public void run(){
				fAssistance.setSelection(fOverlayStore.getBoolean(IConstants.SQL_ASSIST));
			}
		});
	
	}
	public void dispose() {
		this.setPreferenceStore(null);
		/*if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore= null;
		}*/
		if(fPreviewRowCountEditor!=null){
			fPreviewRowCountEditor.setPreferenceStore(null);
			fPreviewRowCountEditor.setPage(null);
		}
		if(fMaxSqlRowEditor!=null){
			fMaxSqlRowEditor.setPreferenceStore(null);
			fMaxSqlRowEditor.setPage(null);
		}
		
		super.dispose();
	}
	public boolean performOk() {
		if(fPreviewRowCountEditor!=null){
			fPreviewRowCountEditor.store();

		}
		if(fMaxSqlRowEditor!=null){
			fMaxSqlRowEditor.store();
		}
		((OverlayPreferenceStore)fOverlayStore).propagate();
		return true;
	}
	protected void performDefaults() {	
		((OverlayPreferenceStore)fOverlayStore).loadDefaults();
		if(fPreviewRowCountEditor!=null){
			fPreviewRowCountEditor.loadDefault();
//			System.out.println("Previouw defaul value:="+fPreviewRowCountEditor.getIntValue());
		}
		if(fMaxSqlRowEditor!=null)
		{
			fMaxSqlRowEditor.loadDefault();
		}

		super.performDefaults();
	}

}
