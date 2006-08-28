package net.sourceforge.sqlexplorer.mysql.nodes;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

public class ProcedureFolder extends AbstractSQLFolderNode {

    public ProcedureFolder() {
    }

    /**
     * All childnodes of this folder are procedures.
     */
    public String getChildType() {
        return "procedure";
    }

    /**
     * Label for the procedure folder node.
     */
    public String getName() {
        return Messages.getString("mysql.dbstructure.procedures");
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