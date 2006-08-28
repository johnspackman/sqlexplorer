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
package net.sourceforge.sqlexplorer.sqleditor.actions;

import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;

import org.eclipse.jface.action.Action;


/**
 * Abstract implementation for a sql editor actions.
 * Extend this class to add new actions to the sql editor.
 * 
 * @author Davy Vanherbergen
 *
 */
public abstract class AbstractEditorAction extends Action {
   
    public abstract String getText();
    
    public String getToolTipText() {
        return getText();
    }
    
    public abstract void run();
    
    protected SQLEditor _editor;
    
    public final void setEditor(SQLEditor editor) {
        _editor = editor;
    }

    public boolean isDisabled() {

        boolean active = _editor.getSessionTreeNode() != null;
        return !active;
    }
    
}
