package baselines;

import java.util.HashSet;
import java.util.List;

import graphInfra.GraphInfraReaderArray;

public class QBEGraph {

	public HashSet<Integer> nodes = new HashSet<Integer>();
	public HashSet<Integer> relationships = new HashSet<Integer>();

	public QBEGraph() {

	}

	public int getNumberOfEdges() {
		return relationships.size();
	}

	public void union(QBEGraph graph) {

	}

	public void union(HashSet<Integer> visitedNodes, HashSet<Integer> visitedEdges) {
		nodes.addAll(visitedNodes);
		nodes.addAll(visitedNodes);

	}

	public void createByEdges(GraphInfraReaderArray graph, List<RelAndWeightPair> edgesList) {

		for (RelAndWeightPair relWeightPair : edgesList) {
			int relId = relWeightPair.getRelId();
			relationships.add(relId);
			nodes.add(graph.relationOfRelId.get(relId).sourceId);
			nodes.add(graph.relationOfRelId.get(relId).destId);
		}

	}

}
