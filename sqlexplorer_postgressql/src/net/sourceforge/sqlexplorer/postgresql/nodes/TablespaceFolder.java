package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;

/**
 * Support for PostgreSQL's tablespace concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TablespaceFolder extends AbstractFolder {

	private static final ILogger logger = LoggerController
			.createLogger(TablespaceFolder.class);

	private static final String TYPE = "tablespace";

	private static final String QUERY = "SELECT DISTINCT spcname as \"Name\",usename AS \"Owner\", "
			+ "CASE spclocation WHEN '' THEN setting ELSE spclocation END AS \"Within default location\", "
			+ "CASE spclocation WHEN '' THEN TRUE ELSE FALSE END AS \"Default\", "
			+ "pg_size_pretty(pg_tablespace_size(spcname)) AS \"Approximate size\" "
			+ "FROM pg_tablespace spc "
			+ "JOIN pg_user us ON spcowner = usesysid "
			+ "LEFT JOIN pg_settings ON name = 'data_directory' "
			+ "WHERE ? LIKE '%' AND spcname LIKE ?;";

	private static final Object[] DEFAULT_LIMIT = { "%", "%" };

	private static final String OID_QUERY = "SELECT DISTINCT oid FROM "
			+ "pg_tablespace WHERE ? LIKE '%' AND spcname = ?";

	@Override
	public String getChildType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return "Tablespaces";
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
		String s = QUERY_REQUIRES_HEAD + OID_QUERY + QUERY_REQUIRES_MID
				+ OID_QUERY + QUERY_REQUIRES_TAIL;
		logger.debug("Requires [" + s + "]");
		return s;
	}

	public String getRequiredBySQL(Object[] params) {
		String oid = getList("SELECT DISTINCT oid FROM pg_tablespace WHERE spcname = '"
				+ params[1] + "'");
		String s = "SELECT "
				+ "CASE relkind "
				+ "WHEN 'M' THEN 'Tablespace' "
				+ "WHEN 'd' THEN 'Database' "
				+ "WHEN 'r' THEN 'Table' "
				+ "WHEN 'i' THEN 'Index' "
				+ "WHEN 'S' THEN 'Sequence' "
				+ "WHEN 'v' THEN 'View' "
				+ "WHEN 'c' THEN 'Composite' "
				+ "WHEN 's' THEN 'Special' "
				+ "WHEN 't' THEN 'Toast' "
				+ "WHEN 'n' THEN 'Schema' "
				+ "WHEN 'y' THEN 'Type' "
				+ "WHEN 'd' THEN 'Domain' "
				+ "WHEN 'C' THEN 'Conversion' "
				+ "WHEN 'p' THEN 'Function' "
				+ "WHEN 'T' THEN 'Trigger' "
				+ "WHEN 'o' THEN 'Operator' "
				+ "END AS \"Type\", "
				+ "CASE relkind "
				+ "WHEN 'n' THEN relname "
				+ "WHEN 'i' THEN CASE WHEN nspname IS NOT NULL THEN nspname||'.'||relname||'.'||indname ELSE relname||'.'||indname END "
				+ "ELSE CASE WHEN nspname IS NULL THEN relname ELSE nspname||'.'||relname END "
				+ "END AS \"Name\" "
				+ "FROM ( "
				+ "SELECT cl.relkind, COALESCE(cin.nspname, cln.nspname) as nspname, COALESCE(ci.relname, cl.relname) as relname, cl.relname as indname "
				+ "FROM pg_class cl "
				+ "JOIN pg_namespace cln ON cl.relnamespace=cln.oid "
				+ "LEFT OUTER JOIN pg_index ind ON ind.indexrelid=cl.oid "
				+ "LEFT OUTER JOIN pg_class ci ON ind.indrelid=ci.oid "
				+ "LEFT OUTER JOIN pg_namespace cin ON ci.relnamespace=cin.oid, "
				+ "pg_database "
				+ "WHERE datname = current_database() "
				+ "AND (cl.reltablespace = "
				+ oid
				+ " OR (cl.reltablespace=0 AND dattablespace =  "
				+ oid
				+ " )) AND ? LIKE '%' AND ? LIKE '%' AND ? LIKE '%' AND ? LIKE '%'"
				+ " ) AS tmp " + "	ORDER BY 1,2";
		logger.debug("Will run [" + s + "]");

		return s;
	}

}
