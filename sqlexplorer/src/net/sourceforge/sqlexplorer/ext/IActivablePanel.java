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
 
package net.sourceforge.sqlexplorer.ext;


import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public interface IActivablePanel {
	public void activate();

	/**
	 * @return
	 */
	public String getText();

	/**
	 * @param sessionTreeNode
	 * @param tn
	 * @param tabFolder
	 * @return
	 */
	public Control create(SessionTreeNode sessionTreeNode, IDbModel tn, Composite parent);
}
