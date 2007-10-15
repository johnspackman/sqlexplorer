package net.sourceforge.sqlexplorer.postgresql.actions.explain;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.sqleditor.actions.AbstractEditorAction;

import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Class plugging into SQL editor to provide explain support for PostgreSQL.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public abstract class AbstractExplainAction extends AbstractEditorAction {

	protected final int type;

	/**
	 * Constant indicating a normal explain.
	 */
	public static final int EXPLAIN_NORMAL = 0;

	/**
	 * Constant indicating an analyzed (actually executed) explain.
	 */
	public static final int EXPLAIN_ANALYZE = 1;

	/**
	 * Create new explain editor action.
	 * 
	 * @param type
	 *            The explain type
	 * @see #EXPLAIN_ANALYZE
	 * @see #EXPLAIN_NORMAL
	 */
	public AbstractExplainAction(int type) {
		this.type = type;
	}

	@Override
	public String getToolTipText() {
		return getText();
	}

	@Override
	public void run() {
		Session session = getSession();
		if (session == null)
			return;


        // execute explain plan for all statements
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
        new ExplainExecution(_editor, qt, type).schedule();
	}
}
