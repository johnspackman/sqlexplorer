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

import net.sourceforge.sqlexplorer.ext.oracle.AbstractTypeNode;
import net.sourceforge.sqlexplorer.ext.oracle.OraclePlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;


public class RefreshTypeNode extends Action {
	TreeViewer tv;
	AbstractTypeNode node;
	/**
	 * @param node
	 * @param tv
	 */
	public RefreshTypeNode(AbstractTypeNode node, TreeViewer tv) {
		
		this.node=node;
		this.tv=tv;
	}
	public void run(){
		node.refresh();
		tv.refresh();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#getText()
	 */
	public String getText() {
		return "Refresh";
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromURL(OraclePlugin.getUrl("plugins/icons/update.gif"));
	}

}
