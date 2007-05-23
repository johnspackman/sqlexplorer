package net.sourceforge.sqlexplorer.postgresql.dataset.tree;

/**
 * Interface of a node within an {@link ITreeDataSet}. Each node simply has a
 * name within the hierarchy (on its particular level) and data assigned.
 * Intermediate nodes can have data, too. And intermediate node is created for
 * 'B' when inserting two nodes with paths 'A,B,C' and 'A,B,D' for example.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public interface ITreeDataSetNode {
	/**
	 * Get this node's name on the current level. This is supposed to be unique.
	 * 
	 * @return Name.
	 */
	public Object getName();

	/**
	 * Get this node's data. Intermediate nodes can have data, too.
	 * 
	 * @return Data.
	 */
	public Object[] getData();

	/**
	 * Get this node's parent node.
	 * 
	 * @return Parent or <tt>null</tt> if none.
	 */
	public ITreeDataSetNode getParent();

	/**
	 * Get this node's children.
	 * 
	 * @return Children or <tt>null</tt> if none.
	 */
	public ITreeDataSetNode[] getChildren();

	/**
	 * Test whether this node has children. This is to be preferred over testing
	 * {@link #getChildren()} returning an array with positive member count.
	 * 
	 * @return Yes/No.
	 */
	public boolean hasChildren();
}
