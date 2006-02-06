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
package net.sourceforge.sqlexplorer.plugin.views;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.DBTreeActionGroup;
import net.sourceforge.sqlexplorer.dbstructure.DBTreeContentProvider;
import net.sourceforge.sqlexplorer.dbstructure.DBTreeLabelProvider;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.ISessionTreeClosedListener;
import net.sourceforge.sqlexplorer.sessiontree.model.RootSessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.part.ViewPart;

/**
 * Database Structure View. Shows the database outline. Selections made in this
 * view are shown in the DatabaseDetailView.
 * 
 * @author Davy Vanherbergen
 */
public class DatabaseStructureView extends ViewPart {

    /** We use one tab for every session */
    private TabFolder _tabFolder;

    private Composite _parent;

    /**
     * Initializes the view and creates the root tabfolder that holds all the
     * sessions.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {

        _parent = parent;
        
        // load all open sessions
        RootSessionTreeNode sessionRoot = SQLExplorerPlugin.getDefault().stm.getRoot();
        Object[] sessions = sessionRoot.getChildren();
        if (sessions != null) {
            for (int i = 0; i < sessions.length; i++) {
                SessionTreeNode session = (SessionTreeNode) sessions[i];
                addSession(session);
            }
        }

        // set default message
        if (sessions == null || sessions.length == 0) {
            setDefaultMessage();
        }
    }


    /**
     * Update the detail view with the selection in the active treeviewer.
     */
    public void synchronizeDetailView(final DatabaseDetailView detailView) {

        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            public void run() {

                if (detailView == null) {
                    return;
                }

                if (_tabFolder == null || _tabFolder.getItemCount() == 0) {
                    return;
                }

                TreeViewer treeViewer = (TreeViewer) _tabFolder.getItem(_tabFolder.getSelectionIndex()).getData();
                INode selectedNode = null;

                if (treeViewer != null) {

                    // find our target node..
                    IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();

                    // check if we have a valid selection
                    if (selection != null && (selection.getFirstElement() instanceof INode)) {
                        selectedNode = (INode) selection.getFirstElement();
                    }

                }

                detailView.setSelectedNode(selectedNode);                
            }
        });
        
        

    }


    /**
     * Set focus on our database structure view..
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        // we don't need to do anything here..
    }


    /**
     * Cleanup and reset detail view.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {

        // refresh detail view
        DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);

        if (detailView != null) {
            detailView.setSelectedNode(null);
        }
    }


    /**
     * Add a new session to the database structure view. This will create a new
     * tab for the session.
     * 
     * @param sessionTreeNode
     */
    public void addSession(SessionTreeNode sessionTreeNode) {

        if (_tabFolder == null || _tabFolder.isDisposed()) {
            
            clearParent();

            // create tab folder for different sessions
            _tabFolder = new TabFolder(_parent, SWT.NULL);

            // add listener to keep both views on the same active tab
            _tabFolder.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    
                    // set the selected node in the detail view.
                    DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
                    synchronizeDetailView(detailView);
                }

            });
            
            _parent.layout();
            _parent.redraw();
        
        }
        
        
        // create tab
        final TabItem tabItem = new TabItem(_tabFolder, SWT.NULL);

        // set tab text
        String labelText = sessionTreeNode.toString();
        tabItem.setText(labelText);

        // create composite for our outline
        Composite composite = new Composite(_tabFolder, SWT.NULL);
        composite.setLayout(new FillLayout());
        tabItem.setControl(composite);

        // create outline
        TreeViewer treeViewer = new TreeViewer(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER);

        // use hash lookup to improve performance
        treeViewer.setUseHashlookup(true);

        // add content and label provider
        treeViewer.setContentProvider(new DBTreeContentProvider());
        treeViewer.setLabelProvider(new DBTreeLabelProvider());

        // set input session
        treeViewer.setInput(sessionTreeNode.dbModel);

        // add selection change listener, so we can update detail view as required.
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent ev) {
            	
                // set the selected node in the detail view.
                DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
                synchronizeDetailView(detailView);
            }
        });

        // bring detail to front on doubleclick of node
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                
                try {
                    // find view
                    DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
                    if (detailView == null) {
                        getSite().getPage().showView(SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
                    }
                   getSite().getPage().bringToTop(detailView);
                   synchronizeDetailView(detailView);
                } catch (Exception e) {
                    // fail silent
                }                
            }
            
        });
        
        
        // store tree for later use.
        tabItem.setData(treeViewer);

        // add dispose listener for when session gets closed
        sessionTreeNode.addListener(new ISessionTreeClosedListener() {

            public void sessionTreeClosed() {
                
                // if it is the last session, clear detail tab
                if (tabItem.getParent().getItemCount() == 1) {
                    
                    DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
                    if (detailView != null) {
                        detailView.setSelectedNode(null);
                    }             
                    
                    setDefaultMessage();
                    
                } else {
                
                    // remove tab
                    tabItem.setData(null);
                    tabItem.dispose();
                }
            }
        });

        // set new tab as the active one
        _tabFolder.setSelection(_tabFolder.getItemCount() - 1);

        // update detail view
        DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);

        if (detailView != null) {

            // synchronze detail view with new session
            synchronizeDetailView(detailView);

            // bring detail to top of the view stack
            getSite().getPage().bringToTop(detailView);
        }

        // refresh view
        composite.layout();
        _tabFolder.layout();
        _tabFolder.redraw();

        // bring this view to top of the view stack, above detail if needed..
        getSite().getPage().bringToTop(this);

        // add context menu
        final DBTreeActionGroup actionGroup = new DBTreeActionGroup(treeViewer);
        MenuManager menuManager = new MenuManager("DBTreeContextMenu");
        menuManager.setRemoveAllWhenShown(true);
        Menu contextMenu = menuManager.createContextMenu(treeViewer.getTree());
        treeViewer.getTree().setMenu(contextMenu);

        menuManager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                actionGroup.fillContextMenu(manager);
            }
        });
    }


    
    /**
     * Set a default message, this method is called
     * when no sessions are available for viewing.
     */
    private void setDefaultMessage() {
        
        clearParent();
        
        // add message
        String message = Messages.getString("DatabaseStructureView.NoSession");
        Label label = new Label(_parent, SWT.FILL);
        label.setText(message);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        _parent.layout();
        _parent.redraw();
    }
    
    
    /**
     * Remove all items from parent
     */
    private void clearParent() {
        
        Control[] children = _parent.getChildren();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                children[i].dispose();
            }
        }
    }
}