package net.sourceforge.sqlexplorer.sqleditor.results.export;

import java.util.Iterator;
import java.nio.charset.Charset;
import java.util.SortedMap;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import com.swtdesigner.ResourceManager;

/**
 * Dialog for Exporting.  Modified from the original to a) handle the task of exporting
 * instead of just getting values, and b) use Eclipse 3.2 string externalisation to allow
 * SWT designer etc to present descriptions at design time.
 * 
 * The Export to XLS option has been removed because it was simply outputting an HTML table
 * which Excel could read, and therefore provided little benefit over CSV.  The previous
 * version used flags to indicate which fields were required, but the only difference in
 * usage was that CSV asked for delimiters but HTML and XLS did not.  The dialog now takes
 * responsibility for asking for the desired export format and modifies questions to suit.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * @auther John Spackman <a href="mailto:john.spackman@zenesis.com">john.spackman@zenesis.com</a>
 * 
 */
public class ExportDlg extends TitleAreaDialog {

	private Combo uiCharset;

	private Combo uiDelim;

	private Text uiNullValue;

	private Button uiIncHeaders;

	private Button uiQuoteText;

	private Button uiRtrim;

	private Text uiFile;

	private String charset;

	private String delim;

	private String nullValue;

	private boolean incHeaders;

	private boolean quoteText;

	private boolean rtrim;

	private String file;

	private ExporterCSV exporter;

