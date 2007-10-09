/*
 * Copyright (C) 2007 SQL Explorer Development Team
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction;
import net.sourceforge.sqlexplorer.connections.actions.NewAliasAction;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.ConnectionListener;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.actions.OpenPasswordConnectDialogAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class ConnectionsView extends ViewPart implements ConnectionListener {
	
	private static final HashSet<SQLConnection> EMPTY_CONNECTIONS = new HashSet<SQLConnection>();
	private static final HashSet<Alias> EMPTY_ALIASES = new HashSet<Alias>();
	private static final HashSet<User> EMPTY_USERS = new HashSet<User>();

    private TreeViewer _treeViewer;

    /**
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, SQLExplorerPlugin.PLUGIN_ID + ".AliasView");

        SQLExplorerPlugin.getDefault().getAliasManager().addListener(this);

        // create outline
        _treeViewer = new TreeViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        getSite().setSelectionProvider(_treeViewer);

        // create action bar
        IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();

        AbstractConnectionTreeAction newAliasAction = new NewAliasAction();
        toolBarMgr.add(newAliasAction);

        // use hash lookup to improve performance
        _treeViewer.setUseHashlookup(true);

        // add content and label provider
        _treeViewer.setContentProvider(new ConnectionTreeContentProvider());
        _treeViewer.setLabelProvider(new ConnectionTreeLabelProvider());

        // set input session
        _treeViewer.setInput(SQLExplorerPlugin.getDefault().getAliasManager());

        // doubleclick on alias opens session
        _treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (selection != null) {
                    if (selection.getFirstElement() instanceof Alias) {
                        Alias alias = (Alias) selection.getFirstElement();
                        OpenPasswordConnectDialogAction openDlgAction = new OpenPasswordConnectDialogAction(alias, alias.getDefaultUser(), false);
                        openDlgAction.run();
                        _treeViewer.refresh();
                    }
                }
            }
        });

        // add context menu
        final ConnectionTreeActionGroup actionGroup = new ConnectionTreeActionGroup();
        MenuManager menuManager = new MenuManager("ConnectionTreeContextMenu");
        menuManager.setRemoveAllWhenShown(true);
        Menu contextMenu = menuManager.createContextMenu(_treeViewer.getTree());
        _treeViewer.getTree().setMenu(contextMenu);

        menuManager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                actionGroup.fillContextMenu(manager);
            }
        });
        _treeViewer.expandToLevel(2);

        parent.layout();

        SQLExplorerPlugin.getDefault().startDefaultConnections(getSite());
    }

	public void connectionClosed(Session session) {
    	modelChanged();
	}

	public void connectionOpened(Session session) {
    	modelChanged();
	}

    public void modelChanged() {
        getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
            	if (!_treeViewer.getTree().isDisposed())
            		_treeViewer.refresh();
            }
        });
    }

	public void dispose() {
        SQLExplorerPlugin.getDefault().getAliasManager().removeListener(this);
        super.dispose();
    }

    public TreeViewer getTreeViewer() {
        return _treeViewer;
    }

	public void refresh() {
		_treeViewer.refresh();
	}
	
	/**
	 * Returns the objects which are currently selected.  NOTE this is package
	 * private and should remain that way - the implementation of the ConnectionsView
	 * is now hidden from the rest of the application (see the getSelectedXxxx() methods
	 * below for a structured API)
	 * @return
	 */
	/*package*/ Object[] getSelected() {
    	IStructuredSelection selection = (IStructuredSelection)_treeViewer.getSelection();
    	if (selection == null)
    		return null;
    	Object[] result = selection.toArray();
    	if (result.length == 0)
    		return null;
    	return result;
	}
	
	/**
	 * Returns a list of the selected Aliases.  If recurse is true then the result will
	 * include any aliases associated with other objects; eg, if a connection is selected
	 * and recurse is true, then the connection's alias will also be returned
	 * @param recurse
	 * @return Set of Aliases, never returns null
	 */
    public Set<Alias> getSelectedAliases(boolean recurse) {
    	IStructuredSelection selection = (IStructuredSelection)_treeViewer.getSelection();
    	if (selection == null)
    		return EMPTY_ALIASES;
    	
    	LinkedHashSet<Alias> result = new LinkedHashSet<Alias>();
    	Iterator iter = selection.iterator();
    	while (iter.hasNext()) {
    		Object obj = iter.next();
    		if (obj instanceof Alias)
    			result.add((Alias)obj);
    		else if (recurse) {
    			if (obj instanceof User) {
    				User user = (User)obj;
    				result.add(user.getAlias());
    			} else if (obj instanceof Session) {
    				Session session = (Session)obj;
    				result.add(session.getUser().getAlias());
    			}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Returns the first available selected alias; if recurse is true, then 
     * indirectly selected aliases are included (eg a selected connection's alias)
     * @param recurse
     * @return
     */
    public Alias getSelectedAlias(boolean recurse) {
    	return (Alias)getFirstOf(getSelectedAliases(recurse));
    }
    
    /**
     * Returns a list of selected Users; if recurse is true, indirectly selected users
     * are included also (eg a session's user)
     * @param recurse
     * @return Set of Users, never returns null
     */
    public Set<User> getSelectedUsers(boolean recurse) {
    	IStructuredSelection selection = (IStructuredSelection)_treeViewer.getSelection();
    	if (selection == null)
    		return EMPTY_USERS;
    	
    	LinkedHashSet<User> result = new LinkedHashSet<User>();
    	Iterator iter = selection.iterator();
    	while (iter.hasNext()) {
    		Object obj = iter.next();
    		if (obj instanceof User)
    			result.add((User)obj);
    		else if (recurse) {
    			if (obj instanceof Alias) {
    				Alias alias = (Alias)obj;
    				result.addAll(alias.getUsers());
    			} else if (obj instanceof Session) {
    				Session session = (Session)obj;
    				result.add(session.getUser());
    			}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Returns the first selected user; if recurse is true, this includes indirectly
     * selected users (eg an Alias' user)
     * @param recurse
     * @return
     */
    public User getSelectedUser(boolean recurse) {
    	return (User)getFirstOf(getSelectedUsers(recurse));
    }
    
    /**
     * Returns a list of selected sessions; if recurse is true, then it includes indirectly
     * selected sessions (eg a selected user's sessions) 
     * @param recurse
     * @return Set of Sessions, never returns null
     */
    public Set<SQLConnection> getSelectedConnections(boolean recurse) {
    	IStructuredSelection selection = (IStructuredSelection)_treeViewer.getSelection();
    	if (selection == null)
    		return EMPTY_CONNECTIONS;
    	
    	LinkedHashSet<SQLConnection> result = new LinkedHashSet<SQLConnection>();
    	Iterator iter = selection.iterator();
    	while (iter.hasNext()) {
    		Object obj = iter.next();
    		if (obj instanceof SQLConnection)
    			result.add((SQLConnection)obj);
    	}
    	
    	return result;
    }

    /**
     * Returns the first selected connection; if recurse is true, then includes indirectly
     * selected sessions
     * @param recurse
     * @return
     */
    public SQLConnection getSelectedConnection(boolean recurse) {
    	return (SQLConnection)getFirstOf(getSelectedConnections(recurse));
    }

    public Alias getDefaultAlias() {
    	IStructuredSelection selection = (IStructuredSelection)_treeViewer.getSelection();
    	if (selection == null)
    		return null;
    	
    	Object element = selection.getFirstElement();
    	
    	if (element instanceof Alias)
    		return (Alias)element;
    	else if (element instanceof Session) {
    		ITreeContentProvider provider = (ITreeContentProvider)_treeViewer.getContentProvider();
    		return (Alias)provider.getParent(element);
    	}
    	
    	return null;
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {

    }

    /**
     * Helper method which returns the first element of a set, or null if the set is
     * empty (or if the set is null)
     * @param set the set to look into (may be null)
     * @return
     */
    private Object getFirstOf(Set set) {
    	if (set == null)
    		return null;
    	Iterator iter = set.iterator();
    	if (iter.hasNext())
    		return iter.next();
    	return null;
    }
}
