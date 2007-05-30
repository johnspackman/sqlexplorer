package net.sourceforge.sqlexplorer.postgresql.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.postgresql.actions.explain.AbstractExplainAction;

/**
 * Extension class for PostgreSQL's <tt>EXPLAIN ANALYZE</tt> feature.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class AnalyzeExplainAction extends AbstractExplainAction {

	/**
	 * Create new editor action.
	 * 
	 */
	public AnalyzeExplainAction() {
		super(AbstractExplainAction.EXPLAIN_ANALYZE);
	}

	@Override
	public String getText() {
		return Messages.getString("postgresql.explain.analyze");
	}

}
