package net.sourceforge.sqlexplorer.mssql.nodes;

import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;
import net.sourceforge.sqlexplorer.Messages;

public class FunctionFolder extends AbstractSQLFolderNode {

	public FunctionFolder() {
		super(Messages.getString("mssql.dbstructure.functions"));
	}

	@Override
	public String getChildType() {
		return "function";
	}

	@Override
	public String getSQL() {
		return "SELECT name FROM "+getSchemaOrCatalogName()+"..sysobjects so WHERE type='FN' AND category=0 ORDER BY name";
	}

	@Override
	public Object[] getSQLParameters() {
		return null;
	}

}
