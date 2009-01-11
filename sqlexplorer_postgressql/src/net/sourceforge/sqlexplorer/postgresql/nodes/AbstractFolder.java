package net.sourceforge.sqlexplorer.postgresql.nodes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;

/**
 * Base class for folders. This is a crude hack to give subclasses the
 * functionality to run additional queries to make up SQL templates.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public abstract class AbstractFolder extends AbstractSQLFolderNode implements
		InfoNode, RequiredByNode, RequiresNode {

	private static final ILogger logger = LoggerController
			.createLogger(AbstractFolder.class);

	public AbstractFolder(String name) {
		super(name);
	}

	/**
	 * Obtain a query's first columns as comma separated list. This is useful
	 * to, for example, get a list of login roles given a query joining on login
	 * groups or vice versa, etc.
	 * 
	 * @param sql
	 *            The query to run.
	 * @return A comma separated list containing all row's first column.
	 */
	protected String getList(String sql) {
		String ret = "";

		Statement stmt = null;
		ResultSet rs = null;
		SQLConnection connection = null;
		try {
			connection = _session.grabConnection();
			stmt = connection.createStatement();
			logger.debug("Running [" + sql + "]");
			rs = stmt.executeQuery(sql);
			int i = 0;
			while (rs.next())
				ret += rs.getString(1) + (i++ == 0 ? "" : ",");
			rs.close();
			rs = null;
			stmt.close();
			stmt = null;
			logger.debug("List turns out as [" + ret + "]");
		} catch (Exception e) {
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					SQLExplorerPlugin.error("Cannot close result set", e);
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					SQLExplorerPlugin.error("Cannot close result set", e);
				}
			if (connection != null)
				_session.releaseConnection(connection);
		}

		return ret;
	}

}
