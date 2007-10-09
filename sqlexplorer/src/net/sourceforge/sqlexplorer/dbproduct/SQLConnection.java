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
package net.sourceforge.sqlexplorer.dbproduct;

import java.sql.Connection;

import net.sourceforge.squirrel_sql.fw.sql.SQLDriverPropertyCollection;

/**
 * Our SQLConnection, which adds the connection to our User object
 * @author John Spackman
 *
 */
public class SQLConnection extends net.sourceforge.squirrel_sql.fw.sql.SQLConnection {
	
	// The User that established this connection
	private User user;
	
	// When the connection was established
	private long createdTime;

	public SQLConnection(User user, Connection connection, SQLDriverPropertyCollection properties) {
		super(connection, properties);
		this.user = user;
		createdTime = System.currentTimeMillis();
	}

	public User getUser() {
		return user;
	}
	
	/*package*/ void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * Returns true if this is a pooled connection
	 * @return
	 */
	public boolean isPooled() {
		return user.isInPool(this);
	}

	public long getCreatedTime() {
		return createdTime;
	}
}
