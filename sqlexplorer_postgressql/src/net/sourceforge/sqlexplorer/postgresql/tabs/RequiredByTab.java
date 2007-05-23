package net.sourceforge.sqlexplorer.postgresql.tabs;

import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.postgresql.nodes.RequiredByNode;

/**
 * Detail tab providing info about object's dependants.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class RequiredByTab extends AbstractSQLTab {

	@Override
	public String getLabelText() {
		return "Required by";
	}

	private String _getSQL() {
		INode n = getNode().getParent();
		if (n instanceof RequiredByNode)
			return ((RequiredByNode) n).getRequiredBySQL(getSQLParameters());
		return "SELECT 'Error: required by info not implemented for node type "
				+ n.getType()
				+ "' AS \"Message\","
				+ "? AS \"1st parameter\", ? AS \"2nd parameter\", ? AS \"3rd parameter\", ? AS \"4th parameter\";";
	}

	@Override
	public String getSQL() {
		String s = _getSQL();
		_logger.debug("Reguires query turns out as [" + s + "]");
		return s;
	}

	@Override
	public Object[] getSQLParameters() {
		String s = getNode().getSchemaOrCatalogName();
		if (s == null || s.trim().length() == 0)
			s = "%";
		return new Object[] { s, getNode().getName(), s, getNode().getName() };
	}

	@Override
	public String getStatusMessage() {
		return "Objects requiring " + getNode().getType() + " "
				+ getNode().getName();
	}

}
