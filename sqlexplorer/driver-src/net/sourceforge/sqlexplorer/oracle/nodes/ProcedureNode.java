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
 * Reprezents a procedure node in Database Tree view 
 * 
 * @author Patras Vlad
 */

package net.sourceforge.sqlexplorer.oracle.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.swt.graphics.Image;

public class ProcedureNode extends AbstractNode {
	
	protected String _package;

	/*
	 * Overload number for procedure
	 * 0 if it is not overloaded
	 */
	protected int _overload;

	
	public ProcedureNode() {
		super(null, null, null, "PROCEDURE");
	}

	public ProcedureNode(INode parent, String name, MetaDataSession session) {
		super(parent, name, session, "PROCEDURE");
	}

	public int getOverload() {
		return _overload;
	}

	public void setOverload(int overload) {
		_overload = overload;
	}

	public String getPackage() {
		return _package;
	}

	public void setPackage(String packageName) {
		_package = packageName;
		_type = "PACKAGE_PROCEDURE";
	}
	
	public Image getImage() {
		return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.oracle", Messages.getString("oracle.images.procedure"));
	}	

	public String getName() {
		return _name + ((_overload == 0) ? "" : (" [" + _overload + "]"));
	}
	
	public String getQualifiedName() {
		return _name;
	}
	
	public String getUniqueIdentifier() {
		return getParent().getQualifiedName() + "." + getName();
	}

	public boolean isEndNode() {
		return true;
	}
	
	public void loadChildren() {
	}

	
}
