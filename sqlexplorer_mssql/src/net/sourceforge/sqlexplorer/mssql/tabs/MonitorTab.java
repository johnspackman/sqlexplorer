package net.sourceforge.sqlexplorer.mssql.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

public class MonitorTab extends AbstractSQLTab {

    public MonitorTab() {
    }

    @Override
    public String getLabelText() {
        return Messages.getString("mssql.DatabaseDetailView.Tab.Monitor");
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
            " CONVERT(varchar(24),last_batch,121) AS [Batch Time] " +
            " FROM master..sysprocesses sp WITH (nolock)" +
            " INNER JOIN master..sysdatabases sd WITH (nolock) ON sp.dbid = sd.dbid" +
            " WHERE ltrim(rtrim(sd.name)) = ? " +
            " ORDER BY 3 DESC,1";
    }

    @Override
    public Object[] getSQLParameters() {
        return new Object[] {getNode().getSchemaOrCatalogName()};
    }

    @Override
    public String getStatusMessage() {
        return Messages.getString("mssql.DatabaseDetailView.Tab.Monitor.status")
                + " " + getNode().getSession().toString();
    }

}
