package bank.keywordSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class DijkstraRunner {

	GraphInfraReaderArray graph;
	int nextNodeId = 0;
	public int originNodeId;
	public String originNodeKeyword;
	boolean hasInitialized = false;
	ExtendedNodeComparator comparator = new ExtendedNodeComparator();
	PriorityQueue<ExtendedNode> frontier = new PriorityQueue<ExtendedNode>(comparator);
	HashSet<Integer> explored = new HashSet<Integer>();
	HashMap<Integer, ExtendedNode> createdExtendedNodeOfNodeId;
	int distanceBound;
	boolean debugMode;
	HashSet<Integer> candidatesSet = new HashSet<Integer>();

	public DijkstraRunner(GraphInfraReaderArray graph, HashMap<Integer, ExtendedNode> createdExtendedNodeOfNodeId,
			int originNodeId, String keyword, int distanceBound, boolean debugMode, HashSet<Integer> candidatesSet) {
		this.graph = graph;
		this.originNodeId = originNodeId;
		this.originNodeKeyword = keyword;
		// origin.put(cleanedNodeIdOfLabel.get(keyword), originNodeId);
		this.createdExtendedNodeOfNodeId = createdExtendedNodeOfNodeId;
		this.distanceBound = distanceBound;
		this.debugMode = debugMode;
		this.candidatesSet = candidatesSet;

		// Node node = dataGraph.getNodeById(originNodeId);
		NodeInfra node = graph.nodeOfNodeId.get(originNodeId);
		double cost = 0;
		// HashMap<String, ExtendedNode> exNodeMap = new HashMap<String,
		// ExtendedNode>();
		ExtendedNode exNode = new ExtendedNode(createdExtendedNodeOfNodeId, null, node, cost, debugMode, originNodeId);
		exNode.originsOfKeywords.put(this.originNodeKeyword, new HashSet<>());
		exNode.originsOfKeywords.get(originNodeKeyword).add(this.originNodeId);

		// frontier ← priority queue containing node only
		frontier.add(exNode);

		hasInitialized = true;

	}

	public ExtendedNode getNextExtendedNode() {

		if (frontier.isEmpty()) {
			return null;
		}
		ExtendedNode exNode = getNextNode();
		return exNode;
	}

	public String getKeyword() {
		return this.originNodeKeyword;
	}

	private ExtendedNode getNextNode() {
		// get the node with closest distance to the origin
		ExtendedNode exNode = frontier.poll();

		int exNodeId = exNode.node.nodeId;
		explored.add(exNode.node.nodeId);

		// check if the distance bound is exceeded
		if (exNode.distanceFromOriginId.get(originNodeId) >= distanceBound)
			return exNode;

//		for (int anyOriginId : exNode.distanceFromOriginId.keySet()) {
//			if (exNode.distanceFromOriginId.get(anyOriginId) >= distanceBound) {
//				int i=0;
//				i++;
//			}
//		}

		// backward search but backward edges have been added
		for (int otherNodeId : exNode.node.getOutgoingRelIdOfSourceNodeId().keySet()) {

			NodeInfra otherNode = graph.nodeOfNodeId.get(otherNodeId);

			RelationshipInfra rel = graph.relationOfRelId
					.get(exNode.node.getOutgoingRelIdOfSourceNodeId().get(otherNodeId));

			ExtendedNode otherExtendedNode = null;
			if (!explored.contains(otherNode.nodeId)) {

				if (createdExtendedNodeOfNodeId.containsKey(otherNode.nodeId)) {
					otherExtendedNode = createdExtendedNodeOfNodeId.get(otherNode.nodeId);

					otherExtendedNode.costOfPathFromOriginId.putIfAbsent(originNodeId,
							exNode.costOfPathFromOriginId.get(originNodeId) + rel.weight);

					otherExtendedNode.distanceFromOriginId.putIfAbsent(originNodeId,
							exNode.distanceFromOriginId.get(originNodeId) + 1);

					for (String originkeyword : exNode.originsOfKeywords.keySet()) {
						otherExtendedNode.originsOfKeywords.putIfAbsent(originkeyword, new HashSet<>());
						otherExtendedNode.originsOfKeywords.get(originkeyword)
								.addAll(exNode.originsOfKeywords.get(originkeyword));
					}

					for (int origId : exNode.pathOfRootToTheOrigin.keySet()) {
						if (!otherExtendedNode.pathOfRootToTheOrigin.containsKey(origId)) {
							otherExtendedNode.pathOfRootToTheOrigin.put(origId, new ArrayList<Integer>());
							otherExtendedNode.pathOfRootToTheOrigin.get(origId)
									.addAll(exNode.pathOfRootToTheOrigin.get(origId));
							otherExtendedNode.pathOfRootToTheOrigin.get(origId).add(otherNode.nodeId);
						}
					}

				} else {
					// COST: this should be the cost of each edge by default: 1.
					otherExtendedNode = new ExtendedNode(createdExtendedNodeOfNodeId, exNode, otherNode,
							exNode.costOfPathFromOriginId.get(originNodeId) + rel.weight, debugMode, originNodeId);

				}

				frontier.add(otherExtendedNode);

			} else if (frontier.contains(otherNode.nodeId)) {
				otherExtendedNode = createdExtendedNodeOfNodeId.get(otherNode.nodeId);

				for (ExtendedNode fNode : frontier) {
					if (fNode.node.nodeId == otherNode.nodeId) {
						if (otherExtendedNode.costOfPathFromOriginId.get(originNodeId) > fNode.costOfPathFromOriginId
								.get(originNodeId)) { // TODO:
							// shouldn't
							// cost
							// be
							// less?
							frontier.remove(otherExtendedNode);
							frontier.add(fNode);
							// if (debugMode) {
							// // TO DO
							// System.out
							// .println("fNode: " + fNode.node.nodeId + ",
							// label: " + fNode.node.getLabels());
							// }
						}
					}
				}
			}

		}

		return exNode;
	}

	public boolean hasMoreNodesToOutput() {
		if (frontier.isEmpty())
			return false;
		else
			return true;
	}

	@Override
	public String toString() {
		return "originNodeId: " + originNodeId + " originNodeKeyword:" + originNodeKeyword + " frontier.size:"
				+ frontier.size() + " explored:" + explored;
	}

}

