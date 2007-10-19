/*
 * Copyright (C) 2006 SQL Explorer Development Team
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
package net.sourceforge.sqlexplorer.plugin.actions;

import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.ConnectionJob;
import net.sourceforge.sqlexplorer.dbproduct.User;

import org.eclipse.jface.action.Action;

public class OpenPasswordConnectDialogAction extends Action {

	// The Alias to login - this MUST be the same as the user's alias; the
	//	reason we keep this in addition to the User object is in case we are
	//	logging in a new user that we have no initial definition for
	private Alias alias;
	
	// The user to try and login
    private User user;
    
    // Whether to prompt for the password, even if the user is auto-logon etc
    private boolean alwaysPrompt;

    public OpenPasswordConnectDialogAction(Alias alias, User user, boolean alwaysPrompt) {
    	super();
    	if (alias == null)
    		throw new IllegalArgumentException("Alias cannot be null!");
    	if (user != null && alias != user.getAlias())
    		throw new IllegalArgumentException("User is attached the wrong alias");
    	this.alias = alias;
        this.user = user;
        this.alwaysPrompt = alwaysPrompt;
    }

    public void run() {
    	ConnectionJob.createSession(alias, user, null, alwaysPrompt);
    }
}
