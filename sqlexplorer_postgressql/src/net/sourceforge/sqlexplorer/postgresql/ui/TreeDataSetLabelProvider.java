package net.sourceforge.sqlexplorer.postgresql.ui;

import net.sourceforge.sqlexplorer.postgresql.dataset.tree.ITreeDataSetNode;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for a single {@link ITreeDataSetNode}.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TreeDataSetLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof ITreeDataSetNode) {
			ITreeDataSetNode node = (ITreeDataSetNode) element;
			Object o = null;
			if (columnIndex == 0)
				o = node.getName();
			else {
				Object[] d = node.getData();
				if (d == null || columnIndex > d.length)
					return "";
				o = d[columnIndex - 1];
			}
			if (o == null)
				return "";
			return o.toString();
		}
		return "";
	}

}
