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

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label Provider for DataSet.
 * 
 * @author Davy Vanherbergen
 */
/**
 * @author Davy Vanherbergen
 * 
 */
public class DataSetTableLabelProvider implements ITableLabelProvider {

    private SimpleDateFormat _dateFormatter = new SimpleDateFormat(
            SQLExplorerPlugin.getDefault().getPluginPreferences().getString(IConstants.DATASETRESULT_DATE_FORMAT));

    private DecimalFormat _decimalFormat = new DecimalFormat();

    private boolean _formatDates = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(
            IConstants.DATASETRESULT_FORMAT_DATES);


    public DataSetTableLabelProvider() {

        _decimalFormat.setGroupingUsed(false);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {

        // noop
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {

        // noop

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
     *      int)
     */
    public Image getColumnImage(Object element, int columnIndex) {

        return null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
     *      int)
     */
    public String getColumnText(Object element, int columnIndex) {

        DataSetRow row = (DataSetRow) element;

        Object tmp = row.getPrettyObjectValue(columnIndex);

        if (tmp != null) {

            Class clazz = tmp.getClass();

            // filter out scientific values
            if (clazz == Double.class || clazz == Integer.class) {
                return _decimalFormat.format(tmp);
            }

            // format dates
            if (_formatDates && clazz == Timestamp.class) {
                return _dateFormatter.format(new java.util.Date(((Timestamp) tmp).getTime()));
            }
            if (_formatDates && clazz == Date.class) {
                return _dateFormatter.format(new java.util.Date(((Date) tmp).getTime()));
            }

            return tmp.toString();
        }
        return "<null>";

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
     *      java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {

        return false;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {

        // noop
    }

}
