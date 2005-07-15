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

package net.sourceforge.sqlexplorer.ext.oracle;

import java.util.ArrayList;

import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.model.DatabaseNode;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.swt.widgets.Composite;

public class SecurityNode implements IDbModel {
    private ArrayList list = new ArrayList(10);;

    private IDbModel parent;

    private String txt;

    public SecurityNode(DatabaseNode root, String name, SQLConnection conn) {
        txt = name;
        parent = root;
        list.add(new UsersNode(this, "Users", conn));
    }

    public Object[] getChildren() {
        return list.toArray();
    }

    public Composite getComposite(DetailManager detailManager) {
        return null;
    }

    public Object getParent() {
        return parent;
    }

    public String getTitle() {
        return txt;
    }

    public String toString() {
        return txt;
    };
}
