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
 
package net.sourceforge.sqlexplorer.ext.edextensions.actions;

import java.util.StringTokenizer;

import net.sourceforge.sqlexplorer.ext.edextensions.EditorExtensionPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.StyledText;

public class Java2Sql extends Action {
	SQLEditor editor;
	public Java2Sql(SQLEditor editor){
		this.editor=editor;
	}
	public void run(){
		StyledText st=editor.sqlTextViewer.getTextWidget();
		String text=st.getText();
		StringBuffer newText=new StringBuffer();
		String ld=st.getLineDelimiter();
		StringTokenizer tokenizer=new StringTokenizer(text,ld);
		while(tokenizer.hasMoreTokens()) {
			String line=tokenizer.nextToken();
			if(line==null)
				continue;
			line=line.trim();
			if((line.indexOf("+"))==0){
				line=line.substring(1);
			}
			if((line.indexOf("+"))==line.length()-1){
				if(line.length()>=1)
					line=line.substring(0,line.length()-1);
			}
			line=line.replace('"',' ');
			newText.append(line);
			newText.append(ld);
		}
		st.setText(newText.toString());
	}
	public String getText(){
		return "Java to Sql";
	}
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromURL(EditorExtensionPlugin.getUrl("plugins/icons/synch_synch.gif"));
	}
	public String getToolTipText(){
		return "try to convert from java to sql";
	}
}
