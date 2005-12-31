package net.sourceforge.sqlexplorer;


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
public class MultiLineString {

    private String _originalText;
    
    private static final String NEWLINE_SEPARATOR = "\n";
    
    private static final String NEWLINE_EXPR = "\\n";
    
    private static final String RETURN_EXPR = "\\r";
    
    public static final int DEFAULT_WRAPLENGTH = 150;
    
    /**
     * Create new wrapper for a given text string.
     * 
     * @param text String
     */
    public MultiLineString(String text) {
        _originalText = text;
    }
    
    /**
     * Return all text without newline separators.
     */
    public String getSingleLineText() {
        return _originalText.replaceAll(NEWLINE_EXPR, " ").replaceAll(RETURN_EXPR, "");
    }
    
    /**
     * Return the text used to create this wrapper.
     */
    public String getOriginalText() {
        return _originalText;
    }
    
    /**
     * Return the text reformatted to have a max charwidth of maxWidth.
     * @param maxWidth number of chars that the text can be wide.
     */
    public String getMultiLineText(int maxWidth) {
        
        String[] text = _originalText.split(NEWLINE_EXPR);
        String wrappedText = "";
        
        for (int i = 0; i < text.length; i++) {
            
            text[i] = text[i].replaceAll(RETURN_EXPR, "");
            
            if (text[i].length() == 0) {
                continue;
            }
            
            if (text[i].length() <= maxWidth) {
                wrappedText += text[i];
                
                if (i < text.length - 1) {
                    wrappedText += NEWLINE_SEPARATOR;
                }
            } else {                
                
                String tmp = text[i];
                
                while (tmp.length() > maxWidth) {
                    
                    for (int j = tmp.length() - 1; j >= 0; j--) {
                        
                        if (j < maxWidth) {
                            
                            char c = text[i].charAt(j); 
                            if (c == ' ' || c == ',') {
                                wrappedText += tmp.substring(0, j);
                                wrappedText += NEWLINE_SEPARATOR;
                                tmp = tmp.substring(j + 1);
                                break;
                            }
                        }
                        
                        if (j == 0) {
                            wrappedText += tmp.substring(0, j);
                            tmp = "";
                            break;
                        }
                    }
                    
                }
                
                wrappedText += tmp;
                wrappedText += NEWLINE_SEPARATOR;
            }            
            
        }        
        
        return wrappedText;
    }
    
    
    public String toString() {
        return getSingleLineText();
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {

        if (!(arg0 instanceof MultiLineString)) {
            return false;
        }
        MultiLineString otherString = (MultiLineString) arg0;
        return otherString.getSingleLineText().equals(getSingleLineText());
    }
    
    
}
