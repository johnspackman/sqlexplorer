package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractDataSetTab;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.postgresql.dataset.PropertyDataSet;
import net.sourceforge.sqlexplorer.postgresql.nodes.InfoNode;

/**
 * Detail tab providing info about object's in a key-value list fashion.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class PropertyInfoTab extends AbstractDataSetTab {

	@Override
	public DataSet getDataSet() throws Exception {
		INode n = getNode().getParent();
		String s = getNode().getSchemaOrCatalogName();
		if (s == null || s.trim().length() == 0)
			s = "%";
		Object[] params = new Object[] { s, getNode().getName() };
		if (!(n instanceof InfoNode))
			return PropertyDataSet.getPropertyDataSet(getNode().getSession(),
					"SELECT 'Error: detail info not implemented for node type "
							+ n.getType() + "' AS \"Message\";", params);

		return PropertyDataSet.getPropertyDataSet(getNode().getSession(), ((InfoNode) n)
				.getDetailSQL(params), params);
	}

	@Override
	public String getStatusMessage() {
		String t = Messages.getString("postgresql.object." +
				getNode().getType());
		return Messages.getString("postgresql.detail.db.info.status",
				new Object[] { t, getNode().getName() });
	}

	@Override
	public String getLabelText() {
		return Messages.getString("postgresql.detail.db.info.label");
	}

}
