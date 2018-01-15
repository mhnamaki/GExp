package neo4jBasedKWS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class DijkstraRunner {

	Long nextNodeId = 0l;
	public Long originNodeId;
	public String originNodeKeyword;
	boolean hasInitialized = false;
	ExtendedNodeComparator comparator = new ExtendedNodeComparator();
	PriorityQueue<ExtendedNode> frontier = new PriorityQueue<ExtendedNode>(comparator);
	HashSet<Long> explored = new HashSet<Long>();
	HashMap<Long, ExtendedNode> createdExtendedNodeOfNodeId;
	int distanceBound;
	boolean debugMode;
	HashSet<Long> candidatesSet = new HashSet<Long>();

	public DijkstraRunner(GraphDatabaseService dataGraph, HashMap<Long, ExtendedNode> createdExtendedNodeOfNodeId,
			Long originNodeId, String keyword, int distanceBound, boolean debugMode, HashSet<Long> candidatesSet,
			HashMap<String, String> cleanedNodeIdOfLabel) {
		this.originNodeId = originNodeId;
		this.originNodeKeyword = cleanedNodeIdOfLabel.get(keyword);
		// origin.put(cleanedNodeIdOfLabel.get(keyword), originNodeId);
		this.createdExtendedNodeOfNodeId = createdExtendedNodeOfNodeId;
		this.distanceBound = distanceBound;
		this.debugMode = debugMode;
		this.candidatesSet = candidatesSet;

		Node node = dataGraph.getNodeById(originNodeId);
		double cost = 0;
		// HashMap<String, ExtendedNode> exNodeMap = new HashMap<String,
		// ExtendedNode>();
		ExtendedNode exNode = new ExtendedNode(createdExtendedNodeOfNodeId, null, node, cost, debugMode);
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
		Long exNodeId = exNode.node.getId();
		explored.add(exNode.node.getId());

		// check if the distance bound is exceeded
		if (exNode.distanceFromOriginId.get(originNodeId) >= distanceBound)
			return exNode;

		for (Relationship rel : exNode.node.getRelationships()) {

			Node otherNode = rel.getOtherNode(exNode.node);

			// TODO: if delete this line, then neighbor keywords problem solve
			// if (candidatesSet.contains(otherNode.getId()))
			// continue;

			ExtendedNode otherExtendedNode = null;
			if (!explored.contains(otherNode.getId())) {

				if (createdExtendedNodeOfNodeId.containsKey(otherNode.getId())) {
					otherExtendedNode = createdExtendedNodeOfNodeId.get(otherNode.getId());

					// if (debugMode) {
					// System.out.println("update a previously seen node for
					// node id: " + otherNode.getId()
					// + " and update info from it parent id:" +
					// exNode.node.getId());
					// }

					otherExtendedNode.costOfPathFromOriginId.putIfAbsent(originNodeId,
							exNode.costOfPathFromOriginId.get(originNodeId) + 1);

					otherExtendedNode.distanceFromOriginId.putIfAbsent(originNodeId,
							exNode.distanceFromOriginId.get(originNodeId) + 1);

					// if (otherExtendedNode.node.getId() == 2348051) {
					// System.out.println();
					// }

					otherExtendedNode.originsOfKeywords.putIfAbsent(this.originNodeKeyword, new HashSet<>());
					otherExtendedNode.originsOfKeywords.get(this.originNodeKeyword).add(originNodeId);

					if (!otherExtendedNode.pathOfRootToTheOrigin.containsKey(originNodeId)) {
						otherExtendedNode.pathOfRootToTheOrigin.put(originNodeId, new ArrayList<Long>());
						otherExtendedNode.pathOfRootToTheOrigin.get(originNodeId)
								.addAll(exNode.pathOfRootToTheOrigin.get(originNodeId));
						otherExtendedNode.pathOfRootToTheOrigin.get(originNodeId).add(otherNode.getId());

					}

				} else {
					// COST: this should be the cost of each edge by default: 1.
					otherExtendedNode = new ExtendedNode(createdExtendedNodeOfNodeId, exNode, otherNode,
							exNode.costOfPathFromOriginId.get(originNodeId) + 1, debugMode);

				}

				frontier.add(otherExtendedNode);

			} else if (frontier.contains(otherNode.getId())) {
				otherExtendedNode = createdExtendedNodeOfNodeId.get(otherNode.getId());

				for (ExtendedNode fNode : frontier) {
					if (fNode.node.getId() == otherNode.getId()) {
						if (otherExtendedNode.costOfPathFromOriginId.get(originNodeId) > fNode.costOfPathFromOriginId
								.get(originNodeId)) {

							frontier.remove(otherExtendedNode);
							frontier.add(fNode);
							if (debugMode) {
								// TO DO
								System.out
										.println("fNode: " + fNode.node.getId() + ", label: " + fNode.node.getLabels());
							}
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
	Node node;
	HashMap<Long, Integer> distanceFromOriginId = new HashMap<Long, Integer>();
	HashMap<Long, Double> costOfPathFromOriginId = new HashMap<Long, Double>();

	// key: keyword
	// value: ArrayList<Long>: ra.nodeId()
	// v.Li--for Tree-- contains tree # matched to arraylist of candidates found
	// TODO: why arraylist and not hashset?
	HashMap<String, HashSet<Long>> originsOfKeywords = new HashMap<String, HashSet<Long>>();
	// HashSet<Long> originsOfKeywords = new HashSet<Long>();

	HashMap<Long, ArrayList<Long>> pathOfRootToTheOrigin = new HashMap<Long, ArrayList<Long>>();// to
																								// create
																								// result
																								// tree

	public ExtendedNode(HashMap<Long, ExtendedNode> createdExtendedNodeOfNodeId, ExtendedNode parentExtendedNode,
			Node node, double cost, boolean debugMode) {
		this.node = node;

		if (parentExtendedNode != null) {

			// if (debugMode) {
			// System.out.println("init an extended node for node id: " +
			// node.getId()
			// + " and add info from it parent id:" +
			// parentExtendedNode.node.getId());
			// }
			// if(node.getId()==2348051){
			// System.out.println();
			// }

			for (String keyword : parentExtendedNode.originsOfKeywords.keySet()) {
				originsOfKeywords.putIfAbsent(keyword, new HashSet<Long>());
				originsOfKeywords.get(keyword).addAll(parentExtendedNode.originsOfKeywords.get(keyword));
				// originsOfKeywords.get(keyword).add(node.getId());
			}

			for (Long originateNodeId : parentExtendedNode.pathOfRootToTheOrigin.keySet()) {
				pathOfRootToTheOrigin.putIfAbsent(originateNodeId, new ArrayList<Long>());
				pathOfRootToTheOrigin.get(originateNodeId)
						.addAll(parentExtendedNode.pathOfRootToTheOrigin.get(originateNodeId));
				pathOfRootToTheOrigin.get(originateNodeId).add(node.getId());
			}

			for (Long originateNodeId : parentExtendedNode.distanceFromOriginId.keySet()) {
				distanceFromOriginId.put(originateNodeId,
						parentExtendedNode.distanceFromOriginId.get(originateNodeId) + 1);
			}
			for (Long originateNodeId : parentExtendedNode.costOfPathFromOriginId.keySet()) {
				costOfPathFromOriginId.put(originateNodeId,
						parentExtendedNode.costOfPathFromOriginId.get(originateNodeId) + cost);
			}

		} else {
			pathOfRootToTheOrigin.put(node.getId(), new ArrayList<Long>());
			pathOfRootToTheOrigin.get(node.getId()).add(node.getId());
			distanceFromOriginId.put(node.getId(), 0);
			costOfPathFromOriginId.put(node.getId(), cost);

		}

		createdExtendedNodeOfNodeId.put(node.getId(), this);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) node.getId();
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
		if (this.node.getId() != other.node.getId())
			return false;

		return true;
	}

	@Override
	public String toString() {
		String ss = "id:" + this.node.getId() + " dist:" + this.distanceFromOriginId + " path:";
		for (Long nodeId : pathOfRootToTheOrigin.keySet()) {
			ss += "orig:" + nodeId + "=>" + Arrays.toString(this.pathOfRootToTheOrigin.get(nodeId).toArray());
		}
		return ss;
	}
}
