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

import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Database catalog node.
 * 
 * @author Davy Vanherbergen
 *
 */
public class CatalogNode extends AbstractNode {

	private static final Log _logger = LogFactory.getLog(CatalogNode.class);
	
    /**
     * Create new database Catalog node.
     * 
     * @param parent node
     * @param name of this node
     * @param sessionNode session for this node
     */
    public CatalogNode(INode parent, String name, SessionTreeNode sessionNode) {
        
        _sessionNode = sessionNode;
        _parent = parent;
        _name = name;

    }

    
    
    /**
     * Location extenstion nodes for a given tableType
     * @param tableType for which to find extension node
     * @return INode or null if no extensions found
     */
    private INode findExtensionNode(String tableType) {
        
        String databaseProductName = getSession().getRoot().getDatabaseProductName().toLowerCase().trim();
        
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("net.sourceforge.sqlexplorer", "node");
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {

            IExtension e = extensions[i];

            IConfigurationElement[] ces = e.getConfigurationElements();

            for (int j = 0; j < ces.length; j++) {
                try {
                    
                    // include only nodes that are attachted to the schema node..
                    String parent = ces[j].getAttribute("parent-node");
                    if (parent.indexOf("catalog") == -1) {
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
                        
                        String regex = product.replace("*", ".*");
                        if (databaseProductName.matches(regex)) {
                            isValidProduct = true;
                            break;
                        }
                        
                    }
                    
                    if (!isValidProduct) {
                        continue;
                    }
                    
                    // check if it is the correct type
                    String type = ces[j].getAttribute("table-type").trim();
                    if (!type.equalsIgnoreCase(tableType)) {
                        continue;
                    }
                    
                    AbstractNode childNode = (AbstractNode) ces[j].createExecutableExtension("class");                        
                    childNode.setParent(this);
                    childNode.setSession(_sessionNode);
                    
                    return childNode;
                    
                    
                } catch (Throwable ex) {
                    SQLExplorerPlugin.error("Could not create child node", ex);
                }
            }
        }
        
        return null;
    }
    
    
    /* (non-Javadoc)
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {
               
        try {
            
        	ITableInfo[] tables = null;
        	String[] tableTypes = _sessionNode.getConnection().getSQLMetaData().getTableTypes();
        	
        	try {        		
        		tables = _sessionNode.getConnection().getSQLMetaData().getTables(_name, null, "%", tableTypes);
        	} catch (Throwable e) {
        		_logger.debug("Loading all tables at once is not supported");
        	}
        	           
            for (int i = 0; i < tableTypes.length; ++i) {

                INode childNode = findExtensionNode(tableTypes[i]);
                if (childNode != null) {
                    addChildNode(childNode);
                } else {
                    addChildNode(new TableTypeNode(this, tableTypes[i], _sessionNode, tables));    
                }
            }
            
        } catch (Throwable e) {
            SQLExplorerPlugin.error("Could not load childnodes for " + _name, e);
        }        
    }


    /**
     * Returns "catalog" as the type for this node.   
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getType()
     */
    public String getType() {
        return "catalog";
    }

    
    /* (non-Javadoc)
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getImage()
     */
    public Image getImage() {        
        return ImageDescriptor.createFromURL(SqlexplorerImages.getCatalogNodeIcon()).createImage();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.INode#getUniqueIdentifier()
     */
    public String getUniqueIdentifier() {
        return getQualifiedName();
    }

    
}
