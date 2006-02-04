/*
 * Copyright (C) 2006 Davy Vanherbergen
 * dvanherbergen@users.sourceforge.net
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
package net.sourceforge.sqlexplorer.dataset;

import net.sourceforge.sqlexplorer.dataset.actions.AbstractDataSetTableContextAction;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.actions.ActionGroup;

/**
 * ActionGroup for DataSetTable. This group controls what context
 * menu actions are being shown.
 * 
 * @author Davy Vanherbergen
 */
public class DataSetTableActionGroup extends ActionGroup {

    private Table _table;

    private TableCursor _cursor;
    
    /**
     * Construct a new action group for a given Table
     * 
     * @param table Table that displays the context menu
     * @param cursor TableCursor that displays the context menu
     */
    public DataSetTableActionGroup(Table table, TableCursor cursor) {
        _table = table;
        _cursor = cursor;
    }


    /**
     * Fill the context menu with all the correct actions.
     * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void fillContextMenu(IMenuManager menu) {
        
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("net.sourceforge.sqlexplorer", "dataSetTableContextAction");
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {

            IExtension e = extensions[i];

            IConfigurationElement[] ces = e.getConfigurationElements();

            for (int j = 0; j < ces.length; j++) {
                try {
                    
                    // check if the action thinks it is suitable..
                    AbstractDataSetTableContextAction action = (AbstractDataSetTableContextAction) ces[j].createExecutableExtension("class");
                    action.setTable(_table);
                    action.setTableCursor(_cursor);
                    if (action.isAvailable()) {
                        menu.add(action);
                    }
                    
                } catch (Throwable ex) {
                    SQLExplorerPlugin.error("Could not create menu action", ex);
                }
            }
        }

    }


}
