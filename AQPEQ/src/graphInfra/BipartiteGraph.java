package graphInfra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BipartiteGraph {
	
	public BipartiteGraph() {
		
	}

	static int maxCurrentNodeId = 0;
	static int maxCurrentRelId = 0;

	public enum BipartiteSide {
		USide, VSide
	};

	/// we keep all bipartite nodes in here as a list
	ArrayList<NodeInfra> allBipartiteNodes = new ArrayList<NodeInfra>();

	// one side of nodes is in this set let's call it U
	ArrayList<Integer> uNodes = new ArrayList<Integer>();

	// another side of nodes is in this set let's call it V
	ArrayList<Integer> vNodes = new ArrayList<Integer>();

	// We also keep relationships
	ArrayList<RelationshipInfra> rels = new ArrayList<RelationshipInfra>();

	// to add a new bipartite node and get that node, call this function
	public NodeInfra getAndAddANewBipartiteNodeInfra(int tokenId, float weight, BipartiteSide bipartiteSide) {
		int currNodeId = maxCurrentNodeId;

		NodeInfra nodeInfra = new NodeInfra(currNodeId);
		nodeInfra.addToken(tokenId);
		nodeInfra.weight = weight;
		nodeInfra.outgoingRelIdOfSourceNodeId = new HashMap<>();

		if (bipartiteSide == BipartiteSide.USide) {
			uNodes.add(nodeInfra.nodeId);
		} else {
			vNodes.add(nodeInfra.nodeId);
		}

		allBipartiteNodes.add(nodeInfra);
		maxCurrentNodeId++;

		return nodeInfra;
	}

	// to add a new relationship for bipartite graph, call this one
	public void addBipartiteRelationship(int sourceId, int destId, float weight) {
		int currentRelId = maxCurrentRelId;
		rels.add(new RelationshipInfra(currentRelId, sourceId, destId, weight, null, null));
		allBipartiteNodes.get(sourceId).getOutgoingRelIdOfSourceNodeId().put(destId, currentRelId);
		allBipartiteNodes.get(destId).getOutgoingRelIdOfSourceNodeId().put(sourceId, currentRelId);
		maxCurrentRelId++;
	}

	public Set<Integer> getNextNodes(int nodeId) {
		if (allBipartiteNodes.size() <= nodeId) {
			return new HashSet<Integer>();
		}

		return allBipartiteNodes.get(nodeId).getOutgoingRelIdOfSourceNodeId().keySet();
	}

	public ArrayList<Integer> getNodesOfSide(BipartiteSide side) {
		if (side == BipartiteSide.USide) {
			return uNodes;
		} else {
			return vNodes;
		}
	}

	public HashSet<Integer> getAllNodesInSet() {
		HashSet<Integer> set = new HashSet<>();
		set.addAll(uNodes);
		set.addAll(vNodes);
		return set;
	}

	public ArrayList<NodeInfra> getAllNodes() {
		return allBipartiteNodes;
	}

	public NodeInfra getNodeById(int nodeId) {
		return allBipartiteNodes.get(nodeId);
	}

	public void printTheGraph() {

		System.out.println("U size: " + uNodes.size());
		System.out.println("V size: " + vNodes.size());
		System.out.println("E size: " + rels.size());

		System.out.print("U: ");
		for (Integer uNode : uNodes) {
			System.out.print(uNode + ", ");
		}
		System.out.println();

		System.out.print("V: ");
		for (Integer vNode : vNodes) {
			System.out.print(vNode + ", ");
		}
		System.out.println();

		System.out.print("Rels: ");
		for (RelationshipInfra rel : rels) {
			if (rel.sourceId <= rel.destId)
				System.out.print(rel.sourceId + " -> " + rel.destId + "; ");
		}
		System.out.println();
	}

	public int getNodeCount() {
		return allBipartiteNodes.size();
	}

	public int getEdgeCount() {
		return rels.size();
	}

	public static void main(String[] args) {

	}

}
