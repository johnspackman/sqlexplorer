package net.sourceforge.sqlexplorer.mssql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;
import net.sourceforge.sqlexplorer.mssql.nodes.JobsNode;

public class JobSchedule extends AbstractSQLTab {

	public JobSchedule() {
	}

	@Override
	public String getLabelText() {
		return Messages.getString("mssql.dbdetail.tab.jobschedule");
	}

	public String getStatusMessage() {
		return Messages.getString("mssql.dbdetail.tab.jobscheduleFor") + " " + getNode().getName();
	}

	@Override
	public String getSQL() {
		return "exec msdb.dbo.sp_help_jobschedule @job_id = ?";
	}

	@Override
	public Object[] getSQLParameters() {
		return new Object[] { ((JobsNode)getNode()).getID() };
	}
}
