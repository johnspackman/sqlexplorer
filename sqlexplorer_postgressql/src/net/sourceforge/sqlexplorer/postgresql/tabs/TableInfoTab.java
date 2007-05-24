package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.postgresql.nodes.InfoNode;

/**
 * Detail tab providing info about object's in a database table fashion.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TableInfoTab extends AbstractSQLTab {

	@Override
	public String getLabelText() {
		return Messages.getString("postgresql.detail.db.info.label");
	}

	private String _getSQL() {
		INode n = getNode().getParent();
		if (n instanceof InfoNode)
			return ((InfoNode) n).getDetailSQL(getSQLParameters());
		return "SELECT 'Error: detail info not implemented for node type "
				+ n.getType() + "' AS \"Message\","
				+ "? AS \"1st parameter\", ? AS \"2nd parameter\";";
	}

	@Override
	public String getSQL() {
		String s = _getSQL();
		_logger.debug("Info query turns out as [" + s + "]");
		return s;
	}

	@Override
	public Object[] getSQLParameters() {
		String s = getNode().getSchemaOrCatalogName();
		if (s == null || s.trim().length() == 0)
			s = "%";
		return new Object[] { s, getNode().getName() };
	}

	@Override
	public String getStatusMessage() {
		String t = Messages.getString("postgresql.object." +
				getNode().getType());
		return Messages.getString("postgresql.detail.db.info.status",
				new Object[] { t, getNode().getName() });
	}

}
