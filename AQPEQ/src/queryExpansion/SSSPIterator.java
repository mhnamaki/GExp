package queryExpansion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import org.apache.xerces.dom.DOMOutputImpl;

import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class SSSPIterator {

	GraphInfraReaderArray graph;
	public int originNodeId;
	SSSPNodeComparator comparator = new SSSPNodeComparator();
	PriorityQueue<SSSPNode> frontier = new PriorityQueue<SSSPNode>(comparator);
	HashMap<Integer, Double> costOfFrontierNodeId = new HashMap<Integer, Double>();
	HashSet<Integer> explored = new HashSet<Integer>();
	HashMap<Integer, SSSPNode> createdExtendedNodeOfNodeId;
	int distanceBound;

	public SSSPIterator(GraphInfraReaderArray graph, int originNodeId, int distanceBound) {

		this.distanceBound = distanceBound;
		this.originNodeId = originNodeId;
		this.graph = graph;

		// node ← start
		// cost ← 0
		// frontier ← priority queue containing node only
		// explored ← empty set

		NodeInfra node = graph.nodeOfNodeId.get(originNodeId);
		double cost = 0;
		SSSPNode ssspNode = new SSSPNode(node, 0, 0);

		// frontier ← priority queue containing node only
		frontier.add(ssspNode);
		costOfFrontierNodeId.put(ssspNode.node.nodeId, 0d);
	}

	// procedure UniformCostSearch(Graph, start, goal)

	public double peekDist() {
		if (frontier.isEmpty() || frontier.peek().costFromOriginId > distanceBound) {
			return Double.MAX_VALUE;
		}

		return frontier.peek().costFromOriginId;
	}

	public SSSPNode getNextSSSPNode() {
		// do
		// if frontier is empty
		// return
		if (frontier.isEmpty()) {
			return null;
		}
		SSSPNode ssspNode = getNextNode();
		return ssspNode;
	}

	private SSSPNode getNextNode() {

		// node ← frontier.pop()
		// get the node with closest distance to the origin
		SSSPNode ssspNode = frontier.poll();
		costOfFrontierNodeId.remove(ssspNode.node.nodeId);
		explored.add(ssspNode.node.nodeId);

		if (ssspNode.costFromOriginId > distanceBound) {
			return null;
		}

		if (ssspNode.costFromOriginId >= distanceBound)
			return ssspNode;

		// for each of node's neighbors n
		// backward search but backward edges have been added
		for (int otherNodeId : ssspNode.node.getOutgoingRelIdOfSourceNodeId().keySet()) {

			NodeInfra otherNodeInfra = graph.nodeOfNodeId.get(otherNodeId);

			RelationshipInfra rel = graph.relationOfRelId
					.get(ssspNode.node.getOutgoingRelIdOfSourceNodeId().get(otherNodeId));

			SSSPNode otherSSSPNode = new SSSPNode(otherNodeInfra, ssspNode.costFromOriginId + rel.weight,
					ssspNode.distanceFromOriginId + 1);

			// if n is not in explored or frontier
			if (!explored.contains(otherNodeInfra.nodeId) && !costOfFrontierNodeId.containsKey(otherNodeInfra.nodeId)) {

				// frontier.add(n)
				frontier.add(otherSSSPNode);
				costOfFrontierNodeId.put(otherNodeInfra.nodeId, otherSSSPNode.costFromOriginId);

				// replace existing
				// node with n
				// else if n is in frontier with higher cost
			} else if (costOfFrontierNodeId.containsKey(otherNodeInfra.nodeId)
					&& otherSSSPNode.costFromOriginId < costOfFrontierNodeId.get(otherNodeInfra.nodeId)) {

				Iterator<SSSPNode> itr = frontier.iterator();
				while (itr.hasNext()) {
					SSSPNode fNode = itr.next();
					if (fNode.node.nodeId == otherNodeInfra.nodeId) {
						itr.remove();
						costOfFrontierNodeId.remove(fNode.node.nodeId);
						frontier.add(otherSSSPNode);
						costOfFrontierNodeId.put(otherSSSPNode.node.nodeId, otherSSSPNode.costFromOriginId);

					}
				}
			}

		}

		return ssspNode;
	}

	public boolean hasMoreNodesToOutput() {
		if (frontier.isEmpty())
			return false;
		else
			return true;
	}
}

class SSSPNode {
	NodeInfra node;
	public double costFromOriginId = 0d;
	public double distanceFromOriginId = 0d;

	public SSSPNode(NodeInfra node, double costFromOriginId, double distanceFromOriginId) {
		this.node = node;
		this.costFromOriginId = costFromOriginId;
		this.distanceFromOriginId = distanceFromOriginId;
	}

}
