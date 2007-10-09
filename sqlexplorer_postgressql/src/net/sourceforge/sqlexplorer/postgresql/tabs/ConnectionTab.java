package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

/**
 * Detail tab providing info about database connections.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class ConnectionTab extends AbstractSQLTab {

	private final static String QUERY = "SELECT "
			+ "    usename AS \"Username\", "
			+ "    procpid AS \"Backend pid\", "
			+ "    TRANSLATE(current_query,E'\n',' ') AS \"Query\", "
			+ "    query_start AS \"Running since\", "
			+ "    backend_start AS \"Backend started\", "
			+ "    CASE WHEN client_addr IS NULL THEN 'Local' ELSE "
			+ "	   SUBSTR(client_addr::text,1,POSITION('/' IN client_addr::text)-1)||':'||text(client_port) "
			+ "    END AS \"Client\"" + "FROM " + "    pg_stat_activity ac "
			+ "WHERE " + "    datname = current_database() " + "ORDER BY "
			+ "    usename,procpid";

	@Override
	public String getLabelText() {
		return Messages.getString("postgresql.detail.db.connection.label");
	}

	@Override
	public String getSQL() {
		return QUERY;
	}

	@Override
	public String getStatusMessage() {
		String s = getNode().getSession().getUser().getAlias().getName();
		return Messages.getString("postgresql.detail.db.connection.status", s);
	}

}
