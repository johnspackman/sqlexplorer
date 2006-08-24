/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
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

package net.sourceforge.sqlexplorer.dbstructure.actions;

import java.sql.ResultSet;
import java.util.ArrayList;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;
import net.sourceforge.squirrel_sql.fw.sql.SQLDatabaseMetaData;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Create table script for the selected node.
 * 
 * @modified Davy Vanherbergen
 * 
 */
public class CreateTableScriptAction extends AbstractDBTreeContextAction {

    private static final ImageDescriptor _image = ImageUtil.getDescriptor("Images.TableIcon");


    /**
     * Custom image for refresh action
     * 
     * @see org.eclipse.jface.action.IAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return _image;
    }


    /**
     * Set the text for the menu entry.
     * 
     * @see org.eclipse.jface.action.IAction#getText()
     */
    public String getText() {
        return Messages.getString("DatabaseStructureView.Actions.CreateTableScript");
    }


    /**
     * Create table script for selected node.
     * 
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {

        TableNode tableNode = (TableNode) _selectedNodes[0];
        ITableInfo info = tableNode.getTableInfo();

        SQLDatabaseMetaData metaData = tableNode.getSession().getMetaData();

        ResultSet resultSet;
        StringBuffer buf = new StringBuffer(4 * 1024);
        String sep = System.getProperty("line.separator");

        try {

            ArrayList pks = new ArrayList();
            ResultSet rsPks = metaData.getPrimaryKeys(info);

            while (rsPks.next()) {
                String name = rsPks.getString(4);
                pks.add(name);
            }
            rsPks.close();

            resultSet = metaData.getColumns(info);
            String tableName = _selectedNodes[0].getQualifiedName();
            buf.append("CREATE TABLE ");
            buf.append(tableName);
            buf.append("(");

            while (resultSet.next()) {
                String columnName = resultSet.getString(4);
                String typeName = resultSet.getString(6);
                String columnSize = resultSet.getString(7);
                String decimalDigits = resultSet.getString(9);
                String defaultValue = resultSet.getString(13);
                boolean notNull = "NO".equalsIgnoreCase(resultSet.getString(18)); 
                String sLower = typeName.toLowerCase();
                buf.append(sep);
                buf.append(columnName + " ");

                buf.append(typeName);

                boolean bNumeric = false;
                if (sLower.equals("numeric") || sLower.equals("number") || sLower.equals("decimal"))
                    bNumeric = true;

                if (sLower.indexOf("char") != -1 || sLower.indexOf("int") != -1) {
                    buf.append("(");
                    buf.append(columnSize);
                    buf.append(")");
                } else if (bNumeric) {
                    buf.append("(");
                    buf.append(columnSize);
                    String tmp = decimalDigits;
                    if (tmp != null && !"".equals(tmp) && !"0".equals(tmp))
                    {
                        buf.append(",");
                        buf.append(tmp);
                    }
                    buf.append(")");
                }
                if (pks.size() == 1 && pks.get(0).equals(columnName)) {
                    buf.append(" PRIMARY KEY");
                }

                if (defaultValue != null && !defaultValue.equals("")) {
                    buf.append(" default ");
                    boolean isSystemValue = bNumeric; 
                    
                    if (defaultValue.equalsIgnoreCase("CURRENT_TIMESTAMP")) {
                        isSystemValue = true;
                    }
                    
                    if (!isSystemValue)
                        buf.append("'");
                    buf.append(defaultValue);
                    if (!isSystemValue)
                        buf.append("'");

                }

                if (notNull) {
                    buf.append(" not null");
                }
                buf.append(",");
            }
            buf.deleteCharAt(buf.length() - 1);
            resultSet.close();
            buf.append(")" + sep);

            SQLEditorInput input = new SQLEditorInput("SQL Editor (" + SQLExplorerPlugin.getDefault().getNextElement() + ").sql");
            input.setSessionNode(_selectedNodes[0].getSession());
            IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();

            SQLEditor editorPart = (SQLEditor) page.openEditor((IEditorInput) input, "net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");
            editorPart.setText(buf.toString());
        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error creating export script", e);
        }

    }


    /**
     * Action is availble when a node is selected
     * 
     * @see net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction#isAvailable()
     */
    public boolean isAvailable() {

        if (_selectedNodes.length != 0) {
            return true;
        }
        return false;
    }
}
