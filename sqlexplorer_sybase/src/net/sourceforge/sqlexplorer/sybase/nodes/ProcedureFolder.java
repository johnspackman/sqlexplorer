package net.sourceforge.sqlexplorer.sybase.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

public class ProcedureFolder extends AbstractSQLFolderNode {

	public ProcedureFolder() {
		// TODO Auto-generated constructor stub
	}

	public String getChildType() {
		return "procedure";
	}

	public String getName() {
		return Messages.getString("sybase.dbstructure.procedures");
	}

	public String getSQL() {
		return "select b.name + '.' + a.name from " + 
			getSchemaOrCatalogName() + "..sysobjects a, " + 
			getSchemaOrCatalogName() + "..sysusers b " +
			" where a.uid = b.uid and a.type = 'P' order by a.name";
	}

	public Object[] getSQLParameters() {
		return null;
	}
}
