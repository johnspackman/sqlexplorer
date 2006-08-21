package net.sourceforge.sqlexplorer.dbstructure.nodes;

import org.eclipse.swt.graphics.Image;


public class ObjectNode extends AbstractNode {

    private String _type;
    
    
    /**
     * Hidden default constructor.
     */
    private ObjectNode() {
        
    }
    
    public ObjectNode(String name, String type, INode parent, Image image) {
        _type = type;
        _name = name;
        _sessionNode = parent.getSession();
        _parent = parent;
        _image = image;
    }
    

    /**
     * This node cannot have childnodes.
     */
    public boolean isEndNode() {
        return true;
    }

    /**
     * This node cannot have childnodes.
     */
    public void loadChildren() {
        return;
    }


    public String getType() {
        return _type;
    }

    public String getQualifiedName() {
        return "\"" + getSchemaOrCatalogName() + "\".\"" + getName() + "\"";
    }

}
