package net.sourceforge.sqlexplorer;

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

import java.util.Comparator;
import java.util.Iterator;

import net.sourceforge.sqlexplorer.util.SortedList;
import net.sourceforge.squirrel_sql.fw.id.IIdentifier;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.util.DuplicateObjectException;

/**
 * The aliases model.
 */
public class AliasModel {

    private static IISQLAliasComparator iISQLAliasComparator = new IISQLAliasComparator();

    private SortedList sl;

    private DataCache cache;


    public AliasModel(DataCache c) {
        cache = c;
        sl = new SortedList(iISQLAliasComparator);
        sl.addAll(c.aliases());

    };


    public Object[] getElements() {
        return sl.toArray();

    }


    public void removeAlias(ISQLAlias as) {
        cache.removeAlias(as);
        sl.remove(as);

    }


    public void addAlias(ISQLAlias as) throws DuplicateObjectException {
        cache.addAlias(as);
        sl.add(as);

    }


    public SQLAlias createAlias(IIdentifier id) {
        return (SQLAlias) cache.createAlias(id);
    }

    private static class IISQLAliasComparator implements Comparator {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2) {
            return ((ISQLAlias) o1).getName().compareToIgnoreCase(((ISQLAlias) o2).getName());
        }

    }

    
    /**
     * Find a specific alias by name
     * @param name of alias
     * @return alias or null if not found.
     */
    public ISQLAlias getAliasByName(String name) {
        
        if (sl != null) {
            
            Iterator it = sl.iterator();
            while (it.hasNext()) {
                
                ISQLAlias alias = (ISQLAlias) it.next();
                if (alias != null && alias.toString().equals(name)) {
                    return alias;
                }                
            }
        }

        return null;
    }
}
