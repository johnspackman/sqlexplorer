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
package net.sourceforge.sqlexplorer.ext.oracle.dialogs;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.ext.oracle.ParamObj;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.SqlResultsView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sqlpanel.SQLTableSorter;
import net.sourceforge.sqlexplorer.sqlpanel.SqlTableModel;
import net.sourceforge.sqlexplorer.util.SQLString;
import net.sourceforge.squirrel_sql.fw.sql.ResultSetReader;

public class Utils {
    private Utils() {
    }

    public static int decodeType(String dataType) {
        if (dataType.equalsIgnoreCase("VARCHAR2") || dataType.equals("VARCHAR"))
            return Types.VARCHAR;
        if (dataType.equalsIgnoreCase("NUMBER"))
            return Types.NUMERIC;
        if (dataType.equalsIgnoreCase("CHAR"))
            return Types.CHAR;
        if (dataType.equalsIgnoreCase("LONG"))
            return Types.LONGVARCHAR;
        if (dataType.equalsIgnoreCase("REF CURSOR"))
            return -10;
        return -1;
    }

    public static void execute(SessionTreeNode session, String sql, ArrayList paramList, ArrayList textInputList) {
        CallableStatement cs = null;
        try {
            cs = session.getConnection().getConnection().prepareCall(sql);
            int k = 0;
            int outputParam = -1;
            for (int i = 0, s = paramList.size(); i < s; i++) {
                ParamObj pObj = (ParamObj) paramList.get(i);
                String dataType = pObj.dataType;
                String inOut = pObj.inOut;
                if (inOut.equalsIgnoreCase("IN") || inOut.equals("IN/OUT")) {
                    int tipo = Utils.decodeType(dataType);
                    if (tipo != -10) {
                        cs.setString(i + 1, ((Text) textInputList.get(k)).getText());
                    }
                    k++;
                }
                if (inOut.equalsIgnoreCase("OUT") || inOut.equals("IN/OUT")) {
                    int tipo = Utils.decodeType(dataType);
                    if (tipo == -1)
                        throw new Exception("Data Type " + dataType + " not yet supported");
                    cs.registerOutParameter(i + 1, tipo);
                    if (outputParam != -1)
                        throw new Exception("More than one output paramater is not supported");
                    outputParam = i + 1;
                }

            }
            cs.execute();
            if (outputParam != -1) {
                Object obj = cs.getObject(outputParam);
                if (obj instanceof ResultSet) {
                    ResultSet rs = (ResultSet) obj;
                    final ResultSetMetaData metaData = rs.getMetaData();
                    final int count = metaData.getColumnCount();

                    final String[] ss = new String[count];
                    for (int i = 0; i < count; i++) {
                        ss[i] = metaData.getColumnName(i + 1);
                    }
                    final SQLTableSorter sorter = new SQLTableSorter(count, metaData);
                    ResultSetReader reader = new ResultSetReader(rs);
                    final SqlTableModel mo = new SqlTableModel(reader, metaData, SQLExplorerPlugin.getDefault().getPluginPreferences().getInt(
                            IConstants.MAX_SQL_ROWS), session.getConnection(), ss, sorter, new SQLString(sql));
                    final IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if (page != null) {
                        Display.getCurrent().asyncExec(new Runnable() {
                            public void run() {
                                try {
                                    SqlResultsView resultsView = (SqlResultsView) page.showView("net.sourceforge.sqlexplorer.plugin.views.SqlResultsView");
                                    resultsView.setData(new SqlTableModel[] { mo });

                                } catch (java.lang.Exception e) {

                                    SQLExplorerPlugin.error("Error displaying data", e);
                                }
                            };
                        });
                    }

                } else {
                    if (obj != null)
                        MessageDialog.openInformation(null, "Output", obj.toString());
                    else
                        MessageDialog.openInformation(null, "Output", null);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            SQLExplorerPlugin.error("Error executing stored", e);
            MessageDialog.openError(null, "Error", "See the Eclipse error log view for more info.");
        } finally {
            if (cs != null) {
                try {
                    cs.close();
                } catch (Throwable e) {
                }
            }
        }

    }
}
