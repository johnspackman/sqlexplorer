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

import net.sourceforge.sqlexplorer.ext.oracle.FunctionNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;


public class RunFunctionWizard extends Wizard implements IWorkbenchWizard {
	SessionTreeNode sessionNode;
	FunctionNode node;
	String sql;
	ArrayList paramList;
	ArrayList textInputList=new ArrayList();
	InternalWizardPage funcRun=null;
	public boolean performFinish() {
		funcRun.execute();
		return true;
	}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
		 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	public RunFunctionWizard(Object object, SessionTreeNode sessionTreeNode, FunctionNode node, String sql, ArrayList paramList) {
		this.setWindowTitle("Running Function");
		this.node=node;
		this.sessionNode=sessionTreeNode;
		this.sql=sql;
		this.paramList=paramList;
	}
	public void addPages() {
		funcRun=new InternalWizardPage("Run Function "+node.toString(),sessionNode,sql,paramList);
		addPage(funcRun);
	}
}
