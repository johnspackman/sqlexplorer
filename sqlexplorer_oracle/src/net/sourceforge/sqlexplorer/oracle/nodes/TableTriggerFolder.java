package net.sourceforge.sqlexplorer.oracle.nodes;


import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;


public class TableTriggerFolder extends AbstractSQLFolderNode {

    public String getChildType() {
        return "TRIGGER";
    }
   
    public String getName() {
        return Messages.getString("oracle.dbstructure.triggers");
    }
    
    public String getSQL() {
        return "select trigger_name from sys.all_triggers where table_name = ? and table_owner = ?";
    }
    
    public Object[] getSQLParameters() {
        return new Object[] {getParent().getName(), getSchemaOrCatalogName()};
    }

    public String getType() {
        return "TRIGGER_FOLDER";
    }
    
    
}
