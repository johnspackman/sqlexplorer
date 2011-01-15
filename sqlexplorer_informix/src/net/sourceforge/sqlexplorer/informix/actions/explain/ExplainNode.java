package net.sourceforge.sqlexplorer.informix.actions.explain;

import java.util.ArrayList;
import java.util.List;

public class ExplainNode {

    public String toString() {
        StringBuffer sb = new StringBuffer(50);
        if (object_type != null) {
            sb.append(object_type).append(" ");
        }
        if (operation != null) {
            sb.append(operation).append(" ");
        }
        if (options != null) {
            sb.append(options).append(" ");
        }
        if (object_owner != null && object_name != null)
            sb.append(object_owner + "." + object_name).append(" ");
        if (optimizer != null) {
            sb.append("[" + optimizer + "]");
        }
        return sb.toString();
    }

    ExplainNode parent;


    public ExplainNode(ExplainNode parent) {
        this.parent = parent;
    }


    public ExplainNode getParent() {
        return parent;
    }

    List<ExplainNode> ls = new ArrayList<ExplainNode>();


    public ExplainNode[] getChildren() {
        return (ExplainNode[]) ls.toArray(new ExplainNode[ls.size()]);
    }


    public void add(ExplainNode nd) {
        ls.add(nd);
    }

    String object_type, operation, options, object_owner, object_name, optimizer;

    int cardinality, cost;

    int id, parent_id;


    /**
     * @return
     */
    public int getCardinality() {
        return cardinality;
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


    /**
     * @return
     */
    public String getObject_owner() {
        return object_owner;
    }


    /**
     * @return
     */
    public String getObject_type() {
        return object_type;
    }


    /**
     * @return
     */
    public String getOperation() {
        return operation;
    }


    /**
     * @return
     */
    public String getOptimizer() {
        return optimizer;
    }


    /**
     * @return
     */
    public String getOptions() {
        return options;
    }


    /**
     * @param i
     */
    public void setCardinality(int i) {
        cardinality = i;
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


    /**
     * @param string
     */
    public void setObject_owner(String string) {
        object_owner = string;
    }


    /**
     * @param string
     */
    public void setObject_type(String string) {
        object_type = string;
    }


    /**
     * @param string
     */
    public void setOperation(String string) {
        operation = string;
    }


    /**
     * @param string
     */
    public void setOptimizer(String string) {
        optimizer = string;
    }


    /**
     * @param string
     */
    public void setOptions(String string) {
        options = string;
    }
	
}