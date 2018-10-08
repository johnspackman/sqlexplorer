/*
 * Copyright (C) 2007 Patrac Vlad Sebastian
 * http://sourceforge.net/projects/eclipsesql
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

/**
 * Explain plan context menu action to generate MetaUML activity diagram 
 * 
 * @author Patras Vlad
 */

package net.sourceforge.sqlexplorer.oracle.actions.explain;

import java.io.FileOutputStream;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

public class GenerateMetaUML extends AbstractExplainPlanContextAction {
	
	private static final ImageDescriptor _image = ImageUtil.getFragmentDescriptor("net.sourceforge.sqlexplorer.oracle", Messages.getString("oracle.images.generatemetauml"));
	
	public String getText() {
		return Messages.getString("oracle.explainplan.generatemetauml");
	}
	
	public ImageDescriptor getImageDescriptor() {
		return _image;
	}

	public void run() {
		
		FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
		fileDialog.setFilterExtensions(new String[] { "*.mp", "*.txt", "*.*" } );
		fileDialog.setFilterPath("");
		fileDialog.setText("Save ...");
		
		String selected = fileDialog.open();
		
		if (selected != "") {
			
			String metaUML =
				"input metauml;\nbeginfig(1)\n\n" +
				getMetaUMLActivity(_node)    + "\n" +
				getMetaUMLPositioning(_node) + "\n" +
				getMetaUMLDraw(_node)        + "\n" +
				getMetaUMLLinks(_node)       + "\n" +
				"endfig;\nend\n";
			
			try {
				FileOutputStream outFile =new FileOutputStream(selected);
				outFile.write(metaUML.getBytes());
				outFile.close();
				
			} catch (Exception e) {
				
				SQLExplorerPlugin.error("Error writing MetaUML for explain plan", e);
			}
		}
	}
	
    private String getMetaUMLActivity(final ExplainNode node) {
    	
    	String activity = 
    		("Activity.ET" +
			(char)(node.getId()+97) + "(\"" + (node.getId()+1)            + "\", \"" +
			(node.getOperation()   == null ? "" : node.getOperation())    + "\", \"" +
			(node.getOptions()     == null ? "" : node.getOptions())      + "\", \"" +
			(node.getObject_name() == null ? "" : node.getObject_name())  + "\");\n");    	
		
    	for (ExplainNode n : node.getChildren()) {
    		activity += getMetaUMLActivity(n);
    	}
    	
    	return activity;
    }
    
    private String getMetaUMLPositioning(final ExplainNode node) {
    	
    	String positioning = "";
    	ExplainNode[] children = node.getChildren();
    	int fullSpan = 0;
    	
    	ExplainNodeAnalyser nodeComputing = new ExplainNodeAnalyser(node);
    	nodeComputing.compute();
    	
    	for (int i=1; i<children.length; ++i) {
    		fullSpan += nodeComputing.getNodeJunction(children[i-1], children[i]);
    	}
    	
    	for (int i=0; i<children.length; ++i) {
    		if (i==0) {
    			if (children.length == 1) {
    				positioning += "topToBottom(20)(ET" + (char)(node.getId()+97) + 
    					", ET" + (char)(children[i].getId()+97) + ");\n";
    			} else {
    				positioning += "ET" + (char)(children[i].getId()+97) + ".ne = " +
    					"ET" + (char)(node.getId()+97) +
    					".s - (" + (fullSpan*45) + ", 20);\n"; 
    			}
    		} else {
    			positioning += "leftToRight(" +
    				(nodeComputing.getNodeJunction(children[i-1], children[i])*80+20) +
    				")(ET" + (char)(children[i-1].getId()+97) +
    				", ET" + (char)(children[i  ].getId()+97) + ");\n";
    		}
    		positioning += getMetaUMLPositioning(children[i]);
    	}
    	
    	return positioning;
    }
    
    private String getMetaUMLDraw(final ExplainNode node) {
    	
    	String draw = ((node.getId() == 0) ? "drawObjects(" : ", ") + "ET" + (char)(node.getId()+97) ;
    	
    	for (ExplainNode n : node.getChildren()) {
    		draw += getMetaUMLDraw(n);
    	}
    	
    	return draw + ((node.getId() == 0) ? ");\n" : "");
    }
    
    private String getMetaUMLLinks(final ExplainNode node) {
    	
    	String links = ""; 
    	
    	for (ExplainNode n : node.getChildren()) {
    		links += "clink(associationUni)(ET" + (char)(node.getId()+97) +
    				 ", ET" + (char)(n.getId()+97) + ");\n";
    		links += getMetaUMLLinks(n);
    	}
    	
    	return links;
    }
	
}
