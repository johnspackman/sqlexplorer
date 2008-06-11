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

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.sqleditor.results.ResultsTableAction;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import org.eclipse.jface.resource.ImageDescriptor;
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
    public void run() {
    	ExportDlg dlg = new ExportDlg(shell);
    	dlg.open();
    }

}
