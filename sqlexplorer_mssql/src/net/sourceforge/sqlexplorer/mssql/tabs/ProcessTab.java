package net.sourceforge.sqlexplorer.mssql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

public class ProcessTab extends AbstractSQLTab {

    public ProcessTab() {
    }

    @Override
    public String getLabelText() {
        return Messages.getString("mssql.DatabaseDetailView.Tab.Processes");
    }

    @Override
    public String getSQL() {
        return "select ltrim(rtrim(loginame)) as [User],spid as [Process ID]," +
            " blocked as [Blocked],ltrim(rtrim(sd.name)) as [DataBase]," +
            " ltrim(rtrim(sp.status)) as [Status],cpu as [CPU]," +
            " physical_io as [Physical IO]," +
            " memusage as [Memory Usage],ltrim(rtrim(hostname)) as [Host]," +
            " ltrim(rtrim(program_name)) as [Application]," +
            " CONVERT(varchar(24),login_time,121) AS [Login Time]," +
            " CONVERT(varchar(24),last_batch,121) AS [Login Time] " +
            " FROM master..sysprocesses sp WITH (nolock)" +
            " INNER JOIN master..sysdatabases sd WITH (nolock) ON sp.dbid = sd.dbid" +
            " ORDER BY 3 DESC,1";
    }

    @Override
    public String getStatusMessage() {
        return Messages.getString("mssql.DatabaseDetailView.Tab.Processes.status")
                + " " + getNode().getSession().toString();
    }

}
