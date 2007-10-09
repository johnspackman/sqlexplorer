package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

/**
 * Support for PostgreSQL's conversion concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class ConversionFolder extends AbstractSQLFolderNode implements
		InfoNode, RequiresNode, RequiredByNode {

	private static final String TYPE = "conversion";

	private static final String QUERY = "SELECT conname AS \"${postgresql.hdr.name}\", "
			+ "us.usename AS \"${postgresql.hdr.owner}\", "
			+ "conforencoding AS \"${postgresql.hdr.enc_from}\","
			+ "contoencoding AS \"${postgresql.hdr.enc_to}\", pr.proname AS \"${postgresql.hdr.handler}\","
			+ " condefault AS \"${postgresql.hdr.default}\""
			+ " FROM pg_conversion conv "
			+ "JOIN pg_proc pr ON conv.conproc=pr.oid "
			+ "JOIN pg_namespace ns ON ns.oid = conv.connamespace "
			+ "JOIN pg_user us ON conv.conowner = us.usesysid "
			+ "WHERE ns.nspname LIKE ? AND conname LIKE ?;";

	private static final String OID_QUERY = "SELECT DISTINCT con.oid FROM pg_conversion con "
			+ "JOIN pg_namespace ns ON con.connamespace = ns.oid "
			+ "WHERE ns.nspname LIKE ? AND conname LIKE ?";

	public ConversionFolder() {
		super(Messages.getString("postgresql.node.conversion"));
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
		return new Object[] { getSchemaOrCatalogName(), "%" };
	}

	public String getDetailSQL(Object[] params) {
		return Messages.processTemplate(QUERY);
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
