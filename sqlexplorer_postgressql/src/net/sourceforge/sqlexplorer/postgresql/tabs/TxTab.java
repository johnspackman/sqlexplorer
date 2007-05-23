package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

/**
 * Detail tab providing info about database's prepared transactions.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TxTab extends AbstractSQLTab {

	@Override
	public String getLabelText() {
		return "Prepared transactions";
	}

	@Override
	public String getSQL() {
		return "SELECT * FROM pg_prepared_xacts";
	}

	@Override
	public String getStatusMessage() {
		return "Transactions for";
	}

}
