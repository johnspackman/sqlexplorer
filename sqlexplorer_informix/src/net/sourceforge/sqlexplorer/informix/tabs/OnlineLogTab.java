package net.sourceforge.sqlexplorer.informix.tabs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

public class OnlineLogTab extends AbstractSQLTab {

	public OnlineLogTab() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getLabelText() {
		return Messages.getString("informix.DatabaseDetailView.Tab.OnlineLog");
	}

	@Override
	public String getSQL() {
		return "SELECT skip 1 trim(line) as Message from sysmaster:sysonlinelog where offset > -10000 order by rowid desc";
	}

	@Override
	public String getStatusMessage() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
	}

}
