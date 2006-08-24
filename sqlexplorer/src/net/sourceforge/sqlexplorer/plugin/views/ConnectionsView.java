package net.sourceforge.sqlexplorer.plugin.views;

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
import net.sourceforge.sqlexplorer.AliasModel;
import net.sourceforge.sqlexplorer.DriverModel;
import net.sourceforge.sqlexplorer.SQLAlias;
import net.sourceforge.sqlexplorer.connections.ConnectionTreeActionGroup;
import net.sourceforge.sqlexplorer.connections.ConnectionTreeContentProvider;
import net.sourceforge.sqlexplorer.connections.ConnectionTreeLabelProvider;
import net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction;
import net.sourceforge.sqlexplorer.connections.actions.NewAliasAction;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.actions.OpenPasswordConnectDialogAction;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModelChangedListener;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class ConnectionsView extends ViewPart implements SessionTreeModelChangedListener {

    private AliasModel _aliasModel;

    private DriverModel _driverModel;

    private TreeViewer _treeViewer;


    /**
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, SQLExplorerPlugin.PLUGIN_ID + ".AliasView");

        SQLExplorerPlugin.getDefault().stm.addListener(this);

        _driverModel = SQLExplorerPlugin.getDefault().getDriverModel();
        _aliasModel = SQLExplorerPlugin.getDefault().getAliasModel();

        // create outline
        _treeViewer = new TreeViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        getSite().setSelectionProvider(_treeViewer);

        // create action bar
        IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();

        AbstractConnectionTreeAction newAliasAction = new NewAliasAction();
        newAliasAction.setTreeViewer(_treeViewer);
        newAliasAction.setView(this);
        toolBarMgr.add(newAliasAction);

        // use hash lookup to improve performance
        _treeViewer.setUseHashlookup(true);

        // add content and label provider
        _treeViewer.setContentProvider(new ConnectionTreeContentProvider());
        _treeViewer.setLabelProvider(new ConnectionTreeLabelProvider());

        // set input session
        _treeViewer.setInput(_aliasModel);

        // doubleclick on alias opens session
        _treeViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {

                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (selection != null) {
                    if (selection.getFirstElement() instanceof ISQLAlias) {

                        SQLAlias al = (SQLAlias) selection.getFirstElement();
                        OpenPasswordConnectDialogAction openDlgAction = new OpenPasswordConnectDialogAction(
                                getSite(), al, _driverModel,
                                SQLExplorerPlugin.getDefault().getPreferenceStore(),
                                SQLExplorerPlugin.getDefault().getSQLDriverManager());
                        openDlgAction.run();
                        _treeViewer.refresh();
                    }
                }
            }
        });

        // add context menu
        final ConnectionTreeActionGroup actionGroup = new ConnectionTreeActionGroup(this, _treeViewer);
        MenuManager menuManager = new MenuManager("ConnectionTreeContextMenu");
        menuManager.setRemoveAllWhenShown(true);
        Menu contextMenu = menuManager.createContextMenu(_treeViewer.getTree());
        _treeViewer.getTree().setMenu(contextMenu);

        menuManager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {

                actionGroup.fillContextMenu(manager);
            }
        });

        parent.layout();

        SQLExplorerPlugin.getDefault().startDefaultConnections(getSite());
    }


    public void dispose() {

        SQLExplorerPlugin.getDefault().stm.removeListener(this);
        super.dispose();
    }


    public TreeViewer getTreeViewer() {

        return _treeViewer;
    }


    public void modelChanged(SessionTreeNode newNode) {

        getSite().getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {
                _treeViewer.refresh();
            }
        });
        
    }


    /**
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {

    }

}
