package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractDataSetTab;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.postgresql.dataset.PropertyDataSet;
import net.sourceforge.sqlexplorer.postgresql.nodes.InfoNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

/**
 * Detail tab providing info about object's in a key-value list fashion.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class PropertyInfoTab extends AbstractDataSetTab {

	@Override
	public DataSet getDataSet() throws Exception {
		SQLConnection c = getNode().getSession().getInteractiveConnection();
		INode n = getNode().getParent();
		String s = getNode().getSchemaOrCatalogName();
		if (s == null || s.trim().length() == 0)
			s = "%";
		Object[] params = new Object[] { s, getNode().getName() };
		if (!(n instanceof InfoNode))
			return PropertyDataSet.getPropertyDataSet(c,
					"SELECT 'Error: detail info not implemented for node type "
							+ n.getType() + "' AS \"Message\";", params);

		return PropertyDataSet.getPropertyDataSet(c, ((InfoNode) n)
				.getDetailSQL(params), params);
	}

	@Override
	public String getStatusMessage() {
		return "Detail info for " + getNode().getType() + " "
				+ getNode().getName();
	}

	@Override
	public String getLabelText() {
		return "Info";
	}

}
