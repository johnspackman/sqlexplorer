package net.sourceforge.sqlexplorer.mysql.nodes;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.ObjectNode;

public class ProcedureFolder extends AbstractSQLFolderNode {

    public ProcedureFolder() {
    	super(Messages.getString("mysql.dbstructure.procedures"));
    }

    /**
     * All childnodes of this folder are procedures.
     */
    public String getChildType() {
        return "procedure";
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
        return "select name from mysql.proc where type='PROCEDURE' and db = ?";
    }

    /**
     * Return the parameters for our SQL Statement.
     */
    public Object[] getSQLParameters() {
        return new Object[] {getSchemaOrCatalogName()};
    }
}