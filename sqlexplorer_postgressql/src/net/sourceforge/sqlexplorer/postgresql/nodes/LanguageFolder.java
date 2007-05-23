package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

/**
 * Support for PostgreSQL's language concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class LanguageFolder extends AbstractSQLFolderNode implements InfoNode,
		RequiresNode, RequiredByNode {

	private static final String TYPE = "language";

	private static final String QUERY = "SELECT lanname AS \"Name\","
			+ "NOT lanispl AS \"Is internal\",lanpltrusted AS \"Trusted\","
			+ "proc1.proname AS \"Handler\",proc2.proname AS \"Validator\" "
			+ "FROM pg_language pl LEFT JOIN pg_proc proc1 ON pl.lanplcallfoid = proc1.oid "
			+ "LEFT JOIN pg_proc proc2 ON pl.lanvalidator = proc2.oid "
			+ "WHERE ? LIKE '%' AND lanname LIKE ?;";

	private static final Object[] DEFAULT_LIMIT = { "%", "%" };

	private static final String OID_QUERY = "SELECT DISTINCT oid FROM pg_language where ? LIKE '%' AND lanname LIKE ?";

	@Override
	public String getChildType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return "Languages";
	}

	@Override
	public String getSQL() {
		return QUERY;
	}

	@Override
	public Object[] getSQLParameters() {
		return DEFAULT_LIMIT;
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
