package net.sourceforge.sqlexplorer.informix.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

public class ProcessTab extends AbstractSQLTab {

	public ProcessTab() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getLabelText() {
		return Messages.getString("informix.DatabaseDetailView.Tab.Processes");
	}

	@Override
	public String getSQL() {
		return "select d.name database, s.username, s.hostname, l.owner sid from sysmaster:syslocks l, sysmaster:sysdatabases d, outer sysmaster:syssessions s where l.tabname = \"sysdatabases\" and l.rowidlk = d.rowid and l.owner = s.sid order by 1";
	}

	@Override
	public String getStatusMessage() {
		return "staatus "+getNode().getName()+ " "+getNode().getSchemaOrCatalogName();
	}

}
