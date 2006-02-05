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

package net.sourceforge.sqlexplorer.sqleditor.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

public class ExecSQLAction extends Action {

    SQLEditor txtComp;

    private ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getExecSQLIcon());

    SessionTreeNode preferredNode;

    private Button _limitResults;

    private Text _resultLimit;


    public ExecSQLAction(SQLEditor txtComp) {

        this.txtComp = txtComp;
    }


    public ExecSQLAction(SQLEditor txtComp, SessionTreeNode node_) {

        this.txtComp = txtComp;
        this.preferredNode = node_;
    }


    public String getText() {
        return Messages.getString("Execute_SQL_2");
    }


    public String getToolTipText() {
        return Messages.getString("Execute_SQL_3");
    }


    public void run() {

        if (_limitResults == null || _resultLimit == null) {
            return;
        }

        int maxresults = 0;
        try {
            if (_limitResults.getSelection()) {
                String tmp = _resultLimit.getText();
                maxresults = Integer.parseInt(tmp);
            }

            if (maxresults < 0) {
                throw new Exception(Messages.getString("SQLEditor.LimitRows.Error"));
            }

            final ExecSQLAction action = this;

            if (maxresults == 0 || maxresults > 2000) {

                final int largeResults = maxresults;
                txtComp.getSite().getShell().getDisplay().asyncExec(new Runnable() {

                    public void run() {

                        boolean okToExecute = MessageDialog.openConfirm(txtComp.getSite().getShell(),
                                Messages.getString("SQLEditor.LimitRows.ConfirmNoLimit.Title"),
                                Messages.getString("SQLEditor.LimitRows.ConfirmNoLimit.Message"));
                        if (okToExecute) {
                            action.run(largeResults);
                        }
                    }
                });

            } else {
                action.run(maxresults);
            }

        } catch (final Exception e) {

            txtComp.getSite().getShell().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    MessageDialog.openError(txtComp.getSite().getShell(), Messages.getString("SQLEditor.LimitRows.Error.Title"), e.getMessage());
                }
            });

        }
    }


    public void setInputFields(Button limitResults, Text resultLimit) {
        _limitResults = limitResults;
        _resultLimit = resultLimit;
    }


    public void run(int maxRows) {
        SessionTreeNode runNode = null;
        if (preferredNode == null)
            runNode = txtComp.getSessionTreeNode();
        else
            runNode = preferredNode;
        if (runNode == null)
            return;

        final SqlExecProgress sExecP = new SqlExecProgress(txtComp.getSQLToBeExecuted(), txtComp, maxRows, runNode);

        ProgressMonitorDialog pg = new ProgressMonitorDialog(txtComp.getSite().getShell());
        try {
            pg.run(true, true, sExecP);
        } catch (java.lang.Exception e) {
            SQLExplorerPlugin.error("Error executing the SQL statement ", e);
        }
        if (sExecP.isSqlError()) {
            txtComp.getSite().getShell().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    MessageDialog.openError(txtComp.getSite().getShell(), Messages.getString("Error..._2"), sExecP.getException().getMessage());

                }
            });
        }
    }


    public ImageDescriptor getHoverImageDescriptor() {
        return img;
    }


    public ImageDescriptor getImageDescriptor() {
        return img;
    };

}
