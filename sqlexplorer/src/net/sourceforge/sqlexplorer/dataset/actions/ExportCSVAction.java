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
package net.sourceforge.sqlexplorer.dataset.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TableItem;

/**
 * Copy an entire datasettable to the clipboard.
 * 
 * @author Davy Vanherbergen
 */
public class ExportCSVAction extends AbstractDataSetTableContextAction {

    private static final ImageDescriptor _image = ImageDescriptor.createFromURL(SqlexplorerImages.getExportIcon());


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IAction#getText()
     */
    public String getText() {
        return Messages.getString("DataSetTable.Actions.ExportCSV");
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return _image;
    }


    /**
     * Copy all table data to clipboard
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {

        FileDialog fileDialog = new FileDialog(_table.getShell(), SWT.SAVE);        
        String[] filterExtensions = new String[] {"*.csv"};
        fileDialog.setFilterExtensions(filterExtensions);       
        
        final String fileName = fileDialog.open();
        if (fileName == null && fileName.trim().length() == 0) {
            return;
        }
        
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            public void run() {

                try {

                    File file = new File(fileName);

                    if (file.exists()) {
                        // overwrite existing files
                        file.delete();
                    }
                    
                    file.createNewFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    StringBuffer buffer = new StringBuffer("");
                    
                    // get preferences
                    String columnSeparator = SQLExplorerPlugin.getDefault().getPreferenceStore().getString(IConstants.CLIP_EXPORT_SEPARATOR);
                    boolean includeColumnNames = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.CLIP_EXPORT_COLUMNS);
                                       
                    TableItem[] items = _table.getItems();                    
                    DataSet dataSet = (DataSet) _table.getData();
                    
                    if (items == null || dataSet == null) {
                        return;
                    }
                    
                    // export column names
                    if (includeColumnNames) {
                        
                        String[] columnNames = dataSet.getColumnLabels();
                        for (int i = 0; i < columnNames.length; i++) {
                            buffer.append(columnNames[i]);
                            buffer.append(columnSeparator);
                        }
                        writer.write(buffer.toString(), 0, buffer.length());
                        writer.newLine();
                    }

                    // export column data
                    int columnCount = _table.getColumnCount();
                    for (int i = 0; i < items.length; i++) {
                                           
                        buffer = new StringBuffer("");
                        
                        for (int j = 0; j < columnCount; j++) {
                            buffer.append(items[i].getText(j));
                            buffer.append(columnSeparator);
                        }
                        writer.write(buffer.toString(), 0, buffer.length());
                        writer.newLine();
                    }

                    writer.close();


                } catch (final Exception e) {
                    _table.getShell().getDisplay().asyncExec(new Runnable() {

                        public void run() {
                            MessageDialog.openError(_table.getShell(), Messages.getString("SQLResultsView.Error.Export.Title"), e.getMessage());
                        }
                    });
                }
            }
        });

    }

}
