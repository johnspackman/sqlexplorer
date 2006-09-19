package net.sourceforge.sqlexplorer.sessiontree.model.utility;

/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.CatalogNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.DatabaseNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.SchemaNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.sqlexplorer.sqleditor.SQLCodeScanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;

public class Dictionary {

    // TODO check if we need to add more types or remove restriction completely?
    private static final String[] SUPPORTED_CONTENT_ASSIST_TYPES = new String[] {"TABLE_FOLDER", "TABLE_TYPE", "VIEW_FOLDER", "VIEW_TYPE"};

    private static final Log _logger = LogFactory.getLog(Dictionary.class);


    public Dictionary() {

    }

    private static TernarySearchTree keywordsTree = new TernarySearchTree();
    static {
        String[] str = SQLCodeScanner.getFgKeywords();
        for (int i = 0; i < str.length; i++)
            keywordsTree.put(str[i], str[i]);

    }

    private TernarySearchTree tree = new TernarySearchTree();

    private TernarySearchTree catalogSchemaTree = new TernarySearchTree();

    private TernarySearchTree externalObjectTree = new TernarySearchTree();

    private HashMap realTables = new HashMap();

    private HashMap realCatalogSchemas = new HashMap();

    private HashMap realExternalObjects = new HashMap();

    private HashMap col_map = new HashMap();

    private static int ROOT_WORK_UNIT = 1000;


    public void putTableName(String key, Object value) {

        tree.put(key.toLowerCase(), value);
        realTables.put(key.toLowerCase(), key);
    }


    public void putCatalogSchemaName(String key, Object value) {

        catalogSchemaTree.put(key.toLowerCase(), value);
        realCatalogSchemas.put(key.toLowerCase(), key);
    }


    public void putExternalObjectName(String key, Object value) {

        externalObjectTree.put(key.toLowerCase(), value);
        realExternalObjects.put(key.toLowerCase(), key);
    }


    public Object getByTableName(String key) {

        return tree.get(key);
    }


    public Object getByCatalogSchemaName(String key) {

        return catalogSchemaTree.get(key);
    }


    public Object getByExternalObjectName(String key) {

        return catalogSchemaTree.get(key);
    }


    public void putColumnsByTableName(String key, Object value) {

        col_map.put(key.toLowerCase(), value);
    }


    public Object getColumnListByTableName(String key) {

        return col_map.get(key);
    }


    public Iterator getTableNames() {

        return realTables.keySet().iterator();
    }


    public Iterator getCatalogSchemaNames() {

        return realCatalogSchemas.keySet().iterator();
    }


    public Iterator getExternalObjectNames() {

        return realExternalObjects.keySet().iterator();
    }


    public ArrayList getTableObjectList(String tableName) {

        return (ArrayList) tree.get(tableName.toLowerCase());
    }


    public String[] matchTablePrefix(String prefix) {

        prefix = prefix.toLowerCase();
        DoublyLinkedList linkedList = tree.matchPrefix(prefix);
        int size = linkedList.size();
        DoublyLinkedList.DLLIterator iterator = linkedList.iterator();
        String[] result = new String[size];
        int k = 0;
        while (iterator.hasNext()) {
            result[k++] = (String) realTables.get(iterator.next());
        }
        return result;
    }


    public String[] matchCatalogSchemaPrefix(String prefix) {

        prefix = prefix.toLowerCase();
        DoublyLinkedList linkedList = catalogSchemaTree.matchPrefix(prefix);
        int size = linkedList.size();
        DoublyLinkedList.DLLIterator iterator = linkedList.iterator();
        String[] result = new String[size];
        int k = 0;
        while (iterator.hasNext()) {
            result[k++] = (String) realCatalogSchemas.get(iterator.next());
        }
        return result;
    }


    public String[] matchExternalObjectPrefix(String prefix) {

        prefix = prefix.toLowerCase();
        DoublyLinkedList linkedList = externalObjectTree.matchPrefix(prefix);
        int size = linkedList.size();
        DoublyLinkedList.DLLIterator iterator = linkedList.iterator();
        String[] result = new String[size];
        int k = 0;
        while (iterator.hasNext()) {
            result[k++] = (String) realExternalObjects.get(iterator.next());
        }
        return result;
    }


    public static String[] matchKeywordsPrefix(String prefix) {

        prefix = prefix.toLowerCase();
        DoublyLinkedList linkedList = keywordsTree.matchPrefix(prefix);
        int size = linkedList.size();
        DoublyLinkedList.DLLIterator iterator = linkedList.iterator();
        String[] result = new String[size];
        int k = 0;
        while (iterator.hasNext()) {
            result[k++] = (String) iterator.next();
        }
        return result;
    }


    /**
     * Loads the persisted dictionary from a previous session.
     * 
     * @param dbNode DatabaseNode for which to load the dictionary
     * @return true if dictionary was found and loaded
     */
    public boolean restore(DatabaseNode dbNode, IProgressMonitor monitor) throws InterruptedException {

        // TODO implement
        return false;
    }


    /**
     * Persists this dictionary so that it can be reused in next sessions
     * without having to be reloaded from database.
     */
    public void store() {

        // TODO implement
    }


