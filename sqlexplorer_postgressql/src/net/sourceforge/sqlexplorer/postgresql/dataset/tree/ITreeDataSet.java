package net.sourceforge.sqlexplorer.postgresql.dataset.tree;

/**
 * Interface for hierarchical data sets. Most logic (i.e. which makes up the
 * actual tree) is in {@link ITreeDataSetNode} so that this class is supposed to
 * more or less a container only.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public interface ITreeDataSet {
	/**
	 * Get tree column label. This label specifies the text to be displayed in
	 * the hierarchy column.
	 * 
	 * @return Tree column label.
	 */
	public String getTreeColumnLabel();

	/**
	 * Get data column labels.
	 * 
	 * @return Data column labels.
	 */
	public String[] getDataColumnLabels();

	/**
	 * Get tree's root node.
	 * 
	 * @return Root or <tt>null</tt> if none.
	 */
	public ITreeDataSetNode getRoot();
}
