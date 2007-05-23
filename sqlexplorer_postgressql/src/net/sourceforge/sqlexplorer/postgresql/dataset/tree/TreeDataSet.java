package net.sourceforge.sqlexplorer.postgresql.dataset.tree;

/**
 * An {@link ITreeDataSet} implementation based on a root node
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TreeDataSet implements ITreeDataSet {

	private final ITreeDataSetNode root;

	private final String[] columnLabels;

	private final String treeColumnLabel;

	/**
	 * Create new tree data set.
	 * 
	 * @param root
	 *            The tree's root node.
	 * @param columnLabels
	 *            The data column labels.
	 * @param treeColumnLabel
	 *            The tree column label.
	 */
	public TreeDataSet(ITreeDataSetNode root, String[] columnLabels,
			String treeColumnLabel) {
		this.root = root;
		this.columnLabels = columnLabels;
		this.treeColumnLabel = treeColumnLabel;
	}

	public String[] getDataColumnLabels() {
		return columnLabels;
	}

	public ITreeDataSetNode getRoot() {
		return root;
	}

	public String getTreeColumnLabel() {
		return treeColumnLabel;
	}

}