class ExtendedNode {
	int originalOriginId;
	double costFromOriginalOriginId = 0d;
	double distanceFromOriginalOriginId = 0d;
	NodeInfra node;
	HashMap<Integer, Integer> distanceFromOriginId = new HashMap<Integer, Integer>();
	HashMap<Integer, Double> costOfPathFromOriginId = new HashMap<Integer, Double>();

	// key: keyword
	// value: ArrayList<Long>: ra.nodeId()
	// v.Li--for Tree-- contains tree # matched to arraylist of candidates found
	// TODO: why arraylist and not hashset?
	HashMap<String, HashSet<Integer>> originsOfKeywords = new HashMap<String, HashSet<Integer>>();
	// HashSet<Long> originsOfKeywords = new HashSet<Long>();

	HashMap<Integer, ArrayList<Integer>> pathOfRootToTheOrigin = new HashMap<Integer, ArrayList<Integer>>();// to
	// create
	// result
	// tree

	public ExtendedNode(HashMap<Integer, ExtendedNode> createdExtendedNodeOfNodeId, ExtendedNode parentExtendedNode,
			NodeInfra node, double cost, boolean debugMode, int originalOriginId) {
		this.node = node;
		this.originalOriginId = originalOriginId;

		if (parentExtendedNode != null) {

			// if (debugMode) {
			// System.out.println("init an extended node for node id: " +
			// node.getId()
			// + " and add info from it parent id:" +
			// parentExtendedNode.node.getId());
			// }
			// if (node.nodeId == 245759) {
			// System.out.println();
			// }

			for (String keyword : parentExtendedNode.originsOfKeywords.keySet()) {
				originsOfKeywords.putIfAbsent(keyword, new HashSet<Integer>());
				originsOfKeywords.get(keyword).addAll(parentExtendedNode.originsOfKeywords.get(keyword));
				// originsOfKeywords.get(keyword).add(node.getId());
			}

			for (int originateNodeId : parentExtendedNode.pathOfRootToTheOrigin.keySet()) {

				// if (originateNodeId == 46848 && node.nodeId == 1654782) {
				// int i = 0;
				// i++;
				// }

				pathOfRootToTheOrigin.putIfAbsent(originateNodeId, new ArrayList<Integer>());
				pathOfRootToTheOrigin.get(originateNodeId)
						.addAll(parentExtendedNode.pathOfRootToTheOrigin.get(originateNodeId));
				pathOfRootToTheOrigin.get(originateNodeId).add(node.nodeId);
			}

			for (int originateNodeId : parentExtendedNode.distanceFromOriginId.keySet()) {
				distanceFromOriginId.put(originateNodeId,
						parentExtendedNode.distanceFromOriginId.get(originateNodeId) + 1);
			}
			for (int originateNodeId : parentExtendedNode.costOfPathFromOriginId.keySet()) {
				costOfPathFromOriginId.put(originateNodeId,
						parentExtendedNode.costOfPathFromOriginId.get(originateNodeId) + cost);
			}

		} else {
			pathOfRootToTheOrigin.put(node.nodeId, new ArrayList<Integer>());
			pathOfRootToTheOrigin.get(node.nodeId).add(node.nodeId);
			distanceFromOriginId.put(node.nodeId, 0);
			costOfPathFromOriginId.put(node.nodeId, cost);

		}

		costFromOriginalOriginId = costOfPathFromOriginId.get(originalOriginId);
		distanceFromOriginalOriginId = distanceFromOriginId.get(originalOriginId);

		createdExtendedNodeOfNodeId.put(node.nodeId, this);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + node.nodeId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtendedNode other = (ExtendedNode) obj;
		if (this.node.nodeId != other.node.nodeId)
			return false;

		return true;
	}

	@Override
	public String toString() {
		String ss = "id:" + this.node.nodeId + " dist:" + this.distanceFromOriginId + " path:";
		for (int nodeId : pathOfRootToTheOrigin.keySet()) {
			ss += "orig:" + nodeId + "=>" + Arrays.toString(this.pathOfRootToTheOrigin.get(nodeId).toArray());
		}
		return ss;
	}
}
