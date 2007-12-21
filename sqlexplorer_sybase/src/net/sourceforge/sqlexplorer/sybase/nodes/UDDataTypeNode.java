package net.sourceforge.sqlexplorer.sybase.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.swt.graphics.Image;

public class UDDataTypeNode extends SysObjectNode {
	
	public UDDataTypeNode() {
		_type = "userdatatype";
	}
	
	public UDDataTypeNode(INode parent, String name, MetaDataSession session) {
		super(parent, name, session, "userdatatype");
	}
	

	public Image getImage() {
		//TODO: change image
		return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.sybase", Messages.getString("sybase.images.udd"));
	}
	
	public String getUniqueIdentifier() {
		return getSchemaOrCatalogName() + "." + getUName();
	}
}
