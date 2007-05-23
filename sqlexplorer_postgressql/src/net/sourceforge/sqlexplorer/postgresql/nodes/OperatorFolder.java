package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

/**
 * Support for PostgreSQL's operator concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class OperatorFolder extends AbstractSQLFolderNode implements InfoNode,
		RequiresNode, RequiredByNode {

	private static final String TYPE = "operator";

	private static final String QUERY = "SELECT DISTINCT oprname FROM pg_operator"
			+ " op JOIN pg_namespace ns ON op.oprnamespace=ns.oid WHERE ns.nspname = ?";

	/* '%' is an operator but '_' is not: escape % with _ */
	private static final String DETAIL_QUERY = "SELECT op.oprname AS \"Name\","
			+ "CASE op.oprkind WHEN 'b' THEN 'Infix' WHEN 'l' THEN 'Prefix' WHEN 'r' THEN 'Postfix' END || ' operator' AS \"Type\","
			+ "us.usename AS \"Owner\",format_type(op.oprleft,NULL) AS \"Left argument\",format_type(op.oprright,NULL)  "
			+ "AS \"Right argument\",format_type(op.oprresult,NULL) AS \"Resulting argument\",com.oprname AS \"Commutator\",neg.oprname "
			+ "AS \"Negator\" FROM pg_operator op JOIN pg_namespace ns ON op.oprnamespace=ns.oid JOIN pg_user us "
			+ "ON op.oprowner=us.usesysid LEFT JOIN pg_operator com ON op.oprcom=com.oid LEFT JOIN pg_operator neg"
			+ " ON op.oprnegate=neg.oid WHERE ns.nspname LIKE ? AND op.oprname "
			+ "LIKE REPLACE(?, '%', '_%') ESCAPE '_'";

	private static final String OID_QUERY = "SELECT DISTINCT op.oid FROM pg_operator"
			+ " op JOIN pg_namespace ns ON op.oprnamespace=ns.oid WHERE ns.nspname = ? AND op.oprname LIKE "
			+ " REPLACE(?, '%', '_%') ESCAPE '_'";

	@Override
	public String getChildType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return "Operators";
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