	private static final String[] DELIMS = { ";", "|", "\\t [TAB]", "," }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/**
	 * Create new base dialog.
	 * 
	 * @param parentShell
	 *            Parent's shell.
	 */
	public ExportDlg(Shell parentShell) {
		super(parentShell);
	}

	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(Messages.ExportDlg_Title);
		setMessage(Messages.ExportDlg_TitleMessage);
		setTitleImage(ResourceManager.getPluginImage(SQLExplorerPlugin.getDefault(), "icons/ExportDataLarge.png"));
		return contents;
	}

	protected Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new FillLayout(SWT.VERTICAL));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		String columnSeparator = SQLExplorerPlugin.getDefault().getPreferenceStore().getString(IConstants.CLIP_EXPORT_SEPARATOR);
		boolean hdr = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.CLIP_EXPORT_COLUMNS);

		Label l = null;

		Group fmtGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
		fmtGroup.setText(Messages.ExportDlg_Format);
		fmtGroup.setLayout(new GridLayout(2, false));
		int i = 0, def = 0;

		l = new Label(fmtGroup, SWT.NONE);
		l.setText(Messages.ExportDlg_CharacterSet);
		uiCharset = new Combo(fmtGroup, SWT.READ_ONLY);
		SortedMap m = Charset.availableCharsets();
		for (Iterator it = m.keySet().iterator(); it.hasNext(); i++) {
			Charset cs = (Charset) m.get(it.next());
			uiCharset.add(cs.displayName());
			if (cs.displayName().toLowerCase().equals("utf-8")) //$NON-NLS-1$
				def = i;
		}
		uiCharset.select(def);

		l = new Label(fmtGroup, SWT.NONE);
		l.setText(Messages.ExportDlg_Delimiter);
		uiDelim = new Combo(fmtGroup, SWT.NONE);
		for (i = 0, def = 0; i < DELIMS.length; i++) {
			uiDelim.add(DELIMS[i]);
			if (DELIMS[i].toLowerCase().equals(columnSeparator))
				def = i;
		}
		uiDelim.select(def);
		
		l = new Label(fmtGroup, SWT.NONE);
		l.setText(Messages.ExportDlg_NullValue);
		uiNullValue = new Text(fmtGroup, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		uiNullValue.setText(Messages.ExportDlg_null);
		uiNullValue.setLayoutData(new GridData(50, SWT.DEFAULT));

		Group optionsGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
		optionsGroup.setText(Messages.ExportDlg_Options);
		optionsGroup.setLayout(new GridLayout(1, true));

		uiIncHeaders = new Button(optionsGroup, SWT.CHECK);
		uiIncHeaders.setText(Messages.ExportDlg_Headers);
		uiIncHeaders.setSelection(hdr);

		uiQuoteText = new Button(optionsGroup, SWT.CHECK);
		uiQuoteText.setText(Messages.ExportDlg_QuoteTextValues);

		uiRtrim = new Button(optionsGroup, SWT.CHECK);
		uiRtrim.setText(Messages.ExportDlg_RightTrimValues);

		Group fileGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
		fileGroup.setText(Messages.ExportDlg_Destination);
		fileGroup.setLayout(new GridLayout(2, false));

		uiFile = new Text(fileGroup, SWT.BORDER | SWT.FILL | SWT.SINGLE);
		uiFile.setLayoutData(new GridData(300, SWT.DEFAULT));
		uiFile.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				sync();
			}

			public void keyReleased(KeyEvent e) {
				sync();
			}
		});
		Button choose = new Button(fileGroup, SWT.NONE);
		choose.setText(Messages.ExportDlg_Choose);
		choose.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				chooseFilename();
			}
		});

		comp.pack();
		sync();
		return comp;
	}

	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		/*
		 * interupt dialog setup to force Ok button to disabled since filename
		 * field is empty.
		 */
		sync();
		return c;
	}

	/**
	 * Toggle accessibility of Ok button depending on whether all input is
	 * given. This currently only depends on the filename being present.
	 */
	private void sync() {
		String filename = uiFile.getText();
		if (filename == null || filename.trim().length() == 0)
			setErrorMessage(Messages.ExportDlg_Destination);
		else
			setErrorMessage(null);
		Button ok = getButton(IDialogConstants.OK_ID);
		if (ok != null)
			ok.setEnabled(filename != null && filename.trim().length() != 0);
	}

	private void chooseFilename() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
		fileDialog.setFilterExtensions(exporter.getFileFilter());

		final String fileName = fileDialog.open();
		if (fileName != null && fileName.trim().length() != 0) {
			uiFile.setText(fileName);
		}
		sync();
	}

	protected void okPressed() {
		charset = uiCharset != null ? uiCharset.getText() : null;
		delim = uiDelim != null ? uiDelim.getText() : null;
		incHeaders = uiIncHeaders != null && uiIncHeaders.getSelection();
		quoteText = uiQuoteText != null && uiQuoteText.getSelection();
		rtrim = uiRtrim != null && uiRtrim.getSelection();
		file = uiFile != null ? uiFile.getText() : null;
		nullValue = uiNullValue != null ? uiNullValue.getText() : null;
		super.okPressed();
	}

	/**
	 * Return chosen character set. The underlying list is obtained from
	 * Character set, i.e. it's valid.
	 * 
	 * @return Character set or <tt>null</tt> if not requested.
	 */
	public String getCharacterSet() {
		return charset;
	}

	/**
	 * Return chosen delimiter.
	 * 
	 * @return Delimiter or <tt>null</tt> if not requested.
	 */
	public String getDelimiter() {
		if (delim == null)
			return null;
		if (delim.toLowerCase().startsWith("\\t")) //$NON-NLS-1$
			return "\t"; //$NON-NLS-1$
		return delim;
	}

	/**
	 * Return whether to include column headers.
	 * 
	 * @return Whether to include column headers or <tt>false</tt> if not
	 *         requested.
	 */
	public boolean includeHeaders() {
		return incHeaders;
	}

	/**
	 * Return whether to quote text values.
	 * 
	 * @return Whether to quote text values or <tt>false</tt> if not
	 *         requested.
	 */
	public boolean quoteText() {
		return quoteText;
	}

	/**
	 * Return whether to right-trim spaces.
	 * 
	 * @return Whether to right-trim spaces or <tt>false</tt> if not
	 *         requested.
	 */
	public boolean trimSpaces() {
		return rtrim;
	}

	/**
	 * Return chosen filename.
	 * 
	 * @return Filename
	 */
	public String getFilename() {
		return file;
	}
	
	/**
	 * Return chosen null value replacement string.
	 * @return String or <tt>null</tt> if not requested.
	 */
	public String getNullValue() {
		return nullValue;
	}
}