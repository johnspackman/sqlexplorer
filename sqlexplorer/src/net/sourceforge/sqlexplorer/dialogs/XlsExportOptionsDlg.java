package net.sourceforge.sqlexplorer.dialogs;

import net.sourceforge.sqlexplorer.Messages;

import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for obtaining XLS export options.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class XlsExportOptionsDlg extends AbstractExportOptionsDlg {

	private static final String[] FILTER = { "*.xls" };

	private static final int FLAGS = FMT_CHARSET | OPT_HDR | OPT_QUOTE
			| OPT_RTRIM;

	public XlsExportOptionsDlg(Shell parentShell) {
		super(parentShell);
	}

	public String[] getFileFilter() {
		return FILTER;
	}

	public int getFlags() {
		return FLAGS;
	}

	public String getMessage() {
		return Messages.getString("ExportDialog.xls.message");
	}

	public String getTitle() {
		return Messages.getString("ExportDialog.xls.title");
	}

}
