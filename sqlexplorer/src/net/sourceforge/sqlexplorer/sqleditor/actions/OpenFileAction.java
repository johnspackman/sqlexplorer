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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

public class OpenFileAction extends AbstractEditorAction {

    private ImageDescriptor img = ImageUtil.getDescriptor("Images.OpenFileIcon");


    public String getText() {
        return Messages.getString("Open_1"); //$NON-NLS-1$
    }

    public boolean isEnabled() {
        return true;
    }

    public void run() {

        FileDialog dlg = new FileDialog(_editor.getSite().getShell(), SWT.OPEN | SWT.MULTI);

        dlg.setFilterExtensions(new String[] {"*.sql;*.txt"});

        String path = dlg.open();
        if (path != null) {
            String[] files = dlg.getFileNames();
            loadFiles(files, dlg.getFilterPath());
        }

    }


    /**
     * Load one or more files into the editor.
     * 
     * @param files string[] of relative file paths
     * @param filePath path where all files are found
     */
    public void loadFiles(String[] files, String filePath) {

        BufferedReader reader = null;

        try {

            StringBuffer all = new StringBuffer();
            String str = null;
            String delimiter = _editor.sqlTextViewer.getTextWidget().getLineDelimiter();

            for (int i = 0; i < files.length; i++) {

                String path = "";
                if (filePath != null) {
                    path += filePath + File.separator;
                }
                path += files[i];

                reader = new BufferedReader(new FileReader(path));

                while ((str = reader.readLine()) != null) {
                    all.append(str);
                    all.append(delimiter);
                }

                if (files.length > 1) {
                    all.append(delimiter);
                }
            }

            _editor.sqlTextViewer.setDocument(new Document(all.toString()));

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error loading document", e);

        } finally {
            try {
                reader.close();
            } catch (java.io.IOException e) {
                // noop
            }
        }

    }
    
    
    public String getToolTipText() {
        return Messages.getString("Open_2"); 
    }


    public ImageDescriptor getHoverImageDescriptor() {
        return img;
    }


    public ImageDescriptor getImageDescriptor() {
        return img;
    };
}
