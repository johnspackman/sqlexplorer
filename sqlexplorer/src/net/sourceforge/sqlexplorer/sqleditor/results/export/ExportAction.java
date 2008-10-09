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
package net.sourceforge.sqlexplorer.sqleditor.results.export;

import java.io.File;
import java.io.PrintStream;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.sqleditor.results.CellRangeRow;
import net.sourceforge.sqlexplorer.sqleditor.results.ResultProvider;
import net.sourceforge.sqlexplorer.sqleditor.results.ResultsTableAction;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Export table contents to a CSV file.
 * @author Davy Vanherbergen
 */
public class ExportAction extends ResultsTableAction {

    private static final ImageDescriptor _image = ImageUtil.getDescriptor("Images.ExportIcon");
    
    private Shell shell;

    @Override
	public void initialise(Shell shell) {
		super.initialise(shell);
		this.shell = shell;
	}

	/**
     * Return the text that will be displayed in the context popup menu for this action. 
     */
    public String getText() {
        return Messages.getString("DataSetTable.Actions.Export.CSV");
    }

    /**
     * Provide image for action
     */
    public ImageDescriptor getImageDescriptor() {
        return _image;
    }

    /**
     * Main method. Prompt for file name and save table contents to csv file.
     */
    public void run() 
    {
    	final ExportDlg	dlg = new ExportDlg(shell);
    	if (dlg.open() != Window.OK)
    		return;

        // let's show the fancy wait cursor..
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() 
        {

            public void run() 
            {

                try 
                {
                    // create new file
                    File file = new File(dlg.getFilename());

                    if (file.exists()) 
                    {
                        // overwrite existing files
                        file.delete();
                    }
                    
                    file.createNewFile();
                    PrintStream writer = new PrintStream(file, dlg.getCharacterSet()); 
                    
                    // get column header and separator preferences
                    String columnSeparator = dlg.getDelimiter(); 
                    boolean includeColumnNames = dlg.includeHeaders();
                    boolean rtrim = dlg.trimSpaces();
                    boolean quote = dlg.quoteText();
                    String nullValue = dlg.getNullValue();
                                       
                    // check if there is somethign in our table                    
                    ResultProvider data = getResultsTable().getResultProvider();
                    
                    int columnCount = data.getNumberOfColumns();
                    // export column names if we need to 
                    if (includeColumnNames) 
                    {
                        
                        for (int i = 0; i < columnCount; i++) 
                        {
                            if (i != 0)
                            {
                            	writer.print(columnSeparator);
                            }
                            writer.print(data.getColumn(i).getCaption());
                        }
                        writer.println();
                    }

                    // export column data
                    for (CellRangeRow row : data.getRows()) 
                    {
                                           
                        for (int j = 0; j < columnCount; j++) 
                        {
                        	Object o = row.getCellValue(j);
                        	String t = o == null ? nullValue : o.toString();
                        	if (rtrim)
                        	{
                        		t = TextUtil.rtrim(t);
                        	}
                        	if (quote && o instanceof String) 
                        	{
                        		writer.print("\"");
                        		writer.print(t.replaceAll("\"", "\"\""));
                        		writer.print("\"");
                        	} 
                        	else
                        	{
                        		writer.print(t);
                        	}
                        	/* don't append separator _after_ last column */
                        	if (j < columnCount - 1)
                        	{
                        		writer.print(columnSeparator);
                        	}
                        }
                        writer.println();
                    }

                    writer.close();


                } 
                catch (final Exception e) 
                {
                    shell.getDisplay().asyncExec(new Runnable() 
                    {

                        public void run() {
                            MessageDialog.openError(shell, Messages.getString("SQLResultsView.Error.Export.Title"), e.getMessage());
                        }
                    });
                }
            }
        });
    }

}
