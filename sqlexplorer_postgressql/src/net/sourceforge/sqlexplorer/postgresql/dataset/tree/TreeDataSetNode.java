package net.sourceforge.sqlexplorer.postgresql.dataset.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation for {@link ITreeDataSetNode}.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TreeDataSetNode implements ITreeDataSetNode {

	private Object[] props;

	private final Object name;

	private TreeDataSetNode parent = null;

	private final List<TreeDataSetNode> children = new ArrayList<TreeDataSetNode>();

	/**
	 * Create a new node.
	 * 
	 * @param name
	 *            It's name.
	 * @param props
	 *            It's properties, i.e. data.
	 */
	public TreeDataSetNode(Object name, Object[] props) {
		this.props = props;
		this.name = name;
	}

	/**
	 * Add a child to ourselves.
	 * 
	 * @param node
	 *            Child node.
	 * @return The child node just added.
	 */
	private TreeDataSetNode addChild(TreeDataSetNode node) {
		children.add(node);
		node.parent = this;
		return children.get(children.size() - 1);
	}

	/**
	 * Insert a node to the subtree below this node.
	 * 
	 * @param path
	 *            The relative path of the node to be added.
	 * @param node
	 *            The node itself.
	 */
	public void insert(Object[] path, TreeDataSetNode node) {
		insert(0, path, node);
	}

	/**
	 * Really do insert a node. Inserting is recursive, so
	 * {@link #insert(Object[], TreeDataSetNode)} is the public entry point
	 * only.
	 * 
	 * @param level
	 *            The current depth while searching for a place to insert.
	 * @param path
	 *            The node's path.
	 * @param node
	 *            The node to be added.
	 */
	private void insert(int level, Object[] path, TreeDataSetNode node) {
		Object p = level <= path.length - 1 ? path[level] : null;
		if (p == null) {
			/*
			 * given we have A->B->C where B is an intermediate node created
			 * below to insert A->B->C, and now we get just A->B, set B's
			 * properties accordingly instead of adding another B child to B
			 */
			if (name.equals(node.name))
				props = node.props;
			else
				addChild(node);
			return;
		}
		int i = 0;
		for (i = 0; i < children.size(); i++) {
			if (children.get(i).name.equals(path[level])) {
				children.get(i).insert(level + 1, path, node);
				break;
			}
		}
		if (i == children.size()) {
			if (level == path.length - 1) {
				addChild(node);
			} else {
				addChild(new TreeDataSetNode(path[level], null)).insert(
						level + 1, path, node);
			}
		}
	}

	public ITreeDataSetNode[] getChildren() {
		if (children.size() == 0)
			return null;
		return children.toArray(new ITreeDataSetNode[children.size()]);
	}

	public Object[] getData() {
		return props;
	}

	public Object getName() {
		return name;
	}

	public ITreeDataSetNode getParent() {
		return parent;
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

}
