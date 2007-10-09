package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;

/**
 * Support for PostgreSQL's (group) role concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class GroupRoleFolder extends AbstractRoleFolder {

	private static final ILogger logger = LoggerController
			.createLogger(GroupRoleFolder.class);

	private static final String TYPE = "grouprole";

	private static final String OID_QUERY = "SELECT DISTINCT grosysid FROM pg_group WHERE ? LIKE '%' AND groname LIKE ?";

	private static final String QUERY = "SELECT DISTINCT groname ";

	private static final String TAIL = " FROM pg_group gp JOIN pg_roles rl ON gp.grosysid = rl.oid WHERE ? LIKE '%' AND groname LIKE ?";

	public GroupRoleFolder() {
		super(Messages.getString("postgresql.node.role.group"));
	}

	@Override
	public String getChildType() {
		return TYPE;
	}

	@Override
	public String getSQL() {
		return QUERY + TAIL;
	}

	@Override
	public Object[] getSQLParameters() {
		return new LoginRoleFolder().getSQLParameters();
	}

	public String getDetailSQL(Object[] params) {
		String members = getList("SELECT rolname FROM pg_roles rl, pg_group gp WHERE gp.groname = '"
				+ params[1] + "' AND rl.oid = ANY(gp.grolist)");
		logger.debug("Role [" + params[1]
				+ "] turns out to have these members [" + members + "]");
		String s = Messages.processTemplate(DETAIL_QUERY_HEAD + " '" + members + "' AS \"${postgresql.hdr.members}\" "
				+ TAIL);
		logger.debug("Will run [" + s + "]");
		return s;
	}

	@Override
	public String getOidSubquery() {
		return OID_QUERY;
	}
}
