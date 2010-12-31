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
package net.sourceforge.sqlexplorer.oracle.dbproduct;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbproduct.AbstractDatabaseProduct;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.parsers.NamedParameter;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.Message;
import oracle.jdbc.driver.OracleTypes;

/**
 * Implementation for Oracle
 * @author John Spackman
 *
 */
public class OracleDatabaseProduct extends AbstractDatabaseProduct {
	
	private LinkedList<String> warnings;

	
	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.dbproduct.AbstractDatabaseProduct#describeConnection(java.sql.Connection)
	 */
	@Override
	public String describeConnection(Connection connection) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement("SELECT SID, SERIAL#, AUDSID FROM V$SESSION WHERE AUDSID = TO_NUMBER(USERENV('SESSIONID'))");
			rs = stmt.executeQuery();
			rs.next();
			return rs.getString("SID") + "," + rs.getString("SERIAL#");
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

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.dbproduct.DatabaseProduct#createDataSet(java.sql.ResultSet)
	 */
	public DataSet createDataSet(ResultSet resultSet) throws SQLException {
		return new OracleDataSet(resultSet, null);
	}
	
	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.dbproduct.DatabaseProduct#getTokenizer()
	 */
	public QueryParser getQueryParser(String sql, int initialLineNo) {
		return new OracleQueryParser(sql, initialLineNo);
	}

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.dbproduct.DatabaseProduct#getServerMessages(net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode)
	 */
	public Collection<Message> getServerMessages(SQLConnection connection) throws SQLException {
		LinkedList<Message> messages = new LinkedList<Message>();
		CallableStatement stmt = null;
		
		try {
			stmt = connection.getConnection().prepareCall("begin dbms_output.enable; dbms_output.get_line(:line, :status); end;");
			stmt.registerOutParameter(1, Types.VARCHAR);
			stmt.registerOutParameter(2, Types.INTEGER);
			while (true) {
				stmt.execute();
				int status = stmt.getInt(2);
				if (status != 0)
					break;
				String msg = stmt.getString(1);
				messages.add(new Message(Message.Status.STATUS, msg));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch(SQLException e) {
				}
		}
		
		if (warnings != null) {
			for (String msg : warnings)
				messages.add(new Message(Message.Status.STATUS, msg));
			warnings = null;
		}
		
		return messages;
	}


	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.dbproduct.DatabaseProduct#getErrorMessages(net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode)
	 */
	public Collection<Message> getErrorMessages(SQLConnection connection, SQLException e, int lineNoOffset) throws SQLException {
		LinkedList<Message> messages = new LinkedList<Message>();
		String msg = e.getMessage();
		
		/*
		 * Messages returned by the server are always errors and they are passed
		 * back to us in SQLException instances.  Where more than one message was
		 * returned (EG because of multiple errors found when compiling a PL/SQL
		 * procedure), they are concatenated into the exception message.
		 * 
		 * All Oracle errors are prefixed with an error code in the form AAA-99999.
		 */
		
		// Parse the message looking for AAA-99999 immediately after \n or at the
		//	begining of the message.
		int start = -1;
		for (int i = 0; i < msg.length(); i++) {
			char c = msg.charAt(i);
			if (i == 0 || c == '\n') {
				boolean match = isOracleError(msg, (i == 0) ? 0 : (i + 1));
				
				// If we've got a match but it's a PLS-99999 error code then it's
				//	a PL/SQL error code nested inside an ORA-99999 code; the ORA-99999
				//	code has the line and column number, the PLS-99999 the error code.
				// So, do not count nested PLS-99999 codes as a match
				if (match && start > -1 && msg.substring(i + 1, i + 4).equals("PLS") && msg.substring(start, start + 3).equals("ORA"))
					match = false;
				
				// Found a match
				if (match) {
					// If the first match is AFTER the start of the line then we've got 
					//	some unexpected prefix.
					if (start == -1 && i > 0) {
						messages.add(new Message(Message.Status.FAILURE, msg.substring(0, i)));
					} else if (i > 0) {
						if (start == -1)
							start = 0;
						messages.add(handleErrorText(msg.substring(start, i), lineNoOffset));
					}
					start = i;
				}
			}
		}

		// The last message; if start is -1, then we didn't find an Oracle error code
		//	so just add it as text
		if (start == -1) {
			if (msg.length() > 0)
				messages.add(new Message(Message.Status.FAILURE, lineNoOffset, 1, msg));
			start = msg.length();
		}
		
		// The last message is an Oracle error
		if (start < msg.length())
			messages.add(handleErrorText(msg.substring(start), lineNoOffset));
		
		return messages;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.dbproduct.DatabaseProduct#getErrorMessages(net.sourceforge.squirrel_sql.fw.sql.SQLConnection, net.sourceforge.sqlexplorer.parsers.Query)
	 */
	public Collection<Message> getErrorMessages(SQLConnection connection, Query _query) throws SQLException {
		OracleQuery query = (OracleQuery)_query;
		if (query.getCreateObjectType() == null || query.getCreateObjectName() == null)
			return null;
		
		LinkedList<Message> messages = new LinkedList<Message>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = connection.getConnection().prepareStatement("select * from all_errors where type = ? and name = ? order by sequence");
			stmt.setString(1, query.getCreateObjectType());
			stmt.setString(2, query.getCreateObjectName());
			rs = stmt.executeQuery();
			while (rs.next()) {
				String msg = rs.getString("TEXT");
				int lineNo = rs.getInt("LINE") + query.getLineNo() - 1;
				int charNo = rs.getInt("POSITION");
				messages.add(new Message(Message.Status.FAILURE, lineNo, charNo, msg));
			}
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
		
		if (warnings != null) {
			for (String msg : warnings)
				messages.add(new Message(Message.Status.SUCCESS, msg));
			warnings = null;
		}
		return messages;
	}

	/**
	 * Logs a warning that will appear in the results of getServerMessages(); for use internally
	 * by the Oracle module only
	 * @param chars
	 */
	public void logWarning(String chars) {
		if (warnings == null)
			warnings = new LinkedList<String>();
		warnings.add(chars);
	}
	
	/**
	 * Checks whether there is an Oracle style error (uppercase AAA-99999)
	 * starting at startIndex into msg
	 * @param msg
	 * @param startIndex
	 * @return
	 */
	private boolean isOracleError(String msg, int startIndex) {
		// Shortest Oracle error code is AAA-99999, i.e. 9 characters
		if (msg.length() - startIndex < 9)
			return false;
		
		boolean match = true;
		
		// Find out if the next three characters are uppercase letters
		for (int j = 0; match && j < 3; j++)
			if (!Character.isUpperCase(msg.charAt(startIndex + j)))
				match = false;
		startIndex += 3;
		
		// ...followed by a hyphen
		if (msg.charAt(startIndex) != '-')
			match = false;
		startIndex++;
		
		// Followed by 5 numbers
		for (int j = 0; match && j < 5; j++)
			if (!Character.isDigit(msg.charAt(startIndex + j)))
				match = false;
		
		return match;
	}
	
	/**
	 * Called internally to handle an Oracle error message; it parses the text
	 * looking for line and column information, and returns a Message object.
	 * Note that errorText should have already been split up into 
	 * @param text
	 * @param lineNoOffset the line number to offset error message line numbers by
	 */
	private Message handleErrorText(String text, int lineNoOffset) {
		int lineNo = 1;
		int charNo = 0;
		
		// Message with line & column is:
		//		AAA-99999: line 1, column 1
//		Pattern pattern = Pattern.compile("[A-Z][A-Z][A-Z]\\-\\d\\d\\d\\d\\d\\: line (\\d++), column (\\d++)");
		Pattern pattern = Pattern.compile("[A-Z][A-Z][A-Z]\\-\\d\\d\\d\\d\\d\\: line (\\d++)(, column (\\d++))?+");
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			int end = -1;
			if (matcher.groupCount() > 0) {
				lineNo = Integer.parseInt(matcher.group(1));
				end = matcher.end(1);
			}
			if (matcher.groupCount() > 2) {
				charNo = Integer.parseInt(matcher.group(3));
				end = matcher.end(3);
			}
			if (end > -1)
				text = text.substring(end + 1).trim();
		}
		
		return new Message(Message.Status.FAILURE, lineNo + lineNoOffset, charNo, text);
	}

	@Override
	public void configureStatement(CallableStatement stmt, NamedParameter param, int columnIndex) throws SQLException {
		if (param.getDataType() == NamedParameter.DataType.CURSOR) {
			stmt.registerOutParameter(columnIndex, OracleTypes.CURSOR);
		} else
			super.configureStatement(stmt, param, columnIndex);
	}

	@Override
	public ResultSet getResultSet(CallableStatement stmt, NamedParameter param, int columnIndex) throws SQLException {
		if (param.getDataType() == NamedParameter.DataType.CURSOR)
			return (ResultSet)stmt.getObject(columnIndex);

		return super.getResultSet(stmt, param, columnIndex);
	}

}
