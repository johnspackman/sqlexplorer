package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractDataSetTab;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.postgresql.dataset.PropertyDataSet;

/**
 * Detail tab providing info about a table's statistics.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TableStatisticsTab extends AbstractDataSetTab {

	private static final String QUERY = "SELECT "
			+ "    a.schemaname||'.'||a.relname AS \"Qualified name\", "
			+ "    a.seq_scan AS \"Sequential scans\", "
			+ "    a.seq_tup_read AS \"Rows fetched during sequal scans\", "
			+ "    sum(b.idx_scan) AS \"Index scans\", "
			+ "    sum(b.idx_tup_read) AS \"Index reads\", "
			+ "    a.idx_tup_fetch AS \"Rows fetched during index scans\", "
			+ "    a.n_tup_ins AS \"Rows inserted\", "
			+ "    a.n_tup_upd AS \"Rows updated\", "
			+ "    a.n_tup_del AS \"Rows deleted\" "
			+ "FROM "
			+ "    pg_stat_all_tables a  "
			+ "	LEFT JOIN pg_stat_all_indexes b ON a.schemaname = b.schemaname AND a.relname = b.relname "
			+ "WHERE a.schemaname = ? AND a.relname = ? "
			+ "GROUP BY a.schemaname,a.relname,a.seq_scan,a.seq_tup_read,a.idx_tup_fetch,a.n_tup_ins,a.n_tup_upd,a.n_tup_del";

	@Override
	public DataSet getDataSet() throws Exception {
		INode node = getNode();
		return PropertyDataSet.getPropertyDataSet(node.getSession(), QUERY, new Object[] {
				node.getSchemaOrCatalogName(), node.getName() });
	}

	@Override
	public String getStatusMessage() {
		return Messages.getString("postgresql.detail.stat.status", getNode().getName());
	}

	@Override
	public String getLabelText() {
		return Messages.getString("postgresql.detail.stat.label");
	}

}
