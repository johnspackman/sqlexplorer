/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.ext.oracle.actions.explain;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


public class ExplainPlanDialog extends TitleAreaDialog {

	ExplainNode nd_parent;
	public ExplainPlanDialog(Shell  parentShell, ExplainNode nd_parent) {
		
		super(parentShell);
		this.nd_parent=nd_parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite)super.createDialogArea(parent);
		Composite pp=new Composite(parentComposite,SWT.NULL);
		pp.setLayout(new FillLayout());
		pp.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableTreeViewer tv=new TableTreeViewer(pp,SWT.BORDER|SWT.FULL_SELECTION);
		Table table=tv.getTableTree().getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableColumn tc=new TableColumn(table,SWT.NULL);
		tc.setText(""); //$NON-NLS-1$
		tc=new TableColumn(table,SWT.NULL);
		tc.setText("Cost"); //$NON-NLS-1$
		tc=new TableColumn(table,SWT.NULL);
		tc.setText("Cardinality"); //$NON-NLS-1$		
		TableLayout tableLayout=new TableLayout();
		for(int i=0;i<3;i++)
			tableLayout.addColumnData(new ColumnWeightData(1, 50, true));
		table.setLayout(tableLayout);
		
		tv.setContentProvider(new ITreeContentProvider(){
			private TableTreeViewer m_viewer;
			public Object[] getChildren(Object parentElement) {
				return ((ExplainNode)parentElement).getChildren();
			}

			public Object getParent(Object element) {
				return ((ExplainNode)element).getParent();
			}

			public boolean hasChildren(Object element) {
				if(((ExplainNode)element).getChildren().length>0)
					return true;
				return false;
			}

			public Object[] getElements(Object inputElement) {
				ExplainNode nd=((ExplainNode)inputElement);
				//if(nd.getParent()==null)
				//	return new Object[]{nd};
				return nd.getChildren();//((ExplainNode)inputElement).getChildren();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				m_viewer=(TableTreeViewer)viewer;
			}
		});
		tv.setLabelProvider(new TreeLabelProvider(){});
		tv.setInput(nd_parent);
		tv.refresh();
		tv.expandAll();
		return parentComposite;
	}
	static class TreeLabelProvider extends LabelProvider implements ITableLabelProvider{

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			ExplainNode en=(ExplainNode)element;
			if(columnIndex==0)
				return en.toString();
			if(columnIndex==1){
				int cost=en.getCost();
				if(cost!=-1)
					return ""+cost;
				else
					return "";
			}
				
			else if(columnIndex==2){
				int card=en.getCardinality();
				if(card!=-1)
					return ""+card;
				else
					return "";
			}
			return "";
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(newShellStyle|SWT.RESIZE);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle("Oracle Explain Plan "); //$NON-NLS-1$
		//setMessage("Note: the Direct Table Editing is an experimental feature.");
		return contents;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		newShell.setText("Oracle Explain Plan");
		super.configureShell(newShell);
	}

}
