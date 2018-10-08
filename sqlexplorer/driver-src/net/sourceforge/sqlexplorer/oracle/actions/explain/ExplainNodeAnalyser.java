/*
 * Copyright (C) 2007 Patrac Vlad Sebastian
 * http://sourceforge.net/projects/eclipsesql
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

/**
 * Computes several statistics for ExplainNode trees 
 * 
 * @author Patras Vlad
 */

package net.sourceforge.sqlexplorer.oracle.actions.explain;

import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Point;

public class ExplainNodeAnalyser {
	
	/**
	 * The node to use
	 */
	protected ExplainNode _node = null;
	
	/**
	 * Keeps the span of each node by it's index
	 */
	protected HashMap<Integer, Point[]> _nodeSpan;
	
	/**
	 * Keeps the junction of nodes (the minimum distance between
	 * 		them so theyr children do not overlap
	 */
	protected HashMap<Point, Integer> _nodeJunction;
	
	/**
	 * Keeps all nodes indexed by their id 
	 */
	protected HashMap<Integer, ExplainNode> _nodeList;
	
	protected int _maxLevel;

	public ExplainNodeAnalyser() {
	}

	public ExplainNodeAnalyser(ExplainNode node) {
		setNode(node);
	}

	/**
	 * @return the node
	 */
	public ExplainNode getNode() {
		return _node;
	}
	
	/**
	 * @return the max level
	 */
	public int getMaxLevel() {
		return _maxLevel;
	}
	
	/**
	 * @return the computed number of nodes
	 */
	public int getNodesNumber() {
		return _nodeList.size();
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(ExplainNode node) {
		_node = node;
	}
	
	/**
	 * Searches for the node's id in computed spans
	 * and returns it's span if it was found
	 * 
	 * @param node node to search for
	 * @return span of node, null if it's id does not exist
	 */
	public Point[] getNodeSpan(ExplainNode node) {
		
		return _nodeSpan.get(node.getId());
	}

	/**
	 * Searches for id's of nodes in computed junctions
	 * and returns their junction if it was found
	 * 
	 * @param left left node to search for
	 * @param right right node to search for
	 * @return junction of nodes, null if it's id does not exist
	 */	
	public int getNodeJunction(ExplainNode left, ExplainNode right) {
		
		return _nodeJunction.get(new Point(left.getId(), right.getId()));
	}
	
	/**
	 * Computes information for specified node.
	 * Node must be set
	 */
	public void compute() {
		
		_nodeList = new HashMap<Integer, ExplainNode>();
		listNodes(_node);
		_nodeSpan = new HashMap<Integer, Point[]>(_nodeList.size());
		
		_maxLevel = 1;
		for (ExplainNode n : _nodeList.values()) {
			_maxLevel = (_maxLevel < n.getLevel()) ? n.getLevel() : _maxLevel;
		}
		
		//fil spans
		for (ExplainNode n : _nodeList.values()) {
			
			Point[] newSpan = new Point[_maxLevel];

			computeNodeSpan(n, newSpan, 0);
			_nodeSpan.put(n.getId(), newSpan);		
		}
		
		//fill node junctions for nodes on the same level
		_nodeJunction = new HashMap<Point, Integer>();		
		
		for (Entry<Integer, Point[]> e1 : _nodeSpan.entrySet()) {
			for (Entry<Integer, Point[]> e2 : _nodeSpan.entrySet()) {	
				
				if (_nodeList.get(e1.getKey()).getLevel() == _nodeList.get(e2.getKey()).getLevel())
				_nodeJunction.put(new Point(e1.getKey(), e2.getKey()),
						findNodeJunction(e1.getValue(), e2.getValue()));
			}
		}
	}

	private void listNodes(ExplainNode node) {
		
		_nodeList.put(node.getId(), node);
		
		for (ExplainNode n : node.getChildren()) {
			
			listNodes(n);
		}
	}	
	
	/**
	 * Computes left (to x member) and right (to y member) spans of a node
	 * for each level below it's level (above levels are not set)
	 * 
	 * @parm node node to compute spans for
	 * @param span Array of Point objects to be filled with spans, the index
	 * 		is asociated with the level. The x any y members of Point object
	 * 		will be the left and right spans.
	 * 		The array must be large enough for all levels.
	 */
    public static void computeNodeSpan(final ExplainNode node, Point[] span, int offset) {
    	
    	ExplainNode[] children = node.getChildren();
    	
    	int level = node.getLevel() - 1;
    	
    	if (span[level] == null) {
    		span[level] = new Point(0, 0);
    	}
    	if (span[level].x > offset) {
    		span[level].x = offset; 
    	}
    	if (span[level].y < offset) {
    		span[level].y = offset; 
    	}    	
    	
    	for (int i=0; i<children.length; ++i) {
    		computeNodeSpan(children[i], span, offset + (i*2 - (children.length-1)));
    	}

    }	
	
	/**
	 * Computes the minimum distance between two nodes, specified by their spans
	 * 
	 * @param left left node
	 * @param right right node
	 * 
	 * @return minimum disance between the nodes
	 */
    public static int findNodeJunction(Point[] left, Point right[]) {
    	
    	int junction = 0;
    	
    	for (int i=0; i<left.length; ++i) {
    		
    		if (left[i] != null && right[i] != null && junction < left[i].y - right[i].x) {
    			junction = left[i].y - right[i].x;
    		}
    	}
    	return junction;
    }	
}
