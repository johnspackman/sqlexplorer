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

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

/**
 * Represents a username and password combo used to connect to an alias; contains
 * a list of all connections made
 *  
 * @author John Spackman
 */
public class User implements Comparable<User> {
	
	/*package*/ static final String USER = "user";
	/*package*/ static final String USER_NAME = "user-name";
	/*package*/ static final String PASSWORD = "password";
	private static final String AUTO_COMMIT = "auto-commit";
	private static final String COMMIT_ON_CLOSE = "commit-on-close";
	
	// Maximum number of connections to keep in the pool
	public static final int MAX_POOL_SIZE = 3;
	
	// The Alias we belong to
	private Alias alias;

	// Username and password to login as
	private String userName;
	private String password;

	// Pool of available connections
	private LinkedList<SQLConnection> unused = new LinkedList<SQLConnection>();
	
	// List of connections in use
	private LinkedList<SQLConnection> allocated = new LinkedList<SQLConnection>();
	
	// List of Sessions
	private LinkedList<Session> sessions = new LinkedList<Session>();
	
	// Auto commit behaviour
	private boolean autoCommit;
	private boolean commitOnClose;
	
	/**
	 * Constructor
	 * @param userName
	 * @param password
	 */
	public User(String userName, String password) {
		super();
		this.userName = userName;
		this.password = password;
		
		// Get default autocommit behaviour
		autoCommit = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.AUTO_COMMIT);
		commitOnClose = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.COMMIT_ON_CLOSE);
	}
	
	/**
	 * Constructs a User, from a definition previously recorded by describeAsXml()
	 * @param root
	 */
	public User(Element root) {
		super();
		this.userName = root.elementText(USER_NAME);
		this.password = root.elementText(PASSWORD);
		autoCommit = getBoolean(root.attributeValue(AUTO_COMMIT), true);
		commitOnClose = getBoolean(root.attributeValue(COMMIT_ON_CLOSE), true);
	}
	
	/**
	 * Describes the User in XML
	 * @return
	 */
	public Element describeAsXml() {
		Element root = new DefaultElement(USER);
		root.addElement(USER_NAME).setText(userName);
		root.addElement(PASSWORD).setText(password);
		root.addAttribute(AUTO_COMMIT, Boolean.toString(autoCommit));
		root.addAttribute(COMMIT_ON_CLOSE, Boolean.toString(commitOnClose));
		return root;
	}

	/**
	 * Creates a duplicate of this User 
	 * @param alias
	 * @return
	 */
	public User createCopy() {
		User copy = new User(userName, password);
		return copy;
	}
	
	/**
	 * Merges the definition of the User "that" - IE takes the password, auto-commit
	 * behaviour, etc
	 * @param that
	 */
	public void mergeWith(User that) {
		password = that.getPassword();
		autoCommit = that.isAutoCommit();
		commitOnClose = that.isCommitOnClose();
		for (SQLConnection connection : that.unused) {
			connection.setUser(this);
			if (unused.size() < MAX_POOL_SIZE) 
				unused.add(connection);
			else
				try {
					connection.close();
				}catch(SQLException e) {
					SQLExplorerPlugin.error("Cannot close connection", e);
				}
		}
		for (SQLConnection connection : that.allocated) {
			connection.setUser(this);
			allocated.add(connection);
		}
		for (Session session : that.sessions) {
			session.setUser(this);
			sessions.add(session);
		}
		
		// Make "that" unusable
		that.unused = null;
		that.allocated = null;
		that.sessions = null;
		that.password = null;
		
		SQLExplorerPlugin.getDefault().getAliasManager().modelChanged();
	}
	
	/**
	 * Creates a new session
	 * @return
	 */
	public Session createSession() throws SQLException {
		Session session = new Session(this);
		sessions.add(session);
		SQLExplorerPlugin.getDefault().getAliasManager().modelChanged();
		return session;
	}
	
	/**
	 * Releases a session
	 * @param session
	 */
	/*package*/ void releaseSession(Session session) {
		SQLExplorerPlugin.getDefault().getAliasManager().modelChanged();
		sessions.remove(session);
	}
	
	/**
	 * Returns a list of sessions
	 * @return
	 */
	public List<Session> getSessions() {
		return sessions;
	}
	
	/**
	 * Returns true if the session belongs to this User
	 * @param session
	 * @return
	 */
	public boolean contains(Session session) {
		return sessions.contains(session);
	}
	
	/**
	 * Closes all connections; note that ConnectionListeners are NOT invoked
	 */
	/*package*/ void closeAllSessions() {
		for (Session session : sessions)
			session.close();
	}
	
	/**
	 * Retrieves a new connection, either from the pool or by allocating a new one
	 * @return
	 * @throws ExplorerException
	 */
	public SQLConnection getConnection() throws SQLException {
		SQLConnection connection;
		if (!unused.isEmpty())
			connection = unused.removeFirst();
		else {
			connection = createNewConnection();
			SQLExplorerPlugin.getDefault().getAliasManager().modelChanged();
		}
		allocated.add(connection);
		return connection;
	}
	
	/**
	 * Releases a connection; the connection will be returned to the pool, unless
	 * the pool has grown too large (in which case the connection is closed).
	 * @param connection
	 * @throws ExplorerException
	 */
	public void releaseConnection(SQLConnection connection) throws SQLException {
		boolean forPool = allocated.remove(connection);
        boolean commitOnClose = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.COMMIT_ON_CLOSE);
        
        if (commitOnClose)
        	connection.commit();
        else
        	connection.rollback();
	
		// Keep the pool small
		if (forPool && unused.size() < MAX_POOL_SIZE) { 
			unused.add(connection);
			return;
		}
	
		// Close unwanted connections
		connection.close();
	}
	
	/**
	 * Returns the connection from the pool, assuming the connection is currently in
	 * the pool
	 * @param connection
	 * @return true if the connection was in the and has been removed
	 */
	public synchronized boolean releaseFromPool(SQLConnection connection) {
		if (unused.remove(connection)) {
			SQLExplorerPlugin.getDefault().getAliasManager().modelChanged();
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the connection is part of the pool of available connections
	 * @param connection
	 * @return
	 */
	public boolean isInPool(SQLConnection connection) {
		return unused.contains(connection);
	}
	
	/**
	 * Returns true if the User is in use (ie has any connections in use or active sessions)
	 * @return
	 */
	public boolean isInUse() {
		return !allocated.isEmpty() || !sessions.isEmpty();
	}
	
	/**
	 * Returns all connections
	 * @return
	 */
	public List<SQLConnection> getConnections() {
		LinkedList<SQLConnection> result = new LinkedList<SQLConnection>();
		result.addAll(allocated);
		result.addAll(unused);
		return result;
	}
	
	/**
	 * Returns true if the user has successfully authenticated at some point; IE, 
	 * will grabConnection() be able to return a valid connection, either from the
	 * pool or by establishing a new connection, without normally causing an 
	 * authentication failure 
	 * @return
	 */
	public boolean hasAuthenticated() {
		return allocated.size() + unused.size() > 0;
	}
	
	/**
	 * Creates a new connection
	 * @return
	 * @throws ExplorerException
	 * @throws SQLException
	 */
	protected SQLConnection createNewConnection() throws SQLException {
        SQLConnection connection = alias.getDriver().getConnection(this);
		return connection;
	}

	/**
	 * Returns the Alias for this User
	 * @return
	 */
	public Alias getAlias() {
		return alias;
	}
	
	/**
	 * Changes the alias for the User
	 * @param alias
	 */
	/*package*/ void setAlias(Alias alias) {
		if (this.alias != null && alias != null) {
			if (this.alias != alias)
				throw new IllegalArgumentException("Cannot change a User's Alias");
			return;
		}
		this.alias = alias;
	}

	public String getPassword() {
		return password;
	}

	public String getUserName() {
		return userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDescription() {
		return getAlias().getName() + '/' + getUserName();
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public boolean isCommitOnClose() {
		return commitOnClose;
	}

	public void setCommitOnClose(boolean commitOnClose) {
		this.commitOnClose = commitOnClose;
	}

	public int compareTo(User that) {
		return userName.compareToIgnoreCase(that.getUserName());
	}
	
	private boolean getBoolean(String value, boolean defaultValue) {
		try {
			return Boolean.parseBoolean(value);
		}catch(Exception e) {
			return defaultValue;
		}
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
