package net.sourceforge.sqlexplorer.informix.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;



public class ProcedureFolder extends AbstractSQLFolderNode {

	public ProcedureFolder() {
		super(Messages.getString("informix.dbstructure.procedures"));
	}

	public String getChildType() {
		return "procedure";
	}

	public String getSQL() {
		return "SELECT RTRIM(procname) FROM " + getParent().getName() + ":sysprocedures where mode=UPPER(mode) AND mode!='P' AND isproc='t' order by procname";
	}

	public Object[] getSQLParameters() {
		return null;
	}
}