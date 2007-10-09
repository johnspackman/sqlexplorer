package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.Messages;
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
			+ "opcname AS \"${postgresql.hdr.name}\",am.amname AS \"${postgresql.hdr.foridx}\", "
			+ "format_type(opcintype,NULL) AS \"${postgresql.hdr.fortype}\",us.usename AS \"${postgresql.hdr.owner}\" "
			+ "FROM pg_opclass cl JOIN pg_namespace ns ON cl.opcnamespace=ns.oid "
			+ "JOIN pg_am am ON cl.opcamid=am.oid LEFT JOIN pg_user us ON us.usesysid=opcowner "
			+ "WHERE ns.nspname LIKE ? AND opcname LIKE ?";

	private static final String OID_QUERY = "SELECT DISTINCT cl.oid "
			+ "FROM pg_opclass cl JOIN pg_namespace ns ON cl.opcnamespace=ns.oid WHERE ns.nspname = ? AND cl.opcname LIKE ?";

	public OperatorClassFolder() {
		super(Messages.getString("postgresql.node.opclass"));
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
