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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;

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
	
	public static class Column {
		private String caption;
		private boolean rightJustify;
		
		public Column(String caption, boolean rightJustify) {
			super();
			this.caption = caption;
			this.rightJustify = rightJustify;
		}

		public String getCaption() {
			return caption;
		}

		public boolean isRightJustify() {
			return rightJustify;
		}

		public Format getFormat() {
			return null;
		}
	}
	
	public static class FormattedColumn extends Column {
		private Format format;

		public FormattedColumn(String caption, boolean rightJustify, Format format) {
			super(caption, rightJustify);
			this.format = format;
		}

		public Format getFormat() {
			return format;
		}
	}

	// Caption for the results tabs
	private String caption;
	
    private Column[] columns;

    private DataSetRow[] _rows;

    private DataSetTableSorter _sorter;
    
    // Whether dates are formatted (from preferences)
    private Boolean formatDates;

    // Default date format (from preferences)
	private SimpleDateFormat dateFormat; 
	
    /**
     * Create a new dataSet based on an existing ResultSet.
     * @param resultSet ResultSet with values [mandatory]
     * @param relevantIndeces int[] of all columns to add to the dataSet, use
     *            null if all columns should be included.
     * 
     * @throws Exception if the dataset could not be created
     */
    public DataSet(ResultSet resultSet, int[] relevantIndeces) throws SQLException {
        initialize(null, resultSet, relevantIndeces);
    }

    /**
     * Create a new dataSet based on an existing ResultSet.
     * @param resultSet ResultSet with values [mandatory]
     * @param relevantIndeces int[] of all columns to add to the dataSet, use
     *            null if all columns should be included.
     * 
     * @throws Exception if the dataset could not be created
     */
    public DataSet(String caption, ResultSet resultSet, int[] relevantIndeces) throws SQLException {
    	this.caption = caption;
        initialize(null, resultSet, relevantIndeces);
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
    public DataSet(String[] columnLabels, String sql, int[] relevantIndeces, Session session) throws SQLException, ExplorerException {
    	SQLConnection connection = null;
    	Statement statement = null;
    	ResultSet resultSet = null;
    	try {
    		connection = session.grabConnection();
    		statement = connection.createStatement();
    		statement.execute(sql);
    		resultSet = statement.getResultSet();
    		initialize(columnLabels, resultSet, relevantIndeces);
    	}finally {
            if (resultSet != null)
            	try {
            		resultSet.close();
            	}catch(SQLException e) {
            		SQLExplorerPlugin.error("Error closing result set", e);
            	}
            if (statement != null)
                try {
                	statement.close();
                } catch (SQLException e) {
                    SQLExplorerPlugin.error("Error closing statement", e);
                }
            if (connection != null)
            	session.releaseConnection(connection);
    	}
    }

    /**
     * Create new dataset based on String[][].
     * 
     * @param columnLabels string[] of columnLabels [mandatory]
     * @param data string[][] with values for dataset [mandatory]
     * @throws Exception if dataSet could not be created
     */
    public DataSet(String[] columnLabels, Comparable[][] data) {
        this(null, columnLabels, data);
    }

    /**
     * Create new dataset based on String[][].
     * @param caption 
     * @param columnLabels string[] of columnLabels [mandatory]
     * @param data string[][] with values for dataset [mandatory]
     * @throws Exception if dataSet could not be created
     */
    public DataSet(String caption, String[] columnLabels, Comparable[][] data) {
    	this.caption = caption;
        columns = convertColumnLabels(columnLabels);

        _rows = new DataSetRow[data.length];

        for (int i = 0; i < data.length; i++)
            _rows[i] = new DataSetRow(this, data[i]);
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
            columns = convertColumnLabels(columnLabels);
        } else {
            columns = new Column[ri.length];
            for (int i = 0; i < ri.length; i++) {
            	int columnIndex = ri[i];
            	columns[i] = createColumn(metadata, columnIndex);
            }
        }

        loadRows(resultSet, ri);
    }
    
    /**
     * Called to create a Column object from the given metadata; this is broken out into
     * a separate method so that database-specific implementations can override it
     * @param metadata
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    protected Column createColumn(ResultSetMetaData metadata, int columnIndex) throws SQLException {
    	int type = metadata.getColumnType(columnIndex);
    	
    	// Numeric - figure out a display format
    	if (type == Types.DECIMAL || type == Types.NUMERIC || type == Types.DOUBLE || type == Types.FLOAT || type == Types.REAL) {
        	int precision = metadata.getPrecision(columnIndex);
        	int scale = metadata.getScale(columnIndex);
        	if (precision < 1)
            	return new FormattedColumn(metadata.getColumnName(columnIndex), true, new DecimalFormat("#.#"));
        	
    		/*
    		 * NOTE: Scale can be negative (although possibly limited to Oracle), but we cope with 
    		 * this by specifing # after the decimal place precision-1 times; eg a precision of 10 
    		 * will return  #.#########
    		 */
        	StringBuffer sb = new StringBuffer(precision + 2);
        	for (int j = 0; j < precision; j++)
        		if (scale < 0 || j < precision - scale - 1)
        			sb.append('#');
        		else
        			sb.append('0');
        	
        	if (scale > 0)
        		sb.insert(precision - scale, '.');
        	else if (scale < 0)
        		sb.insert(1, '.');
        	
        	return new FormattedColumn(metadata.getColumnName(columnIndex), true, new DecimalFormat(sb.toString()));
    	}
    	
    	if (type == Types.DATE || type == Types.TIMESTAMP || type == Types.TIME) {
    		return new FormattedColumn(metadata.getColumnName(columnIndex), false, getDateFormat());
    	}

    	return new Column(metadata.getColumnName(columnIndex), false);
    }
    
    /**
     * Creates an array of Column descriptors from an array of strings
     * @param columnLabels
     * @return
     */
    private Column[] convertColumnLabels(String[] columnLabels) {
    	Column[] result = new Column[columnLabels.length];
    	for (int i = 0; i < columnLabels.length; i++)
    		result[i] = new Column(columnLabels[i], false);
    	return result;
    }

    /**
     * Get the column index for a given column name
     * 
     * @param name
     * @return index of column whose name matches or 0 if none found
     */
    public int getColumnIndex(String name) {
        for (int i = 0; i < columns.length; i++)
            if (columns[i].getCaption().equalsIgnoreCase(name))
                return i;
        return 0;
    }

    /**
     * @return String[] with all column labels
     */
    public Column[] getColumns() {
        return columns;
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

            DataSetRow row = new DataSetRow(this);
            for (int i = 0; i < columns.length; i++) {
            	int columnIndex = relevantIndeces != null ? relevantIndeces[i] : i;
            	Comparable obj = loadCellValue(columnIndex, metadata.getColumnType(columnIndex), resultSet);
                if (resultSet.wasNull())
                    row.setValue(i, null);
                else
                	row.setValue(i, obj);
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
	
	        case Types.BIGINT:
	            return new Long(resultSet.getLong(columnIndex));
		            
	        case Types.DECIMAL:
	        case Types.NUMERIC:
	        case Types.DOUBLE:
	        case Types.FLOAT:
	        case Types.REAL:
	        	int precision = resultSet.getMetaData().getPrecision(columnIndex);
	        	if (precision > 16 || precision < 1)
	        		return resultSet.getBigDecimal(columnIndex);
	            return new Double(resultSet.getDouble(columnIndex));
	
	        case Types.DATE:
	        case Types.TIMESTAMP:                    
	            return resultSet.getTimestamp(columnIndex);
	            
	        case Types.TIME:
	            return resultSet.getTime(columnIndex);
	            
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
	
	private DateFormat getDateFormat() {
		if (formatDates == null)
		    formatDates = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.DATASETRESULT_FORMAT_DATES);
		if (!formatDates)
			return null;
		
		if (dateFormat == null)
			dateFormat = new SimpleDateFormat(
	            SQLExplorerPlugin.getDefault().getPluginPreferences().getString(IConstants.DATASETRESULT_DATE_FORMAT));
		
		return dateFormat;
	}

	public String getCaption() {
		return caption;
	}
}
