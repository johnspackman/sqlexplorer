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
package net.sourceforge.sqlexplorer.ext.hbexport.actions;

import java.io.File;


import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbviewer.model.CatalogNode;
import net.sourceforge.sqlexplorer.dbviewer.model.SchemaNode;
import net.sourceforge.sqlexplorer.dbviewer.model.TableNode;
import net.sourceforge.sqlexplorer.dbviewer.model.TableObjectTypeNode;
import net.sourceforge.sqlexplorer.ext.hbexport.MapGenerator;
import net.sourceforge.sqlexplorer.ext.hbexport.dialogs.ExportDialog;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * @author mazzolini
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ExportWizard extends Action {
	SessionTreeNode sessionNode;
	TableNode node;
	
	/**
	 * @param sessionNode
	 * @param node
	 */
	public ExportWizard(SessionTreeNode sessionNode, TableNode node) {
		this.sessionNode=sessionNode;
		this.node=node;
	}
	public String getText(){
		return "Export through Hibernate..."; //$NON-NLS-1$
	}
	public void run(){
		ExportDialog ed=new ExportDialog(null,sessionNode,node);
		if(Dialog.OK==ed.open()){
			
			String pkgName=ExportDialog.pkgName;
			String generator=ExportDialog.generator;
			String idType=ExportDialog.idType;
			String dir=ExportDialog.outDirText;
			boolean hibernateType=ExportDialog.hibernateTypeSelected;
			
			MapGenerator mg=new MapGenerator();
			TableObjectTypeNode totn=(TableObjectTypeNode) node.getParent();
			if(totn.getParent() instanceof SchemaNode){
				mg.setSchemaPattern(totn.getParent().toString());
			}else if(totn.getParent() instanceof CatalogNode){
				CatalogNode cn=(CatalogNode)totn.getParent();
				String cn_name=cn.toString();
				if(cn_name!=Messages.getString("NoCatalog_2"))
					mg.setCatalog(cn.toString());
			}
			
			
			try {
				mg.setTableNames(new String[]{node.toString()});
				mg.setPackageName(pkgName);
				mg.setGenerator(generator);
				mg.setIdType(idType);
				mg.setOutputDirectory(new File(dir));
				mg.setHibernateTypes(hibernateType);
				mg.generate(sessionNode.getConnection().getConnection() );
			} catch (Exception e) {
				SQLExplorerPlugin.error("Hibernate Export Error",e);
				MessageDialog.openError(null,"Error",e.getMessage());
			} 

			
		}
	}
}
