package net.sourceforge.sqlexplorer.informix.tabs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

public class ConfigTab extends AbstractSQLTab {

	public ConfigTab() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getLabelText() {
		return Messages.getString("informix.DatabaseDetailView.Tab.Config");
	}

	@Override
	public String getSQL() {
		return "select trim(cf_name) as parameter, trim(cf_effective) as value from sysmaster:sysconfig";
	}

	@Override
	public String getStatusMessage() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
	}

}
