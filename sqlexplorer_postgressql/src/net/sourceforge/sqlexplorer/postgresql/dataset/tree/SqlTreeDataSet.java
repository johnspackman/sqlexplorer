package net.sourceforge.sqlexplorer.postgresql.dataset.tree;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

/**
 * An {@link ITreeDataSet} implementation based on a SQL query. The caller
 * creating an instance of this class must supply a query and information which
 * columns (in which order) make up the tree's hierarchy. Additionally, all
 * non-hierarchy columns have to be specified to keep the use flexible instead
 * of taking all non-hierarchy columns implicitely as data columns.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class SqlTreeDataSet implements ITreeDataSet {

	private final TreeDataSetNode root;

	private final String treeColumnLabel;

	private final String[] dataColumnLabels;

	/**
	 * Create new tree data set.
	 * 
	 * @param connection
	 *            The connection to run the query against.
	 * @param sql
	 *            The query.
	 * @param treeColumns
	 *            The column numbers used to make up the hierarchy. The first
	 *            column has index 1.
	 * @param dataColumns
	 *            The column numbers used to make up a leaf/intermediate node's
	 *            data.
	 * @param treeColumnLabel
	 *            How to label the tree column. Other column labels are derived
	 *            from the metadata of the query's resultset.
	 * @throws Exception
	 *             In case something goes wrong.
	 */
	public SqlTreeDataSet(Session session, String sql,
			int[] treeColumns, int[] dataColumns, String treeColumnLabel)
			throws Exception {
		this.treeColumnLabel = treeColumnLabel;
		root = new TreeDataSetNode("root", null);
		Statement s = null;
		ResultSet rs = null;
		SQLConnection connection = null;
		try {
			connection = session.grabConnection();
			Connection c = connection.getConnection();
			s = c.createStatement();
			rs = s.executeQuery(sql);
			ResultSetMetaData meta = rs.getMetaData();
			dataColumnLabels = new String[dataColumns.length];
			for (int i = 0; i < dataColumns.length; i++)
				dataColumnLabels[i] = meta.getColumnLabel(dataColumns[i]);
			while (rs.next()) {
				Object[] props = new Object[dataColumns.length];
				List<Object> paths = new ArrayList<Object>();
				for (int i = 0; i < dataColumns.length; i++)
					props[i] = rs.getObject(dataColumns[i]);
				for (int i = 0; i < treeColumns.length; i++) {
					Object o = rs.getObject(treeColumns[i]);
					if (o != null)
						paths.add(o);
				}
				/* make sure name != null */
				Object name = paths.get(paths.size() - 1);
				for (int i = paths.size() - 2; i >= 0 && name == null; i--)
					name = paths.get(i);
				if (name == null)
					name = "<null>";
				TreeDataSetNode node = new TreeDataSetNode(name, props);
				root.insert(paths.toArray(new Object[paths.size()]), node);
			}
			rs.close();
			rs = null;
			s.close();
			s = null;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (s != null)
				try {
					s.close();
				} catch (Exception e) {
				}
			if (connection != null)
				session.releaseConnection(connection);
		}
	}

	public String[] getDataColumnLabels() {
		return dataColumnLabels;
	}

	public ITreeDataSetNode getRoot() {
		return root;
	}

	public String getTreeColumnLabel() {
		return treeColumnLabel;
	}

}
