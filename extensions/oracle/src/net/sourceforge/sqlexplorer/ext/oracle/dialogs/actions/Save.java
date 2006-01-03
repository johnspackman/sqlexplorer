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
package net.sourceforge.sqlexplorer.ext.oracle.dialogs.actions;

import java.sql.CallableStatement;
import java.sql.SQLException;

import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.ext.oracle.dialogs.ObjectEditor;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author mazzolini
 */
public class Save extends Action {
	ObjectEditor editor;
	/**
	 * @param editor
	 */
	public Save(ObjectEditor editor) {
		this.editor=editor;
		
		
	}
	private ImageDescriptor img=ImageDescriptor.createFromURL(SqlexplorerImages.getSaveFileAsIcon()); 

	public  String getText() {
		 return "Save to Database"; //$NON-NLS-1$
	}

	public void run() {
		
		String sql=editor.getVw().getDocument().get();
		if(sql.endsWith("/"))
			sql = sql.substring(0, sql.length() - 1);
		CallableStatement cs=null;
		try
		{
			cs = editor.getConn().getConnection().prepareCall("BEGIN EXECUTE IMMEDIATE ?; END;");
			cs.setString(1, sql.replace('\r',' '));
			cs.execute();
			cs.close();
			cs = null;
		}
		catch(SQLException se)
		{
			//if(se.getErrorCode() != 24344){
			
				SQLExplorerPlugin.error("Error Saving ",se);
				MessageDialog.openError(editor.getShell(),"Error",se.getMessage());
			//}
			//se.printStackTrace();
			
		}
		finally{
			try{
				cs.close();
			}catch(Throwable e){
			}
		}
		if(editor.getObjectName()!=null){
			GetErrors getErrors=new GetErrors(editor.getConn(),editor.getOwner(),editor.getObjectType(),editor.getObjectName());
			String errors=getErrors.getError(editor.getSt());
			//editor.setErrorText(errors);
			if(!errors.equals("")){
				editor.showErrorPage();
			}
		}
		
	}
   
	
	public String getToolTipText(){
		return "Save to Database";  //$NON-NLS-1$
	}
	public ImageDescriptor getHoverImageDescriptor(){
		return img;
	}
	public ImageDescriptor getImageDescriptor(){
		return img;
	};
}
