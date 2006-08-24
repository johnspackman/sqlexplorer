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
package net.sourceforge.sqlexplorer.connections;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.RootSessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for database structure outline.
 * 
 * @author Davy Vanherbergen
 */
public class ConnectionTreeLabelProvider extends LabelProvider {

    private Image _inactiveAliasImage = ImageUtil.getImage("Images.AliasIcon");

    private Image _activeAliasImage = ImageUtil.getImage("Images.ConnectedAliasIcon");

    private Image _sessionImage = ImageUtil.getImage("Images.ConnectionIcon");


    public void dispose() {
        
        super.dispose();
        
        ImageUtil.disposeImage("Images.AliasIcon");
        ImageUtil.disposeImage("Images.ConnectedAliasIcon");
        ImageUtil.disposeImage("Images.ConnectionIcon");
        
    }


    /**
     * Return the image used for the given node.
     * 
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {

        if (element instanceof ISQLAlias) {

            ISQLAlias alias = (ISQLAlias) element;

            // locate open sessions
            RootSessionTreeNode sessionRoot = SQLExplorerPlugin.getDefault().stm.getRoot();
            Object[] sessions = sessionRoot.getChildren();
            List children = new ArrayList();
            if (sessions != null) {
                for (int i = 0; i < sessions.length; i++) {
                    SessionTreeNode session = (SessionTreeNode) sessions[i];
                    if (session.getAlias().getIdentifier().equals(alias.getIdentifier())) {
                        children.add(session);
                    }
                }
            }
            if (children.size() != 0) {
                return _activeAliasImage;
            } else {
                return _inactiveAliasImage;
            }

        } else {

            return _sessionImage;
        }

    }


    /**
     * Return the text to display
     * 
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {

        if (element instanceof ISQLAlias) {

            ISQLAlias alias = (ISQLAlias) element;
            String label = alias.getName();

            // locate open sessions
            RootSessionTreeNode sessionRoot = SQLExplorerPlugin.getDefault().stm.getRoot();
            Object[] sessions = sessionRoot.getChildren();
            List children = new ArrayList();
            if (sessions != null) {
                for (int i = 0; i < sessions.length; i++) {
                    SessionTreeNode session = (SessionTreeNode) sessions[i];
                    if (session.getAlias().getIdentifier().equals(alias.getIdentifier())) {
                        children.add(session);
                    }
                }
            }
            if (children.size() == 0) {
                return label;
            }

            if (children.size() == 1) {
                return label += " (" + children.size() + " " + Messages.getString("ConnectionsView.ConnectedAlias.single.Postfix") + ")";
            }

            if (children.size() > 1) {
                return label += " (" + children.size() + " " + Messages.getString("ConnectionsView.ConnectedAlias.multiple.Postfix") + ")";
            }

            return label;

        } else {

            String label = Messages.getString("ConnectionsView.ConnectedAlias.activeSession");            
            SessionTreeNode session = (SessionTreeNode) element;
            
            SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
            return label + " " + fmt.format(new Date(session.getCreated()));
        }

    }

}
