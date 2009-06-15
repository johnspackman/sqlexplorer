/**
 * 
 */
package net.sourceforge.sqlexplorer.sqleditor.actions;

import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;

/**
 * @author Heiko Hilbert
 *
 */
public class ExecCurrentSQLAction extends ExecSQLAction {
	public static final String COMMAND_ID = "net.sourceforge.sqlexplorer.executeCurrentSQL";
	/**
	 * @param editor
	 */
	public ExecCurrentSQLAction(SQLEditor editor) {
		super(editor);
	}

    protected QueryParser getQueryParser(Session session)
    {
    	return  session.getDatabaseProduct().getQueryParser(_editor.getSQLToBeExecuted(true), _editor.getSQLLineNumber(true));
    }
	
}
