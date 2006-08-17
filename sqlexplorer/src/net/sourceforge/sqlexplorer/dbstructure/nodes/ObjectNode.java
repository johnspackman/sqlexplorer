package net.sourceforge.sqlexplorer.dbstructure.nodes;


public class ObjectNode extends AbstractNode {

    private String _type;
    
    /**
     * Hidden default constructor.
     */
    private ObjectNode() {
        
    }
    
    public ObjectNode(String type) {
        _type = type;
    }
    
    public String getUniqueIdentifier() {

        // TODO Auto-generated method stub
        return null;
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

}
