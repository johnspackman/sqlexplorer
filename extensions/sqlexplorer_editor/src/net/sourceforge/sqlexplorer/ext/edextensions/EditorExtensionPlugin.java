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

package net.sourceforge.sqlexplorer.ext.edextensions;

import java.net.URL;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;


import net.sourceforge.sqlexplorer.URLUtil;
import net.sourceforge.sqlexplorer.ext.DefaultEditorPlugin;
import net.sourceforge.sqlexplorer.ext.IEditorPlugin;
import net.sourceforge.sqlexplorer.ext.edextensions.actions.ExecuteOnItem;
import net.sourceforge.sqlexplorer.ext.edextensions.actions.ExecuteSQL;
import net.sourceforge.sqlexplorer.ext.edextensions.actions.Java2Sql;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;




public class EditorExtensionPlugin extends DefaultEditorPlugin implements IEditorPlugin {

	
	public void load() {
		
		
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getInternalName()
	 */
	public String getInternalName() {
		return "editorExtensionPlugin";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getDescriptiveName()
	 */
	public String getDescriptiveName() {
		return "Editor Extensions Plugin for JFaceDbc";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getAuthor()
	 */
	public String getAuthor() {
		return "andreamazzolini@users.sourceforge.net";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getWebSite()
	 */
	public String getWebSite() {
		
		return "http://jfacedbc.sourceforge.net";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jfacedbc.ext.IPlugin#getVersion()
	 */
	public String getVersion() {
		
		return "0.1";
	}


	

	/* (non-Javadoc)
	 * @see net.java.sqlexplorer.ext.IEditorPlugin#getContextMenuActions(net.java.sqlexplorer.plugin.editors.SQLEditor)
	 */
	public IContributionItem[] getContextMenuActions(SQLEditor editor) {
		return new IContributionItem[]{
			new ActionContributionItem(new Java2Sql(editor)),
			new ActionContributionItem(new ExecuteSQL(editor)),
			new ExecuteOnItem(editor)
		};
	}
	public static URL getUrl(String path){
		URL url=null;
		URL baseURL=URLUtil.getBaseURL();
		try{
			url = new URL(baseURL, path);
			url=net.sourceforge.sqlexplorer.ext.PluginManager.asLocalURL(url);
		}catch(Exception e){

		}
		return url;
	}

	/* (non-Javadoc)
	 * @see net.java.sqlexplorer.ext.IEditorPlugin#getEditorToolbarActions(net.java.sqlexplorer.plugin.editors.SQLEditor)
	 */
	public IAction[] getEditorToolbarActions(SQLEditor editor) {
		
		return new IAction[]{
			new Java2Sql(editor)
		};
	}

}
