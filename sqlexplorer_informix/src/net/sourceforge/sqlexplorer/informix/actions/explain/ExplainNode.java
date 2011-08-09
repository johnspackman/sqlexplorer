package net.sourceforge.sqlexplorer.informix.actions.explain;

import java.util.ArrayList;
import java.util.List;

public class ExplainNode {

    protected List<ExplainNode> ls = new ArrayList<ExplainNode>();
    ExplainNode parent;
    String object_name, tooltip;
    int cost = -1;
    int est_rows = -1;
    int id, parent_id;
    String dataID;
    String nodeType;

    public String toString() {
        StringBuffer sb = new StringBuffer(50);

        if (object_name != null)
            sb.append(object_name).append(" ");

        return sb.toString();
    }


    
    public ExplainNode(ExplainNode parent) {
        this.parent = parent;
    }

    public void setDataId(String dataID) {
    	this.dataID = dataID;
    }
    
    public void setType(String typeStr) {
    	this.nodeType = typeStr;
    }

    public String getType() {
    	return this.nodeType;
    }


    public String getDataId() {
    	return this.dataID;
    }

    public String getToolTipText() {
        return tooltip;
    }

    public ExplainNode getParent() {
        return parent;
    }

    public ExplainNode[] getChildren() {
        return (ExplainNode[]) ls.toArray(new ExplainNode[ls.size()]);
    }

    public void add(ExplainNode nd) {
        ls.add(nd);
    }

    /**
     * @return
     */
    public int getEstRows() {
        return est_rows;
    }


    /**
     * @return
     */
    public int getCost() {
        return cost;
    }


    /**
     * @return
     */
    public int getId() {
        return id;
    }


    /**
     * @return
     */
    public String getObject_name() {
        return object_name;
    }

    public void setToolTip(String s) {
        tooltip = s;
    }

    /**
     * @param i
     */
    public void setEstRows(int i) {
        est_rows = i;
    }

    /**
     * @param i
     */
    public void setCost(int i) {
        cost = i;
    }

    /**
     * @param i
     */
    public void setId(int i) {
        id = i;
    }

    /**
     * @param string
     */
    public void setObject_name(String string) {
        object_name = string;
    }
	
}
