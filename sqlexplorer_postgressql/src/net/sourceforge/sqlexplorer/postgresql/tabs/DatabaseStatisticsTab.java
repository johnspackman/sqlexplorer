package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractDataSetTab;
import net.sourceforge.sqlexplorer.dbstructure.nodes.DatabaseNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.postgresql.dataset.PropertyDataSet;

/**
 * Detail tab providing info about database statistics.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class DatabaseStatisticsTab extends AbstractDataSetTab {

	private static final String QUERY = "SELECT numbackends AS \"Running backends\","
			+ "xact_commit AS \"Committed transactions\","
			+ "xact_rollback AS \"Rolled back transactions\","
			+ "blks_read AS \"Block fetch requests\", "
			+ "blks_hit AS \"Block fetch cache hits\" "
			+ "FROM"
			+ "	pg_stat_database WHERE datname = current_database()";

	@Override
	public DataSet getDataSet() throws Exception {
		INode node = getNode();
		DatabaseNode db = (DatabaseNode) node;
		return PropertyDataSet.getPropertyDataSet(db.getSession(), QUERY, null);
	}

	@Override
	public String getStatusMessage() {
		String s = getNode().getSession().getUser().getAlias().getName();
		return Messages.getString("postgresql.detail.stat.status", s);
	}

	@Override
	public String getLabelText() {
		return Messages.getString("postgresql.detail.stat.label");
	}

}
