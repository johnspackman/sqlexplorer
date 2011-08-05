package net.sourceforge.sqlexplorer.db2.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

/**
 * Database Structure Node for Sequences.
 * 
 * @author Davy Vanherbergen
 */
public class SequenceFolder extends AbstractSQLFolderNode {

	public SequenceFolder() {
		super(Messages.getString("db2.dbstructure.sequences"));
	}

	/**
	 * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode#getChildType()
	 */
	public String getChildType() {
		return "sequence";
	}

    /**
     * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode#getSQL()
     */
	public String getSQL() {
		String sql = "select rtrim(seqname) from syscat.sequences a where seqschema = ?";
		return sql;
	}

	/**
	 * @see net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode#getSQLParameters()
	 */
	public Object[] getSQLParameters() {
		return new Object[] {getSchemaOrCatalogName()};
	}

}
