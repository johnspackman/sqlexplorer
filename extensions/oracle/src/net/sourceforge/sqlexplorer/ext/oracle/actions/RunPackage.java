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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import net.sourceforge.sqlexplorer.ext.oracle.InternalFunc;
import net.sourceforge.sqlexplorer.ext.oracle.PackageBodyNode;
import net.sourceforge.sqlexplorer.ext.oracle.PackageNode;
import net.sourceforge.sqlexplorer.ext.oracle.dialogs.RunPackageWizard;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.action.Action;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;


public class RunPackage extends Action {
	SessionTreeNode sessionTreeNode;
	String name;
	String owner;
	/**
	 * @param sessionTreeNode
	 * @param node
	 */
	public RunPackage(SessionTreeNode sessionTreeNode, PackageBodyNode node) {
		
		this.sessionTreeNode=sessionTreeNode;
		this.name=node.toString();
		this.owner=node.getOwner();
	}

	/**
	 * @param sessionTreeNode
	 * @param node
	 */
	public RunPackage(SessionTreeNode sessionTreeNode, PackageNode node) {
		this.sessionTreeNode=sessionTreeNode;
		this.name=node.toString();
		this.owner=node.getOwner();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		final String sql="SELECT   distinct object_name,overload FROM SYS.ALL_ARGUMENTS WHERE OWNER = ? and object_id= "+
		"(SELECT OBJECT_ID FROM SYS.ALL_OBJECTS WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE='PACKAGE')";
		ResultSet rs=null;
		ArrayList lsFunctions=new ArrayList();
		try{
			PreparedStatement ps=sessionTreeNode.getConnection().prepareStatement(sql);
			ps.setString(1,owner);
			ps.setString(2,owner);
			ps.setString(3,name);
			rs=ps.executeQuery();

			while(rs.next()){
				InternalFunc iFunc=new InternalFunc();
				iFunc.txt=rs.getString(1);
				iFunc.overload=rs.getString(2);
				lsFunctions.add(iFunc);
			}
			rs.close();
			ps.close();
		}catch(Throwable e){
			//e.printStackTrace();
		}finally{
			try{
				rs.close();
			}catch(Throwable e){
			}	
		}
		RunPackageWizard rpw=new RunPackageWizard(lsFunctions,sessionTreeNode,name,owner);
		rpw.init(SQLExplorerPlugin.getDefault().getWorkbench(), null);
		WizardDialog runPackageWizard = new WizardDialog(null, rpw){
			protected int getShellStyle() {
				
				return super.getShellStyle()|SWT.RESIZE;
			}

		};
		runPackageWizard.open();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#getText()
	 */
	public String getText() {
		return "Execute...";
	}
}

