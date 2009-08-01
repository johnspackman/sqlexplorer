/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.postgresql.dbproduct;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.sqlexplorer.dbproduct.AbstractDatabaseProduct;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.Message;

/**
 * Implementation for Oracle
 * @author John Spackman
 *
 */
public class PostgresDatabaseProduct extends AbstractDatabaseProduct {
	
	@Override
	public String describeConnection(Connection connection) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement("SELECT pg_backend_pid() as SID");
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
			stmt = connection.prepareStatement("SELECT current_database() as DB");
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

	public Collection<Message> getErrorMessages(SQLConnection connection, SQLException e, int lineNoOffset) throws SQLException {
		Collection<Message> list = new LinkedList<Message>();
		list.add(new Message(Message.Status.FAILURE, lineNoOffset + 1, 0, e.getMessage()));
		return list;
	}

	public Collection<Message> getServerMessages(SQLConnection connection) throws SQLException {
		return null;
	}

	public QueryParser getQueryParser(String sql, int initialLineNo) {
		return new PostgresQueryParser(sql, initialLineNo);
	}

	public Collection<Message> getErrorMessages(SQLConnection connection, Query query) throws SQLException {
		return null;
	}


}
