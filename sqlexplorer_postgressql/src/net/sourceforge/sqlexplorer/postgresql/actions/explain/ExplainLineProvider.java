package net.sourceforge.sqlexplorer.postgresql.actions.explain;

/**
 * Interface for classes prodiving input lines in PostgreSQL explain format. The
 * parser is implemented using an interface to test the parser with different
 * input sources (e.g. JDBC connection, flat file).
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public interface ExplainLineProvider {
	/**
	 * Return the next line of input.
	 * 
	 * @return Line, or <tt>null</tt> on end-of-file.
	 * @throws Exception
	 *             in case something goes wrong.
	 */
	public String getLine() throws Exception;
}
