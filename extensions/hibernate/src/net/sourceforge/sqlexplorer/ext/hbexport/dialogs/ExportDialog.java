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
package net.sourceforge.sqlexplorer.ext.hbexport.dialogs;


import net.sourceforge.sqlexplorer.dbviewer.model.TableNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class ExportDialog extends TitleAreaDialog {

	SessionTreeNode sessionNode;
	TableNode node;
	Combo comboKeyField;
	private Combo comboSchemaExport;
	private Text packageText;
	Text dirText;
	private Button radio1;
	public static boolean hibernateTypeSelected=true;
	public static String idType;
	public static String outDirText;
	public static String generator;
	public static String pkgName;
	final String[] hibernateKeyFields={"date","long","string","timestamp"};
	final String[] javaKeyFields={"java.util.Date","java.lang.Long","java.lang.String","java.sql.Timestamp"};
	final String[] schemaExport={"uuid.hex","uuid.string","vm.long","vm.hex","assigned","native","sequence","hilo.long","hilo.hex","seqhilo.long"};
	public  ExportDialog(Shell parentShell,SessionTreeNode sessionNode, TableNode node) {
		super(parentShell);
		this.node=node;
		this.sessionNode=sessionNode;
	}
	protected void configureShell(Shell newShell) {
		newShell.setText("Exporting "+node.getTableInfo().getSimpleName());
		super.configureShell(newShell);
	}
	protected void setShellStyle(int newShellStyle){
		super.setShellStyle(newShellStyle|SWT.RESIZE);//Make this dialog resizable
	}
	protected Control createDialogArea(final Composite parent) {
		
		Composite pp = new Composite(parent,SWT.NULL);
		pp.setLayoutData(new GridData(GridData.FILL_BOTH));
		pp.setLayout(new FillLayout());
		Composite parentComposite=new Composite(pp,SWT.NULL);
		FormLayout formLay=new FormLayout();
		formLay.marginHeight = formLay.marginWidth =10; 
		parentComposite.setLayout(formLay);
		
		
		Label label1=new Label(parentComposite,SWT.NONE);
		label1.setText("Package prefix");
		packageText=new Text(parentComposite,SWT.BORDER|SWT.SINGLE);
		if(pkgName!=null)
			packageText.setText(pkgName);
		Label label2=new Label(parentComposite,SWT.NONE);
		label2.setText("OutputFolder");
		dirText=new Text(parentComposite,SWT.BORDER|SWT.SINGLE);
		if(outDirText!=null)
			dirText.setText(outDirText);
		Button dirButton=new Button(parentComposite,SWT.NULL);
		dirButton.setText("Browse");
		dirButton.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dd=new DirectoryDialog(parent.getShell());
				dd.setText("Output Folder");
				String path=dd.open();
				if(path!=null)
					dirText.setText(path);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
		
				
			}
		});
		
		Label label3=new Label(parentComposite,SWT.NONE);
		label3.setText("Schema Export");
		comboSchemaExport=new Combo(parentComposite,SWT.DROP_DOWN|SWT.READ_ONLY);
		comboSchemaExport.setItems(this.schemaExport);
		if(generator==null)
			comboSchemaExport.select(5);
		else
			comboSchemaExport.setText(generator);
		
		Label label4=new Label(parentComposite,SWT.NONE);
		label4.setText("Key Field Type");
		radio1=new Button(parentComposite,SWT.RADIO);
		radio1.setText("Hibernate Types");
		radio1.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent e) {
				int i=comboKeyField.getSelectionIndex();
				comboKeyField.setItems(hibernateKeyFields);
				comboKeyField.select(i);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		Button radio2=new Button(parentComposite,SWT.RADIO);
		radio2.setText("Java Types");
		radio2.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent e) {
				int i=comboKeyField.getSelectionIndex();
				comboKeyField.setItems(javaKeyFields);
				comboKeyField.select(i);
			}

			public void widgetDefaultSelected(SelectionEvent e) {	
			}
		});
		
		Label label5=new Label(parentComposite,SWT.NONE);
		label5.setText("Key Field Class");
		comboKeyField=new Combo(parentComposite,SWT.DROP_DOWN|SWT.READ_ONLY);
		comboKeyField.setItems(this.hibernateKeyFields);
		
		
		FormData data1 = new FormData();
		data1.left = new FormAttachment(0,12);
		data1.top=new FormAttachment(0,12);
		//data1.right = new FormAttachment(25,0);
		label1.setLayoutData(data1);
		
		       
		FormData data2 = new FormData();
		data2.left = new FormAttachment(label1,12);
		data2.right = new FormAttachment(100,-12);
		data2.top=new FormAttachment(0,12);
		packageText.setLayoutData(data2);
		
		FormData data3 = new FormData();
		data3.top = new FormAttachment(label1,12);
		data3.left = new FormAttachment(0,12);
		label2.setLayoutData(data3);
		
		FormData data4 = new FormData();
		data4.left = new FormAttachment(label1,12);
		data4.top = new FormAttachment(label1,12);
		data4.right = new FormAttachment(dirButton,-12);
		dirText.setLayoutData(data4);
		
		FormData data5 = new FormData();
		data5.right = new FormAttachment(100,-12);
		data5.top = new FormAttachment(label1,12);
		dirButton.setLayoutData(data5);
		
		
		
		FormData data6 = new FormData();
		
		data6.top = new FormAttachment(label2,12);
		data6.left = new FormAttachment(0,12);
		label3.setLayoutData(data6);
		
		FormData data7 = new FormData();
		data7.left = new FormAttachment(label1,12);
		data7.top = new FormAttachment(label2,12);
		data7.right=new FormAttachment(100,-12);	
		comboSchemaExport.setLayoutData(data7);
		
		FormData data8 = new FormData();
		data8.top = new FormAttachment(label3,12);
		data8.left = new FormAttachment(0,12);
		label4.setLayoutData(data8);
		
		FormData data9 = new FormData();
		data9.top = new FormAttachment(label3,12);
		data9.left=new FormAttachment(label1,12);
		radio1.setLayoutData(data9);

		FormData data10 = new FormData();
		data10.top = new FormAttachment(label3,12);
		data10.left=new FormAttachment(radio1,12);
		radio2.setLayoutData(data10);
		       
		FormData data11 = new FormData();
		data11.top = new FormAttachment(label4,12);
		data11.left = new FormAttachment(0,12);
		label5.setLayoutData(data11);
		
		FormData data12 = new FormData();
		data12.top = new FormAttachment(label4,12);
		data12.left=new FormAttachment(label1,12);
		data12.right=new FormAttachment(100,-12);
		comboKeyField.setLayoutData(data12);
		
		
		radio1.setSelection(hibernateTypeSelected);
		radio2.setSelection(!hibernateTypeSelected);
		if(hibernateTypeSelected){
			comboKeyField.setItems(hibernateKeyFields);
		}else{
			comboKeyField.setItems(javaKeyFields);
		}
		if(idType!=null)
			comboKeyField.setText(idType);
		else
			comboKeyField.select(1);

		
		return parentComposite;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		
		hibernateTypeSelected=radio1.getSelection();
		outDirText=dirText.getText();
		idType=comboKeyField.getText();
		generator=comboSchemaExport.getText();
		pkgName=packageText.getText();
		super.okPressed();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents =super.createContents(parent);
		setTitle("Exporting "+node.getTableInfo().getQualifiedName()); //$NON-NLS-1$
		setMessage(""); //$NON-NLS-1$
		return contents;
	}

}
