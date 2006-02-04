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
package net.sourceforge.sqlexplorer.sessiontree.ui;

import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeContentProvider;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeLabelProvider;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModel;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModelChangedListener;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;

public class SessionViewer extends TreeViewer{
	
	
	SessionTreeModel md;

	/**
	 * @param parent
	 */
	public SessionViewer(final Composite parent,int style,SessionTreeModel md,final IViewPart viewPart) {
		super(parent,style);
		this.md=md;
		
		setContentProvider(new SessionTreeContentProvider());
		final SessionTreeLabelProvider stlp=new SessionTreeLabelProvider();
		this.getControl().addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent e) {
				stlp.dispose();	
			}
		});
		setLabelProvider(stlp);
		setUseHashlookup(true);
		setInput(md);
		md.addListener(new SessionTreeModelChangedListener(){
			public void modelChanged(final SessionTreeNode node_){
				parent.getDisplay().asyncExec(new Runnable(){
					public void run(){
						SessionViewer.this.refresh();
						SessionViewer.this.expandAll();
						if(node_!=null){
							SessionViewer.this.setSelection(new StructuredSelection(node_));						}
					}
				});
				
			}
		});

	}
}
