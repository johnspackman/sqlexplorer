package net.sourceforge.sqlexplorer.informix.actions;

import org.eclipse.jface.dialogs.MessageDialog;

import net.sourceforge.sqlexplorer.informix.actions.explain.ExplainExecution;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction;

public class ExplainSQL extends AbstractEditorAction {

	public String getText() {
		return "Explain current SQL.";
	}
	
    public String getToolTipText() {
        return getText();
    }
	

	@Override
	public void run() {
		Session session = getSession();
        if (session == null) return;
		
        QueryParser qt = session.getDatabaseProduct().getQueryParser(_editor.getSQLToBeExecuted(), _editor.getSQLLineNumber());
        try {
            qt.parse();
        }catch(final ParserException e) {
            _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(_editor.getSite().getShell(), "mingi error", e.getMessage());
                }
            });
        }
       	new ExplainExecution(_editor, qt).schedule();
	}

}
