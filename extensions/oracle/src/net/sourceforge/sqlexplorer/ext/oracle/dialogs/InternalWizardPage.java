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

import java.util.ArrayList;

import net.sourceforge.sqlexplorer.ext.oracle.ParamObj;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class InternalWizardPage extends WizardPage {
	
	public void execute() {
		Utils.execute(this.sessionNode,sql,paramList,textInputList);		
	}
	/**
	 * @param pageName
	 */
	protected InternalWizardPage(String pageName, SessionTreeNode sessionTreeNode, String sql, ArrayList paramList) {
		super(pageName);
		this.setTitle(pageName);
		this.sessionNode=sessionTreeNode;
		this.sql=sql;
		this.paramList=paramList;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	
	public void createControl(Composite parentComposite) {
		Composite pp=new Composite(parentComposite,SWT.NULL);
		pp.setLayout(new FillLayout());
		pp.setLayoutData(new GridData(GridData.FILL_BOTH));
		final ScrolledComposite sc1 = new ScrolledComposite(pp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		sc1.setExpandHorizontal(true);
		sc1.setExpandVertical(true);
		
		final Composite c1 = new Composite(sc1, SWT.NULL);
		sc1.setContent(c1);
		
		FormLayout layout = new FormLayout();
		layout.marginHeight = layout.marginWidth =10; 
		c1.setLayout(layout);
				
		for(int i=0,s=paramList.size();i<s;i++){
			ParamObj pObj=(ParamObj)paramList.get(i);
			
			if(pObj.inOut.equalsIgnoreCase("IN")|| pObj.inOut.equals("IN/OUT")){
				Label lb=new Label(c1,SWT.NULL);
				lb.setText(pObj.argumentName+" ["+pObj.dataType+"]");
				Text txt=new Text(c1,SWT.BORDER);
				txt.setText("");
				
				
				if(textInputList.isEmpty()){
					FormData data1 = new FormData();
					data1.left = new FormAttachment(0,12);
					data1.top=new FormAttachment(0,12);
					lb.setLayoutData(data1);
					data1 = new FormData();
					data1.top=new FormAttachment(0,12);
					data1.left = new FormAttachment(lb,12);
					data1.right = new FormAttachment(100,-12);
					txt.setLayoutData(data1);
				}else{
					FormData data1 = new FormData();
					data1.left = new FormAttachment(0,12);
					data1.top=new FormAttachment((Control)textInputList.get(textInputList.size()-1),12);
					lb.setLayoutData(data1);
					data1 = new FormData();
					data1.top=new FormAttachment((Control)textInputList.get(textInputList.size()-1),12);
					data1.left = new FormAttachment(lb,12);
					data1.right = new FormAttachment(100,-12);
					txt.setLayoutData(data1);	
				}
					
				textInputList.add(txt);
			}
				
		}
		sc1.setMinSize(c1.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		super.setControl(pp);
	}
	SessionTreeNode sessionNode;
	String sql;
	ArrayList paramList;
	ArrayList textInputList=new ArrayList();
}
