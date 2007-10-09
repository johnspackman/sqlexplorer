package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.Messages;
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
	private static final String DETAIL_QUERY = "SELECT "
		   +  "op.oprname AS \"${postgresql.hdr.name}\","
			+ "CASE op.oprkind WHEN 'b' THEN '${postgresql.op.infix}' WHEN 'l' THEN '${postgresql.op.prefix}' WHEN 'r' THEN '${postgresql.op.postfix}' END || ' ${postgresql.op}' AS \"${postgresql.hdr.type}\","
			+ "us.usename AS \"${postgresql.hdr.owner}\",format_type(op.oprleft,NULL) AS \"${postgresql.hdr.leftarg}\",format_type(op.oprright,NULL)  "
			+ "AS \"${postgresql.hdr.rightarg}\",format_type(op.oprresult,NULL) AS \"${postgresql.hdr.resarg}\",com.oprname AS \"${postgresql.hdr.commutator}\",neg.oprname "
			+ "AS \"${postgresql.hdr.negator}\" FROM pg_operator op JOIN pg_namespace ns ON op.oprnamespace=ns.oid JOIN pg_user us "
			+ "ON op.oprowner=us.usesysid LEFT JOIN pg_operator com ON op.oprcom=com.oid LEFT JOIN pg_operator neg"
			+ " ON op.oprnegate=neg.oid WHERE ns.nspname LIKE ? AND op.oprname "
			+ "LIKE REPLACE(?, '%', '_%') ESCAPE '_'";

	private static final String OID_QUERY = "SELECT DISTINCT op.oid FROM pg_operator"
			+ " op JOIN pg_namespace ns ON op.oprnamespace=ns.oid WHERE ns.nspname = ? AND op.oprname LIKE "
			+ " REPLACE(?, '%', '_%') ESCAPE '_'";

	public OperatorFolder() {
		super(Messages.getString("postgresql.node.operator"));
	}

	@Override
	public String getChildType() {
		return TYPE;
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
		return Messages.processTemplate(DETAIL_QUERY);
	}

	public String getRequiresSQL() {
		return Messages.processTemplate(QUERY_REQUIRES_HEAD + OID_QUERY +
				QUERY_REQUIRES_MID + OID_QUERY + QUERY_REQUIRES_TAIL);
	}

	public String getRequiredBySQL(Object[] params) {
		return Messages.processTemplate(QUERY_REQUIREDBY_HEAD + OID_QUERY +
				QUERY_REQUIREDBY_MID + OID_QUERY + QUERY_REQUIREDBY_TAIL);
	}

}
