package net.sourceforge.sqlexplorer.db2.nodes;


import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;


public class TableTriggerFolder extends AbstractSQLFolderNode {

    public String getChildType() {
        return "TRIGGER";
    }
   
    public String getName() {
        return Messages.getString("db2.dbstructure.triggers");
    }
    
    public String getSQL() {
        return "SELECT DISTINCT A.TRIGNAME FROM SYSCAT.TRIGGERS A, SYSCAT.TRIGDEP B WHERE (B.BNAME=? AND B.BSCHEMA=? AND B.BTYPE='T' AND A.TRIGNAME=B.TRIGNAME AND A.TRIGSCHEMA=B.TRIGSCHEMA) FOR FETCH ONLY;";
    }
    
    public Object[] getSQLParameters() {
        return new Object[] {getParent().getName(), getSchemaOrCatalogName()};
    }

    public String getType() {
        return "TRIGGER_FOLDER";
    }
    
    
}
