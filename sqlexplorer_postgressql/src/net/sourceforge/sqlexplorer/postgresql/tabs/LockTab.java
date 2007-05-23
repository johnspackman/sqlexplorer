package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

/**
 * Detail tab providing info about database wide locks.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class LockTab extends AbstractSQLTab {

	@Override
	public String getLabelText() {
		return "Locks";
	}

	@Override
	public String getSQL() {
		return "SELECT (SELECT datname FROM pg_database WHERE oid = pgl.database) AS dbname, "
				+ "pgl.relation::regclass AS class, "
				+ "pg_get_userbyid(pg_stat_get_backend_userid(svrid)) as user, "
				+ "pgl.transaction, pg_stat_get_backend_pid(svrid) AS pid, pgl.mode, pgl.granted, "
				+ "pg_stat_get_backend_activity(svrid) AS current_query, "
				+ "pg_stat_get_backend_activity_start(svrid) AS query_start "
				+ "FROM pg_stat_get_backend_idset() svrid, pg_locks pgl "
				+ "WHERE pgl.pid = pg_stat_get_backend_pid(svrid) "
				+ "ORDER BY pid";
	}

	@Override
	public String getStatusMessage() {
		return "Locks for";
	}

}
