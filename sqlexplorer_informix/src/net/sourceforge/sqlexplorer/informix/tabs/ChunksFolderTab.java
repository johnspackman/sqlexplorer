package net.sourceforge.sqlexplorer.informix.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

public class ChunksFolderTab extends AbstractSQLTab {

	public ChunksFolderTab() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getLabelText() {
		return Messages.getString("informix.storage.chunks");
	}

	@Override
	public String getSQL() {
		return "select c.chknum Number, d.name DBSpace, c.fname Pathname from sysmaster:sysdbspaces d, sysmaster:syschunks c where d.dbsnum=c.dbsnum order by c.chknum";
	}

	@Override
	public String getStatusMessage() {
		return Messages.getString("informix.storage.chunks")+" information.";
	}

}
