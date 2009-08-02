package net.sourceforge.sqlexplorer.sqleditor.results.export;

import java.io.File;
import java.io.PrintStream;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.sqleditor.results.CellRangeRow;
import net.sourceforge.sqlexplorer.sqleditor.results.ResultProvider;
import net.sourceforge.sqlexplorer.util.TextUtil;

/**
 * XLS (HTML) Export
 * @author Heiko
 *
 */
public class ExporterXLS implements Exporter {

	private static final String[] FILTER = { "*.xls"};
	
	public String[] getFileFilter() {
		return FILTER;
	}

	public String getFormatName() {
		return Messages.getString("ExportDlg.XLS");
	}

	public int getFlags() {
		return FMT_CHARSET | FMT_NULL | OPT_HDR | OPT_RTRIM;
	}

	public void export(ResultProvider data, ExportOptions options, File file) throws Exception
	{
        PrintStream writer = new PrintStream(file, options.characterSet); 
        
        // get column header and separator preferences
        boolean includeColumnNames = options.includeColumnNames;
        boolean rtrim = options.rtrim;
        boolean quote = options.quote;
        String nullValue = options.nullValue;
                                   
        int columnCount = data.getNumberOfColumns();
        
        writer.println("<table>");
        
        // export column names
        if (includeColumnNames) {
        	writer.print("<tr>");
            for (int i = 0; i < columnCount; i++) 
            {
            	writer.print("<th>");
                writer.print(data.getColumn(i).getCaption());
                writer.print("</th>");
                
            }
        	writer.println("</tr>");
        }
        // export column data
        for (CellRangeRow row : data.getRows()) 
        {
        	writer.print("<tr>");
                                   
            for (int j = 0; j < columnCount; j++) 
            {
            	Object o = row.getCellValue(j);
                String t = o == null ? nullValue : o.toString();
            	if (rtrim)
            	{
            		t = TextUtil.rtrim(t);
            	}

            	writer.print("<td>");
            	if (quote && o instanceof String) 
            	{
            		t = TextUtil.quote(t);
            	}
            	writer.print(TextUtil.htmlEscape(t));
                writer.print("</td>");
            }
        	writer.println("</tr>");
        }

        writer.println("</table>");

        writer.close();
		
	}
	
}
