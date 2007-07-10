package net.sourceforge.sqlexplorer.mssql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;
import net.sourceforge.sqlexplorer.mssql.nodes.JobsNode;

public class JobHistoryTab extends AbstractSQLTab {

	public JobHistoryTab() {
	}

	@Override
	public String getLabelText() {
		return Messages.getString("mssql.dbdetail.tab.jobhistory");
	}

	@Override
	public String getSQL() {
        return "USE msdb " +
        "select case WHEN LEN( cast(run_date as varchar)) < 8 THEN 'Unknown ' " +
		"ELSE "+
			"SUBSTRING(cast( run_date as varchar),1,4)+'/'+ "+
			"SUBSTRING(cast( run_date as varchar),5,2)+'/'+ "+
			"SUBSTRING(cast( run_date as varchar),7,2)+' '+ " +

			"case LEN(cast(run_time as varchar)) " +
				"WHEN 5 THEN "+
					"'0'+SUBSTRING(cast(run_time as varchar),1,1)+':'+ "+
					"SUBSTRING(cast(run_time as varchar),2,2)+':'+ "+
					"SUBSTRING(cast(run_time as varchar),4,2) "+
				"WHEN 4 THEN " +
					"'00:'+"+
					"SUBSTRING(cast(run_time as varchar),1,2)+':' +"+
					"SUBSTRING(cast(run_time as varchar),3,2) " +
				"WHEN 3 THEN " +
					"'00:0'+" +
					"SUBSTRING(cast(run_time as varchar),1,1)+':' +"+
					"SUBSTRING(cast(run_time as varchar),2,2) " +
				"WHEN 2 THEN" +
					"'00:00:'+" +
					"SUBSTRING(cast(run_time as varchar),1,2) " +
				"WHEN 1 THEN"+
					"'00:00:0'+"+
					"SUBSTRING(cast(run_time as varchar),1,1) " +
				"WHEN 0 THEN" +
					"'00:00:00' "+
				"ELSE "+
					"SUBSTRING(cast(run_time as varchar),1,2)+':'+ "+
					"SUBSTRING(cast(run_time as varchar),3,2)+':'+ "+
					"SUBSTRING(cast(run_time as varchar),5,2) "+
			"END "+
		"END "+
		"\"Run Time\", " +
		"case run_status "+
			"WHEN 1 THEN 'Succeeded' "+
			"WHEN 2 THEN 'Retry' "+
			"WHEN 0 THEN 'Failed' "+
		"END "+
		"\"Run Status\" "+
		", run_duration \"Run Duration\" " +
		", message " +
		"from msdb.dbo.sysjobhistory (nolock) where job_id = ? and " +
		"step_name = '(Job outcome)'" +
		"Order by run_date desc, run_time desc";
    }

	@Override
	public Object[] getSQLParameters() {
		return new Object[] {((JobsNode)getNode()).getID()};
	}

	@Override
	public String getStatusMessage() {
		return Messages.getString("mssql.dbdetail.tab.jobsHistoryFor") + " " + getNode().getName();
	}

}
