package net.sourceforge.sqlexplorer.informix.nodes;

import org.eclipse.swt.graphics.Image;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.MetaDataSession;
import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.util.ImageUtil;

/**
 * @author Vladimir Rüntü
 * 
 */
public class LogNode extends AbstractNode {

    private int _idx;
    private String _id;

    public LogNode(INode parent, String name, MetaDataSession session, int pIdx, String pId) {
    	super(parent, name, session, "log");
        _idx = pIdx;
        _id = pId;
    }
       
	public Image getImage() {
		return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.informix", Messages.getString("informix.images.log"));
	}	

    public boolean isEndNode() {
        return true;
    }

    public void loadChildren() {
        // noop
    }

    public int getIdx() {
    	return _idx;
    }
    
    public String getId() {
    	return _id;
    }
    
}