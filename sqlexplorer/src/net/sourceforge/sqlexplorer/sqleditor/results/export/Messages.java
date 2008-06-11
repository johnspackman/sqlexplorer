package net.sourceforge.sqlexplorer.sqleditor.results.export;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.sourceforge.sqlexplorer.sqleditor.results.export.messages"; //$NON-NLS-1$

	public static String ExportDlg_CharacterSet;

	public static String ExportDlg_Choose;

	public static String ExportDlg_Delimiter;

	public static String ExportDlg_Destination;

	public static String ExportDlg_Format;

	public static String ExportDlg_Headers;

	public static String ExportDlg_null;

	public static String ExportDlg_NullValue;

	public static String ExportDlg_Options;

	public static String ExportDlg_QuoteTextValues;

	public static String ExportDlg_RightTrimValues;

	public static String ExportDlg_Title;

	public static String ExportDlg_TitleMessage;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
