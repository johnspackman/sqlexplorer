package net.sourceforge.sqlexplorer.postgresql.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.postgresql.actions.explain.AbstractExplainAction;

/**
 * Extension class for PostgreSQL's <tt>EXPLAIN</tt> feature.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class NormalExplainAction extends AbstractExplainAction {

	/**
	 * Create new editor action.
	 * 
	 */
	public NormalExplainAction() {
		super(AbstractExplainAction.EXPLAIN_NORMAL);

	}

	@Override
	public String getText() {
		return Messages.getString("postgresql.explain.normal");
	}
}
