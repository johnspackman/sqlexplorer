package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.Messages;

/**
 * Support for PostgreSQL's procedure concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class ProcedureFolder extends AbstractFunctionFolder {

	private static final String TYPE = "procedure";

	private static final String RESTRICT = "format_type(type.oid,NULL) = 'void' AND lanname = 'edbspl'";

	@Override
	public String getChildType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return Messages.getString("postgresql.node.procedure");
	}

	@Override
	public String getQueryRestriction() {
		return RESTRICT;
	}
}
