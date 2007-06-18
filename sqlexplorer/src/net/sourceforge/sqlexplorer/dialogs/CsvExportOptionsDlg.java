package net.sourceforge.sqlexplorer.dialogs;

import net.sourceforge.sqlexplorer.Messages;

import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for obtaining CSV export options.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class CsvExportOptionsDlg extends AbstractExportOptionsDlg {

	private static final String[] FILTER = { "*.csv", "*.txt" };

	private static final int FLAGS = FMT_CHARSET | FMT_DELIM | FMT_NULL
			| OPT_HDR | OPT_QUOTE | OPT_RTRIM;

	public CsvExportOptionsDlg(Shell parentShell) {
		super(parentShell);
	}

	public String[] getFileFilter() {
		return FILTER;
	}

	public int getFlags() {
		return FLAGS;

	}

	public String getMessage() {
		return Messages.getString("ExportDialog.csv.message");
	}

	public String getTitle() {
		return Messages.getString("ExportDialog.csv.title");
	}

}
