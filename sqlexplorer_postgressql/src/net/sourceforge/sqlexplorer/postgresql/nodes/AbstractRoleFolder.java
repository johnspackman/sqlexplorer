package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.postgresql.util.PgUtil;

/**
 * Abstract base class for PostgreSQL's role concept. This is put into its own
 * class since only the OID queries for login roles and group roles differ but
 * are otherwise identical to handle.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 */
public abstract class AbstractRoleFolder extends AbstractFolder {

	/**
	 * Get the OID subquery for the current node.
	 * 
	 * @return A SQL <tt>select</tt> statement.
	 */
	public abstract String getOidSubquery();

	public String getRequiresSQL() {
		return QUERY_REQUIRES_HEAD + getOidSubquery() + QUERY_REQUIRES_MID
				+ getOidSubquery() + QUERY_REQUIRES_TAIL;
	}

	public String getRequiredBySQL(Object[] params) {
		return "SELECT "
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
				+ "SELECT rl.rolname, cl.relkind, COALESCE(cin.nspname, cln.nspname) as nspname, COALESCE(ci.relname, cl.relname) as relname, cl.relname as indname "
				+ "FROM pg_class cl "
				+ "JOIN pg_namespace cln ON cl.relnamespace=cln.oid "
				+ "JOIN pg_roles rl ON cl.relowner = rl.oid "
				+ "LEFT OUTER JOIN pg_index ind ON ind.indexrelid=cl.oid "
				+ "LEFT OUTER JOIN pg_class ci ON ind.indrelid=ci.oid "
				+ "LEFT OUTER JOIN pg_namespace cin ON ci.relnamespace=cin.oid "
				+ "UNION ALL "
				+ "SELECT rl.rolname, 'd', null, datname, null "
				+ "FROM pg_database db, pg_roles rl "
				+ "UNION ALL "
				+ (PgUtil.hasVersion(_sessionNode, 7, 5) ? "SELECT rl.rolname, 'M', null, spcname, null "
						+ "FROM pg_tablespace ns JOIN pg_roles rl ON ns.spcowner = rl.oid "
						+ "UNION ALL "
						: "")
				+ "SELECT rl.rolname, 'n', null, nspname, null "
				+ "FROM pg_namespace nsp JOIN pg_roles rl ON nsp.nspowner = rl.oid "
				+ "UNION ALL "
				+ "SELECT rl.rolname, CASE WHEN typtype='d' THEN 'd' ELSE 'y' END, null, typname, null "
				+ "FROM pg_type ty JOIN pg_roles rl ON ty.typowner = rl.oid "
				+ "UNION ALL "
				+ "SELECT rl.rolname, 'C', null, conname, null "
				+ "FROM pg_conversion co JOIN pg_roles rl ON co.conowner = rl.oid "
				+ "UNION ALL "
				+ "SELECT rl.rolname, CASE WHEN format_type(prorettype,NULL)='\"trigger\"' THEN 'T' ELSE 'p' END, null, proname, null "
				+ "FROM pg_proc pr JOIN pg_roles rl ON pr.proowner = rl.oid "
				+ "UNION ALL "
				+ "SELECT rl.rolname, 'o', null, oprname || '('::text ||  "
				+ "COALESCE(tl.typname, ''::text) ||  "
				+ "CASE WHEN tl.oid IS NOT NULL AND tr.oid IS NOT NULL THEN ','::text END ||  "
				+ "COALESCE(tr.typname, ''::text) || ')'::text, null "
				+ "FROM pg_operator op "
				+ "JOIN pg_roles rl ON op.oprowner = rl.oid "
				+ "LEFT JOIN pg_type tl ON tl.oid=op.oprleft "
				+ "LEFT JOIN pg_type tr ON tr.oid=op.oprright "
				+ ") AS tmp "
				+ "WHERE ? LIKE '%' AND tmp.rolname = ? AND ? LIKE '%' AND tmp.rolname = ? "
				+ "ORDER BY 1,2";
	}

	/**
	 * Detail query head. Subclasses may use it to append membership lists (i.e.
	 * all login roles for a group role and all group roles for a login role) as
	 * well as the from clause which must include the <tt>pg_roles</tt> table.
	 */
	protected static final String DETAIL_QUERY_HEAD = "SELECT DISTINCT "
			+ "	rolname as \"Name\", rolsuper AS \"Is superuser?\", rolinherit AS \"Inherits?\", "
			+ "	rolcreaterole AS \"Can create roles?\", rolcreatedb AS \"Can create databases?\", "
			+ "	rolcatupdate AS \"Can update catalog?\", rolcanlogin AS \"Can login?\", "
			+ "	rolconnlimit AS \"Connection limit\", rolvaliduntil AS \"Valid until\", ";
}
