/*
 * Created on 25-mar-2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package net.sourceforge.sqlexplorer.ext.oracle.utility;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

/**
 * @author mazzolini
 */
public class OracleUtil {
	public static String getDictionaryViewPrefix(SQLConnection conn)
			throws SQLException
		{
			String prefix = "all";
			String sql="SELECT COUNT(*) FROM   user_role_privs WHERE  granted_role = 'SELECT_CATALOG_ROLE'";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			if(rs.next())
			{
				if(rs.getInt(1) > 0)
					prefix = "dba";
			}
			rs.close();
			rs = null;
			stmt.close();
			stmt = null;
			return prefix;
		}
}
