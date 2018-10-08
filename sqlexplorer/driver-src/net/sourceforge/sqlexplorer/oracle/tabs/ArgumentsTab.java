/*
 * Copyright (C) 2007 Patrac Vlad Sebastian
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

/**
 * Generates query for information about a selected argument node 
 * 
 * @author Patras Vlad
 */

package net.sourceforge.sqlexplorer.oracle.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;
import net.sourceforge.sqlexplorer.oracle.nodes.ProcedureNode;

public class ArgumentsTab extends AbstractSQLTab {

	public String getLabelText() {
		return Messages.getString("oracle.dbstructure.argument");
	}

	public String getStatusMessage() {
		return "Argument information for " + getNode().getQualifiedName() + " of " + getNode().getParent().getQualifiedName();
	}

	public String getSQL() {
		
		String baseSql = 
			"SELECT NVL(A.ARGUMENT_NAME, '<alternate>') AS \"Name\" , NVL(CAST(A.POSITION AS VARCHAR(15)), 'Return Value') AS \"Position\", A.SEQUENCE AS \"Sequence\", " +
			"A.DATA_LEVEL AS \"Data level\", A.DATA_TYPE AS \"Data type\", NVL(CAST(A.DATA_LENGTH AS VARCHAR(3)), 'N/A') AS \"Data length\", " +
			"NVL(CAST(A.DATA_PRECISION AS VARCHAR(3)), 'N/A') AS \"Data Precision\", NVL(CAST(A.DATA_SCALE AS VARCHAr(3)), 'N/A') AS \"Data Scale\", " +
			"A.DEFAULT_VALUE AS \"Default value\", A.DEFAULT_LENGTH AS \"Default length\", " +
			"A.IN_OUT As \"In/Out\", NVL(CAST(A.RADIX AS VARCHAR(3)), 'N/A') AS \"Radix\", A.CHARACTER_SET_NAME AS \"CharacterSet Name\", " +
			"NVL(CAST(A.TYPE_NAME AS VARCHAR(3)), 'N/A') AS \"Type name\", NVL(CAST(A.TYPE_SUBNAME AS VARCHAR(3)), 'N/A') AS \"Type subname\", " +
			"NVL(CAST(A.TYPE_OWNER AS VARCHAR(3)), 'N/A') AS \"Type Owner\", NVL(CAST(A.TYPE_LINK AS VARCHAR(3)), 'N/A') AS \"Type link\", " +
			"A.PLS_TYPE As \"PL/SQL Type\", NVL(CAST(A.CHAR_LENGTH AS VARCHAR(3)), 'N/A') AS \"Char length\", A.CHAR_USED AS \"Char used\" " +
			"FROM sys.ALL_ARGUMENTS A WHERE OWNER = ? AND A.OBJECT_NAME = ? AND NVL(A.OVERLOAD, 0) = ?";
		
		return baseSql + ((((ProcedureNode)getNode()).getPackage() == null) ?
				" AND PACKAGE_NAME IS NULL" : " AND PACKAGE_NAME = ?") + " ORDER BY A.POSITION";
	}
	
	public Object[] getSQLParameters() {
		
		ProcedureNode procedure = (ProcedureNode)getNode();
		if (procedure.getPackage() == null) {
			
			return new Object[] { procedure.getSchemaOrCatalogName(), procedure.getQualifiedName(),
					 new Integer(procedure.getOverload()) };
		} else {
			return new Object[] { getNode().getSchemaOrCatalogName(), procedure.getQualifiedName(),
					 new Integer(procedure.getOverload()), procedure.getPackage() };
		}
	}

}
