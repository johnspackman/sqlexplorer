package net.sourceforge.sqlexplorer.sqleditor.results.export;

import net.sourceforge.sqlexplorer.Messages;

/**
 * Handles export to CSV
 * @author John Spackman
 *
 */
public class ExporterCSV implements Exporter {

	private static final String[] FILTER = { "*.csv", "*.txt" };
	
	public String[] getFileFilter() {
		return FILTER;
	}

	public String getTitle() {
		return Messages.getString("ExportDlg.CSV");
	}

}
