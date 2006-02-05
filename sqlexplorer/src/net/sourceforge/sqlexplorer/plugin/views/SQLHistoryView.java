/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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

import net.sourceforge.sqlexplorer.AliasModel;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SQLAlias;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.SqlHistoryChangedListener;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.sessiontree.actions.OpenPasswordConnectDialogAction;
import net.sourceforge.sqlexplorer.sessiontree.model.RootSessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.util.SQLString;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;

/**
 * SQL History view shows all succesfully executed sql statements.
 * The list of statements remains persistent between sessions.
 * 
 * @modified Davy Vanherbergen
 */
public class SQLHistoryView extends ViewPart implements SqlHistoryChangedListener {

    private TableViewer _tableViewer;


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(final Composite parent) {
        
        SQLExplorerPlugin.getDefault().addListener(this);
        _tableViewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        Table table = _tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        _tableViewer.setLabelProvider(new LabelProvider());
        
        _tableViewer.setContentProvider(new IStructuredContentProvider() {

            public Object[] getElements(Object inputElement) {
                return SQLExplorerPlugin.getDefault().getSQLHistory().toArray();
            }


            public void dispose() {

            }


            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

            }
        });
        
        _tableViewer.setInput(this);
        TableColumn tc = new TableColumn(table, SWT.NULL);
        tc.setText(Messages.getString("SQLHistoryView.ColumnLabel"));
        TableLayout tableLayout = new TableLayout();

