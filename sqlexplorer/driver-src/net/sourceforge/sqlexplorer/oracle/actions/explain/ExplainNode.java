/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.oracle.actions.explain;

import java.util.ArrayList;
import java.util.List;

public class ExplainNode {

    int cardinality, cost;

    int id, parent_id;
    
    int level;

    List<ExplainNode> ls = new ArrayList<ExplainNode>();

    String object_type, operation, options, object_owner, object_name, optimizer;

    ExplainNode parent;


    public ExplainNode(ExplainNode parent) {

        this.parent = parent;
    }


    public void add(ExplainNode nd) {

        ls.add(nd);
    }


    /**
     * @return
     */
    public int getCardinality() {

        return cardinality;
    }


    public ExplainNode[] getChildren() {

        return (ExplainNode[]) ls.toArray(new ExplainNode[ls.size()]);
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
    public int getLevel() {

        return level;
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


    public ExplainNode getParent() {

        return parent;
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
     * @param i
     */
    public void setLevel(int i) {

        level = i;
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

}
