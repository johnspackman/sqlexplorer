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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import net.sourceforge.sqlexplorer.ext.oracle.InternalFunc;
import net.sourceforge.sqlexplorer.ext.oracle.ParamObj;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;


public class RunPackageWizard extends Wizard implements IWorkbenchWizard {
	ArrayList lsFunctions;
	String name, owner;
	SessionTreeNode session;
	SelectFunctionPage selectFunc;
	LastWizardPage  lwp;
	/**
	 * @param lsFunctions
	 */
	public RunPackageWizard(ArrayList lsFunctions,SessionTreeNode session, String name,String owner) {
		this.setWindowTitle("Running "+name);
		this.lsFunctions=lsFunctions;
		this.name=name;
		this.owner=owner;
		this.session=session;
		this.setForcePreviousAndNextButtons(true);
		String sql1="SELECT argument_name, DATA_TYPE, data_length, data_precision,in_out,sequence FROM SYS.ALL_ARGUMENTS WHERE OWNER = ? and data_level=0 and object_id=(SELECT OBJECT_ID FROM SYS.ALL_OBJECTS WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE='PACKAGE') "+  
		"  and object_name=? and overload is null and sequence >0 order by sequence asc";
		
		String sql2="SELECT argument_name, DATA_TYPE, data_length, data_precision,in_out,sequence FROM SYS.ALL_ARGUMENTS WHERE OWNER = ? and data_level=0 and object_id=(SELECT OBJECT_ID FROM SYS.ALL_OBJECTS WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE='PACKAGE') "+  
		"  and object_name=? and overload =? and sequence >0 order by sequence asc";
		
		for(int i=0,sz=lsFunctions.size();i<sz;i++){
			InternalFunc iFunc=(InternalFunc)lsFunctions.get(i);
			String sql=null;
			boolean overloaded=false;
			if(iFunc.overload==null){
				sql=sql1;
			}else{
				overloaded=true;
				sql=sql2;
			}
			ResultSet rs=null;
			try{
				PreparedStatement ps=session.getConnection().prepareStatement(sql);
				ps.setString(1,owner);
				ps.setString(2,owner);
				ps.setString(3,name);
				ps.setString(4,iFunc.txt);
				if(overloaded)
					ps.setString(5,iFunc.overload);
				rs=ps.executeQuery();
				ArrayList paramLs=new ArrayList();
				while(rs.next()){
					ParamObj pObj=new ParamObj();
					pObj.argumentName=rs.getString(1);
					pObj.dataType=rs.getString(2);
					pObj.dataLength=rs.getInt(3);
					if(rs.wasNull())
						pObj.dataLength=-1;

					pObj.dataPrecision=rs.getInt(4);
					if(rs.wasNull())
						pObj.dataPrecision=-1;
					pObj.inOut=rs.getString(5);
					paramLs.add(pObj);
				}
				iFunc.paramLs=paramLs;
				rs.close();
				ps.close();
			}catch(Throwable e){
				e.printStackTrace();
			}finally{
				try{
					rs.close();
				}catch(Throwable e){
				}	
			}

			//if(iFunc.)
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		if(lwp!=null){
			lwp.execute();
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		
		
	}

	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		selectFunc=new SelectFunctionPage("Choose Function or Procedure",lsFunctions,name,owner);
		addPage(selectFunc);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		if(page==selectFunc){	
			int k=selectFunc.getSelected();
			InternalFunc iFunc=(InternalFunc)lsFunctions.get(k);
			
			if(!iFunc.paramLs.isEmpty()&& ((ParamObj)iFunc.paramLs.get(0)).argumentName==null){
				
				String sql="{?=call "+owner+"."+name+"."+iFunc.txt+"(";
				for(int i=1;i<iFunc.paramLs.size();i++)
					if(i==1)
						sql=sql+"?";
					else
						sql=sql+",?";
				sql=sql+")}";
				//pwp.setPageComplete(true);
				//FunctionWizardPage fwp=new FunctionWizardPage("Running function",session,sql,iFunc.paramLs);
				//fwp.setWizard(this);
				lwp=new LastWizardPage(this,"Running function",session,sql,iFunc.paramLs,iFunc.txt);
				lwp.setWizard(this);
				return lwp;
			}else{
				String sql="{call "+owner+"."+name+"."+iFunc.txt+"(";
				for(int i=0;i<iFunc.paramLs.size();i++)
					if(i==0)
						sql=sql+"?";
					else
						sql=sql+",?";
				sql=sql+")}";
				//pwp.setPageComplete(false);
				//ProcWizardPage pwp=new ProcWizardPage("Running procedure",session,sql,iFunc.paramLs);
				//pwp.setWizard(this);
				lwp=new LastWizardPage(this,"Running procedure",session,sql,iFunc.paramLs,iFunc.txt);
				lwp.setWizard(this);
				return lwp;
			}
		}
		return super.getNextPage(page);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#canFinish()
	 */
	public boolean canFinish() {
		return canFinishing;
	}
	boolean canFinishing=false;
	/**
	 * @param b
	 */
	public void setCanFinishing(boolean b) {
		canFinishing = b;
	}

}
class SelectFunctionPage extends WizardPage{

	/**
	 * @param pageName
	 */
	ArrayList lsFunctions;
	String name, owner;
	Table tb;
	protected SelectFunctionPage(String pageName, ArrayList lsFunctions, String name, String owner) {
		super(pageName);
		this.lsFunctions=lsFunctions;
		this.setTitle(pageName);
		this.name=name;
		this.owner=owner;
		this.setPageComplete(false);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite composite) {
		initializeDialogUnits(composite);
		Composite composite1 = new Composite(composite, SWT.NULL);
		composite1.setLayout(new FillLayout());
		tb=new Table(composite1,SWT.SINGLE|SWT.FULL_SELECTION|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER);
		TableColumn tc=new TableColumn(tb,SWT.NULL);
		tc.setText("Function or Procedure");
		for(int i=0;i<lsFunctions.size();i++){
			TableItem tItem=new TableItem(tb,SWT.NULL);
			InternalFunc iFunc=(InternalFunc)lsFunctions.get(i);
			tItem.setText(getFuncText(iFunc.paramLs,iFunc.txt));
		}
		TableLayout tableLayout=new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(1, 200, true));
		tb.setLayout(tableLayout);
		tb.layout();
		tb.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent e) {
				if(tb.getSelectionCount()>0){
					SelectFunctionPage.this.setPageComplete(true);	
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		super.setControl(composite1);
	}
	public int getSelected(){
		return tb.getSelectionIndex();
	}
	public static  String getFuncText(ArrayList paramLs, String txt){
		StringBuffer msgBuf=new StringBuffer(20);
		msgBuf.append(txt+"(");
		boolean funz=false;
		if(!paramLs.isEmpty()&& ((ParamObj)paramLs.get(0)).argumentName==null)
			funz=true;
		if(!funz){
			for(int i=0,sz=paramLs.size();i<sz;i++){
				if(i>0)
					msgBuf.append(",");
				ParamObj po=(ParamObj) paramLs.get(i);
				msgBuf.append(po.argumentName);
			}
			msgBuf.append(")");
		}else{
			for(int i=1,sz=paramLs.size();i<sz;i++){
				if(i>1)
					msgBuf.append(",");
				ParamObj po=(ParamObj) paramLs.get(i);
				msgBuf.append(po.argumentName);
			}
			msgBuf.append(")");
		}
		return msgBuf.toString().toLowerCase();
	}
}
class LastWizardPage extends InternalWizardPage{

	/**
	 * @param pageName
	 */
	RunPackageWizard wizard;
	/**
	 * @param string
	 * @param session
	 * @param sql
	 * @param list
	 */
	
	public LastWizardPage(RunPackageWizard wizard,String pageName, SessionTreeNode session, String sql, ArrayList list,String nameProc) {
		
		super(pageName,session,sql,list);
		setTitle(pageName);
		this.wizard=wizard;
		this.setPageComplete(true);
		this.setMessage(SelectFunctionPage.getFuncText(list,nameProc));
		wizard.setCanFinishing(true);	
	}

	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getPreviousPage()
	 */
	public IWizardPage getPreviousPage() {
		wizard.setCanFinishing(false);
		return super.getPreviousPage();
	}

}