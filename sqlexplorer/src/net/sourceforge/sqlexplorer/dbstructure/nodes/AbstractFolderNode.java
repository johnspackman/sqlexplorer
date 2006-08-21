package net.sourceforge.sqlexplorer.dbstructure.nodes;

import net.sourceforge.sqlexplorer.ImageUtil;

import org.eclipse.swt.graphics.Image;


public abstract class AbstractFolderNode extends AbstractNode {

    public AbstractFolderNode() {
        _imageKey = "Images.closedFolder";
        _expandedImageKey = "Images.OpenFolder";
    }
    
    public final String getUniqueIdentifier() {        
        return getParent().getName() + '.' + getType();
    }


    public abstract void loadChildren();  


       
    public abstract String getName();
    
    /**
     * Override this method to change the image that is displayed for this node
     * in the database structure outline.
     */
    public Image getImage() {

        if (_imageKey == null) {
            return _image;
        }
        return ImageUtil.getImage(_imageKey);
    }
}
