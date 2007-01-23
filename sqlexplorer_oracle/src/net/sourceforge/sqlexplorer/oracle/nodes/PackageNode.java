/*
 * Copyright (C) 2007 Patrac Vlad Sebastian
 * http://sourceforge.net/projects/eclipsesql
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

/**
 * Reprezents a package node in Database Tree view 
 * 
 * @author Patras Vlad
 */

package net.sourceforge.sqlexplorer.oracle.nodes;

import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

public class PackageNode extends AbstractNode {
	
	public PackageNode(INode parent, String name, SessionTreeNode sessionNode, String owner) {
		_type = "package";
		initialize(parent, name, sessionNode);
	}

	public void loadChildren() {
		addChildNode(new ProcedureFolder(this, _sessionNode));
	}
	
}
