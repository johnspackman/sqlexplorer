package net.sourceforge.sqlexplorer.mssql.nodes;

import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;
import net.sourceforge.sqlexplorer.Messages;

public class TriggerFolder extends AbstractSQLFolderNode {

	public TriggerFolder() {
		super(Messages.getString("mssql.dbstructure.triggers"));
	}

	@Override
	public String getChildType() {
		return "trigger";
	}

	@Override
	public String getSQL() {
		return "SELECT name FROM "+getSchemaOrCatalogName()+"..sysobjects so WHERE type='TR' AND category=0 ORDER BY name";
	}

	@Override
	public Object[] getSQLParameters() {
		return null;
	}

}
