package net.sourceforge.sqlexplorer.mssql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;
import net.sourceforge.sqlexplorer.mssql.nodes.JobsNode;

public class JobTab extends AbstractSQLTab {

	public JobTab() {
	}

	@Override
	public String getLabelText() {
		return Messages.getString("mssql.dbdetail.tab.job");
	}

	@Override
	public String getSQL() {
        return "USE msdb " +
        "select a.name \"Name\" , " +
        "case when a.enabled=1 Then 'Yes' ELSE 'No' END \"Enabled\", " +
    	"case when b.enabled=1 THEN 'Yes' ELSE 'No' END \"Scheduled\", " +
    	"case when max( d.run_status ) = 4 THEN 'Running' ELSE 'Not Running' END \"Status\", " +
    	"case WHEN LEN( cast(e.last_run_date as varchar)) < 8 THEN 'Unknown ' " +
    	"ELSE "+
    		"SUBSTRING(cast( e.last_run_date as varchar),1,4)+'/'+ "+
    		"SUBSTRING(cast( e.last_run_date as varchar),5,2)+'/'+ "+
    		"SUBSTRING(cast( e.last_run_date as varchar),7,2)+' '+ " +

    		"case LEN(cast(e.last_run_time as varchar)) " +
    			"WHEN 5 THEN "+
					"'0'+SUBSTRING(cast(e.last_run_time as varchar),1,1)+':'+ "+
					"SUBSTRING(cast(e.last_run_time as varchar),2,2)+':'+ "+
					"SUBSTRING(cast(e.last_run_time as varchar),4,2) "+
				"WHEN 4 THEN " +
					"'00:'+"+
					"SUBSTRING(cast(e.last_run_time as varchar),1,2)+':' +"+
					"SUBSTRING(cast(e.last_run_time as varchar),3,2) " +
				"WHEN 3 THEN " +
					"'00:0'+" +
					"SUBSTRING(cast(e.last_run_time as varchar),1,1)+':' +"+
					"SUBSTRING(cast(e.last_run_time as varchar),2,2) " +
				"WHEN 2 THEN" +
					"'00:00:'+" +
					"SUBSTRING(cast(e.last_run_time as varchar),1,2) " +
				"WHEN 1 THEN"+
					"'00:00:0'+"+
					"SUBSTRING(cast(e.last_run_time as varchar),1,1) " +
				"WHEN 0 THEN" +
					"'00:00:00' "+
				"ELSE "+
					"SUBSTRING(cast(e.last_run_time as varchar),1,2)+':'+ "+
					"SUBSTRING(cast(e.last_run_time as varchar),3,2)+':'+ "+
					"SUBSTRING(cast(e.last_run_time as varchar),5,2) "+
			"END "+
		"END "+
		"\"Last Run\", "+
    	"case e.last_run_outcome when 1 THEN 'Succeded' " +
    	"  when 0 THEN 'Failed' " +
    	"  when 3 THEN 'Canceled' " +
    	"  ELSE 'Unknown' END \"Last Status\", " +
    	"case WHEN LEN( cast(b.next_run_date as varchar)) < 8 THEN 'Unknown ' " +
		"ELSE "+
			"SUBSTRING(cast( b.next_run_date as varchar),1,4)+'/'+ "+
			"SUBSTRING(cast( b.next_run_date as varchar),5,2)+'/'+ "+
			"SUBSTRING(cast( b.next_run_date as varchar),7,2)+' '+ " +

			"case LEN(cast(b.next_run_time as varchar)) " +
				"WHEN 5 THEN "+
					"'0'+SUBSTRING(cast(b.next_run_time as varchar),1,1)+':'+ "+
					"SUBSTRING(cast(b.next_run_time as varchar),2,2)+':'+ "+
					"SUBSTRING(cast(b.next_run_time as varchar),4,2) "+
				"WHEN 4 THEN " +
					"'00:'+"+
					"SUBSTRING(cast(b.next_run_time as varchar),1,2)+':' +"+
					"SUBSTRING(cast(b.next_run_time as varchar),3,2) " +
				"WHEN 3 THEN " +
					"'00:0'+" +
					"SUBSTRING(cast(b.next_run_time as varchar),1,1)+':' +"+
					"SUBSTRING(cast(b.next_run_time as varchar),2,2) " +
				"WHEN 2 THEN" +
					"'00:00:'+" +
					"SUBSTRING(cast(b.next_run_time as varchar),1,2) " +
				"WHEN 1 THEN"+
					"'00:00:0'+"+
					"SUBSTRING(cast(b.next_run_time as varchar),1,1) " +
				"WHEN 0 THEN" +
					"'00:00:00' "+
				"ELSE "+
					"SUBSTRING(cast(b.next_run_time as varchar),1,2)+':'+ "+
					"SUBSTRING(cast(b.next_run_time as varchar),3,2)+':'+ "+
					"SUBSTRING(cast(b.next_run_time as varchar),5,2) "+
			"END "+
		"END "+
		"\"Next Run\" "+
    	"from sysjobs a (nolock) " +
    	"left join msdb.dbo.sysjobschedules b (nolock) on a.job_id = b.job_id " +
    	"left join msdb.dbo.sysjobhistory d (nolock) on a.job_id = d.job_id " +
    	"left join msdb.dbo.sysjobservers e (nolock) on a.job_id = e.job_id " +
    	"WHERE a.job_id = ?"+

    	"group by a.name, a.enabled, b.enabled, e.last_run_date, e.last_run_time, e.last_run_outcome, b.next_run_date, b.next_run_time";
    }

	@Override
	public Object[] getSQLParameters() {
		return new Object[] {((JobsNode)getNode()).getID()};
	}

	@Override
	public String getStatusMessage() {
		return Messages.getString("mssql.dbdetail.tab.jobsFor") + " " + getNode().getName();
	}
}