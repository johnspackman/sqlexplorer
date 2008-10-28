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

package net.sourceforge.sqlexplorer.mssql.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.swt.graphics.Image;

public class JobNodeStep extends AbstractNode {

	protected int _id;

	public JobNodeStep(String name) {
		super(name);
		_type = "JobNodeStep";
	}

	public JobNodeStep( INode parent, String name, int id, MetaDataSession session){
		super( parent, name, session, "JobNodeStep");
		_id = id;
	}

	@Override
	public boolean isEndNode() {
		return true;
	}

	@Override
	public Image getImage() {
		return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.mssql", Messages.getString("mssql.images.job"));
	}

	@Override
	public String getQualifiedName() {
		return _name;
	}

	public int getID(){
		return _id;
	}

	@Override
	public String getUniqueIdentifier() {
		return getParent().getQualifiedName() + "." + getName();
	}

	@Override
	public void loadChildren() {
	}

}
