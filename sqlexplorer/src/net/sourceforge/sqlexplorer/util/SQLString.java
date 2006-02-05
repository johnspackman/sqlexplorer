package net.sourceforge.sqlexplorer.util;

/**
 * Utility class to wrap long text into multiple lines, or to combine multiple
 * lines into a single line.
 * 
 * This class is used to wrap the long sql statements in the sql history and
 * tooltip displays.
 * 
 * @author Davy Vanherbergen
 * 
 */
public class SQLString {

    private String _sessionName;

    private String _text;


    /**
     * Create new wrapper for a given text string.
     * 
     * @param sql query string
     * @param sessionName name of the session in which this query was/is
     *            executed.
     */
    public SQLString(String sql, String sessionName) {
        _text = sql;
        _sessionName = sessionName;
    }


    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {

        if (!(arg0 instanceof SQLString)) {
            return false;
        }
        SQLString otherString = (SQLString) arg0;
        return otherString.getSingleLineText().equals(getSingleLineText());
    }


    public String getSessionName() {
        return _sessionName;
    }


    /**
     * Return all text without newline separators.
     */
    public String getSingleLineText() {
        return TextUtil.removeLineBreaks(_text);
    }


    /**
     * Return the text used to create this wrapper.
     */
    public String getText() {
        return _text;
    }


    public String toString() {
        return getSingleLineText();
    }

}
