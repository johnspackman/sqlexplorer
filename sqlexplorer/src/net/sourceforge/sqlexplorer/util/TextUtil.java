package net.sourceforge.sqlexplorer.util;


/**
 * Text handling utility.
 * 
 * @author Davy Vanherbergen
 */
public class TextUtil {

    public static final int DEFAULT_WRAPLENGTH = 150;
    
    private static final String NEWLINE_SEPARATOR = "\n";
    
    private static final String NEWLINE_EXPR = "\\n";
    
    private static final String RETURN_EXPR = "\\r";
    
    private static final String TAB_EXPR = "\\t";
    
    
    /**
     * Clear all linebreaks and carriage returns from input text.
     * @return cleaned string
     */
    public static String removeLineBreaks(String input) {
        if (input == null) {
            return null;
        }
        String tmp = input.replaceAll(NEWLINE_EXPR, " ");
        tmp = tmp.replaceAll(TAB_EXPR, " ");
        return tmp.replaceAll(RETURN_EXPR, "");
    }
    
    
    
    /**
     * Return the text reformatted to have a max charwidth of maxWidth.
     * @param maxWidth number of chars that the text can be wide.
     */
    public static String getWrappedText(String input) {
        return getWrappedText(input, DEFAULT_WRAPLENGTH);
    }
    
    /**
     * Return the text reformatted to have a max charwidth of maxWidth.
     * @param maxWidth number of chars that the text can be wide.
     */
    public static String getWrappedText(String input, int maxWidth) {
              
        if (input == null) {
            return "";
        }
        
        String[] text = input.split(NEWLINE_EXPR);
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
                            if (c == ',') {
                                wrappedText += tmp.substring(0, j + 1);
                                wrappedText += NEWLINE_SEPARATOR;
                                tmp = tmp.substring(j + 1);
                                break;
                            }
                            if (c == ' ') {
                                wrappedText += tmp.substring(0, j + 1);
                                wrappedText += NEWLINE_SEPARATOR;
                                tmp = tmp.substring(j + 1);
                                break;
                            }
                        }
                        
                        if (j == 0) {
                            wrappedText += tmp.substring(0, maxWidth + 1);
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
    

    /**
     * Replace all occurrences of replaceFrom in inputString with replaceTo.
     * 
     * @param inputString string to update
     * @param replaceFrom occurrences to replace
     * @param replaceTo string that replaces occurrences
     * @return
     */
    public static String replaceChar(String inputString, char replaceFrom, String replaceTo) {

        if (inputString == null || inputString.length() == 0) {
            return inputString;
        }
        
        StringBuffer buffer = new StringBuffer();                
        char[] input = inputString.toCharArray();
            
        for (int i = 0; i < input.length; i++) {
            
            if (input[i] == replaceFrom) {
                buffer.append(replaceTo);
            } else {
                buffer.append(input[i]);
            }                
        }            

        return buffer.toString();
    }
    
    
}
