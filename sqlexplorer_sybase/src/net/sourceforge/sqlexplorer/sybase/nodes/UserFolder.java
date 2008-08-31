package net.sourceforge.sqlexplorer.sybase.nodes;

import net.sourceforge.sqlexplorer.Messages;

public class UserFolder  extends SysObjectFolder {
	public UserFolder() {
		
	}

	public String getChildType() {
		return "user";
	}

	public Class<? extends SysObjectNode> getChildClass() {
		return UserNode.class;
	}

	public String getName() {
		return Messages.getString("sybase.dbstructure.users");
	}

	public String getSQL() {
		return " select u.name, u.uid, u.name, -1 from " + 
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

