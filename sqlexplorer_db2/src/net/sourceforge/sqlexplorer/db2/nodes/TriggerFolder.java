package net.sourceforge.sqlexplorer.db2.nodes;


import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;


public class TriggerFolder extends AbstractSQLFolderNode {

    public TriggerFolder() {
		super(Messages.getString("db2.dbstructure.triggers"));
	}

	public String getChildType() {
        return "TRIGGER";
    }
    
    public String getSQL() {
        return "SELECT DISTINCT A.TRIGNAME FROM SYSCAT.TRIGGERS A, SYSCAT.TRIGDEP B WHERE (B.BSCHEMA=? AND B.BTYPE='T' AND A.TRIGNAME=B.TRIGNAME AND A.TRIGSCHEMA=B.TRIGSCHEMA) FOR FETCH ONLY;";
    }
    
    public Object[] getSQLParameters() {
        return new Object[] {getSchemaOrCatalogName()};
    }
}
