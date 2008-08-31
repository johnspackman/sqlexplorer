/*
 * Copyright (C) 2006 Davy Vanherbergen
 * dvanherbergen@users.sourceforge.net
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
package net.sourceforge.sqlexplorer.oracle.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Comparator;

import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.ColumnNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

/**
 * @author Davy Vanherbergen
 * 
 */
public class TableIndexNode extends AbstractNode {

    private TableNode _parentTable;


    public TableIndexNode(INode parent, String name, MetaDataSession session, TableNode parentTable) {
    	super(parent, name, session, "index");
        _parentTable = parentTable;
        setImageKey("Images.IndexIcon");
    }


    public Comparator<INode> getComparator() {

        // we don't want any sorting here.
        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getQualifiedName() {

        return getSchemaOrCatalogName() + "." + _name;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getUniqueIdentifier() {

        return getSchemaOrCatalogName() + "." + _name;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {

        SQLConnection connection = null;
        ResultSet rs = null;
        PreparedStatement pStmt = null;

        try {
        	connection = getSession().grabConnection();
        	
            // use prepared statement
            pStmt = connection.prepareStatement("select column_name , descend from sys.all_ind_columns where index_name = ? and table_owner = ? and table_name = ? order by column_position");
            pStmt.setString(1, getName());
            pStmt.setString(2, getSchemaOrCatalogName());
            pStmt.setString(3, _parentTable.getName());

            rs = pStmt.executeQuery();

            while (rs.next()) {
                String columnName = rs.getString(1);
                String sort = rs.getString(2);

                ColumnNode col = new ColumnNode(this, columnName, _session, _parentTable, false);
                col.setLabelDecoration(sort);
                addChildNode(col);
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
            if (connection != null)
           		getSession().releaseConnection(connection);
        }

    }

}
