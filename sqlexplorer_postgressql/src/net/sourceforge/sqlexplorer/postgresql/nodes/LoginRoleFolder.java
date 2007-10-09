package net.sourceforge.sqlexplorer.postgresql.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;

/**
 * Support for PostgreSQL's (login) role concept.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class LoginRoleFolder extends AbstractRoleFolder {

	private static final ILogger logger = LoggerController
			.createLogger(LoginRoleFolder.class);

	private static final String TYPE = "loginrole";

	private static final String QUERY = "SELECT DISTINCT usename ";

	private static final String TAIL = " FROM pg_user us JOIN pg_roles rl ON us.usesysid = rl.oid WHERE ? LIKE '%' AND usename LIKE ?";

	private static final String OID_QUERY = "SELECT DISTINCT usesysid FROM pg_user WHERE ? LIKE '%' AND usename LIKE ?";

	private static final Object[] DEFAULT_LIMIT = { "%", "%" };

	public LoginRoleFolder() {
		super(Messages.getString("postgresql.node.role.login"));
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
		return DEFAULT_LIMIT;
	}

	public String getDetailSQL(Object[] params) {
		String groups = getList("SELECT groname FROM pg_group WHERE (SELECT usesysid FROM pg_user WHERE usename='"
				+ params[1] + "') = ANY(grolist)");
		logger.debug("User [" + params[1] + "] turns out to be member of ["
				+ groups + "]");
		String s = Messages.processTemplate(DETAIL_QUERY_HEAD + " '" + groups + "' AS \"${postgresql.hdr.memberof}\" "
				+ TAIL);
		logger.debug("Will run [" + s + "]");
		return s;
	}

	@Override
	public String getOidSubquery() {
		return OID_QUERY;
	}

}
