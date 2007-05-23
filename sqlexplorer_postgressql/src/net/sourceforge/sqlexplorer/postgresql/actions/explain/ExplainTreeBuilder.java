package net.sourceforge.sqlexplorer.postgresql.actions.explain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class building up a tree of nodes based on PostgreSQL explain format.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class ExplainTreeBuilder {
	/* debug */
	private static final boolean KEEP_RAW_OUTPUT = false;

	/* debug */
	private static final String RAW_OUTPUT_FILE = "/tmp/explain.txt";

	/* debug */
	private static final boolean STRICT_PARSE = false;

	private ExplainNode root = new ExplainNode(
			AbstractExplainAction.EXPLAIN_NORMAL);

	private final int type;

	public ExplainTreeBuilder(int type) {
		this.type = type;
	}

	/**
	 * Get explain tree root. This is is a fake tree node to group all actual
	 * explain nodes under it in a tree.
	 * 
	 * @return Tree's root.
	 */
	public ExplainNode getRoot() {
		return root;
	}

	/**
	 * Get the indentation level of an input line.
	 * 
	 * @param line
	 *            Input line.
	 * @return Level, currently the number of leading spaces.
	 */
	private static int getLevel(String line) {
		int i = 0;
		for (i = 0; i < line.length() && line.charAt(i) == ' '; i++)
			;
		return i;
	}

	/**
	 * Get the action of an input line.
	 * 
	 * @param line
	 *            Input line.
	 * @return Action, i.e. sequential scans, joins, merges, etc.
	 */
	private static String getAction(String line) {
		int i = line.indexOf('(');
		if (i < 0)
			return line.trim();
		return line.substring(0, i).trim();
	}

	/**
	 * Parse a group of details and set node's properties accordingly. A group
	 * may be '<tt>cost=0.0..10.00 rows=1 width=1</tt>'. The braces need to
	 * be stripped prior to calling it. Currently this only cares about the
	 * <tt>cost</tt> and <tt>actual cost</tt> entries (the latter for
	 * analyzed explains).
	 * 
	 * @param dst
	 *            The node to set properties of.
	 * @param input
	 *            Group string.
	 */
	private static void eatGroup(ExplainNode dst, String input) {
		String[] v = input.split("[ ]+");
		String last = "";
		for (int i = 0; i < v.length; i++) {
			int brk = v[i].indexOf('=');
			if (brk < 0) {
				last += v[i] + " ";
				continue;
			}
			String key = last + v[i].substring(0, brk);
			String value = v[i].substring(brk + 1);
			if (value.contains("..")) {
				if (key.startsWith("actual"))
					dst.setActualCosts(value);
				else if (key.startsWith("cost"))
					dst.setGuessedCosts(value);
			}
		}
	}

	/**
	 * Parse all present groups of a line and set node's properties accordingly.
	 * 
	 * @param dst
	 *            The node to set properties of.
	 * @param input
	 *            Input line.
	 * @see #eatGroup(ExplainNode, String)
	 */
	private static void eatGroups(ExplainNode dst, String input) {
		int from = 0, to = 0;
		while (true) {
			if (to < 0 || to == input.length() - 1)
				break;
			from = input.indexOf('(', to);
			if (from < 0 || from == input.length() - 1)
				break;
			to = input.indexOf(')', from);
			if (to >= 0) {
				String s = input.substring(from + 1, to);
				eatGroup(dst, s);
			}
		}
	}

	/**
	 * Create a new node based on the given line. This uses its action and
	 * parses groups so that the returned node is complete (but without
	 * parentship information).
	 * 
	 * @param action
	 *            The node's action.
	 * @param line
	 *            Input line.
	 * @return Node
	 * @see #getAction(String)
	 * @see #eatGroups(ExplainNode, String)
	 */
	private ExplainNode getNode(String action, String line) {
		ExplainNode ret = new ExplainNode(type);
		String a = action;
		if (a.startsWith("->"))
			a = a.substring(2).trim();
		ret.setAction(a);
		eatGroups(ret, line);
		return ret;
	}

	/**
	 * Parse PostgreSQL explain output linewise into an explain tree.
	 * 
	 * @param provider
	 *            Class providing linewise input.
	 * @throws Exception
	 *             In case something goes wrong, i.e. parse error.
	 */
	public void parse(ExplainLineProvider provider) throws Exception {
		String line = null;
		int curLevel = -1, nextLevel;
		String action;
		ExplainNode curNode = root;
		long c = 0;
		List<ExplainNode> last = new ArrayList<ExplainNode>();
		BufferedWriter br = null;

		if (KEEP_RAW_OUTPUT)
			new BufferedWriter(new FileWriter(RAW_OUTPUT_FILE));

		while ((line = provider.getLine()) != null) {
			c++;
			if (br != null) {
				br.append(line);
				br.newLine();
			}
			nextLevel = getLevel(line);
			action = getAction(line);
			log("line [" + c + "] [" + line + "]");
			if (action.startsWith("Total "))
				/* done for analyze */
				break;
			if (!action.startsWith("->") && c > 1) {
				/* some more info for last node */
				if (curNode != null)
					curNode.addInfo(line.trim());
				continue;
			}
			ExplainNode n = getNode(action, line);
			n.setLevel(nextLevel);
			last.add(n);
			log("line [" + c + "] [" + action + "] [" + curLevel + "] ["
					+ nextLevel + "]");
			if (nextLevel > curLevel) {
				if (curNode == null)
					root = n;
				else
					curNode.addChild(n);
				if (curNode != null)
					log("line [" + c + "] adding child [" + n.getAction()
							+ "] to [" + curNode.getAction() + "@"
							+ curNode.getInfo() + "]");
			} else if (nextLevel == curLevel) {
				curNode.getParent().addChild(n);
				log("line [" + c + "] adding child [" + n.getAction()
						+ "] to [" + curNode.getParent().getAction() + "]");
			} else {
				ExplainNode newParent = null;
				int off;
				for (off = last.size() - 2; off >= 0; off--)
					if (last.get(off).getLevel() == nextLevel)
						break;
				if (off < 0) {
					if (STRICT_PARSE) {
						try {
							if (br != null)
								br.close();
						} catch (Exception e) {
						}
						throw new Exception("Unable to find parent for node ["
								+ action + "] at line for level [" + nextLevel
								+ "]");
					}
					/*
					 * here we can't find our parent correctly by indentation
					 * and we're told to sit back and relax; since we must
					 * append the node somewhere, take root's last child as
					 * parent (if any) and root itself otherwise
					 */
					if (last.size() >= 1)
						off = 1;
					else
						off = 0;
				}
				newParent = last.get(off);
				log("line [" + c + "] last at level [" + nextLevel + "] was ["
						+ last.get(off).getAction() + "@"
						+ last.get(off).getInfo() + "]");
				newParent.getParent().addChild(n);
				log("line [" + c + "] adding child [" + n.getAction()
						+ "] to [" + newParent.getParent().getAction() + "]");
			}
			curLevel = nextLevel;
			curNode = n;
		}
		try {
			if (br != null)
				br.close();
		} catch (Exception e) {
		}
	}

	/**
	 * Log a string to <tt>stderr</tt>. This class doesn't use a logger since
	 * the output of the parser is just too verbose. This must be explicitely
	 * turned on.
	 * 
	 * @param string
	 *            Log message.
	 */
	private void log(String string) {
		// System.stderr.println(string);
	}

	/**
	 * Test the parser with some flat file. For debugging purpose only.
	 * 
	 * @param args
	 *            Command line arguments, ignored.
	 * @throws Exception
	 *             In case something goes wrong.
	 */
	public static void main(String[] args) throws Exception {
		final BufferedReader br = new BufferedReader(new FileReader(
				"/tmp/foo.txt"));
		ExplainTreeBuilder treeBuilder = new ExplainTreeBuilder(
				AbstractExplainAction.EXPLAIN_ANALYZE);
		treeBuilder.parse(new ExplainLineProvider() {
			public String getLine() throws Exception {
				return br.readLine();
			}
		});
		br.close();
		ExplainNode root = treeBuilder.getRoot();
		if (root != null) {
			root.computeStatistics();
			root.dumpTree("");
		}
	}
}
