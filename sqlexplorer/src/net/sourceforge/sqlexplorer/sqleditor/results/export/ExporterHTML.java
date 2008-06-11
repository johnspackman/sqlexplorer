package net.sourceforge.sqlexplorer.sqleditor.results.export;

import net.sourceforge.sqlexplorer.Messages;

/**
 * HTML Export
 * @author John Spackman
 *
 */
public class ExporterHTML implements Exporter {

	private static final String[] FILTER = { "*.html", "*.htm" };
	
	public String[] getFileFilter() {
		return FILTER;
	}

	public String getTitle() {
		return Messages.getString("ExportDialog.HTML");
	}

}
