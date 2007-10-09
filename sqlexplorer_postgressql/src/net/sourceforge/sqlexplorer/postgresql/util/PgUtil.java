package net.sourceforge.sqlexplorer.postgresql.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

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
	 * @param session
	 *            Our session to get connection from.
	 * @param major
	 *            Minimum major version required.
	 * @param minor
	 *            Minimum minor version required.
	 * @return <tt>true</tt> if the server runs at least the given version,
	 *         <tt>false</tt> otherwise.
	 */
	public static boolean hasVersion(Session session,
			int major, int minor) {
		SQLConnection sqlConnection = null;
		try {
			sqlConnection = session.grabConnection();
			Connection c = sqlConnection.getConnection();
			DatabaseMetaData meta = c.getMetaData();
			if (meta.getDatabaseMajorVersion() > major)
				return true;
			else if (meta.getDatabaseMajorVersion() == major)
				return meta.getDatabaseMinorVersion() >= minor;
			return false;
		} catch (SQLException e) {
			SQLExplorerPlugin.error(Messages.getString("postresql.version.error"), e);
		} finally {
			session.releaseConnection(sqlConnection);
		}
		return false;
	}
}
