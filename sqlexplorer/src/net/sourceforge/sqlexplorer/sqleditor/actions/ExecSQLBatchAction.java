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

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sqlpanel.AbstractSQLExecution;
import net.sourceforge.sqlexplorer.sqlpanel.SQLBatchExecution;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Executes SQL in response to clicking the toolbar or the key mapping
 * @modified Heiko Hilbert
 *
 */
public class ExecSQLBatchAction extends AbstractEditorAction {
	public static final String COMMAND_ID = "net.sourceforge.sqlexplorer.executeBatchSQL";

    private ImageDescriptor img = ImageUtil.getDescriptor("Images.ExecSQLBatchIcon");
    
    public ExecSQLBatchAction(SQLEditor editor) {
		super(editor);
	}

	public ImageDescriptor getImageDescriptor() {
        return img;
    }

    public String getText() {
        return Messages.getString("SQLEditor.Actions.ExecuteBatch");
    }

    public String getToolTipText() {
        return Messages.getString("SQLEditor.Actions.ExecuteBatch.ToolTip");
    }
    
    public void run() {
        try {
        	executeSql();

        } catch (final Exception e) {
            _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    MessageDialog.openError(_editor.getSite().getShell(), Messages.getString("SQLResultsView.Error.Title"), e.getClass().getCanonicalName() + ": " + e.getMessage());
                }
            });
        }
    }

    protected QueryParser getQueryParser(Session session)
    {
    	return  session.getDatabaseProduct().getQueryParser(_editor.getSQLToBeExecuted(false), _editor.getSQLLineNumber(false));
    }
    protected void executeSql() {
        Session session = getSession();
        if (session == null)
            return;

        QueryParser qt = getQueryParser(session);
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
        	AbstractSQLExecution job = new SQLBatchExecution(_editor, qt);
        	job.schedule();
        }
    }
    
}
