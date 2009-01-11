package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;
import net.sourceforge.sqlexplorer.postgresql.util.PgUtil;

/**
 * Detail tab providing info about database wide locks.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class LockTab extends AbstractSQLTab {

	private static final String QUERY1 = "SELECT "
			+ "    pgl.relation::regclass AS \"Class\", "
			+ "    pg_get_userbyid(pg_stat_get_backend_userid(svrid)) AS \"User\", "
			+ "    pgl.transaction";
	private static final String QUERY2 = " AS \"Transaction\", "
			+ "    pg_stat_get_backend_pid(svrid) AS \"Pid\", "
			+ "    pgl.mode AS \"Mode\", "
			+ "    pgl.granted AS \"Granted\", "
			+ "    translate(pg_stat_get_backend_activity(svrid),E'\n',' ') AS \"Query\", "
			+ "    pg_stat_get_backend_activity_start(svrid) AS \"Running since\" "
			+ "FROM "
			+ "    pg_stat_get_backend_idset() svrid, pg_locks pgl, pg_database db "
			+ "WHERE "
			+ "    datname = current_database() AND pgl.pid = pg_stat_get_backend_pid(svrid) AND db.oid = pgl.database "
			+ "ORDER BY " + "    user,pid";

	@Override
	public String getLabelText() {
		return Messages.getString("postgresql.detail.db.lock.label");
	}

	@Override
	public String getSQL() 
	{
		if(PgUtil.hasVersion(getNode().getSession(), 8, 1))
		{
			return QUERY1+"id"+QUERY2;
		}
		return QUERY1+QUERY2;
	}

	@Override
	public String getStatusMessage() {
		String s = getNode().getSession().getUser().getAlias().getName();
		return Messages.getString("postgresql.detail.db.lock.status", s);
	}

}
