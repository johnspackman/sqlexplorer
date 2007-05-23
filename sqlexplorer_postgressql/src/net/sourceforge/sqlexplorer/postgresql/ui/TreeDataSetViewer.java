package net.sourceforge.sqlexplorer.postgresql.ui;

import net.sourceforge.sqlexplorer.postgresql.dataset.tree.ITreeDataSet;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * UI widget to display {@link ITreeDataSet}s.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TreeDataSetViewer extends Composite {

	private Tree tree;

	private TreeViewer viewer;

	private IBaseLabelProvider labelProvider = new TreeDataSetLabelProvider();

	public TreeDataSetViewer(Composite parent) {
		super(parent, SWT.NONE);
	}

	public TreeDataSetViewer(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Set this viewer's tree data set. This is not really safe to set multiple
	 * times.
	 * 
	 * @param treeDataSet
	 *            New tree data set.
	 */
	public void setTreeDataSet(ITreeDataSet treeDataSet) {
		if (tree != null)
			tree.dispose();

		GridData layout = new GridData();
		layout.grabExcessHorizontalSpace = true;
		layout.grabExcessVerticalSpace = true;
		layout.horizontalAlignment = GridData.FILL;
		layout.verticalAlignment = GridData.FILL;
		layout.horizontalSpan = 1;

		setLayout(new FillLayout());
		setLayoutData(layout);

		tree = new Tree(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer = new TreeViewer(tree);

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		TreeColumn tc = new TreeColumn(tree, SWT.LEFT);
		tc.setText(treeDataSet.getTreeColumnLabel());

		String[] hdr = treeDataSet.getDataColumnLabels();
		for (int i = 0; i < hdr.length; i++) {
			tc = new TreeColumn(tree, SWT.LEFT);
			tc.setText(hdr[i]);
		}

		viewer.setContentProvider(new TreeDataSetContentProvider());
		viewer.setUseHashlookup(true);
		viewer.setLabelProvider(labelProvider);
		viewer.setInput(treeDataSet.getRoot());
		viewer.refresh();

		for (int i = 0; i < tree.getColumnCount(); i++)
			tree.getColumn(i).pack();

		this.layout();
		this.redraw();
	}

	/**
	 * Get underlying tree object.
	 * @return {@link Tree} instance.
	 */
	public Tree getTree() {
		return tree;
	}
	
	/**
	 * Get underlying tree viewer object.
	 * @return {@link TreeViewer} instance.
	 */
	public TreeViewer getTreeViewer() {
		return viewer;
	}
}
