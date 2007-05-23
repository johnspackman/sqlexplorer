package net.sourceforge.sqlexplorer.postgresql.dialogs;

import net.sourceforge.sqlexplorer.postgresql.dataset.tree.ITreeDataSet;
import net.sourceforge.sqlexplorer.postgresql.ui.TreeDataSetViewer;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog to display {@link ITreeDataSet}s.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TreeDataDialog extends TitleAreaDialog {
	private final ITreeDataSet treeDataSet;

	private final String title;

	private final String message;

	/**
	 * Create new dialog.
	 * 
	 * @param shell
	 *            Shell to use.
	 * @param title
	 *            Dialog's title.
	 * @param message
	 *            Dialog's message.
	 * @param treeDataSet
	 *            The tree data set to display.
	 */
	public TreeDataDialog(Shell shell, String title, String message,
			ITreeDataSet treeDataSet) {
		super(shell);
		setShellStyle(SWT.TITLE | SWT.RESIZE);
		this.treeDataSet = treeDataSet;
		this.title = title;
		this.message = message;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		setTitle(title);
		setMessage(message);
		return c;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		TreeDataSetViewer v = new TreeDataSetViewer(parent);
		v.setTreeDataSet(treeDataSet);
		return v;
	}
}
