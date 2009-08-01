/**
 * 
 */
package net.sourceforge.sqlexplorer.mssql.dbproduct;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sourceforge.sqlexplorer.dbproduct.DefaultDatabaseProduct;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

/**
 * @author Heiko
 *
 */
public class MsSqlDatabaseProduct extends DefaultDatabaseProduct {

	@Override
	public String describeConnection(Connection connection) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement("SELECT @@SPID as SID");
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
			stmt = connection.prepareStatement("SELECT db_name() as DB");
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

}
