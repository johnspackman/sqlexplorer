package net.sourceforge.sqlexplorer.informix.nodes;

import java.util.Comparator;

import org.eclipse.swt.graphics.Image;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractFolderNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.util.ImageUtil;

public class StorageFolderNode extends AbstractFolderNode {

	public StorageFolderNode() {
		super(Messages.getString("informix.storage"));
	}

	public Image getImage() {
		return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.informix", Messages.getString("informix.images.storage"));
	}	
	
	public StorageFolderNode(INode parent, String name, MetaDataSession session) 
	{
		super(parent, name, session, "STORAGE_FOLDER");
	}


	public Comparator<INode> getComparator() {
        return new Comparator<INode>() {

            public int compare(INode arg0, INode arg1) {

                if (arg0 == null || arg1 == null) return 0;

                if (arg0 instanceof ChunksFolderNode) return -1;
                if (arg0 instanceof LogsFolderNode) return -1;
                
                return 0;
            }

        };
    }

	@Override
	public void loadChildren() {
		addChildNode(new SpacesFolderNode(this, Messages.getString("informix.storage.spaces"), _session));
		addChildNode(new ChunksFolderNode(this, Messages.getString("informix.storage.chunks"), _session));
		addChildNode(new LogsFolderNode  (this, Messages.getString("informix.storage.logs"),   _session));
	}
}
