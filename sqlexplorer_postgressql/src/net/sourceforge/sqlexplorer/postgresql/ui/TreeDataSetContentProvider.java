package net.sourceforge.sqlexplorer.postgresql.ui;

import net.sourceforge.sqlexplorer.postgresql.dataset.tree.ITreeDataSet;
import net.sourceforge.sqlexplorer.postgresql.dataset.tree.ITreeDataSetNode;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for {@link ITreeDataSet} and {@link ITreeDataSetNode}.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public class TreeDataSetContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		return ((ITreeDataSetNode) parentElement).getChildren();
	}

	public Object getParent(Object element) {
		return ((ITreeDataSetNode) element).getParent();
	}

	public boolean hasChildren(Object element) {
		return ((ITreeDataSetNode) element).hasChildren();
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
