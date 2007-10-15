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

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sqlpanel.AbstractSQLExecution;
import net.sourceforge.sqlexplorer.sqlpanel.SQLExecution;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Executes SQL in response to clicking the toolbar or the key mapping
 * @modified John Spackman
 *
 */
public class ExecSQLAction extends AbstractEditorAction {

    private ImageDescriptor img = ImageUtil.getDescriptor("Images.ExecSQLIcon");

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
        try {
        	// Find out how much to restrict results by
            Integer iMax = _editor.getLimitResults();
            if (iMax == null)
    	        _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
    	            public void run() {
    	                MessageDialog.openError(_editor.getSite().getShell(), Messages.getString("SQLEditor.Error.InvalidRowLimit.Title"), Messages.getString("SQLEditor.Error.InvalidRowLimit"));
    	            }
    	        });
            int maxresults = (iMax == null) ? 0 : iMax.intValue();
            if (maxresults < 0)
                throw new Exception(Messages.getString("SQLEditor.LimitRows.Error"));

            final ExecSQLAction action = this;

            boolean warnNoLimit = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.WARN_IF_LARGE_LIMIT);
            int warnLimit = SQLExplorerPlugin.getDefault().getPluginPreferences().getInt(IConstants.WARN_LIMIT);

            // Confirm with the user if they've left it too large
            if (warnNoLimit && (maxresults == 0 || maxresults > warnLimit)) {
                final int largeResults = maxresults;
                _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {

                    public void run() {

                        boolean okToExecute = MessageDialog.openConfirm(_editor.getSite().getShell(),
                                Messages.getString("SQLEditor.LimitRows.ConfirmNoLimit.Title"),
                                Messages.getString("SQLEditor.LimitRows.ConfirmNoLimit.Message"));

                        if (okToExecute)
                            action.run(largeResults);
                    }
                });
                
            // Run it
            } else {
                action.run(maxresults);
            }

        } catch (final Exception e) {
            _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    MessageDialog.openError(_editor.getSite().getShell(), Messages.getString("SQLResultsView.Error.Title"), e.getClass().getCanonicalName() + ": " + e.getMessage());
                }
            });
        }
    }

    protected void run(int maxRows) {
        Session session = getSession();
        if (session == null)
            return;

        QueryParser qt = session.getDatabaseProduct().getQueryParser(_editor.getSQLToBeExecuted(), _editor.getSQLLineNumber());
        try {
            qt.parse();
        }catch(final ParserException e) {
            _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(_editor.getSite().getShell(), Messages.getString("SQLResultsView.Error.Title"), e.getMessage());
                }
            });
        }
        
        if (qt.iterator().hasNext()) {
        	boolean clearResults = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.CLEAR_RESULTS_ON_EXECUTE);
        	if (clearResults)
        		_editor.clearResults();
        	AbstractSQLExecution job = new SQLExecution(_editor, qt, maxRows);
        	job.schedule();
        }
    }
}
