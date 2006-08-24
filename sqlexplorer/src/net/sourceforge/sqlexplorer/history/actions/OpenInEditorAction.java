package net.sourceforge.sqlexplorer.history.actions;

import net.sourceforge.sqlexplorer.AliasModel;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SQLAlias;
import net.sourceforge.sqlexplorer.history.SQLHistoryElement;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.actions.OpenPasswordConnectDialogAction;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.sessiontree.model.RootSessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;

public class OpenInEditorAction extends AbstractHistoryContextAction {

    private ImageDescriptor _imageOpenInEditor = ImageUtil.getDescriptor("Images.OpenSQLIcon");


    public ImageDescriptor getImageDescriptor() {

        return _imageOpenInEditor;
    }


    public String getText() {

        return Messages.getString("SQLHistoryView.OpenInEditor");
    }


    public boolean isEnabled() {

        TableItem[] ti = _table.getSelection();
        if (ti == null || ti.length == 0) {
            return false;
        }
        return true;
    }


    public void run() {

        try {
            TableItem[] ti = _table.getSelection();
            if (ti == null || ti.length == 0) {
                return;
            }

            String queryDelimiter = SQLExplorerPlugin.getDefault().getPluginPreferences().getString(
                    IConstants.SQL_QRY_DELIMITER);
            StringBuffer copiedText = new StringBuffer();

            for (int i = 0; i < ti.length; i++) {

                SQLHistoryElement el = (SQLHistoryElement) ti[i].getData();
                copiedText.append(el.getRawSQLString());

                if (ti.length > 0) {
                    copiedText.append(queryDelimiter);
                    copiedText.append("\n");
                }
            }

            SQLHistoryElement sqlHistoryElement = (SQLHistoryElement) ti[0].getData();
            SessionTreeNode querySession = null;

            if (sqlHistoryElement.getSessionName() != null) {

                // check if we have an active session for this query

                RootSessionTreeNode sessionRoot = SQLExplorerPlugin.getDefault().stm.getRoot();
                Object[] sessions = sessionRoot.getChildren();
                if (sessions != null) {
                    for (int i = 0; i < sessions.length; i++) {
                        SessionTreeNode session = (SessionTreeNode) sessions[i];
                        if (session.toString().equalsIgnoreCase(sqlHistoryElement.getSessionName())) {
                            querySession = session;
                            break;
                        }
                    }
                }

                // check if we need to open new connection
                if (querySession == null) {

                    boolean okToOpen = MessageDialog.openConfirm(_table.getShell(),
                            Messages.getString("SQLHistoryView.OpenInEditor.Confirm.Title"),
                            Messages.getString("SQLHistoryView.OpenInEditor.Confirm.Message.Prefix") + " "
                                    + sqlHistoryElement.getSessionName()
                                    + Messages.getString("SQLHistoryView.OpenInEditor.Confirm.Message.Postfix"));

                    if (okToOpen) {

                        // create new connection..
                        AliasModel aliasModel = SQLExplorerPlugin.getDefault().getAliasModel();
                        SQLAlias al = (SQLAlias) aliasModel.getAliasByName(sqlHistoryElement.getSessionName());

                        if (al != null) {
                            OpenPasswordConnectDialogAction openDlgAction = new OpenPasswordConnectDialogAction(
                                    _view.getSite(), al, SQLExplorerPlugin.getDefault().getDriverModel(),
                                    SQLExplorerPlugin.getDefault().getPreferenceStore(),
                                    SQLExplorerPlugin.getDefault().getSQLDriverManager());
                            openDlgAction.run();
                        }

                        // find new session
                        sessions = sessionRoot.getChildren();
                        if (sessions != null) {
                            for (int i = 0; i < sessions.length; i++) {
                                SessionTreeNode session = (SessionTreeNode) sessions[i];
                                if (session.toString().equalsIgnoreCase(sqlHistoryElement.getSessionName())) {
                                    querySession = session;
                                    break;
                                }
                            }
                        }

                    }

                }
            }

            SQLEditorInput input = new SQLEditorInput("SQL Editor (" + SQLExplorerPlugin.getDefault().getNextElement()
                    + ").sql");
            input.setSessionNode(querySession);
            IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (page == null) {
                return;
            }
            SQLEditor editorPart = (SQLEditor) page.openEditor((IEditorInput) input,
                    "net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");
            editorPart.setText(copiedText.toString());

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error creating sql editor", e);
        }
    }
}
