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
import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.views.SqlResultsView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sqlpanel.SQLExecution;
import net.sourceforge.sqlexplorer.util.QueryTokenizer;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

public class ExecSQLAction extends AbstractEditorAction {

    private Button _limitResults;

    private Text _resultLimit;

    private ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getExecSQLIcon());

    SessionTreeNode preferredNode;


    public ExecSQLAction() {
    }


    public ExecSQLAction(SQLEditor txtComp, SessionTreeNode node_) {

        _editor = txtComp;
        this.preferredNode = node_;
    }


    public ImageDescriptor getHoverImageDescriptor() {
        return img;
    }


    public ImageDescriptor getImageDescriptor() {
        return img;
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


    public void run(int maxRows) {

        SessionTreeNode runNode = null;
        if (preferredNode == null)
            runNode = _editor.getSessionTreeNode();
        else
            runNode = preferredNode;
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


    public void setInputFields(Button limitResults, Text resultLimit) {
        _limitResults = limitResults;
        _resultLimit = resultLimit;
    };

}
