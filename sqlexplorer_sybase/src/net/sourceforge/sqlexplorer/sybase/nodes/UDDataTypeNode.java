package net.sourceforge.sqlexplorer.sybase.nodes;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.swt.graphics.Image;

public class UDDataTypeNode extends SysObjectNode {
	
	public UDDataTypeNode() {
		_type = "userdatatype";
	}

	public Image getImage() {
		//TODO: change image
		return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.sybase", Messages.getString("sybase.images.procedure"));
	}
	
	public String getUniqueIdentifier() {
		return getSchemaOrCatalogName() + "." + getUName();
	}
}
