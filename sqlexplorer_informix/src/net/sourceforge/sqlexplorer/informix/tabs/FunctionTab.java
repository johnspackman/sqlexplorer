package net.sourceforge.sqlexplorer.informix.tabs;

import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLSourceTab;

public class FunctionTab extends AbstractSQLSourceTab {

	public FunctionTab() {
		// TODO Auto-generated constructor stub
	}

	public String getLabelText() {
		return "Function Info";
	}

	@Override
	public String getSQL() {
		return "SELECT b.data FROM " + getNode().getParent().getParent().getName() + ":sysprocedures p, " + getNode().getParent().getParent().getName() + ":sysprocbody b WHERE p.procname=? AND p.procid=b.procid AND b.datakey='T' ORDER BY b.seqno ASC";
	}

	public String getStatusMessage() {
		return "Function body for " + getNode().getName();
	}

	@Override
	public Object[] getSQLParameters() {
		return new Object[] { getNode().getName()};
	}

}
