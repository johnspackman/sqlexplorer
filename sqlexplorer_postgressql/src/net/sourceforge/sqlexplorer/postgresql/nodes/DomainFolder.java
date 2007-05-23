package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

/**
 * Support for PostgreSQL's domain concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class DomainFolder extends AbstractSQLFolderNode implements InfoNode,
		RequiresNode, RequiredByNode {

	private static final String TYPE = "domain";

	private static final String QUERY = "SELECT DISTINCT conname AS \"Name\","
			+ " CASE contype WHEN 'c' THEN 'Check' WHEN 'f' THEN 'Foreign key' "
			+ "WHEN 'p' THEN 'Primary key' WHEN 'u' THEN 'Unique' END || ' constaint' AS \"Type\", "
			+ "condeferrable AS \"Deferrable\", condeferred AS \"Deferred by default\", "
			+ "cl1.relname AS \"On relation\", cl2.relname AS \"Referenced relation\" "
			+ "FROM pg_constraint con JOIN pg_namespace ns ON ns.oid = con.connamespace "
			+ "LEFT JOIN pg_class cl1 ON con.conrelid = cl1.oid "
			+ "LEFT JOIN pg_class cl2 ON con.confrelid=cl2.oid WHERE ns.nspname LIKE ? AND conname LIKE ?";

	private static final String OID_QUERY = "SELECT DISTINCT con.oid "
			+ "FROM pg_constraint con JOIN pg_namespace ns ON ns.oid = con.connamespace "
			+ "WHERE ns.nspname LIKE ? AND conname LIKE ?";

	@Override
	public String getChildType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return "Domains";
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
		return QUERY;
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
