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
package net.sourceforge.sqlexplorer.plugin.views;

import java.util.HashMap;
import java.util.Iterator;

import net.sourceforge.sqlexplorer.dbviewer.DatabaseContentProvider;
import net.sourceforge.sqlexplorer.dbviewer.DatabaseLabelProvider;
import net.sourceforge.sqlexplorer.dbviewer.DetailManager;
import net.sourceforge.sqlexplorer.dbviewer.actions.DatabaseActionGroup;
import net.sourceforge.sqlexplorer.dbviewer.model.DatabaseModel;
import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.dbviewer.model.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.ISessionTreeClosedListener;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
//import net.sourceforge.jfacedbc.sessiontree.model.DatabaseActionGroup;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Andrea Mazzolini
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DBView extends ViewPart {
	SessionTreeNode node_;
	//TreeViewer tv;
	TabFolder tabFolder;
	HashMap nodeTabItemsMap=new HashMap();
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	 //public TreeViewer getTreeViewer(){
	 //	return tv;
	 //}
	private Couple createItem(SessionTreeNode sessionTreeNode){
		TabItem ti=new TabItem(tabFolder,SWT.NULL);
		ti.setText(sessionTreeNode.toString());
		SashForm sash=new SashForm(tabFolder,SWT.VERTICAL);
		ti.setControl(sash);
		sash.setLayout(new FillLayout());
		final TreeViewer tv= new TreeViewer(sash,SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI |  SWT.FULL_SELECTION|SWT.BORDER);
		tv.setUseHashlookup(true);
		Transfer[] transfers = new Transfer[] { TableNodeTransfer.getInstance()};
		tv.addDragSupport(DND.DROP_COPY ,transfers,new DragSourceListener(){

			public void dragStart(DragSourceEvent event) {

				event.doit = !tv.getSelection().isEmpty();
				if(event.doit){
					Object sel=((IStructuredSelection)tv.getSelection()).getFirstElement();
					if(!(sel instanceof TableNode)){
						event.doit=false;
					}else{
						TableNode tn=(TableNode)sel;
						TableNodeTransfer.getInstance().setSelection(tn);
						if(!tn.isTable())
							event.doit=false;
					}
				}
			}

			public void dragSetData(DragSourceEvent event) {
				Object sel=((IStructuredSelection)tv.getSelection()).getFirstElement();
				event.data=sel;
				//System.out.println("Drag dragSetData "+sel);
			}

			public void dragFinished(DragSourceEvent event) {
				TableNodeTransfer.getInstance().setSelection(null);
				//System.out.println("dragFinished ");
			}
		});
		Composite c=new Composite(sash,SWT.BORDER);
		c.setLayout(new FillLayout());
		final DetailManager dm=new DetailManager(tv,c,SQLExplorerPlugin.getDefault().getPreferenceStore(),sessionTreeNode);
		tv.setContentProvider(new DatabaseContentProvider());
		final DatabaseLabelProvider tlp=new DatabaseLabelProvider(SQLExplorerPlugin.getDefault().pluginManager);//Va rimesso il plugin manager
		tv.setLabelProvider(tlp);

		sash.setWeights (new int [] {3,1});
		


		tv.getControl().addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent e){
				tlp.dispose();

			}
		});
		tv.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent ev){
				IStructuredSelection sel=(IStructuredSelection)ev.getSelection();
				if(!sel.isEmpty()){
					try{
						dm.activate((IDbModel)sel.getFirstElement());
						//((IDbModel)sel.getFirstElement()).activate();
					}catch(Throwable e){
						SQLExplorerPlugin.error("Error managing selection changed ",e); //$NON-NLS-1$
				
						//To catch bad-written plugins!
					}
				}
			}
		});

		final DatabaseActionGroup actGroup=new DatabaseActionGroup(this,tv);
		MenuManager  menuMgr= new MenuManager("#DbPopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		Menu fDbContextMenu= menuMgr.createContextMenu(tv.getTree());
		tv.getTree().setMenu(fDbContextMenu);
		menuMgr.addMenuListener(new IMenuListener(){
			public void menuAboutToShow(IMenuManager manager){
		
				actGroup.fillContextMenu(manager);
			}
		});
		return new Couple(ti,tv);
	}
	public void createPartControl(Composite parent) {
		tabFolder=new TabFolder(parent,SWT.NULL);
		tabFolder.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e){
				node_=getSessionTreeNode();
			}
			public void widgetDefaultSelected(SelectionEvent e){}
		});
		parent.setLayout(new FillLayout());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}

	/**
	 * @param node
	 */
	public void setInput(SessionTreeNode node) {
		
		if(node==node_)
			return;
		if(node!=null){
			Couple cp=(Couple)nodeTabItemsMap.get(node);
			if(cp==null){
				cp=this.createItem(node);
				nodeTabItemsMap.put(node,cp);
				final TabItem ti=cp.ti;
				node.addListener(new ISessionTreeClosedListener(){

					public void sessionTreeClosed() {
						ti.dispose();
					}
				});
				DatabaseModel dbModel=node.dbModel;
				cp.tv.setInput(dbModel);
			}
			tabFolder.setSelection(new TabItem[]{cp.ti});
			node_=node;
		}
		
	}
	public SessionTreeNode getSessionTreeNode(){
		TabItem[] items=tabFolder.getSelection();
		if(items.length==0)
			return null;
		TabItem ti=items[0];
		Iterator it=nodeTabItemsMap.keySet().iterator();
		while(it.hasNext()){
			SessionTreeNode nd=(SessionTreeNode) it.next();
			Couple cp=(Couple) nodeTabItemsMap.get(nd);
			if(ti==cp.ti)
				return nd;
		}
		return null;
	}
	public void refresh(){
		if(node_!=null){
			node_.dbModel=new DatabaseModel(node_,SQLExplorerPlugin.getDefault().pluginManager);
			Couple cp=(Couple)nodeTabItemsMap.get(node_);
			if(cp!=null){
				cp.tv.setInput(node_.dbModel);
				cp.tv.refresh();
			}
			else{
				cp=this.createItem(node_);
				nodeTabItemsMap.put(node_,cp);
				final TabItem ti=cp.ti;
				node_.addListener(new ISessionTreeClosedListener(){

					public void sessionTreeClosed() {
						ti.dispose();
					}
				});
				DatabaseModel dbModel=node_.dbModel;
				cp.tv.setInput(dbModel);
				cp.tv.refresh();
			}
		}
	}
	class Couple{
		TabItem ti;
		TreeViewer tv;
		public Couple(TabItem ti, TreeViewer tv){
			this.ti=ti;
			this.tv=tv;
		}
	}
	/**
	 * @param activeTableNode
	 */
	public void tryToSelect(SessionTreeNode sessionNode,IDbModel activeTableNode) {
		if(activeTableNode instanceof TableNode){
			Couple cp=(Couple)nodeTabItemsMap.get(sessionNode);
			cp.tv.reveal(((IDbModel)activeTableNode.getParent()).getParent());
			cp.tv.expandToLevel(activeTableNode.getParent(),2);
			cp.tv.reveal(activeTableNode);
			cp.tv.setSelection(new StructuredSelection(new Object[]{activeTableNode}),true);

		}
	}

}
