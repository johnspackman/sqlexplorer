package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractDataSetTab;
import net.sourceforge.sqlexplorer.dbstructure.nodes.DatabaseNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.postgresql.dataset.PropertyDataSet;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

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
		SQLConnection c0 = db.getSession().getInteractiveConnection();
		return PropertyDataSet.getPropertyDataSet(c0, QUERY, null);
	}

	@Override
	public String getStatusMessage() {
		return "Statistics for " + getNode().getName();
	}

	@Override
	public String getLabelText() {
		return "Statistics";
	}

}
