package net.sourceforge.sqlexplorer.postgresql.nodes;

/**
 * Interface for nodes we can display additional detail info for.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public interface InfoNode {
	/**
	 * Get SQL query to obtain details. <strong>Note</strong> that it
	 * <em>must</em> contain two parameters:
	 * <ul>
	 * <li>Schema name</li>
	 * <li>Relation name</li>
	 * </ul>
	 * 
	 * @param params
	 *            Query parameters (ugly hack).
	 * 
	 * @return Paramterized SQL detail query.
	 */
	public String getDetailSQL(Object[] params);
}
