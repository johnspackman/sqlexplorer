package net.sourceforge.sqlexplorer.sybase.nodes;

import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.swt.graphics.Image;

public class SysObjectNode extends AbstractNode {
	
	protected int _uid;
	protected int _id;
	protected String _uname;

	public int getUID() {
		return _uid;
	}

	public int getID() {
		return _id;
	}

	public String getUName() {
		return _uname;
	}

	public void setUID(int uid) {
		_uid = uid;
	}

	public void setUName(String uname) {
		_uname = uname;
	}

	public void setID(int id) {
		_id = id;
	}

	public SysObjectNode() {
		super("sysobject");
		_type = "none";
		_uid = -1;
		
	}
	
	public Image getImage() {
		//return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.oracle", Messages.getString("oracle.images.procedure"));
		return null;
	}	

	public String getName() {
		return _name;
	}
	
	public String getQualifiedName() {
		return _name;
	}
	
	public String getUniqueIdentifier() {
		return getParent().getQualifiedName() + "." + getUName() + "." + getName();
	}

	public boolean isEndNode() {
		return true;
	}
	
	public void loadChildren() {
	}
}
