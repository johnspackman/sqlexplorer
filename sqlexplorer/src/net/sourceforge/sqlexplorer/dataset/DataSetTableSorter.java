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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;


/**
 * DataSetTableSorter. Sorts dataset table.  Based on original SQLTableSorter from 
 * Andrea Mazzolini.
 * 
 * @author Davy Vanherbergen
 */
public class DataSetTableSorter extends ViewerSorter {

    
    public final static int SORT_ASCENDING = 1;

    public final static int SORT_DEFAULT = 0;

    public final static int SORT_DESCENDING = -1;

    
    
    public DataSetTableSorter(DataSet dataSet) {
        
        _columnTypes = dataSet.getColumnTypes();        
        _priorities = new int[_columnTypes.length];
        _directions = new int[_columnTypes.length];
        _defaultDirections = new int[_columnTypes.length];
        _defaultPriorities = new int[_columnTypes.length];

        for (int i = 0; i < _columnTypes.length; i++) {
            _defaultDirections[i] = SORT_DEFAULT;
            _defaultPriorities[i] = i;
        }

        resetState();
    }
    

    
    protected int[] _priorities;

    protected int[] _directions;

    protected int[] _defaultDirections;

    protected int[] _defaultPriorities;

    protected int _columnTypes[];


    /**
     * @param column
     */
    public void setTopPriority(int priority) {

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
            resetState();
            return;
        }

        // shift the array
        for (int i = index; i > 0; i--) {
            _priorities[i] = _priorities[i - 1];
        }
        _priorities[0] = priority;
        _directions[priority] = SORT_ASCENDING;
    }


    public void resetState() {
        _priorities = _defaultPriorities;
        _directions = _defaultDirections;
    }


    /**
     * 
     */
    public int reverseTopPriority() {
        
        if (_directions[_priorities[0]] == SORT_DEFAULT) {
            _directions[_priorities[0]] = SORT_ASCENDING;
        } else {
            _directions[_priorities[0]] *= -1;    
        }
        
        return _directions[_priorities[0]];
    }


    /**
     * @return int
     */
    public int getTopPriority() {
        return _priorities[0];
    }


    public int compare(Viewer viewer, Object e1, Object e2) {
        return compareColumnValue((DataSetRow) e1, (DataSetRow) e2, 0);
    }


    private int compareColumnValue(DataSetRow m1, DataSetRow m2, int depth) {
        if (depth >= _priorities.length)
            return 0;

        int columnNumber = _priorities[depth];
        int direction = _directions[columnNumber];
        int result = 0;
        String v1 = m1.getStringValue(columnNumber);
        String v2 = m2.getStringValue(columnNumber);

        switch (_columnTypes[columnNumber]) {
            case DataSet.TYPE_STRING:
                result = collator.compare(m1.getStringValue(columnNumber), m2.getStringValue(columnNumber));
                break;
                
            case DataSet.TYPE_DOUBLE:
                double d1 = 0;
                double d2 = 0;
                try {
                    d1 = Double.parseDouble(v1);
                } catch (Exception e) {
                }
                try {
                    d2 = Double.parseDouble(v2);
                } catch (Exception e) {
                }
                if (d1 == d2)
                    result = 0;
                else if (d1 > d2)
                    result = 1;
                else
                    result = -1;
                break;
            case DataSet.TYPE_INTEGER:
                v1 = m1.getStringValue(columnNumber);
                v2 = m2.getStringValue(columnNumber);

                long l1 = 0;
                long l2 = 0;
                try {
                    l1 = Long.parseLong(v1);
                } catch (Exception e) {
                }
                try {
                    l2 = Long.parseLong(v2);
                } catch (Exception e) {
                }
                if (l1 == l2) {
                    result = 0;
                } else if (l1 > l2) {
                    result = 1;
                } else {
                    result = -1;
                }
                break;
                
            
            case DataSet.TYPE_DATE:
                try {
                    Date dt1 = (Date) m1.getObjectValue(columnNumber);
                    Date dt2 = (Date) m2.getObjectValue(columnNumber);
                    if (dt1 == null && dt2 == null) {
                        result = 0;
                    } 
                    if (dt2 == null) {
                        result = 1;
                    } else if (dt1 == null) {
                        result = -1;
                    } else {
                        result = dt1.compareTo(dt2);
                    }
                } catch (Exception e) {

                }

                break;
            case DataSet.TYPE_DATETIME:
                try {
                    Timestamp t1 = (Timestamp) m1.getObjectValue(columnNumber);
                    Timestamp t2 = (Timestamp) m2.getObjectValue(columnNumber);
                    if (t1 == null && t2 == null) {
                        result = 0;
                    }
                    if (t2 == null) {
                        result = 1;
                    } else if (t1 == null) {
                        result = -1;
                    } else {
                        result = t1.compareTo(t2);
                    }
                } catch (Exception e) {

                }
                break;
            case DataSet.TYPE_TIME:
                try {
                    Time t11 = (Time) m1.getObjectValue(columnNumber);
                    Time t22 = (Time) m2.getObjectValue(columnNumber);
                    if (t11 == null && t22 == null) {
                        result = 0;
                    }
                    if (t22 == null) {
                        result = 1;
                    } else if (t11 == null) {
                        result = -1;
                    } else {
                        result = t11.compareTo(t22);
                    }
                } catch (Exception e) {

                }
                break;
        }

        if (result == 0) {
            return compareColumnValue(m1, m2, depth + 1);
        }
        return result * direction;
    }

}
