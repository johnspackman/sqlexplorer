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
package net.sourceforge.sqlexplorer.dbstructure.nodes;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;

public class TableNode extends AbstractNode {

    private ITableInfo _tableInfo;

    private List _columnNames = new ArrayList();

    boolean _includeColumns = true;


    /**
     * Create new database table node.
     * 
     * @param parent node
     * @param name of this node
     * @param sessionNode session for this node
     */
    public TableNode(INode parent, String name, SessionTreeNode sessionNode, ITableInfo tableInfo) {

        _tableInfo = tableInfo;
        _sessionNode = sessionNode;
        _parent = parent;
        _name = name;
        _includeColumns = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.INCLUDE_COLUMNS_IN_TREE);

        _imageKey = "Images.TableNodeIcon";
    }


    /**
     * @return // TODO fix this for sql completion?
     */
    public String getTableDesc() {
        return getTableInfo().getQualifiedName();
    }


    /**
     * @return TableInfo for this node
     */
    public ITableInfo getTableInfo() {
        return _tableInfo;
    }


    /**
     * @return true if this node is a synonym
     */
    public boolean isSynonym() {
        return _tableInfo.getType().equalsIgnoreCase("SYNONYM");
    }


    /**
     * @return true if this node is a table
     */
    public boolean isTable() {
        return _tableInfo.getType().equalsIgnoreCase("TABLE");
    }


    /**
     * @return true if this node is a view
     */
    public boolean isView() {
        return _tableInfo.getType().equalsIgnoreCase("VIEW");
    }


    /**
     * 
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {

        boolean includeColumns = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.INCLUDE_COLUMNS_IN_TREE);

        if (includeColumns) {
            try {
                Iterator it = getColumnNames().iterator();
                while (it.hasNext()) {
                    addChildNode(new TableColumnNode(this, (String) it.next(), _sessionNode));
                }
            } catch (Exception e) {
                SQLExplorerPlugin.error("Could not create child nodes for " + getName(), e);
            }
        }

    }


    /**
     * Returns the table info type as the type for this node.
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getType()
     */
    public String getType() {
        return _tableInfo.getType();
    }


    /**
     * @return Qualified table name
     */
    public String getQualifiedName() {
        return _tableInfo.getQualifiedName();
    }




    /**
     * @return List of column names for this table.
     */
    public List getColumnNames() {
        
        if (_columnNames.size() == 0) {
        
            try {
                ResultSet resultSet = _sessionNode.getMetaData().getColumns(_tableInfo);
                while (resultSet.next()) {
                    _columnNames.add(resultSet.getString(4));
                }
    
            } catch (Exception e) {
                SQLExplorerPlugin.error("Could not load column names", e);
            }
        
        }
        
        return _columnNames;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getUniqueIdentifier() {
        return getQualifiedName();
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#isEndNode()
     */
    public boolean isEndNode() {
        if (!_includeColumns) {
            return true;
        } else {
            return false;
        }
    }
}
