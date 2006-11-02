package net.sourceforge.sqlexplorer.sybase.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

public class ProcessTab extends AbstractSQLTab {

	public ProcessTab() {
		// TODO Auto-generated constructor stub
	}

	public String getLabelText() {
		return Messages.getString("sybase.DatabaseDetailedView.Tab.Processes"); 
	}

	public String getStatusMessage() {
		return Messages.getString("sybase.DatabaseDetailedView.Tab.Processes.status") + " " + getNode().getSession().toString();
	}

	public String getSQL() {
		return "select  fid, " +
        " spid, " +
		" login = a.name, " +
		" cpu, " +
		" io = physical_io, " +
		" status = b.status, " +
		" command = cmd, " +
		" blockedBy = blocked, " +
		" 'database' = db_name(dbid), " +
		" hostname = b.hostname, " +
		" programname = b.program_name, " +
		" objectname = object_name(b.id,b.dbid) " +
		" from    master..syslogins a, master..sysprocesses b " +
		" where   a.suid = b.suid ";
		
		//Posso colocar ? como parametro
	}
	
	//public Object[] getSQLParameters() {
	//		retorna os parâmtros para os ? do SQL
	//}
}
