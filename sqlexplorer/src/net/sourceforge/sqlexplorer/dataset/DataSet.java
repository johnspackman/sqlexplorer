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
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;

import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

/**
 * Generic DataSet to hold values for TableViewer.
 * 
 * This class has been changed to remove dependencies on a fixed list of data types;
 * this is to allow database-specific data types.  Since every row is represented as
 * Objects (typically instances of String, Integer, Double, etc), it is only a requirement 
 * that the cells implement the Comparable interface so that sorting works correctly.
 * The textual representation is obtained by calling toString() on the object.
 * 
 * Any code which used to use the TYPE_XXXX constants defined here should now use
 * instanceof if knowledge of the implementing type is required; however, be aware
 * that non-standard types (i.e. types not defined in java.lang) may be present.  
 * 
 * @author Davy Vanherbergen
 * @modified John Spackman
 */
public class DataSet {

    private String[] _columnLabels;

    private DataSetRow[] _rows;

    private DataSetTableSorter _sorter;
    
    protected ISQLAlias alias;

    /**
     * Create a new dataSet based on an existing ResultSet.
     * 
     * @param columnLabels String[] of column labels [mandatory]
     * @param resultSet ResultSet with values [mandatory]
     * @param relevantIndeces int[] of all columns to add to the dataSet, use
     *            null if all columns should be included.
     * @throws Exception if the dataset could not be created
     */
    public DataSet(ISQLAlias alias, String[] columnLabels, ResultSet resultSet, int[] relevantIndeces) throws SQLException {

    	this.alias = alias;
        initialize(columnLabels, resultSet, relevantIndeces);
    }
    public DataSet(String[] columnLabels, ResultSet resultSet, int[] relevantIndeces) throws SQLException {
    	this(null, columnLabels, resultSet, relevantIndeces);
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
    public DataSet(String[] columnLabels, String sql, int[] relevantIndeces, SQLConnection connection) throws SQLException {
  	
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
     * @throws Exception if dataSet could not be created
     */
    public DataSet(String[] columnLabels, String[][] data) throws Exception {
        _columnLabels = columnLabels;

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
     * Returns the number of columns
     * @return
     */
    public int getNumberOfColumns() {
    	return _columnLabels.length;
    }


    /**
     * @return String[] with all column labels
     */
    public String[] getColumnLabels() {
        return _columnLabels;
    }

    /**
     * Obtain number of rows.
     * @return Number of rows.
     */
    public int getRowCount() {
    	return _rows.length;
    }

    /**
     * @return all rows in this dataset
     */
    public DataSetRow[] getRows() {
        return _rows;
    }

    /**
     * Get a single row in this dataset.
     * @param index Index of row.
     * @return Row.
     * @throws IndexOutOfBoundsException if row at index isn't present.
     */
    public DataSetRow getRow(int index) {
    	if (index < 0 || index >= _rows.length)
    		throw new IndexOutOfBoundsException("DataSetRow index out of range: " + index);
    	return _rows[index];
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
    private void initialize(String[] columnLabels, ResultSet resultSet, int[] relevantIndeces) throws SQLException {

        ResultSetMetaData metadata = resultSet.getMetaData();

        int[] ri = relevantIndeces;
        
        // create default column indexes
        if (ri == null || ri.length == 0) {
            ri = new int[metadata.getColumnCount()];
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                ri[i - 1] = i;
            }
        }

        // create column labels
        if (columnLabels != null && columnLabels.length != 0) {
            _columnLabels = columnLabels;
        } else {
            _columnLabels = new String[ri.length];
            for (int i = 0; i < ri.length; i++) {
                _columnLabels[i] = metadata.getColumnName(ri[i]);
            }
        }

        loadRows(resultSet, ri);
    }
    
    /**
     * Called to load rows from the specified result set; the default implementation
     * simply uses standard JDBC data types to  inten to be
     * overridden.
     * @param resultSet ResultSet to load from
     * @param relevantIndeces int[] of all columns to add to the dataSet, use
     *            null if all columns should be included.
     */
    protected void loadRows(ResultSet resultSet, int[] relevantIndeces) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        
        // create rows
        ArrayList rows = new ArrayList(100);
        while (resultSet.next()) {

            DataSetRow row = new DataSetRow(getNumberOfColumns());
            for (int i = 0; i < getNumberOfColumns(); i++) {
            	int columnIndex = relevantIndeces[i];
            	Comparable obj = loadCellValue(columnIndex, metadata.getColumnType(columnIndex), resultSet);
            	row.setValue(i, obj);
                if (resultSet.wasNull())
                    row.setValue(i, null);
            }
            rows.add(row);
        }
        _rows = (DataSetRow[]) rows.toArray(new DataSetRow[] {});
    }
    
    /**
     * Loads a given column from the current row in a ResultSet; can be overridden to
     * provide database-specific implementation
     * @param columnIndex
     * @param dataType
     * @param resultSet
     * @return
     * @throws SQLException
     */
    protected Comparable loadCellValue(int columnIndex, int dataType, ResultSet resultSet) throws SQLException {
        switch (dataType) {
	        case Types.INTEGER:
	        case Types.SMALLINT:
	        case Types.TINYINT:
	            return new Long(resultSet.getInt(columnIndex));
	
	        case Types.DECIMAL:
	        case Types.NUMERIC:
	        case Types.DOUBLE:
	        case Types.FLOAT:
	        case Types.REAL:
	            return new Double(resultSet.getDouble(columnIndex));
	
	        case Types.DATE:
	        case Types.TIMESTAMP:                    
	            return resultSet.getTimestamp(columnIndex);
	            
	        case Types.TIME:
	            return resultSet.getTime(columnIndex);
	            
	        case Types.BIGINT:
	            return new Long(resultSet.getLong(columnIndex));
	            
	        default:
	            return resultSet.getString(columnIndex);
	    }
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
