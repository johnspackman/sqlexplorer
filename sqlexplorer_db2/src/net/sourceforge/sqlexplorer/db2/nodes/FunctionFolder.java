package net.sourceforge.sqlexplorer.db2.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

public class FunctionFolder extends AbstractSQLFolderNode {

	public FunctionFolder() {
		super(Messages.getString("db2.dbstructure.functions"));
	}

	public String getChildType() {
		return "function";
	}

	public String getSQL() {
		String sql = "select rtrim(routinename) from syscat.routines a where routineschema = ? and routinetype='F';";
		return sql;
	}

	public Object[] getSQLParameters() {
		return new Object[] {getSchemaOrCatalogName()};
	}
}
