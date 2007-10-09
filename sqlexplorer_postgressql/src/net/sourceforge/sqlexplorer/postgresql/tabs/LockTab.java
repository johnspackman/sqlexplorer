package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

/**
 * Detail tab providing info about database wide locks.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class LockTab extends AbstractSQLTab {

	private static final String QUERY = "SELECT "
			+ "    pgl.relation::regclass AS \"Class\", "
			+ "    pg_get_userbyid(pg_stat_get_backend_userid(svrid)) AS \"User\", "
			+ "    pgl.transaction AS \"Transaction\", "
			+ "    pg_stat_get_backend_pid(svrid) AS \"Pid\", "
			+ "    pgl.mode AS \"Mode\", "
			+ "    pgl.granted AS \"Granted\", "
			+ "    translate(pg_stat_get_backend_activity(svrid),E'\n',' ') AS \"Query\", "
			+ "    pg_stat_get_backend_activity_start(svrid) AS \"Running since\" "
			+ "FROM "
			+ "    pg_stat_get_backend_idset() svrid, pg_locks pgl, pg_database db "
			+ "WHERE "
			+ "    datname = current_database() AND pgl.pid = pg_stat_get_backend_pid(svrid) AND db.oid = pgl.database "
			+ "ORDER BY " + "    user,pid";;

	@Override
	public String getLabelText() {
		return Messages.getString("postgresql.detail.db.lock.label");
	}

	@Override
	public String getSQL() {
		return QUERY;
	}

	@Override
	public String getStatusMessage() {
		String s = getNode().getSession().getUser().getAlias().getName();
		return Messages.getString("postgresql.detail.db.lock.status", s);
	}

}
