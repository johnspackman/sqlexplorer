package net.sourceforge.sqlexplorer.postgresql.dataset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;

/**
 * Class providing {@link DataSet} support in a key-value fashion. It simply
 * reads one line of a database table and turns column headers into keys and
 * column values into values. This is highly useful for things like providing
 * property info on database objects from a single SQL <tt>SELECT</tt>
 * statement. In fact, the core should move to an interface for DataSet and
 * provide a default implementation (i.e. what DataSet is now) so that we could
 * simply implement the interface and avoid the factory implementation.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class PropertyDataSet {
	private static final ILogger logger = LoggerController
			.createLogger(PropertyDataSet.class);

	/**
	 * Create a multiline key-value DataSet from a single ResultSet row. Note
	 * that in the case the query returns multiple rows, only the first one is
	 * used to make up the DataSet.
	 * 
	 * @param connection
	 *            Connection to use.
	 * @param sql
	 *            The query to run.
	 * @param params
	 *            Additional query parameters.
	 * @return A DataSet.
	 * @throws Exception
	 *             in case something goes wrong.
	 */
	public static DataSet getPropertyDataSet(Session session,
			String sql, Object[] params) throws Exception {
		SQLConnection sqlConnection = null;
		String[][] data = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			sqlConnection = session.grabConnection();
			Connection c = sqlConnection.getConnection();
			pstmt = c.prepareStatement(sql);
			if (params != null)
				for (int i = 0; i < params.length; i++)
					pstmt.setObject(i + 1, params[i]);

			rs = pstmt.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			int count = meta.getColumnCount();
			data = new String[count][2];

			for (int i = 1; i <= count; i++)
				data[i - 1][0] = meta.getColumnLabel(i);

			if (rs.next())
				for (int i = 1; i <= count; i++)
					data[i - 1][1] = rs.getString(i);

			if (rs.next())
				logger.warn("Creating a PropertyDataSet from a result set with more than 1 row!");

			rs.close();
			pstmt.close();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (Exception e) {
				}
			if (sqlConnection != null)
				session.releaseConnection(sqlConnection);
		}

		return new DataSet(new String[] {
				Messages.getString("postgresql.hdr.property"),
				Messages.getString("postgresql.hdr.value") }, data);
	}
}
