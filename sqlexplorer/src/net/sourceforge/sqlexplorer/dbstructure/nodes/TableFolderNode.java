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

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;

/**
 * TableTypeNode can represents a parent node for VIEW, TABLE, .. depending on
 * what the database supports.
 * 
 * @author Davy Vanherbergen
 * 
 */
public class TableFolderNode extends AbstractFolderNode {

    /** all catalog/schema tables */
    private ITableInfo[] _allTables;

    private String _origName;


    /**
     * Create new database table object type node (view, table, etc...)
     * 
     * @param parent node
     * @param name of this node
     * @param sessionNode session for this node
     */
    public TableFolderNode(INode parent, String name, SessionTreeNode sessionNode, ITableInfo[] tables) {

        _allTables = tables;
        _sessionNode = sessionNode;
        _parent = parent;
        _origName = name;

        // cleanup the names a little
        String[] words = _origName.split(" ");
        _name = "";
        for (int i = 0; i < words.length; i++) {
            _name = _name + words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase() + " ";
        }
        _name = _name.trim();

        if (_name.equals("View")) {
            _name = Messages.getString("DatabaseStructureView.view");
        }
        if (_name.equals("Table")) {
            _name = Messages.getString("DatabaseStructureView.table");
        }
    }


    public String getName() {

        return _name;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getQualifiedName()
     */
    public String getQualifiedName() {

        return _origName;
    }


    /**
     * Returns the type for this node. The type is always suffixed with
     * "_FOLDER".
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getType()
     */
    public String getType() {

        return _origName + "_FOLDER";
    }


    /**
     * Load all the children of this table type.
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {

        try {

            ITableInfo[] tables = null;

            if (_allTables != null && _allTables.length != 0) {

                // we have received all tables from parent node, use
                // those for initial load only.

                tables = (ITableInfo[]) _allTables.clone();
                _allTables = null;

            } else {

                // reload only tables specific for this node.

                String catalogName = null;
                String schemaName = null;

                // get catalog name
                if (_parent instanceof CatalogNode) {
                    catalogName = _parent.toString();
                    if (!_parent.hasChildNodes()) {
                        catalogName = null;
                    }
                }

                // get schema name
                if (_parent instanceof SchemaNode) {
                    schemaName = _parent.toString();
                }

                // get all relevant tables
                tables = _sessionNode.getMetaData().getTables(catalogName, schemaName, "%", new String[] {_origName});

            }

            // add child nodes for all relevant tables
            for (int i = 0; i < tables.length; i++) {
                if (tables[i].getType().equalsIgnoreCase(_origName)) {
                    if (!isExcludedByFilter(tables[i].getSimpleName())) {
                        addChildNode(new TableNode(this, tables[i].getSimpleName(), _sessionNode, tables[i]));
                    }
                }
            }

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Could not load child nodes for " + _name, e);
        }
    }

}
