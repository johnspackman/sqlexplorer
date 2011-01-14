package net.sourceforge.sqlexplorer.informix.nodes;

import org.eclipse.swt.graphics.Image;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractSQLFolderNode;
import net.sourceforge.sqlexplorer.util.ImageUtil;



public class ProcedureFolder extends AbstractSQLFolderNode {

	public ProcedureFolder() {
		super(Messages.getString("informix.dbstructure.procedures"));
	}

 	public Image getImage() {
		return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.informix", Messages.getString("informix.images.proc"));
	}	
	
	public String getChildType() {
		return "procedure";
	}

	public String getSQL() {
		return "SELECT RTRIM(procname) FROM " + getParent().getName() + ":sysprocedures where mode=UPPER(mode) AND mode!='P' AND isproc='t' order by procname";
	}

	public Object[] getSQLParameters() {
		return null;
	}
}