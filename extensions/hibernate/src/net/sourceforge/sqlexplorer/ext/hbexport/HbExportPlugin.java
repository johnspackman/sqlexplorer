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
package net.sourceforge.sqlexplorer.ext.hbexport;

import java.util.ArrayList;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.TreeViewer;

import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.dbviewer.model.TableNode;
import net.sourceforge.sqlexplorer.ext.DefaultSessionPlugin;
import net.sourceforge.sqlexplorer.ext.hbexport.actions.ExportWizard;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

/**
 * @author mazzolini
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class HbExportPlugin extends  DefaultSessionPlugin{

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#load()
	 */
	public void load() {
		
		
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getInternalName()
	 */
	public String getInternalName() {
		return "hibernateExportPlugin";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getDescriptiveName()
	 */
	public String getDescriptiveName() {
		return "Hibernate Export Plugin for JFaceDbc";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getAuthor()
	 */
	public String getAuthor() {
		return "andreamazzolini@users.sourceforge.net";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getWebSite()
	 */
	public String getWebSite() {
		
		return "http://jfacedbc.sourceforge.net";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getVersion()
	 */
	public String getVersion() {
		
		return "0.6";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.ISessionPlugin#getAddedActions(net.sourceforge.jfacedbc.sessiontree.model.SessionTreeNode, net.sourceforge.jfacedbc.dbviewer.IDbModel)
	 */
	public IAction[] getAddedActions(SessionTreeNode sessionNode, IDbModel node, TreeViewer tv) {
		ArrayList ls=new ArrayList();
		if(node instanceof TableNode){
			ls.add(new ExportWizard(sessionNode,(TableNode)node));
		}
		return  (IAction[])ls.toArray(new IAction[0]);
	}
}
