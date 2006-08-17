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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;

import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

/**
 * Generic DataSet to hold values for TableViewer.
 * 
 * @author Davy Vanherbergen
 */
public class DataSet {

    public static final int TYPE_DATE = 3;

    public static final int TYPE_DATETIME = 4;

    public static final int TYPE_DOUBLE = 1;

    public static final int TYPE_INTEGER = 2;
    
    public static final int TYPE_LONG = 6;

    public static final int TYPE_STRING = 0;

    public static final int TYPE_TIME = 5;

    private String[] _columnLabels;

    private int[] _columnTypes;

    private DataSetRow[] _rows;

    private DataSetTableSorter _sorter;

	/**
     * Hidden default constructor.
     */
    private DataSet() {

    }


    /**
     * Create a new dataSet based on an existing ResultSet.
     * 
     * @param columnLabels String[] of column labels [mandatory]
     * @param resultSet ResultSet with values [mandatory]
     * @param relevantIndeces int[] of all columns to add to the dataSet, use
     *            null if all columns should be included.
     * @throws Exception if the dataset could not be created
     */
    public DataSet(String[] columnLabels, ResultSet resultSet, int[] relevantIndeces) throws Exception {

        initialize(columnLabels, resultSet, relevantIndeces);
    }


    /**
     * Create new dataset based on sql query.
     * 
     * @param columnLabels string[] of columnLabels, use null if the column name
     *            can be used as label
     * @param sql query string
     * @param relevantIndeces int[] of all columns to add to the dataSet, use
     *            null if all columns should be included.
     * @param connection An open SQLConnection [mandatory]
     * @throws Exception if dataSet could not be created
     */
    public DataSet(String[] columnLabels, String sql, int[] relevantIndeces, SQLConnection connection) throws Exception {
  	
        Statement statement = connection.createStatement();

        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();

        initialize(columnLabels, resultSet, relevantIndeces);
        
        statement.close();
        

    }


    /**
     * Create new dataset based on String[][].
     * 
     * @param columnLabels string[] of columnLabels [mandatory]
     * @param data string[][] with values for dataset [mandatory]
     * @param columnTypes int[] with valid column types (e.g.
     *            DataSet.TYPE_STRING) [mandatory]
     * @throws Exception if dataSet could not be created
     */
    public DataSet(String[] columnLabels, String[][] data, int[] columnTypes) throws Exception {

        _columnLabels = columnLabels;
        _columnTypes = columnTypes;

        _rows = new DataSetRow[data.length];

        for (int i = 0; i < data.length; i++) {
            _rows[i] = new DataSetRow(data[i]);
        }
    }


    /**
     * Get the column index for a given column name
     * 
     * @param name
     * @return index of column whose name matches or 0 if none found
     */
    public int getColumnIndex(String name) {
        for (int i = 0; i < _columnLabels.length; i++) {
            if (_columnLabels[i].equalsIgnoreCase(name)) {
                return i;
            }
        }
        return 0;
    }


    /**
     * @return String[] with all column labels
     */
    public String[] getColumnLabels() {
        return _columnLabels;
    }


    /**
     * @return int[] with all column types
     */
    public int[] getColumnTypes() {
        return _columnTypes;
    }


    /**
     * @return all rows in this dataset
     */
    public DataSetRow[] getRows() {
        return _rows;
    }


    /**
     * Initialize dataSet based on an existing ResultSet.
     * 
     * @param columnLabels String[] of column labels [mandatory]
     * @param resultSet ResultSet with values [mandatory]
     * @param relevantIndeces int[] of all columns to add to the dataSet, use
     *            null if all columns should be included.
     * @throws Exception if the dataset could not be created
     */
    private void initialize(String[] columnLabels, ResultSet resultSet, int[] relevantIndeces) throws Exception {

        ResultSetMetaData metadata = resultSet.getMetaData();

        // create default column indexes
        if (relevantIndeces == null || relevantIndeces.length == 0) {
            relevantIndeces = new int[metadata.getColumnCount()];
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                relevantIndeces[i - 1] = i;
            }
        }

        // create column labels
        if (columnLabels != null && columnLabels.length != 0) {
            _columnLabels = columnLabels;
        } else {
            _columnLabels = new String[relevantIndeces.length];
            for (int i = 0; i < relevantIndeces.length; i++) {
                _columnLabels[i] = metadata.getColumnName(relevantIndeces[i]);
            }
        }

        // create column types
        _columnTypes = new int[relevantIndeces.length];
        for (int i = 0; i < relevantIndeces.length; i++) {

            switch (metadata.getColumnType(relevantIndeces[i])) {

                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                case -9:
                    _columnTypes[i] = TYPE_STRING;
                    break;

                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                    _columnTypes[i] = TYPE_INTEGER;
                    break;

                case Types.DECIMAL:
                case Types.NUMERIC:
                case Types.DOUBLE:
                case Types.FLOAT:
                case Types.REAL:
                    _columnTypes[i] = TYPE_DOUBLE;
                    break;

                case Types.DATE:
                case Types.TIMESTAMP:                    
                    _columnTypes[i] = TYPE_DATETIME;
                    break;

                case Types.TIME:
                    _columnTypes[i] = TYPE_TIME;
                    break;

                case Types.BIGINT:
                    _columnTypes[i] = TYPE_LONG;
                    break;
                    
                default:
                    _columnTypes[i] = TYPE_STRING;
            }
        }

        // create rows
        ArrayList rows = new ArrayList(100);
        while (resultSet.next()) {

            DataSetRow row = new DataSetRow(relevantIndeces.length);

            for (int i = 0; i < relevantIndeces.length; i++) {

                switch (_columnTypes[i]) {

                    case TYPE_STRING:
                        row.setValue(i, resultSet.getString(relevantIndeces[i]));
                        break;
                    case TYPE_INTEGER:
                        row.setValue(i, new Long(resultSet.getInt(relevantIndeces[i])));
                        break;
                    case TYPE_DOUBLE:
                        row.setValue(i, new Double(resultSet.getDouble(relevantIndeces[i])));
                        break;
                    case TYPE_DATE:
                        row.setValue(i, resultSet.getDate(relevantIndeces[i]));
                        break;
                    case TYPE_DATETIME:
                        row.setValue(i, resultSet.getTimestamp(relevantIndeces[i]));
                        break;
                    case TYPE_TIME:
                        row.setValue(i, resultSet.getTime(relevantIndeces[i]));
                        break;
                    case TYPE_LONG:
                        row.setValue(i, new Long(resultSet.getLong(relevantIndeces[i])));
                        break;
                    default:
                        row.setValue(i, resultSet.getString(relevantIndeces[i]));
                        break;
                }
                
                if (resultSet.wasNull()) {
                    row.setValue(i, null);
                }

            }
            rows.add(row);
        }

        _rows = (DataSetRow[]) rows.toArray(new DataSetRow[] {});

    }
    
    
    
    /**
     * Resort the data using the given column and sortdirection.
     * @param columnIndex primary sort column index
     * @param sortDirection SWT.UP | SWT.DOWN
     */    
	public void sort(int columnIndex, int sortDirection) {
    	
    	if (_sorter == null) {
    		_sorter = new DataSetTableSorter(this);
    	}
    	_sorter.setTopPriority(columnIndex, sortDirection);
    	
    	Arrays.sort(_rows, _sorter);
    }
    
    
}
