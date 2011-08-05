package net.sourceforge.sqlexplorer.db2.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

/**
 * Database Structure Node for Procedures.
 * 
 * @modified Davy Vanherbergen
 */
public class ProcedureFolder extends AbstractSQLFolderNode {

	public ProcedureFolder() {
		super(Messages.getString("db2.dbstructure.procedures"));
	}

	/**
	 * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode#getChildType()
	 */
	public String getChildType() {
		return "procedure";
	}

    /**
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode#getSQL()
     */
	public String getSQL() {
		String sql = "select rtrim(routinename) from syscat.routines a where routineschema = ? and routinetype='P'";
		return sql;
	}

    /**
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode#getSQLParameters()
     */
	public Object[] getSQLParameters() {
		return new Object[] {getSchemaOrCatalogName()};
	}

}
