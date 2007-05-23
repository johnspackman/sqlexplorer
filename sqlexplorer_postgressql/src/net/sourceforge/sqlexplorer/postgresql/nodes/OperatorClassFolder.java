package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

/**
 * Support for PostgreSQL's operator class concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class OperatorClassFolder extends AbstractSQLFolderNode implements
		InfoNode, RequiresNode, RequiredByNode {

	private static final String TYPE = "opclass";

	private static final String QUERY = "SELECT DISTINCT opcname "
			+ "FROM pg_opclass cl JOIN pg_namespace ns ON cl.opcnamespace=ns.oid WHERE ns.nspname = ?";

	private static final String DETAIL_QUERY = "SELECT DISTINCT "
			+ "opcname AS \"Name\",am.amname AS \"For index type\", "
			+ "format_type(opcintype,NULL) AS \"For type\",us.usename AS \"Owner\" "
			+ "FROM pg_opclass cl JOIN pg_namespace ns ON cl.opcnamespace=ns.oid "
			+ "JOIN pg_am am ON cl.opcamid=am.oid LEFT JOIN pg_user us ON us.usesysid=opcowner "
			+ "WHERE ns.nspname LIKE ? AND opcname LIKE ?";

	private static final String OID_QUERY = "SELECT DISTINCT cl.oid "
			+ "FROM pg_opclass cl JOIN pg_namespace ns ON cl.opcnamespace=ns.oid WHERE ns.nspname = ? AND cl.opcname LIKE ?";

	@Override
	public String getChildType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return "Operator Classes";
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
