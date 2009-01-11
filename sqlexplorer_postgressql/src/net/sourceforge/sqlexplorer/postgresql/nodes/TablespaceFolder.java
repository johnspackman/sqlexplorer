package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.Messages;
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

	private static final String QUERY = "SELECT DISTINCT spcname as \"${postgresql.hdr.name}\"," +
			"usename AS \"${postgresql.hdr.owner}\", "
			+ "CASE spclocation WHEN '' THEN setting ELSE spclocation END AS \"${postgresql.hdr.sysspace}\", "
			+ "CASE spclocation WHEN '' THEN TRUE ELSE FALSE END AS \"${postgresql.hdr.default}\", "
			+ "pg_size_pretty(pg_tablespace_size(spcname)) AS \"${postgresql.hdr.approxsize}\" "
			+ "FROM pg_tablespace spc "
			+ "JOIN pg_user us ON spcowner = usesysid "
			+ "LEFT JOIN pg_settings ON name = 'data_directory' "
			+ "WHERE ? LIKE '%' AND spcname LIKE ?;";

	private static final Object[] DEFAULT_LIMIT = { "%", "%" };

	private static final String OID_QUERY = "SELECT DISTINCT oid FROM "
			+ "pg_tablespace WHERE ? = '%' AND spcname = ?";

	public TablespaceFolder() {
		super(Messages.getString("postgresql.node.tablespace"));
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
		return DEFAULT_LIMIT;
	}

	public String getDetailSQL(Object[] params) {
		return Messages.processTemplate(QUERY);
	}

	public String getRequiresSQL() {
		String s = Messages.processTemplate(QUERY_REQUIRES_HEAD + OID_QUERY +
				QUERY_REQUIRES_MID + OID_QUERY + QUERY_REQUIRES_TAIL);
		logger.debug("Requires [" + s + "]");
		return s;
	}

	public String getRequiredBySQL(Object[] params) {
		String s = Messages.processTemplate("SELECT "
				+ "CASE relkind "
				+ "WHEN 'M' THEN '${postgresql.object.Tablespace}' "
				+ "WHEN 'd' THEN '${postgresql.object.Database}' "
				+ "WHEN 'r' THEN '${postgresql.object.Table}' "
				+ "WHEN 'i' THEN '${postgresql.object.Index}' "
				+ "WHEN 'S' THEN '${postgresql.object.Sequence}' "
				+ "WHEN 'v' THEN '${postgresql.object.View}' "
				+ "WHEN 'c' THEN '${postgresql.object.Composite}' "
				+ "WHEN 's' THEN '${postgresql.object.Special}' "
				+ "WHEN 't' THEN '${postgresql.object.Toast}' "
				+ "WHEN 'n' THEN '${postgresql.object.Schema}' "
				+ "WHEN 'y' THEN '${postgresql.object.Type}' "
				+ "WHEN 'd' THEN '${postgresql.object.Domain}' "
				+ "WHEN 'C' THEN '${postgresql.object.Conversion}' "
				+ "WHEN 'p' THEN '${postgresql.object.Function}' "
				+ "WHEN 'T' THEN '${postgresql.object.Trigger}' "
				+ "WHEN 'o' THEN '${postgresql.object.Operator}' "
				+ "END AS \"${postgresql.hdr.type}\", "
				+ "CASE relkind "
				+ "WHEN 'n' THEN relname "
				+ "WHEN 'i' THEN CASE WHEN nspname IS NOT NULL THEN nspname||'.'||relname||'.'||indname ELSE relname||'.'||indname END "
				+ "ELSE CASE WHEN nspname IS NULL THEN relname ELSE nspname||'.'||relname END "
				+ "END AS \"${postgresql.hdr.name}\" "
				+ "FROM ( "
				+ "SELECT cl.relkind, COALESCE(cin.nspname, cln.nspname) as nspname, COALESCE(ci.relname, cl.relname) as relname, cl.relname as indname "
				+ "FROM pg_class cl "
				+ "JOIN pg_namespace cln ON cl.relnamespace=cln.oid "
				+ "LEFT OUTER JOIN pg_index ind ON ind.indexrelid=cl.oid "
				+ "LEFT OUTER JOIN pg_class ci ON ind.indrelid=ci.oid "
				+ "LEFT OUTER JOIN pg_namespace cin ON ci.relnamespace=cin.oid, "
				+ "pg_database "
				+ "WHERE datname = current_database() "
				+ "AND (cl.reltablespace in ("+OID_QUERY+") "
				+ " OR (cl.reltablespace=0 AND dattablespace in ("+OID_QUERY+")  "
				+ " ))"
				+ " ) AS tmp " + "	ORDER BY 1,2");
		logger.debug("Will run [" + s + "]");

		return s;
	}

}
