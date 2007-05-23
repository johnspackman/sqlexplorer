package net.sourceforge.sqlexplorer.postgresql.actions.explain;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.postgresql.dataset.tree.ITreeDataSetNode;

/**
 * Node of an explain tree.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class ExplainNode implements ITreeDataSetNode {

	private double costGuessedSelf = 0;

	private double costGuessedTotal = 0;

	private double costActualSelf = 0;

	private double costActualTotal = 0;

	private String action;

	private String info;

	private List<ExplainNode> children = new ArrayList<ExplainNode>();

	private int level;

	private ExplainNode parent;

	private final int type;

	public ExplainNode(int type) {
		this.type = type;
	}

	/**
	 * Get node's action.
	 * 
	 * @return Some pretty-printed string that can be returned to the user.
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Set node's action
	 * 
	 * @param action
	 *            Some pretty-printed string that can be returned to the user.
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * Add some detail info for this node. PostgreSQL provides things like
	 * filter conditions on joins and scans, etc.
	 * 
	 * @param line
	 *            Input line. If the node already has info, it's appended.
	 */
	public void addInfo(String line) {
		if (info == null)
			info = line;
		else
			info += ", " + line;
	}

	/**
	 * Get additional info for this node. PostgreSQL provides things like filter
	 * conditions for joins and scans, etc.
	 * 
	 * @return Either <tt>null</tt> or a pretty-printed string that can be
	 *         returned to the user.
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * Add a child node making this its parent.
	 * 
	 * @param n
	 *            Child node.
	 */
	public void addChild(ExplainNode n) {
		if (n != null && !children.contains(n)) {
			children.add(n);
			n.parent = this;
		}
	}

	/**
	 * Get all children.
	 * 
	 * @return Array of children or <tt>null</tt> if node is a leaf.
	 */
	public ExplainNode[] getChildren() {
		if (children.size() == 0)
			return null;
		return children.toArray(new ExplainNode[children.size()]);
	}

	/**
	 * Dump this node and all it's children to <tt>stderr</tt>. Debug only.
	 * 
	 * @param prefix
	 *            Line-prefix for level indentation.
	 */
	public void dumpTree(String prefix) {
		System.err.println(prefix + action + ", cost [" + costGuessedSelf + ","
				+ costGuessedTotal + "," + costActualSelf + ","
				+ costActualTotal + "] [" + info + "]");
		for (int i = 0; i < children.size(); i++)
			children.get(i).dumpTree(prefix + "  ");
	}

	/**
	 * Get node's tree level. Used during parsing only.
	 * 
	 * @return Level.
	 */
	protected int getLevel() {
		return level;
	}

	/**
	 * Set node's tree level. Used during parsing only.
	 * 
	 * @param level
	 *            Level.
	 */
	protected void setLevel(int level) {
		this.level = level;
	}

	/**
	 * Get node's parent.
	 * 
	 * @return Parent node reference or <tt>null</tt> if none.
	 */
	public ExplainNode getParent() {
		return parent;
	}

	/**
	 * Tell whether node has children.
	 * 
	 * @return <tt>true</tt>/<tt>false</tt>.
	 */
	public boolean hasChildren() {
		return children.size() > 0;
	}

	/**
	 * Parse cost string into duration. The input is for example
	 * <tt>42.0..666.00</tt>.
	 * 
	 * @param cost
	 *            Input string.
	 * @return Cost or negative value upon parse error.
	 */
	private static double parseCost(String cost) {
		String[] values = cost.split("\\.\\.");
		if (values.length != 2)
			return -1;
		double d1 = 0, d2 = 0;
		try {
			d1 = Double.parseDouble(values[0]);
			d2 = Double.parseDouble(values[1]);
		} catch (Exception e) {
			return -1;
		}
		return d2 - d1;
	}

	/**
	 * Set node's guessed costs. PostgreSQL provides for example
	 * <tt>42.0..666.0</tt>.
	 * 
	 * @param input
	 *            Input.
	 */
	public void setGuessedCosts(String input) {
		costGuessedSelf = parseCost(input);
	}

	/**
	 * Set node's actual costs. PostgreSQL provides for example
	 * <tt>42.0..666.0</tt>.
	 * 
	 * @param input
	 *            Input.
	 */
	public void setActualCosts(String input) {
		costActualSelf = parseCost(input);
	}

	/**
	 * Get node's guessed self cost.
	 * 
	 * @return Cost.
	 */
	public double getGuessedSelfCosts() {
		return costGuessedSelf;
	}

	/**
	 * Get node's guessed tree cost. This includes its self costs as well as
	 * costs of all its children.
	 * 
	 * @return Cost
	 */
	public double getGuessedTotalCosts() {
		double total = getGuessedSelfCosts();
		for (int i = 0; i < children.size(); i++)
			total += children.get(i).getGuessedTotalCosts();
		return total;
	}

	/**
	 * Get node's actual self cost.
	 * 
	 * @return Cost.
	 */
	public double getActualSelfCosts() {
		return costActualSelf;
	}

	/**
	 * Get node's actual tree cost. This includes its self costs as well as
	 * costs of all its children.
	 * 
	 * @return Cost
	 */
	public double getActualTotalCosts() {
		double total = getActualSelfCosts();
		for (int i = 0; i < children.size(); i++)
			total += children.get(i).getActualTotalCosts();
		return total;
	}

	/**
	 * Compute statistics after the complete tree has been build up. This
	 * includes computation of self and tree costs.
	 * 
	 */
	protected void computeStatistics() {
		for (int i = 0; i < children.size(); i++)
			children.get(i).computeStatistics();
		costGuessedTotal = costGuessedSelf;
		costActualTotal = costActualSelf;
		for (int i = 0; i < children.size(); i++) {
			costGuessedTotal += children.get(i).costGuessedTotal;
			costActualTotal += children.get(i).costActualTotal;
		}
	}

	public Object[] getData() {
		if (type == AbstractExplainAction.EXPLAIN_NORMAL)
			return new Object[] { new Double(costGuessedSelf),
					new Double(costGuessedTotal), info };
		return new Object[] { new Double(costGuessedSelf),
				new Double(costGuessedTotal), new Double(costActualSelf),
				new Double(costActualTotal), info };

	}

	public Object getName() {
		return action;
	}
}
