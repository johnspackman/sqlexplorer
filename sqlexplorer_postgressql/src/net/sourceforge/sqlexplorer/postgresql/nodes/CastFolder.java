package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

/**
 * Support for PostgreSQL's cast concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class CastFolder extends AbstractSQLFolderNode implements InfoNode,
		RequiresNode, RequiredByNode {

	private static final String TYPE = "cast";

	private static final String QUERY = "SELECT DISTINCT "
			+ "REPLACE(format_type(s.oid,NULL),'\"','')||' -> '||REPLACE(format_type (t.oid,NULL),'\"','') AS \"${postgresql.hdr.name}\","
			+ " CASE c.castfunc WHEN 0 THEN TRUE ELSE FALSE END AS \"${postgresql.hdr.bincompat}\", "
			+ "COALESCE(proc.proname,'') AS \"${postgresql.hdr.function}\" "
			+ "FROM pg_cast c "
			+ "JOIN pg_type s ON c.castsource=s.oid"
			+ " JOIN pg_type t ON c.casttarget=t.oid "
			+ "LEFT JOIN pg_proc proc ON c.castfunc = proc.oid "
			+ "WHERE ? LIKE '%' AND "
			+ "REPLACE(format_type(s.oid,NULL),'\"','')||' -> '||REPLACE(format_type (t.oid,NULL),'\"','') LIKE ?";

	private static final Object[] DEFAULT_LIMIT = { "%", "%" };

	private static final String OID_QUERY = "SELECT DISTINCT c.oid "
			+ "FROM pg_cast c JOIN pg_type s ON c.castsource=s.oid "
			+ "JOIN pg_type t ON c.casttarget=t.oid WHERE ? LIKE '%' AND "
			+ "REPLACE(format_type(s.oid,NULL),'\"','')||' -> '||REPLACE(format_type (t.oid,NULL),'\"','') LIKE ?";

	public CastFolder() {
		super(Messages.getString("postgresql.node.cast"));
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
		return Messages.processTemplate(QUERY_REQUIRES_HEAD + OID_QUERY +
				QUERY_REQUIRES_MID + OID_QUERY + QUERY_REQUIRES_TAIL);
	}

	public String getRequiredBySQL(Object[] params) {
		return Messages.processTemplate(QUERY_REQUIREDBY_HEAD + OID_QUERY +
				QUERY_REQUIREDBY_MID + OID_QUERY + QUERY_REQUIREDBY_TAIL);
	}

}
