package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

/**
 * Detail tab providing info about database's prepared transactions.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TxTab extends AbstractSQLTab {

	private static final String QUERY = "SELECT " + "   owner AS \"Owner\", "
			+ "    transaction AS \"Transaction\", " + "    gid AS \"Gid\", "
			+ "    prepared AS \"Prepared at\" " + "FROM "
			+ "    pg_prepared_xacts " + "WHERE "
			+ "    database = current_database() " + "ORDER BY " + "    owner";

	@Override
	public String getLabelText() {
		return Messages.getString("postgresql.detail.db.tx.label");
	}

	@Override
	public String getSQL() {
		return QUERY;
	}

	@Override
	public String getStatusMessage() {
		String s = getNode().getSession().getUser().getAlias().getName();
		return Messages.getString("postgresql.detail.db.tx.status", s);
	}

}
