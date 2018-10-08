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

import net.sourceforge.sqlexplorer.parsers.AnnotatedQuery;
import net.sourceforge.sqlexplorer.util.BackedCharSequence;

/**
 * Returned by the OracleQueryParser.  Adds properties to the Query so that
 * we can detect DDL statements and extract the name of the object being created
 * and it's type.
 * 
 * @author John Spackman
 *
 */
public class OracleQuery extends AnnotatedQuery {
	
	private String createObjectName;
	private String createObjectType;

	public OracleQuery(BackedCharSequence buffer, int lineNo, QueryType queryType) {
		super(buffer, lineNo);
		setQueryType(queryType);
	}

	/**
	 * @return the createObjectName
	 */
	public String getCreateObjectName() {
		return createObjectName;
	}

	/**
	 * @param createObjectName the createObjectName to set
	 */
	public void setCreateObjectName(String createObjectName) {
		this.createObjectName = createObjectName;
	}

	/**
	 * @return the createObjectType
	 */
	public String getCreateObjectType() {
		return createObjectType;
	}

	/**
	 * @param createObjectType the createObjectType to set
	 */
	public void setCreateObjectType(String createObjectType) {
		this.createObjectType = createObjectType;
	}

}
