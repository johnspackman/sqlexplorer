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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Comparator;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.swt.graphics.Image;

public class JobsNode extends AbstractNode {

	protected String _id;

	public JobsNode(String name) {
		super(name);
		_type = "JobsNode";
	}

	public JobsNode( INode parent, String name, String id, MetaDataSession session){
		super( parent, name, session, "JobsNode");
		_id = id;
	}


	@Override
	public Image getImage() {
		return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.mssql", Messages.getString("mssql.images.job"));
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getQualifiedName() {
		return _name;
	}

	public String getID(){
		return _id;
	}

	@Override
	public String getUniqueIdentifier() {
		return getParent().getQualifiedName() + "." + getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Comparator<INode> getComparator() {

        return new Comparator() {

            public int compare(Object arg0, Object arg1) {

                if (arg0 == null || arg1 == null) {
                    return 0;
                }
                Integer id0 = ((JobNodeStep) arg0).getID();
                Integer id1 = ((JobNodeStep) arg1).getID();

                return id0 - id1;
            }
        };
    }

	@Override
	public void loadChildren() {
        ResultSet rs = null;
        PreparedStatement pStmt = null;

        try {
    		SQLConnection connection = getSession().grabConnection();

            // use prepared statement
        	pStmt = connection.prepareStatement(
        			"select step_name, step_id from msdb.dbo.sysjobsteps where job_id = '"+ _id+"' order by step_id" );

            rs = pStmt.executeQuery();

            while (rs.next()) {
            	JobNodeStep newNode = new JobNodeStep(this, rs.getString(1), rs.getInt(2), getSession());
                addChildNode(newNode);
            }

            rs.close();

        } catch (Exception e) {

            SQLExplorerPlugin.error("Couldn't load children for: " + getName(), e);

        } finally {

            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (Exception e) {
                    SQLExplorerPlugin.error("Error closing statement", e);
                }
            }
        }
	}

}
