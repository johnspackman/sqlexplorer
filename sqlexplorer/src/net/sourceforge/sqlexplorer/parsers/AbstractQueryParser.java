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
package net.sourceforge.sqlexplorer.parsers;

import java.util.LinkedHashSet;

public abstract class AbstractQueryParser implements QueryParser {
	
	// List of named parameters; note that there can be duplicates, but that
	//	they are scoped according to their position.  The list is expected to
	//	be in the order in which they appeared in the original query
	private LinkedHashSet<NamedParameter> parameters = new LinkedHashSet<NamedParameter>();
	private ExecutionContext context = new ExecutionContext();

	public void addParameter(NamedParameter parameter) {
		parameters.add(parameter);
	}

	public LinkedHashSet<NamedParameter> getParameters() {
		return parameters;
	}
	
	public ExecutionContext getContext()
	{
		return this.context;
	}
}
