package net.sourceforge.sqlexplorer.sybase.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;

public class UserFolder extends AbstractSQLFolderNode {
	public UserFolder() {
		// TODO Auto-generated constructor stub
	}

	public String getChildType() {
		return "user";
	}

	public String getName() {
		return Messages.getString("sybase.dbstructure.users");
	}

	public String getSQL() {
		return " select Name = u.name, 'Group' = g.Name, Login = l.name from " + 
		getSchemaOrCatalogName() + "..sysusers u, " +  
		getSchemaOrCatalogName() + "..sysusers g, " + 
		" master..syslogins l " + 
		" where u.suid *= l.suid " + 
		" and u.gid *= g.uid " + 
		" and u.uid <= 16383 and u.uid != 0 "; 
	}

	public Object[] getSQLParameters() {
		return null;
	}

}

