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
package net.sourceforge.sqlexplorer.ext.oracle.actions;

import java.util.ArrayList;

import net.sourceforge.sqlexplorer.ext.oracle.ProcNode;
import net.sourceforge.sqlexplorer.ext.oracle.dialogs.RunProcWizard;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;


public class RunProcedure extends Action {
	SessionTreeNode sessionTreeNode;
	ProcNode node;
	public RunProcedure(SessionTreeNode sessionTreeNode, ProcNode node) {
		this.sessionTreeNode=sessionTreeNode;
		this.node=node;
	}
	public String getText() {
		
		return "Execute";
	}
	public void run(){
		if(!node.isValid()){
			MessageDialog.openError(null,"Can't Execute","The status is invalid");
			return;
		}
		ArrayList paramList=node.getParamInfoList();
		String sql="{call "+node.getOwner()+"."+node.toString()+"(";
		for(int i=0;i<paramList.size();i++)
			if(i==0)
				sql=sql+"?";
			else
				sql=sql+",?";
		sql=sql+")}";
		
		
		RunProcWizard rpw=new RunProcWizard(null,sessionTreeNode, node, sql,paramList);
		rpw.init(SQLExplorerPlugin.getDefault().getWorkbench(), null);
		WizardDialog runFunctionWizard = new WizardDialog(null, rpw){
			protected int getShellStyle() {

				return super.getShellStyle()|SWT.RESIZE;
			}
		};
		runFunctionWizard.open();
		
		//CallableStatement cs=conn.prepareCall("{call gxxk0000.set_user_name(?)}");
	}
}
