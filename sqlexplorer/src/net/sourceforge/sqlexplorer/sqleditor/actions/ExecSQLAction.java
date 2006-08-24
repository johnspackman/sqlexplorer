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

package net.sourceforge.sqlexplorer.sqleditor.actions;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.SqlResultsView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sqlpanel.SQLExecution;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.sqlexplorer.util.QueryTokenizer;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

public class ExecSQLAction extends AbstractEditorAction {

    private ImageDescriptor img = ImageUtil.getDescriptor("Images.ExecSQLIcon");

    public ExecSQLAction() {
    }

    public ImageDescriptor getImageDescriptor() {
        return img;
    }


    public String getText() {
        return Messages.getString("SQLEditor.Actions.Execute");
    }


    public String getToolTipText() {
        return Messages.getString("SQLEditor.Actions.Execute.ToolTip");
    }


    public void run() {

        if (_editor.getLimitResults() == null || _editor.getMaxResultField() == null) {
            return;
        }

        int maxresults = 0;
        try {
            if (_editor.getLimitResults().getSelection()) {
                String tmp = _editor.getMaxResultField().getText();
                if (tmp != null && tmp.trim().length() != 0) {
                    maxresults = Integer.parseInt(tmp);
                }
            }
        } catch (final Exception e) {

            _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    MessageDialog.openError(_editor.getSite().getShell(), Messages.getString("SQLEditor.Error.InvalidRowLimit.Title"), 
                            Messages.getString("SQLEditor.Error.InvalidRowLimit.") + " " + e.getMessage());
                }
            });

            return;
        }
        
        try {
            
            if (maxresults < 0) {
                throw new Exception(Messages.getString("SQLEditor.LimitRows.Error"));
            }

            final ExecSQLAction action = this;

            boolean warnNoLimit = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.WARN_IF_LARGE_LIMIT);
            int warnLimit = SQLExplorerPlugin.getDefault().getPluginPreferences().getInt(IConstants.WARN_LIMIT);

            if (warnNoLimit && (maxresults == 0 || maxresults > warnLimit)) {

                final int largeResults = maxresults;
                _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {

                    public void run() {

                        boolean okToExecute = MessageDialog.openConfirm(_editor.getSite().getShell(),
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

            _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    MessageDialog.openError(_editor.getSite().getShell(), Messages.getString("SQLResultsView.Error.Title"), e.getMessage());
                }
            });

        }
    }


    protected void run(int maxRows) {

        SessionTreeNode runNode = _editor.getSessionTreeNode();
      
        if (runNode == null)
            return;

        Preferences prefs = SQLExplorerPlugin.getDefault().getPluginPreferences();

        String queryDelimiter = prefs.getString(IConstants.SQL_QRY_DELIMITER);
        String alternateDelimiter = prefs.getString(IConstants.SQL_ALT_QRY_DELIMITER);
        String commentDelimiter = prefs.getString(IConstants.SQL_COMMENT_DELIMITER);

        QueryTokenizer qt = new QueryTokenizer(_editor.getSQLToBeExecuted(), queryDelimiter, alternateDelimiter, commentDelimiter);
        final List queryStrings = new ArrayList();
        while (qt.hasQuery()) {
            final String querySql = qt.nextQuery();
            // ignore commented lines.
            if (!querySql.startsWith("--")) {
                queryStrings.add(querySql);
            }
        }

        try {

            SqlResultsView resultsView = (SqlResultsView) _editor.getSite().getPage().showView(
                    "net.sourceforge.sqlexplorer.plugin.views.SqlResultsView");

            while (!queryStrings.isEmpty()) {

                String querySql = (String) queryStrings.remove(0);

                if (querySql != null) {
                    resultsView.addSQLExecution(new SQLExecution(_editor, resultsView, querySql, maxRows, runNode));
                }
            }

        } catch (Exception e) {
            SQLExplorerPlugin.error("Error creating sql execution tab", e);
        }

    }


}
