package net.sourceforge.sqlexplorer.dbase.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

public class ProcedureFolder extends AbstractSQLFolderNode {

	public ProcedureFolder() {
		super(Messages.getString("dbase.dbstructure.procedures"));
	}

	@Override
	public String getChildType() {
		return "procedure";
	}

	@Override
	public String getSQL() {
		return "SELECT so.Name, sc.text FROM "+
			getSchemaOrCatalogName() +"..sysobjects so, "+ 
			getSchemaOrCatalogName() +"..syscomments sc "+
			"WHERE so.type='P' AND sc.id=so.id";
	}

	@Override
	public Object[] getSQLParameters() {
		return null;
	}

}
