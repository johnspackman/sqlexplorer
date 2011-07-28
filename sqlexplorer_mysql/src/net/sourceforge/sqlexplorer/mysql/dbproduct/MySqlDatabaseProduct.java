/**
 * 
 */
package net.sourceforge.sqlexplorer.mysql.dbproduct;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sourceforge.sqlexplorer.dbproduct.DefaultDatabaseProduct;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

/**
 * @author Heiko
 *
 */
public class MySqlDatabaseProduct extends DefaultDatabaseProduct {

	@Override
	public String describeConnection(Connection connection) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement("SELECT CONNECTION_ID() as SID");
			rs = stmt.executeQuery();
			rs.next();
			return "SID: " + rs.getString("SID");
		}catch (SQLException e) {
			SQLExplorerPlugin.error(e);
			return super.describeConnection(connection);
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch(SQLException e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch(SQLException e) {
				}
		}
	}

	@Override
	public String getCurrentCatalog(Connection connection) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement("SELECT DATABASE() as DB");
			rs = stmt.executeQuery();
			rs.next();
			return rs.getString("DB");
		}catch (SQLException e) {
			SQLExplorerPlugin.error(e);
			return super.describeConnection(connection);
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch(SQLException e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch(SQLException e) {
				}
		}
	}

	/**
	 * set catalog in given connection
	 * 
	 * @param connection the SQLConnection to the database
	 * @param catalogName name of catalog to set
	 * @return
	 * @throws SQLException 
	 */
	@Override
	public void setCurrentCatalog(SQLConnection connection, String catalogName) throws SQLException
	{
		super.setCurrentCatalog(connection, catalogName);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement("use " + catalogName);
			rs = stmt.executeQuery();
		}catch (SQLException e) {
			SQLExplorerPlugin.error(e);
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch(SQLException e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch(SQLException e) {
				}
		}
	}
	
}
