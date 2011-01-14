package net.sourceforge.sqlexplorer.informix.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

public class SpaceFolderTab extends AbstractSQLTab {

	public SpaceFolderTab() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getLabelText() {
		return Messages.getString("informix.storage.spaces");
	}

	@Override
	public String getSQL() {
		return "SELECT dbsnum Number, RTRIM(name) Name, is_mirrored Mirrored, nchunks as Chunks FROM sysmaster:sysdbspaces ORDER BY dbsnum";
	}

	@Override
	public String getStatusMessage() {
		return Messages.getString("informix.storage.spaces")+" information.";
	}

}
