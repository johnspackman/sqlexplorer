package net.sourceforge.sqlexplorer.postgresql.actions.explain;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Class providing labels for explain tree widget.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class ExplainTreeLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	private final int type;

	private final NumberFormat fmt;

	/**
	 * Create new label provider.
	 * 
	 * @param type
	 *            The explain format type.
	 * @see AbstractExplainAction#EXPLAIN_ANALYZE
	 * @see AbstractExplainAction#EXPLAIN_NORMAL
	 */
	public ExplainTreeLabelProvider(int type) {
		this.type = type;
		fmt = new DecimalFormat("0.00");
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/**
	 * Avoid returning <tt>null</tt> for empty strings.
	 * 
	 * @param input
	 *            Some string.
	 * @return Either the input or <tt>""</tt> if input is <tt>null</tt>.
	 */
	private String noNull(String input) {
		if (input == null)
			return "";
		return input;
	}

	public String getColumnText(Object element, int columnIndex) {
		ExplainNode node = (ExplainNode) element;
		switch (columnIndex) {
		case 0:
			return noNull(node.getAction());
		case 1:
			return fmt.format(node.getGuessedSelfCosts());
		case 2:
			return fmt.format(node.getGuessedTotalCosts());
		case 3:
			if (type == AbstractExplainAction.EXPLAIN_ANALYZE)
				return fmt.format(node.getActualSelfCosts());
			else
				return noNull(node.getInfo());
		case 4:
			return fmt.format(node.getActualTotalCosts());
		case 5:
			return noNull(node.getInfo());
		default:
			return "";
		}
	}
}
