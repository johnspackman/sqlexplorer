package net.sourceforge.sqlexplorer.sessiontree.model;



/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.ListenerList;

import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

public class SessionTreeModel implements ISessionTreeNode{
	RootSessionTreeNode root=new RootSessionTreeNode();
	private ListenerList listeners = new ListenerList();
	public SessionTreeModel(){
		
	}

	/**
	 * @return
	 */
	public RootSessionTreeNode getRoot() {
		
		return root;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.sessiontree.model.ISessionTreeNode#getChildren()
	 */
	public Object[] getChildren() {
		return new Object[]{root};
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.sessiontree.model.ISessionTreeNode#getParent()
	 */
	public Object getParent() {
		return root.getParent();
	}

	/**
	 * @param conn
	 * @param alias
	 */
	public void createSessionTreeNode(SQLConnection conn, ISQLAlias alias,IProgressMonitor monitor, String pswd) throws InterruptedException {
		//MessageDialog.openInformation(null,"createSessionTreeNode","");
		SessionTreeNode tt=null;
		try{
			tt=new SessionTreeNode(conn,alias,this,monitor,pswd);
		}finally{
			modelChanged(tt);
		}
		//MessageDialog.openInformation(null,"createdSessionTreeNode","");
		
		
	}

	/**
	 * @param listener
	 */
	public void addListener(SessionTreeModelChangedListener listener) {
		listeners.add(listener);
	}
	public void removeListener(SessionTreeModelChangedListener listener){
		listeners.remove(listener);
	}
	public void modelChanged(SessionTreeNode stn){
		
		Object []ls=listeners.getListeners();
		//MessageDialog.openInformation(null,"modelChanged",""+ls.length);
		for(int i=0;i<ls.length;++i){
			try{
				((SessionTreeModelChangedListener)ls[i]).modelChanged(stn);
			}catch(Throwable e){
			}
			
		}
	}
}

