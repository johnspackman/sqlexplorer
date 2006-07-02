package net.sourceforge.sqlexplorer.sqleditor.actions;

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

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SqlexplorerImages;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

public class OpenFileAction extends AbstractEditorAction {

    private ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getOpenFileIcon());


    public String getText() {
        return Messages.getString("Open_1"); //$NON-NLS-1$
    }


    public void run() {

        FileDialog dlg = new FileDialog(_editor.getSite().getShell(), SWT.OPEN | SWT.MULTI);

        dlg.setFilterExtensions(new String[] {"*.sql;*.txt"});

        String path = dlg.open();
        if (path != null) {
            String[] files = dlg.getFileNames();
            _editor.loadFiles(files, dlg.getFilterPath());
        }

    }


    public String getToolTipText() {
        return Messages.getString("Open_2"); //$NON-NLS-1$
    }


    public ImageDescriptor getHoverImageDescriptor() {
        return img;
    }


    public ImageDescriptor getImageDescriptor() {
        return img;
    };
}
