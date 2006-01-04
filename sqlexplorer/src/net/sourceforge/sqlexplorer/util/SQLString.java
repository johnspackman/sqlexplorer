package net.sourceforge.sqlexplorer.util;


/**
 * Utility class to wrap long text into multiple lines,
 * or to combine multiple lines into a single line.
 * 
 * This class is used to wrap the long sql statements
 * in the sql history and tooltip displays.
 * 
 * @author Davy Vanherbergen
 *
 */
public class SQLString {

    private String _text;
       
    /**
     * Create new wrapper for a given text string.
     * 
     * @param text String
     */
    public SQLString(String text) {
        _text = text;
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

    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {

        if (!(arg0 instanceof SQLString)) {
            return false;
        }
        SQLString otherString = (SQLString) arg0;
        return otherString.getSingleLineText().equals(getSingleLineText());
    }
    
    
}
