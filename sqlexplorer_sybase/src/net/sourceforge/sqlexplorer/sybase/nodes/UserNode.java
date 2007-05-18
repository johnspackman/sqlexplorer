package net.sourceforge.sqlexplorer.sybase.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.swt.graphics.Image;

public class UserNode extends SysObjectNode {
	
	public UserNode() {
		_type = "user";
	}

	public Image getImage() {
		return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.sybase", Messages.getString("sybase.images.procedure"));
	}
	
	public String getName() {
		return _name;
	}
	
	public String getQualifiedName() {
		return _name;
	}

	public String getUniqueIdentifier() {
		return getSchemaOrCatalogName() + "." + getUName();
	}

	public boolean isEndNode() {
		return true;
	}
	
	public void loadChildren() {
		return;
	}

}
