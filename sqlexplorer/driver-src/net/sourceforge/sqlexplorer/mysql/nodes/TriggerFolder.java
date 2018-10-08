package net.sourceforge.sqlexplorer.mysql.nodes;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.ObjectNode;

public class TriggerFolder extends AbstractSQLFolderNode {

    public TriggerFolder() {
    	super(Messages.getString("mysql.dbstructure.triggers"));
    }

    /**
     * All child nodes of this folder are procedures.
     */
    public String getChildType() {
        return "trigger";
    }

    @Override
	protected ObjectNode createChildNode(String name) {
		ObjectNode result = super.createChildNode(name);
		result.setQuoteChar('`');
		return result;
	}

    /**
     * Returns an sql statement that returns all procedure names.
     */
    public String getSQL() {
        return "select TRIGGER_NAME from INFORMATION_SCHEMA.TRIGGERS where TRIGGER_SCHEMA = ?";
    }

    /**
     * Return the parameters for our SQL Statement.
     */
    public Object[] getSQLParameters() {
        return new Object[] {getSchemaOrCatalogName()};
    }
}