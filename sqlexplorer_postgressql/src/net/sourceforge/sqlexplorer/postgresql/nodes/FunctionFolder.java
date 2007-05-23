package net.sourceforge.sqlexplorer.postgresql.nodes;

/**
 * Support for PostgreSQL's function concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class FunctionFolder extends AbstractFunctionFolder {

	private static final String TYPE = "function";

	/* exclude conversions, presented separatedly */
	private static final String RESTRICT = "format_type(type.oid,NULL) <> '\"trigger\"' "
			+ "AND NOT (lanname = 'edbspl' AND format_type(type.oid,NULL) = 'void' ) "
			+ "AND pr.oid NOT IN (SELECT DISTINCT conproc FROM pg_conversion)";

	@Override
	public String getChildType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return "Functions";
	}

	@Override
	public String getQueryRestriction() {
		return RESTRICT;
	}
}
