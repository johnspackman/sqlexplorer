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
package net.sourceforge.sqlexplorer.connections.actions;

import java.util.Iterator;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SQLAlias;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.actions.OpenPasswordConnectDialogAction;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.plugin.views.ConnectionsView;
import net.sourceforge.sqlexplorer.sessiontree.model.RootSessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Davy Vanherbergen
 * 
 */
public class NewEditorAction extends AbstractConnectionTreeAction implements IViewActionDelegate {

    ImageDescriptor _image = ImageUtil.getDescriptor("Images.OpenSQLIcon");

    ImageDescriptor _disabledImage = ImageUtil.getDescriptor("Images.AliasIcon");


    public ImageDescriptor getHoverImageDescriptor() {
        return _image;
    }


    public ImageDescriptor getImageDescriptor() {
        return _image;
    };


    public ImageDescriptor getDisabledImageDescriptor() {
        return _disabledImage;
    }


    public void init(IViewPart view) {
        _treeViewer = ((ConnectionsView) view).getTreeViewer();
    }


    public void run(IAction action) {
        run();
    }


    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(isAvailable());
    }


    public String getText() {
        return Messages.getString("ConnectionsView.Actions.NewEditor");
    }


    public String getToolTipText() {
        return Messages.getString("ConnectionsView.Actions.NewEditorToolTip");
    }


    public void run() {

        StructuredSelection sel = (StructuredSelection) _treeViewer.getSelection();

        Iterator it = sel.iterator();
        while (it.hasNext()) {

            Object o = it.next();
            if (o instanceof SQLAlias) {

                ISQLAlias al = (ISQLAlias) o;
                SessionTreeNode node = null;

                // locate open sessions
                RootSessionTreeNode sessionRoot = SQLExplorerPlugin.getDefault().stm.getRoot();
                Object[] sessions = sessionRoot.getChildren();
                if (sessions != null) {
                    for (int i = 0; i < sessions.length; i++) {
                        SessionTreeNode session = (SessionTreeNode) sessions[i];
                        if (session.getAlias().getIdentifier().equals(al.getIdentifier())) {
                            node = session;
                            break;
                        }
                    }
                }

                if (node == null) {
                    // if no session is open prompt to open one..
                    boolean okToOpen = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
                            Messages.getString("ConnectionsView.Actions.NewEditor.Connect.WindowTitle"),
                            Messages.getString("ConnectionsView.Actions.NewEditor.Connect.Message"));

                    if (okToOpen) {

                        // create new connection..
                        OpenPasswordConnectDialogAction openDlgAction = new OpenPasswordConnectDialogAction(
                                _view.getSite(), al, SQLExplorerPlugin.getDefault().getDriverModel(),
                                SQLExplorerPlugin.getDefault().getPreferenceStore(), SQLExplorerPlugin.getDefault().getSQLDriverManager());
                        openDlgAction.run();

                        // find new session
                        sessions = sessionRoot.getChildren();
                        if (sessions != null) {
                            for (int i = 0; i < sessions.length; i++) {
                                SessionTreeNode session = (SessionTreeNode) sessions[i];
                                if (session.getAlias().getIdentifier().equals(al.getIdentifier())) {
                                    node = session;
                                    break;
                                }
                            }
                        }
                    }
                }

                SQLEditorInput input = new SQLEditorInput("SQL Editor (" + SQLExplorerPlugin.getDefault().getNextElement() + ").sql");
                input.setSessionNode(node);
                IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try {
                    page.openEditor(input, "net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");
                } catch (Throwable e) {
                    SQLExplorerPlugin.error("Error creating sql editor", e);
                }
            }

            if (o instanceof SessionTreeNode) {

                SessionTreeNode node = (SessionTreeNode) o;
                SQLEditorInput input = new SQLEditorInput("SQL Editor (" + SQLExplorerPlugin.getDefault().getNextElement() + ").sql");
                input.setSessionNode(node);
                IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try {
                    page.openEditor(input, "net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");
                } catch (Throwable e) {
                    SQLExplorerPlugin.error("Error creating sql editor", e);
                }
            }

        }

        _treeViewer.refresh();
    }


    /**
     * Only show action when there is at least 1 item selected
     * 
     * @see net.sourceforge.sqlexplorer.connections.actions.AbstractConnectionTreeAction#isAvailable()
     */
    public boolean isAvailable() {

        StructuredSelection sel = (StructuredSelection) _treeViewer.getSelection();

        if (sel.size() > 0) {
            return true;
        }

        return false;
    }
}