    /**
     * Perform full load of dictionary for dbNode
     * 
     * @param dbNode DatabaseNode of which to load dictionary information
     * @param monitor ProgressMonitor displayed whilst loading
     * @throws InterruptedException If user cancelled loading
     */
    public void load(DatabaseNode dbNode, IProgressMonitor monitor) throws InterruptedException {

        try {

            // check for cancellation by user
            if (monitor.isCanceled()) {
                throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled"));
            }

            INode[] children = dbNode.getChildNodes();

            if (children == null) {
                return;
            }

            // start task with a 1000 work units for every root node
            monitor.beginTask(dbNode.getSession().toString(), children.length * ROOT_WORK_UNIT);

            for (int i = 0; i < children.length; i++) {

                // check for cancellation by user
                if (monitor.isCanceled()) {
                    throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled"));
                }

                INode node = (INode) children[i];

                if (node instanceof SchemaNode || node instanceof CatalogNode) {
                    loadSchemaCatalog(node, monitor);
                }

            }

            // store dictionary immediately so that
            // we can resuse it if a second session is opened
            store();

        } finally {
            monitor.done();
        }

    }


    /**
     * Load dictionary data for catalog
     * 
     * @param node catalognode to load
     * @param monitor ProgressMonitor displayed whilst loading
     * @throws InterruptedException If user cancelled loading
     */
    private void loadSchemaCatalog(INode iNode, IProgressMonitor monitor) throws InterruptedException {

        if (_logger.isDebugEnabled()) {
            _logger.debug("Loading dictionary: " + iNode.getName());
        }

        // check for cancellation by user
        if (monitor.isCanceled()) {
            throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled"));
        }

        putCatalogSchemaName(iNode.toString(), iNode);
        monitor.subTask(iNode.getName());

        INode[] children = iNode.getChildNodes();

        if (children != null) {

            // check for cancellation by user
            if (monitor.isCanceled()) {
                throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled"));
            }

            // divide work equally between type nodes
            int typeNodeWorkUnit = ROOT_WORK_UNIT / SUPPORTED_CONTENT_ASSIST_TYPES.length;
            int typeNodeWorkCompleted = 0;

            for (int i = 0; i < children.length; i++) {

                INode typeNode = children[i];

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Loading dictionary: " + typeNode.getName());
                }

                // only load a few types like tables and view nodes into the
                // dictionary
                boolean isIncludedInContentAssist = false;
                for (int j = 0; j < SUPPORTED_CONTENT_ASSIST_TYPES.length; j++) {
                    if (typeNode.getType().equalsIgnoreCase(SUPPORTED_CONTENT_ASSIST_TYPES[j])) {
                        isIncludedInContentAssist = true;
                    }
                }
                if (!isIncludedInContentAssist) {
                    continue;
                }

                monitor.subTask(typeNode.getName());

                // check for cancellation by user
                if (monitor.isCanceled()) {
                    throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled"));
                }

                INode tableNodes[] = typeNode.getChildNodes();
                if (tableNodes != null) {

                    // check for cancellation by user
                    if (monitor.isCanceled()) {
                        throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled"));
                    }

                    int tableNodeWorkUnit = typeNodeWorkUnit / tableNodes.length;

                    for (int j = 0; j < tableNodes.length; j++) {

                        INode tableNode = tableNodes[j];

                        if (_logger.isDebugEnabled()) {
                            _logger.debug("Loading dictionary: " + tableNode.getName());
                        }

                        if (monitor != null) {

                            monitor.worked(tableNodeWorkUnit);
                            typeNodeWorkCompleted = typeNodeWorkCompleted + tableNodeWorkUnit;

                            if (_logger.isDebugEnabled()) {
                                _logger.debug("worked table: " + tableNodeWorkUnit + ", total type work: "
                                        + typeNodeWorkCompleted);
                            }

                            monitor.subTask(tableNode.getQualifiedName());

                            // check for cancellation by user
                            if (monitor.isCanceled()) {
                                throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled"));
                            }
                        }

                        // add table name
                        ArrayList tableDetails = (ArrayList) getByTableName(tableNode.getName());
                        if (tableDetails == null) {
                            tableDetails = new ArrayList();
                            putTableName(tableNode.getName(), tableDetails);
                        }
                        tableDetails.add(tableNode);

                        // add column names
                        if (tableNode instanceof TableNode) {

                            TreeSet columnNames = new TreeSet();
                            List columns = ((TableNode) tableNode).getColumnNames();
                            if (columns != null) {

                                Iterator it = columns.iterator();
                                while (it.hasNext()) {
                                    columnNames.add(it.next());
                                }
                            }
                            putColumnsByTableName(tableNode.getName(), columnNames);
                        }

                    }
                }

                if (typeNodeWorkCompleted < typeNodeWorkUnit) {
                    // consume remainder of work for this type node

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("consuming remainder: " + (typeNodeWorkUnit - typeNodeWorkCompleted));
                    }

                    monitor.worked(typeNodeWorkUnit - typeNodeWorkCompleted);
                }
                typeNodeWorkCompleted = 0;

            }

        }

    }

}
