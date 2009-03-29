package net.sourceforge.sqlexplorer.derby.nodes;

import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

public class SchemaTriggerFolder extends AbstractSQLFolderNode {

	public SchemaTriggerFolder() {
		super("Triggers");
	}
	
	public String getType() {
		return "SCHEMA_FOLDER";
	}


	public String getChildType() {
		return "trigger";
	}


	public String getSQL() {
		return "SELECT TRIGGERNAME FROM SYS.SYSTRIGGERS T, SYS.SYSSCHEMAS S " +
				"WHERE S.SCHEMANAME = ? AND S.SCHEMAID = T.SCHEMAID";
	}


	public Object[] getSQLParameters() {
		return new Object[] {getSchemaOrCatalogName()}; 
	}

}