        tableLayout.addColumnData(new ColumnWeightData(1, 100, true));
        table.setLayout(tableLayout);
        table.layout();
        
        
        // add context menus
        final MenuManager menuMgr = new MenuManager("#HistoryPopupMenu");
        Menu historyContextMenu = menuMgr.createContextMenu(table);
        
        
        // add 'open in editor' action 
        final Action openInEditorAction = new Action() {

            public String getText() {
                return Messages.getString("SQLHistoryView.OpenInEditor");
            }


            public void run() {
                try {
                    TableItem[] ti = _tableViewer.getTable().getSelection();
                    if (ti == null || ti.length < 1)
                        return;

                     
                    SQLString sqlString = (SQLString) ti[0].getData();
                    SessionTreeNode querySession = null;
                    
                    if (sqlString.getSessionName() != null) {                        

                        // check if we have an active session for this query
                        
                        RootSessionTreeNode sessionRoot = SQLExplorerPlugin.getDefault().stm.getRoot();
                        Object[] sessions = sessionRoot.getChildren();
                        if (sessions != null) {
                            for (int i = 0; i < sessions.length; i++) {
                                SessionTreeNode session = (SessionTreeNode) sessions[i];
                                if (session.toString().equalsIgnoreCase(sqlString.getSessionName())) {
                                    querySession = session;
                                    break;
                                }
                            }
                        }
                        
                        // check if we need to open new connection
                        if (querySession == null) {
                            
                            boolean okToOpen = MessageDialog.openConfirm(getSite().getShell(), 
                                    Messages.getString("SQLHistoryView.OpenInEditor.Confirm.Title") , 
                                    Messages.getString("SQLHistoryView.OpenInEditor.Confirm.Message.Prefix")
                                    + " " + sqlString.getSessionName() 
                                    + Messages.getString("SQLHistoryView.OpenInEditor.Confirm.Message.Postfix")
                            );
                            
                            if (okToOpen) {
                                
                                // create new connection..
                                AliasModel aliasModel = SQLExplorerPlugin.getDefault().getAliasModel();
                                SQLAlias al = (SQLAlias) aliasModel.getAliasByName(sqlString.getSessionName());
    
                                if (al != null) {
                                    OpenPasswordConnectDialogAction openDlgAction = new OpenPasswordConnectDialogAction(
                                            _tableViewer.getTable().getShell(), 
                                            al, 
                                            SQLExplorerPlugin.getDefault().getDriverModel(),
                                            SQLExplorerPlugin.getDefault().getPreferenceStore(),
                                            SQLExplorerPlugin.getDefault().getSQLDriverManager());
                                    openDlgAction.run();
                                }
                                
                                // find new session
                                sessions = sessionRoot.getChildren();
                                if (sessions != null) {
                                    for (int i = 0; i < sessions.length; i++) {
                                        SessionTreeNode session = (SessionTreeNode) sessions[i];
                                        if (session.toString().equalsIgnoreCase(sqlString.getSessionName())) {
                                            querySession = session;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    SQLEditorInput input = new SQLEditorInput("SQL Editor (" + SQLExplorerPlugin.getDefault().getNextElement() + ").sql");
                    input.setSessionNode(querySession);
                    IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if (page == null) {
                        return;
                    }
                    SQLEditor editorPart = (SQLEditor) page.openEditor((IEditorInput) input, "net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");
                    editorPart.setText(sqlString.getText());

                } catch (Throwable e) {
                    SQLExplorerPlugin.error("Error creating sql editor", e);
                }
            }
        };
        

        menuMgr.add(openInEditorAction);
        
        // also add action as default when an entry is doubleclicked.
        _tableViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                openInEditorAction.run();                
            }
        });
        
        
        // add remove from history action
        menuMgr.add(new Action() {

            public String getText() {
                return Messages.getString("SQLHistoryView.RemoveFromHistory");
            }


            public void run() {
                try {
                    int i = _tableViewer.getTable().getSelectionIndex();
                    if (i >= 0) {
                        SQLExplorerPlugin.getDefault().getSQLHistory().remove(i);
                        changed();
                    }

                } catch (Throwable e) {
                    SQLExplorerPlugin.error("Error removing item from clipboard", e);
                }
            }
        });

        // add clear history action
        menuMgr.add(new Action() {

            public String getText() {
                return Messages.getString("SQLHistoryView.ClearHistory");
            }


            public void run() {

                try {

                    boolean ok = MessageDialog.openConfirm(getSite().getShell(), Messages.getString("SQLHistoryView.ClearHistory") , 
                            Messages.getString("SQLHistoryView.ClearHistory.Confirm"));
                    
                    if (ok) {
                        SQLExplorerPlugin.getDefault().getSQLHistory().clear();
                        changed();
                    }
                } catch (Throwable e) {
                    SQLExplorerPlugin.error("Error clearing sql history", e);
                }
            }
        });

        
        // add copy to clipboard action
        menuMgr.add(new Action() {

            public String getText() {
                return Messages.getString("SQLHistoryView.CopyToClipboard");
            }


            public void run() {
                try {
                    TableItem[] ti = _tableViewer.getTable().getSelection();
                    if (ti == null || ti.length < 1)
                        return;
                    Clipboard cb = new Clipboard(Display.getCurrent());
                    TextTransfer textTransfer = TextTransfer.getInstance();

                    Object data = ti[0].getData();
                    SQLString mls = (SQLString) data;

                    cb.setContents(new Object[] {mls.getText()}, new Transfer[] {textTransfer});

                } catch (Throwable e) {
                    SQLExplorerPlugin.error("Error copying to clipboard", e);
                }
            }
        });
        table.setMenu(historyContextMenu);
        menuMgr.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                TableItem[] ti = _tableViewer.getTable().getSelection();
                MenuItem[] items = menuMgr.getMenu().getItems();
                if (ti == null || ti.length < 1) {
                    for (int i = 0; i < items.length; i++) {
                        items[i].setEnabled(false);
                    }
                } else {
                    for (int i = 0; i < items.length; i++) {
                        items[i].setEnabled(true);
                    }
                }

            }
        });
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        _tableViewer.getTable().setFocus();

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {
        SQLExplorerPlugin.getDefault().removeListener(this);
        super.dispose();
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.plugin.SqlHistoryChangedListener#changed()
     */
    public void changed() {
        _tableViewer.getTable().getDisplay().asyncExec(new Runnable() {

            public void run() {
                _tableViewer.refresh();
            }
        });

    }

}
