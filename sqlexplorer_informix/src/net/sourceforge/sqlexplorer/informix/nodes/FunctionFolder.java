package net.sourceforge.sqlexplorer.informix.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;



public class FunctionFolder extends AbstractSQLFolderNode {

	public FunctionFolder() {
		super(Messages.getString("informix.dbstructure.functions"));
	}

	public String getChildType() {
		return "function";
	}

	public String getSQL() {
		return "SELECT RTRIM(procname) FROM " + getParent().getName() + ":sysprocedures where mode=UPPER(mode) AND mode!='P' AND isproc='f' order by procname";
	}

	public Object[] getSQLParameters() {
		return null;
	}
}