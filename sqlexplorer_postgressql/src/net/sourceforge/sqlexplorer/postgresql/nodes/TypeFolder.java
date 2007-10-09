package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

/**
 * Support for PostgreSQL's type concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TypeFolder extends AbstractSQLFolderNode implements InfoNode,
		RequiresNode, RequiredByNode {

	private static final String TYPE = "type";

	private static final String QUERY = "SELECT DISTINCT format_type(t.oid,NULL)"
			+ "FROM pg_type t "
			+ "JOIN pg_namespace ns ON t.typnamespace = ns.oid "
			+ "LEFT OUTER JOIN pg_type e ON e.oid=t.typelem "
			+ "LEFT OUTER JOIN pg_class ct ON ct.oid=t.typrelid AND ct.relkind <> 'c'"
			+ "WHERE t.typtype != 'd' AND SUBSTR(t.typname,1,1) <> '_' AND ns.nspname LIKE ?";

	private static final String DETAIL_QUERY = "SELECT DISTINCT "
			+ "format_type(t.oid,NULL) AS \"${postgresql.hdr.name}\","
			+ "t.typlen AS \"${postgresql.hdr.length}\",CASE t.typtype WHEN 'b' THEN '${postgresql.type.base}' WHEN 'c' THEN '${postgresql.type.compound}' "
			+ "WHEN 'p' THEN '${postgresql.type.pseudo}' END || ' ${postgresql.type}' AS \"${postgresql.hdr.type}\",t.typdefault AS \"${postgresql.hdr.default}\", "
			+ "des.description AS \"${postgresql.hdr.desc}\" "
			+ "FROM pg_type t "
			+ "JOIN pg_namespace ns ON t.typnamespace = ns.oid "
			+ "LEFT OUTER JOIN pg_type e ON e.oid=t.typelem "
			+ "LEFT OUTER JOIN pg_class ct ON ct.oid=t.typrelid AND ct.relkind <> 'c'"
			+ "LEFT OUTER JOIN pg_description des ON des.objoid=t.oid	"
			+ "WHERE t.typtype != 'd' AND SUBSTR(t.typname,1,1) <> '_' AND ns.nspname LIKE ? AND format_type(t.oid,NULL) LIKE ?";

	private static final String OID_QUERY = "SELECT DISTINCT t.oid "
			+ "FROM pg_type t "
			+ "JOIN pg_namespace ns ON t.typnamespace = ns.oid "
			+ "LEFT OUTER JOIN pg_type e ON e.oid=t.typelem "
			+ "LEFT OUTER JOIN pg_class ct ON ct.oid=t.typrelid AND ct.relkind <> 'c'"
			+ "WHERE t.typtype != 'd' AND SUBSTR(t.typname,1,1) <> '_' AND ns.nspname LIKE ? AND format_type(t.oid,NULL) LIKE ?";

	public TypeFolder() {
		super(Messages.getString("postgresql.node.type"));
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
