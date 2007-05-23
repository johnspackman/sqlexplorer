package net.sourceforge.sqlexplorer.postgresql.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

/**
 * Static PosgreSQL utility methods.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class PgUtil {
	/**
	 * Test whether server is running at least a given version.
	 * 
	 * @param sessionTreeNode
	 *            Our session to get connection from.
	 * @param major
	 *            Minimum major version required.
	 * @param minor
	 *            Minimum minor version required.
	 * @return <tt>true</tt> if the server runs at least the given version,
	 *         <tt>false</tt> otherwise.
	 */
	public static boolean hasVersion(SessionTreeNode sessionTreeNode,
			int major, int minor) {
		Connection c = sessionTreeNode.getInteractiveConnection()
				.getConnection();
		try {
			DatabaseMetaData meta = c.getMetaData();
			if (meta.getDatabaseMajorVersion() > major)
				return true;
			else if (meta.getDatabaseMajorVersion() == major)
				return meta.getDatabaseMinorVersion() >= minor;
			return false;
		} catch (SQLException e) {
			SQLExplorerPlugin.error("Failed to determin postgres version", e);
		}
		return false;
	}
}
