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
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Comparator;

import org.eclipse.swt.SWT;


/**
 * DataSetTableSorter. 
 * 
 * @author Davy Vanherbergen
 */
public class DataSetTableSorter implements Comparator {
   
    
    public DataSetTableSorter(DataSet dataSet) {
        
        _columnTypes = dataSet.getColumnTypes();        
        _priorities = new int[_columnTypes.length];
        _directions = new int[_columnTypes.length];

        for (int i = 0; i < _columnTypes.length; i++) {
        	_directions[i] = SWT.NONE;
            _priorities[i] = i;
        }

    }
    

    
    protected int[] _priorities;

    protected int[] _directions;

    protected int _columnTypes[];


    /**
     * @param column
     */
    public void setTopPriority(int priority, int direction) {

        if (priority < 0 || priority >= _priorities.length)
            return;

        int index = -1;
        for (int i = 0; i < _priorities.length; i++) {
            if (_priorities[i] == priority) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            return;
        }

        // shift the array
        for (int i = index; i > 0; i--) {
            _priorities[i] = _priorities[i - 1];
        }
        _priorities[0] = priority;
        _directions[priority] = direction;
    }





    public int compare(Object e1, Object e2) {
        return compareColumnValue((DataSetRow) e1, (DataSetRow) e2, 0);
    }


    private int compareColumnValue(DataSetRow m1, DataSetRow m2, int depth) {
        if (depth >= _priorities.length)
            return 0;

        int columnNumber = _priorities[depth];
        int direction = _directions[columnNumber];
        int result = 0;

        // check for null values
        Object o1 = m1.getObjectValue(columnNumber);
        Object o2 = m2.getObjectValue(columnNumber);        
        
        // sort based on null values
    	if (o1 == null || o2 == null) {
    		if (o1 == null && o2 != null) {
    			result = 1;
    		} else if (o1 != null && o2 == null) {
    			result = -1;
    		} else {
    			result = 0;
    		}
    		
            if (result == 0) {
                return compareColumnValue(m1, m2, depth + 1);
            }
            
            if (direction == SWT.DOWN) {
            	return result * -1;
            }
            return result;
    	}
        
    	
    	// sort on non-null values
    	
        switch (_columnTypes[columnNumber]) {
            
        	case DataSet.TYPE_STRING:            	
            	result = ((String)o1).compareTo((String)o2);
                break;
                
            case DataSet.TYPE_DOUBLE:
            	result = ((Double)o1).compareTo((Double)o2);
                break;

            case DataSet.TYPE_LONG:                
            case DataSet.TYPE_INTEGER:
            	result = ((Long)o1).compareTo((Long)o2);
                break;               
            
            case DataSet.TYPE_DATE:
            	result = ((Date)o1).compareTo((Date)o2);
                break;               

            case DataSet.TYPE_DATETIME:
            	result = ((Timestamp)o1).compareTo((Timestamp)o2);
                break;               

            case DataSet.TYPE_TIME:
            	result = ((Time)o1).compareTo((Time)o2);
                break;   
        }

        if (result == 0) {
            return compareColumnValue(m1, m2, depth + 1);
        }
        
        if (direction == SWT.DOWN) {
        	return result * -1;
        }
        return result;
    }

}
