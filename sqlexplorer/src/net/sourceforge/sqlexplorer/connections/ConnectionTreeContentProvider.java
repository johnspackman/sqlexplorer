/*
 * Copyright (C) 2006 SQL Explorer Development Team
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
package net.sourceforge.sqlexplorer.connections;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.AliasModel;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.RootSessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for database structure outline.
 * 
 * @author Davy Vanherbergen
 */
public class ConnectionTreeContentProvider implements ITreeContentProvider {

    /**
     * Cleanup. We don't do anything here.
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        // noop
    }


    /**
     * Return all the children
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {

        if (parentElement instanceof AliasModel) {
        
            Object[] children = ((AliasModel) parentElement).getElements();
            return children;
            
        } else if (parentElement instanceof ISQLAlias){
            
            ISQLAlias alias = (ISQLAlias) parentElement;
            
            // locate open sessions
            RootSessionTreeNode sessionRoot = SQLExplorerPlugin.getDefault().stm.getRoot();
            Object[] sessions = sessionRoot.getChildren();
            List children = new ArrayList();
            if (sessions != null) {
                for (int i = 0; i < sessions.length; i++) {
                    SessionTreeNode session = (SessionTreeNode) sessions[i];
                    if (session.getAlias().getIdentifier().equals(alias.getIdentifier())) {
                        children.add(session);
                    }
                }
            }
            if (children.size() != 0) {
                return children.toArray(new Object[] {});
            } else {
                return null;
            }
            
        } else {
            // no children for sessions
            return null;
        }
        
    }


    /**
     * Return all the children of an INode element.
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {

        return getChildren(inputElement);
    }


    /**
     * Return the parent of an element.
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {

        if (element instanceof AliasModel) {
            // this is root node
            return null;
            
        } else if (element instanceof ISQLAlias){

            // return root node
            return SQLExplorerPlugin.getDefault().getAliasModel();            

        } else if (element instanceof SessionTreeNode){

            // return alias
            SessionTreeNode node = (SessionTreeNode) element;
            return node.getAlias();
            
        } else {
            
            return null;
        }
    }


    /**
     * Returns true if the INode has children.
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        
        Object[] tmp = getChildren(element);
        
        if (tmp != null) {
            return tmp.length != 0;    
        }
        
        return false;
    }


    /**
     * We don't do anything here..
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // noop
    }

}
