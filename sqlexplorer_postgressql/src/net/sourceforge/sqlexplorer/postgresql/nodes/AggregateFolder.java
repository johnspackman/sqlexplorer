package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

/**
 * Support for PostgreSQL's aggregate concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class AggregateFolder extends AbstractSQLFolderNode implements InfoNode,
		RequiresNode, RequiredByNode {

	private static final String TYPE = "aggregate";

	private static final String QUERY = "SELECT DISTINCT pr.proname FROM pg_proc pr "
			+ "JOIN pg_namespace ns ON pr.pronamespace=ns.oid "
			+ "WHERE ns.nspname = ? AND pr.proisagg = TRUE ";

	private static final String DETAIL_QUERY = "SELECT DISTINCT pr.proname AS \"Name\", "
			+ "us.usename AS \"Owner\", lanname AS \"Language\", "
			+ "pr.pronargs AS \"# arguments\", pr.proargnames AS \"Argument names\", "
			+ "oidvectortypes(pr.proargtypes) AS \"Argument types\", "
			+ "format_type(pr.prorettype::oid,NULL) AS \"Returns\", "
			+ "pr.oid::regprocedure AS \"Signature\" "
			+ "FROM pg_proc pr "
			+ "JOIN pg_namespace ns ON pr.pronamespace=ns.oid "
			+ "JOIN pg_user us ON pr.proowner=us.usesysid "
			+ "JOIN pg_language lang ON pr.prolang = lang.oid "
			+ "WHERE ns.nspname LIKE ? AND pr.proisagg = TRUE AND pr.proname LIKE ?";

	private static final String OID_QUERY = "SELECT DISTINCT proc.oid FROM pg_proc proc "
			+ "JOIN pg_namespace NS on proc.pronamespace = ns.oid WHERE proc.proisagg=true "
			+ "AND ns.nspname LIKE ? AND proc.proname LIKE ?";

	@Override
	public String getChildType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return "Aggregates";
	}

	@Override
	public String getSQL() {
		return QUERY;
	}

	@Override
	public Object[] getSQLParameters() {
		return new Object[] { getSchemaOrCatalogName() };
	}

	public String getDetailSQL(Object[] params) {
		return DETAIL_QUERY;
	}

	public String getRequiresSQL() {
		return QUERY_REQUIRES_HEAD + OID_QUERY + QUERY_REQUIRES_MID + OID_QUERY
				+ QUERY_REQUIRES_TAIL;
	}

	public String getRequiredBySQL(Object[] params) {
		return QUERY_REQUIREDBY_HEAD + OID_QUERY + QUERY_REQUIREDBY_MID
				+ OID_QUERY + QUERY_REQUIREDBY_TAIL;
	}

}
