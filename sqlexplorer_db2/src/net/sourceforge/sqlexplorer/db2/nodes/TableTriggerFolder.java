package net.sourceforge.sqlexplorer.db2.nodes;


import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

/**
 * Database Structure Node for Triggers (per table).
 * 
 * @modified Davy Vanherbergen
 */
public class TableTriggerFolder extends AbstractSQLFolderNode {

    public TableTriggerFolder() {
		super(Messages.getString("db2.dbstructure.triggers"));
	}

	/**
	 * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode#getChildType()
	 */
	public String getChildType() {
        return "TRIGGER";
    }
   
    /**
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode#getSQL()
     */
    public String getSQL() {
        return "SELECT DISTINCT A.TRIGNAME FROM SYSCAT.TRIGGERS A, SYSCAT.TRIGDEP B WHERE (B.BNAME=? AND B.BSCHEMA=? AND B.BTYPE='T' AND A.TRIGNAME=B.TRIGNAME AND A.TRIGSCHEMA=B.TRIGSCHEMA) FOR FETCH ONLY";
    }
    
    /**
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode#getSQLParameters()
     */
    public Object[] getSQLParameters() {
        return new Object[] {getParent().getName(), getParent().getSchemaOrCatalogName()};
    }

	/**
	 * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode#getType()
	 */
	@Override
	public String getType() {
		return "TRIGGER_FOLDER";
	}  
    
    
}
