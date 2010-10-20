package net.sourceforge.sqlexplorer.informix.tabs;

import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLSourceTab;

public class ProcedureTab extends AbstractSQLSourceTab {

	public ProcedureTab() {
	}

	@Override
	public String getLabelText() {
		return "Procedure Info";
	}

	@Override
	public String getSQL() {
		return "SELECT b.data FROM " + getNode().getParent().getParent().getName() + ":sysprocedures p, " + getNode().getParent().getParent().getName() + ":sysprocbody b WHERE p.procname=? AND p.procid=b.procid AND b.datakey='T' ORDER BY b.seqno ASC";
	}

	@Override
	public String getStatusMessage() {
		return "Procedure body for " + getNode().getName();
	}

	public Object[] getSQLParameters() {
		return new Object[] { getNode().getName()};
	}

}
