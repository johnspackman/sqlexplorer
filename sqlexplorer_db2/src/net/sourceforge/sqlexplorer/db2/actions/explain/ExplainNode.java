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
package net.sourceforge.sqlexplorer.db2.actions.explain;

import java.util.ArrayList;
import java.util.List;

/**
 * ExplainNode represents a cost node in the DB2 Explain plan Tree view. 
 * 
 * @modified Davy Vanherbergen
 */
public class ExplainNode {

	private Double columnCount;

	private Double cpuCost;

	private Double firstRowCost;

	int id, parent_id;

	private Double ioCost;

	List<ExplainNode> ls = new ArrayList<ExplainNode>();

	String object_type, operation, options, object_owner, object_name, optimizer;

	ExplainNode parent;

	private Double streamCount;

	private Double totalCost;

	public ExplainNode(ExplainNode parent) {
		this.parent = parent;
	}

	public void add(ExplainNode nd) {
		ls.add(nd);
	}

	public ExplainNode[] getChildren() {
		return (ExplainNode[]) ls.toArray(new ExplainNode[ls.size()]);
	}

	public Double getColumnCount() {
		return columnCount;
	}

	/**
	 * Calculate the cost for an element in the explain plan. DB2 doesn't
	 * provide it, so we try to calculate this based on the total cost of this
	 * node, minus the cost of all child nodes.
	 * 
	 * @return estimated cost of this node.
	 */
	public Double getCost() {

		if (getTotalCost() == null) {
			return null;
		}

		if (getChildren().length == 0) {
			return getTotalCost();
		}

		double childCost = 0;

		for (ExplainNode child : getChildren()) {
			if (child.getTotalCost() != null) {
				childCost = childCost + child.getTotalCost().doubleValue();
			}
		}

		double cost = new Double(getTotalCost().doubleValue() - childCost);
		return cost > 0 ? cost : 0;
	}

	public Double getCpuCost() {
		return cpuCost;
	}

	public Double getFirstRowCost() {
		return firstRowCost;
	}

	public int getId() {
		return id;
	}

	public Double getIoCost() {
		return ioCost;
	}

	public String getObject_name() {
		return object_name;
	}

	public String getObject_owner() {
		return object_owner;
	}

	public String getObject_type() {
		return object_type;
	}

	public String getOperation() {
		return operation;
	}

	public String getOptimizer() {
		return optimizer;
	}

	public String getOptions() {
		return options;
	}

	public ExplainNode getParent() {
		return parent;
	}

	public Double getStreamCount() {
		return streamCount;
	}

	public Double getTotalCost() {
		return totalCost;
	}

	/**
	 * Checks if this entry in the explain plan is costly. Costly means that
	 * more than 30% of the total query time is spent on this node in the
	 * explain plan.
	 * 
	 * @return true when this node accounts for more than 30% of the total query
	 *         cost.
	 */
	public boolean isCostly() {

		ExplainNode root = this;
		while (root.getParent() != null && root.getParent().getTotalCost() != null) {
			root = root.getParent();
		}

		if (root.getTotalCost() != null && getCost() != null) {

			if ((getCost().doubleValue() / root.getTotalCost().doubleValue()) * 100 > 30) {
				return true;
			}
		}

		return false;
	}

	public void setColumnCount(Double columnCount) {
		this.columnCount = columnCount;
	}

	public void setCpuCost(Double cpuCost) {
		this.cpuCost = cpuCost;
	}

	public void setFirstRowCost(Double firstRowCost) {
		this.firstRowCost = firstRowCost;
	}

	public void setId(int i) {
		id = i;
	}

	public void setIoCost(Double ioCost) {
		this.ioCost = ioCost;
	}

	public void setObject_name(String string) {
		object_name = string;
	}

	public void setObject_owner(String string) {
		object_owner = string;
	}

	public void setObject_type(String string) {
		object_type = string;
	}

	public void setOperation(String string) {
		operation = string;
	}

	public void setOptimizer(String string) {
		optimizer = string;
	}

	public void setOptions(String string) {
		options = string;
	}

	public void setStreamCount(Double streamCount) {
		this.streamCount = streamCount;
	}

	public void setTotalCost(Double totalCost) {
		this.totalCost = totalCost;
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
			sb.append(object_owner.trim() + "." + object_name).append(" ");
		if (optimizer != null) {
			sb.append("[" + optimizer + "]");
		}
		return sb.toString();
	}
}
