package net.sourceforge.sqlexplorer.informix.tabs;

import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

public class DBDetailTab extends AbstractSQLTab {

	public DBDetailTab() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getLabelText() {
		return "TEST";
	}

	@Override
	public String getSQL() {
		return "select trim(name) as database, case when partnum > 0 THEN (select collate from sysmaster:systabnames t where "+ 
	   "t.partnum = d.partnum) else 'Unknown' END AS collation,	created, case when is_logging = 1 THEN "+ 
	   "case when is_buff_log = 1 THEN 'Buffered' WHEN is_ansi = 1 THEN 'ANSI' else 'Unbuffered' END " + 
	   " else 'Not Logged' end as logging FROM sysmaster:sysdatabases d order by 1";

		
//		return "SELECT * FROM sysmaster:sysdatabases WHERE name=\""+getNode().getName()+"\"";
	}

	@Override
	public String getStatusMessage() {
		return "TEST";
	}

}
