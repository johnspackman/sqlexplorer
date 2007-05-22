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
 * Generates query for information about a selected procedure node 
 * 
 * @author Patras Vlad
 */

package net.sourceforge.sqlexplorer.oracle.tabs;

import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLTab;

public class ProcedureTab extends AbstractSQLTab {

	public ProcedureTab() {
	}

	public String getLabelText() {
		return "Procedure Info";
	}

	public String getStatusMessage() {
		return "Procedure Information for " + getNode().getQualifiedName();
	}

	public String getSQL() {
		return "SELECT A.AGGREGATE AS \"Aggregate\", A.PIPELINED AS \"Pipelined\", NVL(A.IMPLTYPEOWNER, 'N/A') AS \"Implementation type owner\", " +
			   "NVL(A.IMPLTYPENAME, 'N/A') AS \"Implementation type name\", A.PARALLEL AS \"Parallel\", A.INTERFACE AS \"Interface\", " +
			   "A.DETERMINISTIC AS \"Deterministic\", A.AUTHID AS \"Author ID\" " +
			   "FROM ALL_PROCEDURES A WHERE A.PROCEDURE_NAME = ? OR (A.PROCEDURE_NAME IS NULL AND A.OBJECT_NAME = ?)";
	}
	
	public Object[] getSQLParameters() {
		return new Object[] { getNode().getQualifiedName(), getNode().getQualifiedName() };
	}

}
