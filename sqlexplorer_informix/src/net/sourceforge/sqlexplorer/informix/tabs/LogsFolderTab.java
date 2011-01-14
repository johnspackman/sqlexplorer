package net.sourceforge.sqlexplorer.informix.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

public class LogsFolderTab extends AbstractSQLTab {

	public LogsFolderTab() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getLabelText() {
		return Messages.getString("informix.storage.logs");
	}

	@Override
	public String getSQL() {
		return "SELECT * FROM sysmaster:syslogs ORDER BY number";

	}

	@Override
	public String getStatusMessage() {
		return Messages.getString("informix.storage.logs")+" information.";
	}

}
