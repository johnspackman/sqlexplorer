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

/**
 * DataSetRow, represents one row in a dataSet.
 * 
 * @author Davy Vanherbergen
 */
public class DataSetRow {

    private Object[] _values;

    private DecimalFormat _decimalFormat = new DecimalFormat();
    
    private SimpleDateFormat _dateFormatter = new SimpleDateFormat(SQLExplorerPlugin.getDefault().getPluginPreferences().getString(IConstants.DATASETRESULT_DATE_FORMAT)); 
    
    private boolean _formatDates = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.DATASETRESULT_FORMAT_DATES);
    
    /**
     * Create new DataSetRow with columnCount values
     * 
     * @param columnCount number of columns
     */
    public DataSetRow(int columnCount) {
        _values = new Object[columnCount];
        _decimalFormat.setGroupingUsed(false);
    }


    /**
     * Create initialized dataSetRow
     * 
     * @param values
     */
    public DataSetRow(String[] values) {
        _values = values;
    }


    /**
     * Returns string representation for value of given column.
     * 
     * @param column first column is 0
     */
    public String getStringValue(int column) {

        Object tmp = _values[column];
        if (tmp != null) {
            
            Class clazz = tmp.getClass();
            
            // filter out scientific values
            if (clazz == Double.class || clazz == Integer.class)  {                 
                return _decimalFormat.format(tmp); 
            } 
            
            // format dates
            if (_formatDates && clazz == Timestamp.class) {                
                return _dateFormatter.format(new java.util.Date(((Timestamp)tmp).getTime()));
            }
            if (_formatDates && clazz == Date.class) {                
                return _dateFormatter.format(new java.util.Date(((Date)tmp).getTime()));
            }
            
            return tmp.toString();
        }
        return "<null>";
    }

    
    /**
     * Returns value of given column.
     * 
     * @param column first column is 0
     */
    public Object getObjectValue(int column) {

        Object tmp = _values[column];
        if (tmp != null) {
            return tmp;
        }
        return "<null>";
    }
    

    /**
     * Set the value for a given column
     * 
     * @param column first column is 0
     * @param value
     */
    public void setValue(int column, Object value) {
        _values[column] = value;
    }

    
    /**
     * @return number of columns in this row
     */
    public int length() {
        if (_values == null) {
            return 0;
        }
        return _values.length;
    }
}
