package net.sourceforge.sqlexplorer.ext.oracle;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.dbviewer.model.SchemaNode;
import net.sourceforge.sqlexplorer.ext.oracle.utility.InfoBuilder;
import net.sourceforge.sqlexplorer.sqlpanel.SQLTextViewer;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ProcNode implements IDbModel {
    static final Object[] keys = { "Created", "Last DDL Time", "TimeStamp", "Status" };

    private SQLConnection conn;

    private ArrayList list = new ArrayList(1);

    IDbModel parent;

    private Table tbDependentObjects;

    private TableViewer tv;

    private TableViewer tvParams;

    private String txt;

    private SQLTextViewer viewer;

    public ProcNode(IDbModel s, String name, SQLConnection conn) {
        this.conn = conn;
        parent = s;
        txt = name;

    }

    private void createDependentObjectsPanel(Composite parent) {
        tbDependentObjects = new Table(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        tbDependentObjects.setLinesVisible(true);
        tbDependentObjects.setHeaderVisible(true);
        TableColumn tc = new TableColumn(tbDependentObjects, SWT.NULL);
        tc.setText("Owner");
        tc = new TableColumn(tbDependentObjects, SWT.NULL);
        tc.setText("Type");
        tc = new TableColumn(tbDependentObjects, SWT.NULL);
        tc.setText("Name");
        for (int i = 0; i < tbDependentObjects.getColumnCount(); i++) {
            tbDependentObjects.getColumn(i).pack();
        }
    }

    private void createParamsPanel(Composite parent) {
        tvParams = new TableViewer(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        Table tb = tvParams.getTable();
        tb.setLinesVisible(true);
        tb.setHeaderVisible(true);
        TableColumn tc = new TableColumn(tb, SWT.NULL);
        tc.setText("Param Name");
        tc = new TableColumn(tb, SWT.NULL);
        tc.setText("Data Type");
        tc = new TableColumn(tb, SWT.NULL);
        tc.setText("Data Length");
        tc = new TableColumn(tb, SWT.NULL);
        tc.setText("Data Precision");
        tc = new TableColumn(tb, SWT.NULL);
        tc.setText("In/Out");
        for (int i = 0; i < tb.getColumnCount(); i++) {
            tb.getColumn(i).pack();
        }
    }

    /**
     * @param tbDependentObjects
     */
    private void fillDependentObjects() {
        final String sql = "select owner,type,name from sys.ALL_DEPENDENCIES where referenced_owner=? and referenced_name=? and referenced_type='PROCEDURE' order by owner,type,name";
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            String owner = getOwner();
            ps.setString(1, owner);
            ps.setString(2, txt);
            rs = ps.executeQuery();

            while (rs.next()) {
                TableItem ti = new TableItem(tbDependentObjects, SWT.NULL);
                ti.setText(0, rs.getString(1));
                ti.setText(1, rs.getString(2));
                ti.setText(2, rs.getString(3));
            }
            rs.close();
            ps.close();
        } catch (Throwable e) {
            // e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Throwable e) {
            }
        }
        for (int i = 0; i < tbDependentObjects.getColumnCount(); i++) {
            tbDependentObjects.getColumn(i).pack();
        }
        tbDependentObjects.layout();
    }

    public Object[] getChildren() {
        return list.toArray();
    }

    public Composite getComposite(DetailManager detailManager) {
        Composite comp = new Composite(detailManager.getComposite(), SWT.NULL);
        comp.setLayout(new FillLayout());
        TabFolder tabFolder = new TabFolder(comp, SWT.NULL);

        final TabItem tabItem1 = new TabItem(tabFolder, SWT.NULL);
        tabItem1.setText("Parameters");
        tabItem1.setToolTipText("Parameters");
        createParamsPanel(tabFolder);
        tvParams.setLabelProvider(new ParamInfoLabelProvider());
        tabItem1.setControl(tvParams.getControl());

        final TabItem tabItem2 = new TabItem(tabFolder, SWT.NULL);
        tabItem2.setText("Source"); //$NON-NLS-1$
        tabItem2.setToolTipText("Source"); //$NON-NLS-1$
        viewer = new SQLTextViewer(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION, detailManager.getStore(), null);
        tabItem2.setControl(viewer.getControl());

        final TabItem tabItem3 = new TabItem(tabFolder, SWT.NULL);
        tabItem3.setText("Info"); //$NON-NLS-1$
        tabItem3.setToolTipText("Info"); //$NON-NLS-1$
        tv = InfoBuilder.createInfoViewer(tabFolder, tabItem3);
        viewer.setDocument(new Document(getSource()));
        viewer.refresh();
        viewer.setEditable(false);

        final TabItem tabItem4 = new TabItem(tabFolder, SWT.NULL);
        tabItem4.setText("Dependent Objects"); //$NON-NLS-1$
        tabItem4.setToolTipText("Dependent Objects"); //$NON-NLS-1$
        createDependentObjectsPanel(tabFolder);
        tabItem4.setControl(tbDependentObjects);
        fillDependentObjects();

        final HashMap map = this.getInfo();
        tv.setContentProvider(new IStructuredContentProvider() {
            public void dispose() {
            }

            public Object[] getElements(Object input) {
                return map.entrySet().toArray();
            }

            public void inputChanged(Viewer viewer, Object arg1, Object arg2) {
            }
        });
        tv.setInput(this);
        final ArrayList paramLs = this.getParamInfoList();
        tvParams.setContentProvider(new IStructuredContentProvider() {

            public void dispose() {
            }

            public Object[] getElements(Object inputElement) {
                return paramLs.toArray();
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        });
        tvParams.setInput(this);
        return comp;
    }

    private HashMap getInfo() {
        final String sql = "SELECT  created,last_ddl_time, timestamp, status FROM sys.all_objects where owner=? and object_type='PROCEDURE' and object_name=?";
        HashMap map = new HashMap();
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            String owner = ((IDbModel) parent).getParent().toString();
            ps.setString(1, owner);
            ps.setString(2, txt);
            rs = ps.executeQuery();

            if (rs.next()) {
                map.put(keys[0], rs.getString(1));
                map.put(keys[1], rs.getString(2));
                map.put(keys[2], rs.getString(3));
                map.put(keys[3], rs.getString(4));
            }
            ps.close();
        } catch (Throwable e) {
        } finally {
            try {
                rs.close();
            } catch (Throwable e) {
            }
        }
        return map;

    }

    public String getOwner() {
        return ((SchemaNode) ((ProcTypeNode) parent).getParent()).toString();
    }

    public ArrayList getParamInfoList() {
        ArrayList paramLs = new ArrayList();
        final String sql = "SELECT argument_name, DATA_TYPE, data_length, data_precision,in_out,sequence FROM SYS.ALL_ARGUMENTS WHERE OWNER = ? and data_level=0 and object_id=(SELECT OBJECT_ID FROM SYS.ALL_OBJECTS WHERE OWNER = ? AND OBJECT_NAME = ? AND OBJECT_TYPE='PROCEDURE')  and sequence>0 order by sequence asc";
        // DEFAULT_VALUE
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            String owner = getOwner();
            ps.setString(1, owner);
            ps.setString(2, owner);
            ps.setString(3, txt);
            rs = ps.executeQuery();

            while (rs.next()) {
                ParamObj pObj = new ParamObj();
                pObj.argumentName = rs.getString(1);
                pObj.dataType = rs.getString(2);
                pObj.dataLength = rs.getInt(3);
                if (rs.wasNull())
                    pObj.dataLength = -1;

                pObj.dataPrecision = rs.getInt(4);
                if (rs.wasNull())
                    pObj.dataPrecision = -1;
                pObj.inOut = rs.getString(5);
                paramLs.add(pObj);
            }
            rs.close();
            ps.close();
        } catch (Throwable e) {
        } finally {
            try {
                rs.close();
            } catch (Throwable e) {
            }
        }
        return paramLs;
    }

    public Object getParent() {
        return parent;
    }

    private String getSource() {
        StringBuffer buf = new StringBuffer();

        final String sql = "SELECT text FROM sys.all_source where owner=? and name=? and type='PROCEDURE' order by line";
        String delimiter = viewer.getTextWidget().getLineDelimiter();
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            String owner = ((SchemaNode) ((ProcTypeNode) parent).getParent()).toString();
            ps.setString(1, owner);
            ps.setString(2, txt);
            rs = ps.executeQuery();
            while (rs.next()) {
                String text = rs.getString(1);
                // System.out.println(text);
                if (text != null && text.length() > 0) {
                    buf.append(text.substring(0, text.length() - 1));
                }
                buf.append(delimiter);
            }
            ps.close();
        } catch (Throwable e) {

        } finally {
            try {
                rs.close();
            } catch (Throwable e) {
            }
        }
        return buf.toString();

    }

    public boolean isValid() {
        try {
            return getInfo().get(keys[3]).equals("VALID") ? true : false;
        } catch (Exception e) {
        }
        return false;

    }

    public String toString() {
        return txt;
    }
}
