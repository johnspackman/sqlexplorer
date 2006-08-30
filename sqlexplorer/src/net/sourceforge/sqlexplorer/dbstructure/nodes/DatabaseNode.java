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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SQLAlias;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.util.TextUtil;
import net.sourceforge.squirrel_sql.fw.sql.SQLDatabaseMetaData;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Root node for a database. ChildNodes can be filtered based on expressions in
 * the alias.
 * 
 * @author Davy Vanherbergen
 */
public class DatabaseNode extends AbstractNode {

    private SQLAlias _alias;

    private List _childNames = new ArrayList();

    private String _databaseProductName = "";

    private String[] _filterExpressions;

    private boolean _supportsCatalogs = false;

    private boolean _supportsSchemas = false;

    private String _databaseVersion = "";
    

    /**
     * Create a new database node with the given name
     * 
     * @param name
     * @param alias
     */
    public DatabaseNode(String name, SessionTreeNode session) {

        _name = name;
        _sessionNode = session;
        _alias = (SQLAlias) _sessionNode.getAlias();
        _imageKey = "Images.DatabaseIcon";
        
        SQLDatabaseMetaData metadata = _sessionNode.getMetaData();

        try {
            if (metadata.supportsCatalogs()) {
                _supportsCatalogs = true;
            }
            if (metadata.supportsSchemas()) {
                _supportsSchemas = true;
            }
            _databaseProductName = metadata.getDatabaseProductName();
            _databaseVersion = " [v" + metadata.getJDBCMetaData().getDatabaseMajorVersion() + "." 
                + metadata.getJDBCMetaData().getDatabaseMinorVersion() + "]";
            
        } catch (Exception e) {
            SQLExplorerPlugin.error("Error loading database product name.", e);
        } catch (AbstractMethodError e) {
            SQLExplorerPlugin.error("Error loading database product name.", e);
        }

        
    }


    /**
     * @return List of catalog nodes
     */
    public List getCatalogs() {

        ArrayList catalogs = new ArrayList();

        Iterator it = getChildIterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof CatalogNode) {
                catalogs.add(o);
            }
        }

        return catalogs;
    }


    public String[] getChildNames() {

        if (_childNames.size() == 0) {
            getChildNodes();
        }
        return (String[]) _childNames.toArray(new String[] {});
    }


    public String getDatabaseProductName() {

        return _databaseProductName;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getLabelText()
     */
    public String getLabelText() {

        if (_alias.isFiltered()) {
            return _databaseProductName + " " + _databaseVersion + " " + Messages.getString("DatabaseStructureView.filteredPostfix");
        } else {
            return _databaseProductName + " " + _databaseVersion;
        }
    }


    /**
     * @return List of all database schemas
     */
    public List getSchemas() {

        ArrayList schemas = new ArrayList();

        Iterator it = getChildIterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof SchemaNode) {
                schemas.add(o);
            }
        }

        return schemas;
    }


    /**
     * Returns "database" as the type for this node.
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getType()
     */
    public String getType() {

        return "database";
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getUniqueIdentifier() {

        return getQualifiedName();
    }


    /**
     * Checks if a node name should be filtered.
     * 
     * @param name to check for filtering
     * @return true if the name should be filtered
     */
    private boolean isExcludedByFilter(String name) {

        if (_filterExpressions == null || _filterExpressions.length == 0) {
            // no active filter
            return false;
        }

        for (int i = 0; i < _filterExpressions.length; i++) {

            String regex = _filterExpressions[i].trim();
            regex = TextUtil.replaceChar(regex, '?', ".");
            regex = TextUtil.replaceChar(regex, '*', ".*");

            if (regex.length() != 0 && name.matches(regex)) {
                // we have a match, exclude node..
                return true;
            }
        }

        // no match found
        return false;

    }


    /**
     * Loads childnodes, filtered to a subset of schemas/databases depending on
     * whether a comma separated list of regular expression filters has been
     * set.
     */
    public void loadChildren() {

        _childNames = new ArrayList();

        String metaFilterExpression = _alias.getSchemaFilterExpression();
        if (metaFilterExpression != null && metaFilterExpression.trim().length() != 0) {
            _filterExpressions = metaFilterExpression.split(",");
        } else {
            _filterExpressions = null;
        }

        SQLDatabaseMetaData metadata = _sessionNode.getMetaData();

        try {

            if (_supportsCatalogs) {

                final String[] catalogs = metadata.getCatalogs();
                for (int i = 0; i < catalogs.length; ++i) {
                    _childNames.add(catalogs[i]);
                    if (!isExcludedByFilter(catalogs[i])) {
                        addChildNode(new CatalogNode(this, catalogs[i], _sessionNode));
                    }
                }

            } else if (_supportsSchemas) {

                final String[] schemas = metadata.getSchemas();
                for (int i = 0; i < schemas.length; ++i) {
                    _childNames.add(schemas[i]);
                    if (!isExcludedByFilter(schemas[i])) {
                        addChildNode(new SchemaNode(this, schemas[i], _sessionNode));
                    }
                }

            } else {

                addChildNode(new CatalogNode(this, Messages.getString("NoCatalog_2"), _sessionNode));
            }

            // load extension nodes
            String databaseProductName = _databaseProductName.toLowerCase().trim();

            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint point = registry.getExtensionPoint("net.sourceforge.sqlexplorer", "node");
            IExtension[] extensions = point.getExtensions();

            for (int i = 0; i < extensions.length; i++) {

                IExtension e = extensions[i];

                IConfigurationElement[] ces = e.getConfigurationElements();

                for (int j = 0; j < ces.length; j++) {
                    try {

                        // include only nodes that are attachted to the root
                        // node..
                        String parent = ces[j].getAttribute("parent-node");
                        if (!parent.equalsIgnoreCase("root")) {
                            continue;
                        }

                        boolean isValidProduct = false;
                        String[] validProducts = ces[j].getAttribute("database-product-name").split(",");

                        // include only nodes valid for this database
                        for (int k = 0; k < validProducts.length; k++) {

                            String product = validProducts[k].toLowerCase().trim();

                            if (product.length() == 0) {
                                continue;
                            }

                            if (product.equals("*")) {
                                isValidProduct = true;
                                break;
                            }

                            String regex = TextUtil.replaceChar(product, '*', ".*");
                            if (databaseProductName.matches(regex)) {
                                isValidProduct = true;
                                break;
                            }

                        }

                        if (!isValidProduct) {
                            continue;
                        }

                        String type = ces[j].getAttribute("table-type").trim();

                        AbstractNode childNode = (AbstractNode) ces[j].createExecutableExtension("class");
                        childNode.setParent(this);
                        childNode.setSession(_sessionNode);
                        childNode.setType(type);

                        addChildNode(childNode);

                    } catch (Throwable ex) {
                        SQLExplorerPlugin.error("Could not create child node", ex);
                    }
                }
            }

        } catch (Exception e) {
            SQLExplorerPlugin.error("Error loading children", e);
        }

    }


    /**
     * @return true if this database supports catalogs
     */
    public boolean supportsCatalogs() {

        return _supportsCatalogs;
    }


    /**
     * @return true if this database supports schemas
     */
    public boolean supportsSchemas() {

        return _supportsSchemas;
    }

}
