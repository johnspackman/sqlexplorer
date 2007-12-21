package net.sourceforge.sqlexplorer.sybase.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.swt.graphics.Image;

public class UserNode extends SysObjectNode {
	
	public UserNode() {
		_type = "user";
	}

	public UserNode(INode parent, String name, MetaDataSession session) {
		super(parent, name, session, "user");
	}
	

	public Image getImage() {
		return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.sybase", Messages.getString("sybase.images.user"));
	}
	
//	public String getName() {
//		return _name;
//	}
	
//	public String getQualifiedName() {
//		return _name;
//	}

	public String getUniqueIdentifier() {
		return getSchemaOrCatalogName() + "." + getUName();
	}

//	public boolean isEndNode() {
//		return true;
//	}
	
//	public void loadChildren() {
//		return;
//	}

}
