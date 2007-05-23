package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

/**
 * Detail tab providing info about database connections.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class ConnectionTab extends AbstractSQLTab {

	@Override
	public String getLabelText() {
		return "Connections";
	}

	@Override
	public String getSQL() {
		return "SELECT * FROM pg_stat_activity";
	}

	@Override
	public String getStatusMessage() {
		return "Connections for";
	}

}
