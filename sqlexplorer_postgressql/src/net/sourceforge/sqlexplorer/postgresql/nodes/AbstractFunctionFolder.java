package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

/**
 * Abstract base class for PostgreSQL's function concept. These are procedures,
 * "pure" functions and triggers which basically only differ in the query
 * restrictions for identification.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public abstract class AbstractFunctionFolder extends AbstractSQLFolderNode
		implements InfoNode, RequiresNode, RequiredByNode {

	/**
	 * Get the query restriction to identify a function of this type. Filtering
	 * on schema and function name is done already, so subclasses need to focus
	 * on the pure type only; aggregates are treated specially by this extension
	 * so they're already excluded, too.
	 * 
	 * @return The query restriction for a <tt>WHERE</tt> clause.
	 */
	public abstract String getQueryRestriction();

	private static final String OID_QUERY = "SELECT DISTINCT pr.oid FROM pg_proc pr JOIN pg_namespace ns ON pr.pronamespace=ns.oid WHERE nspname LIKE ? AND proname LIKE ?";

	public AbstractFunctionFolder(String name) {
		super(name);
	}

	@Override
	public Object[] getSQLParameters() {
		return new Object[] { getSchemaOrCatalogName() };
	}

	@Override
	public String getSQL() {
		String restrict = getQueryRestriction();

		return "SELECT DISTINCT pr.proname "
				+ "FROM pg_proc pr "
				+ "	JOIN pg_namespace ns ON pr.pronamespace=ns.oid "
				+ "   JOIN pg_type type ON type.oid=pr.prorettype "
				+ "   JOIN pg_language lng ON lng.oid=prolang "
				+ "WHERE ns.nspname = ? AND pr.proisagg = FALSE "
				+ (restrict == null || restrict.trim().length() == 0 ? ""
						: " AND " + restrict) + ";";
	}

	public String getDetailSQL(Object[] params) {
		String restrict = getQueryRestriction();

		return Messages.processTemplate("SELECT DISTINCT "
				+ "pr.proname AS \"${postgresql.hdr.name}\", "
				+ "us.usename AS \"${postgresql.hdr.owner}\", "
				+ "lanname AS \"${postgresql.hdr.language}\", "
				+ "pr.pronargs AS \"${postgresql.hdr.argcount}\", "
				+ "pr.proargnames AS \"${postgresql.hdr.argnames}\", "
				+ "oidvectortypes(pr.proargtypes) AS \"${postgresql.hdr.argtypes}\", "
				+ "format_type(pr.prorettype::oid,NULL) AS \"${postgresql.hdr.returns}\", "
				+ "pr.oid::regprocedure AS \"${postgresql.hdr.sig}\" "
				+ "FROM pg_proc pr "
				+ "JOIN pg_namespace ns ON pr.pronamespace=ns.oid "
				+ "JOIN pg_user us ON pr.proowner=us.usesysid "
				+ "JOIN pg_language lang ON pr.prolang = lang.oid "
				+ "JOIN pg_type type ON type.oid=pr.prorettype "
				+ "WHERE ns.nspname LIKE ? AND pr.proisagg = FALSE AND pr.proname LIKE ? "
				+ (restrict == null || restrict.trim().length() == 0 ? ""
						: " AND " + restrict) + ";");
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
