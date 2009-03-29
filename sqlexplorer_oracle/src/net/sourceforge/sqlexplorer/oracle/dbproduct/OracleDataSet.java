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

import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.jdbc.driver.OracleResultSet;
import oracle.jdbc.OracleTypes;
import oracle.sql.OPAQUE;
import net.sourceforge.sqlexplorer.dataset.DataSet;

/**
 * Oracle-specific DataSet
 * 
 * @author John Spackman
 */
public class OracleDataSet extends DataSet {

	public OracleDataSet(ResultSet resultSet, int[] relevantIndeces) throws SQLException {
		super(resultSet, relevantIndeces);
	}

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.dataset.DataSet#loadCellValue(int, int, java.sql.ResultSet)
	 */
	@Override
	protected Comparable<?> loadCellValue(int columnIndex, int dataType, ResultSet rs) throws SQLException {
		if (dataType == OracleTypes.OPAQUE) {
			OPAQUE opaque = ((OracleResultSet)rs).getOPAQUE(columnIndex);
			if (rs.wasNull())
				return null;
			
			// Check for XML
			String typeName = opaque.getSQLTypeName();
			if (typeName.equalsIgnoreCase("SYS.XMLTYPE"))
				return new OracleXmlDataType(opaque);
			
			// Just an OPAQUE
			return new OracleOpaqueDataType(opaque);
		}
		return super.loadCellValue(columnIndex, dataType, rs);
	}

	
}
