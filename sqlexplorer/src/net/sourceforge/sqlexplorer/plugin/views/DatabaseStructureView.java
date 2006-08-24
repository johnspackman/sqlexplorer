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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.DBTreeActionGroup;
import net.sourceforge.sqlexplorer.dbstructure.DBTreeContentProvider;
import net.sourceforge.sqlexplorer.dbstructure.DBTreeLabelProvider;
import net.sourceforge.sqlexplorer.dbstructure.DatabaseModel;
import net.sourceforge.sqlexplorer.dbstructure.actions.FilterStructureAction;
import net.sourceforge.sqlexplorer.dbstructure.nodes.ColumnNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.ISessionTreeClosedListener;
import net.sourceforge.sqlexplorer.sessiontree.model.RootSessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * Database Structure View. Shows the database outline. Selections made in this
 * view are shown in the DatabaseDetailView.
 * 
 * @author Davy Vanherbergen
 */
public class DatabaseStructureView extends ViewPart {

    private FilterStructureAction _filterAction;

    private Composite _parent;

    /** We use one tab for every session */
    private TabFolder _tabFolder;

    private List _allSessions = new ArrayList();
    
    /**
     * Add a new session to the database structure view. This will create a new
     * tab for the session.
     * 
     * @param sessionTreeNode
     */
    public void addSession(final SessionTreeNode sessionTreeNode) {

        if (_allSessions.contains(sessionTreeNode)) {
            return;
        }
        _allSessions.add(sessionTreeNode);
        
        if (_filterAction != null) {
            _filterAction.setEnabled(true);
        }

        if (_tabFolder == null || _tabFolder.isDisposed()) {

            clearParent();

            // create tab folder for different sessions
            _tabFolder = new TabFolder(_parent, SWT.NULL);

            // add listener to keep both views on the same active tab
            _tabFolder.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {

                    // set the selected node in the detail view.
                    DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(
                            SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
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
        final TreeViewer treeViewer = new TreeViewer(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER);

        // add drag support
        // TODO improve drag support options
        Transfer[] transfers = new Transfer[] {TableNodeTransfer.getInstance()};
        treeViewer.addDragSupport(DND.DROP_COPY, transfers, new DragSourceListener() {

            public void dragFinished(DragSourceEvent event) {

                System.out.println("$drag finished");
                TableNodeTransfer.getInstance().setSelection(null);
            }


            public void dragSetData(DragSourceEvent event) {

                Object sel = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                event.data = sel;
            }


            public void dragStart(DragSourceEvent event) {

                event.doit = !treeViewer.getSelection().isEmpty();
                if (event.doit) {
                    Object sel = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                    if (!(sel instanceof TableNode)) {
                        event.doit = false;
                    } else {
                        TableNode tn = (TableNode) sel;
                        TableNodeTransfer.getInstance().setSelection(tn);
                        if (!tn.isTable())
                            event.doit = false;
                    }
                }
            }
        });

        // use hash lookup to improve performance
        treeViewer.setUseHashlookup(true);

        // add content and label provider
        treeViewer.setContentProvider(new DBTreeContentProvider());
        treeViewer.setLabelProvider(new DBTreeLabelProvider());

        // set input session
        treeViewer.setInput(sessionTreeNode.dbModel);

        // add selection change listener, so we can update detail view as
        // required.
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent ev) {

                // set the selected node in the detail view.
                DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(
                        SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
                synchronizeDetailView(detailView);
            }
        });

        // bring detail to front on doubleclick of node
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {

                try {
                    // find view
                    DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(
                            SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
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

                    DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(
                            SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);
                    if (detailView != null) {
                        detailView.setSelectedNode(null);
                    }

                    setDefaultMessage();
                    _filterAction.setEnabled(false);
                } else {

                    // remove tab
                    tabItem.setData(null);
                    tabItem.dispose();
                }
                
                _allSessions.remove(sessionTreeNode);
            }
        });

        // add expand/collapse listener
        treeViewer.addTreeListener(new ITreeViewerListener() {

            public void treeCollapsed(TreeExpansionEvent event) {

                // refresh the node to change image
                INode node = (INode) event.getElement();
                node.setExpanded(false);
                TreeViewer viewer = (TreeViewer) event.getSource();
                viewer.update(node, null);
            }


            public void treeExpanded(TreeExpansionEvent event) {

                // refresh the node to change image
                INode node = (INode) event.getElement();
                node.setExpanded(true);
                TreeViewer viewer = (TreeViewer) event.getSource();
                viewer.update(node, null);
            }

        });

        // set new tab as the active one
        _tabFolder.setSelection(_tabFolder.getItemCount() - 1);

        // update detail view
        DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(
                SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);

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
        final DBTreeActionGroup actionGroup = new DBTreeActionGroup(treeViewer, this);
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


    /**
     * Initializes the view and creates the root tabfolder that holds all the
     * sessions.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                SQLExplorerPlugin.PLUGIN_ID + ".DatabaseStructureView");

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

        _filterAction = new FilterStructureAction(this);
        _filterAction.setEnabled((sessions != null && sessions.length != 0));
        IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
        toolBarMgr.add(_filterAction);
    }


    /**
     * Cleanup and reset detail view.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {

        // refresh detail view
        DatabaseDetailView detailView = (DatabaseDetailView) getSite().getPage().findView(
                SqlexplorerViewConstants.SQLEXPLORER_DBDETAIL);

        if (detailView != null) {
            detailView.setSelectedNode(null);
        }
    }


    public DatabaseModel getActiveDatabase() {

        if (_tabFolder == null) {
            return null;
        }
        TabItem item = _tabFolder.getItem(_tabFolder.getSelectionIndex());
        DatabaseModel model = (DatabaseModel) ((TreeViewer) item.getData()).getInput();
        return model;
    }


    /**
     * Loop through all tabs and refresh trees for sessions with sessionName
     */
    public void refreshSessionTrees(String sessionName) {

        if (_tabFolder == null) {
            return;
        }
        TabItem[] items = _tabFolder.getItems();
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                TreeViewer viewer = (TreeViewer) items[i].getData();
                DatabaseModel model = (DatabaseModel) viewer.getInput();
                if (model.getSession().toString().equals(sessionName)) {
                    model.getRoot().refresh();
                    viewer.refresh();
                }
            }
        }
    }


    /**
     * Set a default message, this method is called when no sessions are
     * available for viewing.
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
     * Set focus on our database structure view..
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {

        // we don't need to do anything here..
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

                        // if the selected node is a column node, we want to
                        // show it's parent instead
                        // in the detail view.

                        if (selectedNode instanceof ColumnNode) {
                            selectedNode = selectedNode.getParent();
                        }
                    }

                }

                detailView.setSelectedNode(selectedNode);
            }
        });

    }
}
