package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.Messages;

/**
 * Support for PostgreSQL's trigger concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TriggerFolder extends AbstractFunctionFolder {

	private static final String TYPE = "trigger";

	private static final String RESTRICT = "format_type(type.oid,NULL) = '\"trigger\"' AND lanname <> 'edbspl'";

	public TriggerFolder() {
		super(Messages.getString("postgresql.node.trigger"));
	}

	@Override
	public String getChildType() {
		return TYPE;
	}

	@Override
	public String getQueryRestriction() {
		return RESTRICT;
	}
}
