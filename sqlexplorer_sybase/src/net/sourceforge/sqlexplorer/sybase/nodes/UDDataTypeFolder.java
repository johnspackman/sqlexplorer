package net.sourceforge.sqlexplorer.sybase.nodes;

import net.sourceforge.sqlexplorer.Messages;

public class UDDataTypeFolder extends SysObjectFolder {

	public UDDataTypeFolder() {
	}

	public String getChildType() {
		return "userdatatype";
	}

	public Class<? extends SysObjectNode> getChildClass() {
		return UDDataTypeNode.class;
	}

	public String getName() {
		return Messages.getString("sybase.dbstructure.udd");
	}

	public String getSQL() {
		return "SELECT A.name, a.uid, B.name, a.id FROM " +  
		getSchemaOrCatalogName() + "..sysobjects A, " +  
		getSchemaOrCatalogName() + "..sysusers B, " +   
		getSchemaOrCatalogName() + "..sysprocedures P " +   
		" WHERE A.uid = B.uid AND A.id = P.id AND P.sequence = 0 AND P.status & 4096 != 4096 AND A.type = 'D'";
		
	}

	public Object[] getSQLParameters() {
		return null;
	}
	
}
