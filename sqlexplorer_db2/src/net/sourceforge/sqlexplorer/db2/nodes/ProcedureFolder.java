package net.sourceforge.sqlexplorer.db2.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

public class ProcedureFolder extends AbstractSQLFolderNode {

	public ProcedureFolder() {
		super(Messages.getString("db2.dbstructure.procedures"));
	}

	public String getChildType() {
		return "procedure";
	}

	public String getSQL() {
		String sql = "select rtrim(routinename) from syscat.routines a where routineschema = ? and routinetype='P';";
		return sql;
	}

	public Object[] getSQLParameters() {
		return new Object[] {getSchemaOrCatalogName()};
	}

}
