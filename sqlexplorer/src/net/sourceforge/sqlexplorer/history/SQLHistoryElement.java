package net.sourceforge.sqlexplorer.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.TextUtil;

/**
 * SQLHistoryElement represents a single entry in the SQLHistoryView.
 */
public class SQLHistoryElement {

    private int _executionCount = 1;

    private String _formattedTime;

    private String _rawSQLString;

    private String _searchableString;

    private String _sessionName;

    private String _singleLineText;

    private long _time;

    private static SimpleDateFormat _dateFormatter = new SimpleDateFormat(
            SQLExplorerPlugin.getDefault().getPluginPreferences().getString(IConstants.DATASETRESULT_DATE_FORMAT));


    public SQLHistoryElement(String rawSQLString, String sessionName) {

        _rawSQLString = rawSQLString;
        _sessionName = sessionName;
        _time = System.currentTimeMillis();
        intialize();
    }


    public SQLHistoryElement(String rawSQLString, String sessionName, String time, String executions) {

        _rawSQLString = rawSQLString;
        _sessionName = sessionName;

        if (time != null && time.length() != 0) {
            _time = Long.parseLong(time);
        } else {
            _time = System.currentTimeMillis();
        }

        if (executions != null && executions.length() != 0) {
            _executionCount = Integer.parseInt(executions);
        } else {
            _executionCount = 1;
        }

        intialize();
    }


    /**
     * Check if the current element matches a given sql string
     * 
     * @param rawSQL original sql statement to compare too.
     * @return true rawSQL matches this element
     */
    public boolean equals(String rawSQL) {

        return TextUtil.removeLineBreaks(rawSQL).equals(_singleLineText);
    }


    /**
     * @return number of times this statement was executed
     */
    public int getExecutionCount() {

        return _executionCount;
    }


    public String getFormattedTime() {

        return _formattedTime;
    }


    /**
     * @return unformatted sql string
     */
    public String getRawSQLString() {

        return _rawSQLString;
    }


    public String getSearchableString() {

        return _searchableString;
    }


    /**
     * @return name of session under which the statement was executed
     */
    public String getSessionName() {

        return _sessionName;
    }


    /**
     * Return all text without newline separators.
     */
    public String getSingleLineText() {

        return _singleLineText;
    }


    public long getTime() {

        return _time;
    }


    /**
     * increase execution count by 1 and reset the timestamp to the current
     * time.
     */
    public void increaseExecutionCount() {

        _executionCount++;
        _time = System.currentTimeMillis();
        intialize();
    }


    /**
     * initialize our search string immediately, this allows for very fast
     * searching in the history view
     */
    private void intialize() {

        _formattedTime = _dateFormatter.format(new Date(_time));
        _searchableString = (_rawSQLString + " " + _sessionName + " " + _formattedTime).toLowerCase();
        _singleLineText = TextUtil.removeLineBreaks(_rawSQLString);
    }


    /**
     * @param sessionName new SessionName for this element
     */
    public void setSessionName(String sessionName) {

        _sessionName = sessionName;
        intialize();
    }
}
