package neo4jBasedKWS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableUndirectedGraph;

import java.util.Queue;

import aqpeq.utilities.StringPoolUtility;
import aqpeq.utilities.TreeNode;
import graphInfra.GraphInfraReaderArray;
import graphInfra.RelationshipInfra;

public class ResultTree implements Cloneable{

	public Double cost = 0d;
	public ListenableUndirectedGraph<ResultNode, RelationshipInfra> anOutputTree = new ListenableUndirectedGraph<ResultNode, RelationshipInfra>(
			RelationshipInfra.class);
	public ResultNode rootNode;
	private HashMap<String, HashSet<Integer>> originsOfKeywords;
	private HashMap<Integer, ArrayList<Integer>> pathOfRootToTheOrigin;
	public HashMap<Integer, ResultNode> createdTreeNode = new HashMap<Integer, ResultNode>();

	/**
	 * resultTree - creates a tree of all the results from the cross product
	 * that were found
	 * 
	 * @param costOfPathFromOriginId
	 * 
	 * @param dataGraph
	 * 
	 * @param value
	 *            - ArrayList of node ID's that shows the path to each node
	 * @throws Exception
	 */
	// public void resultTree(HashMap<String, HashSet<Long>>
	// keywordNodeIdsParticipatingInTree,
	// HashMap<String, HashSet<Long>> originsOfKeywords, HashMap<Long,
	// ArrayList<Long>> pathOfRootToTheOrigin) {
	//
	// TreeNode<Long> temporaryParent = treeNode;
	//
	// HashMap<Long, TreeNode<Long>> createdTreeNode = new HashMap<Long,
	// TreeNode<Long>>();
	//
	// boolean rootHasInit = false;
	// for (String keyword : keywordNodeIdsParticipatingInTree.keySet()) {
	// Long keywordNodeId =
	// keywordNodeIdsParticipatingInTree.get(keyword).iterator().next();
	// ArrayList<Long> pathStartingFromKeywordNodeToTheRoot =
	// pathOfRootToTheOrigin.get(keywordNodeId);
	// for (int j = (pathStartingFromKeywordNodeToTheRoot.size() - 1); j >= 0;
	// j--) {
	//
	// if (!rootHasInit) {
	// treeNode.setData(pathStartingFromKeywordNodeToTheRoot.get(j));
	// createdTreeNode.put(pathStartingFromKeywordNodeToTheRoot.get(j),
	// treeNode);
	// rootHasInit = true;
	// }
	//
	// if
	// (!createdTreeNode.containsKey(pathStartingFromKeywordNodeToTheRoot.get(j)))
	// {
	// TreeNode<Long> childNode = new TreeNode<Long>();
	// childNode.setData(pathStartingFromKeywordNodeToTheRoot.get(j));
	// temporaryParent.addChildren(childNode);
	// createdTreeNode.put(pathStartingFromKeywordNodeToTheRoot.get(j),
	// childNode);
	// }
	//
	// temporaryParent =
	// createdTreeNode.get(pathStartingFromKeywordNodeToTheRoot.get(j));
	// }
	// }
	//
	// }

	public void resultTree(GraphInfraReaderArray graph,
			HashMap<String, HashSet<Integer>> keywordNodeIdsParticipatingInTree,
			HashMap<String, HashSet<Integer>> originsOfKeywords,
			HashMap<Integer, ArrayList<Integer>> pathOfRootToTheOrigin, HashMap<Integer, Double> costOfPathFromOriginId)
			throws Exception {

		this.originsOfKeywords = originsOfKeywords;
		this.pathOfRootToTheOrigin = pathOfRootToTheOrigin;

		HashMap<String, ResultNode> resultNodeOfKeyword = new HashMap<String, ResultNode>();

		boolean rootHasInit = false;
		for (String keyword : keywordNodeIdsParticipatingInTree.keySet()) {
			ResultNode temporaryParent = null;
			int keywordNodeId = keywordNodeIdsParticipatingInTree.get(keyword).iterator().next();
			ArrayList<Integer> pathStartingFromKeywordNodeToTheRoot = pathOfRootToTheOrigin.get(keywordNodeId);

			for (int j = (pathStartingFromKeywordNodeToTheRoot.size() - 1); j >= 0; j--) {
				ResultNode currentNode;
				if (!rootHasInit) {
					int nodeId = pathStartingFromKeywordNodeToTheRoot.get(j);
					String labels = "";
					for (int tokenId : graph.nodeOfNodeId.get(nodeId).getTokens()) {
						labels += StringPoolUtility.getStringOfId(tokenId).toLowerCase() + ", ";
					}
					currentNode = new ResultNode(labels, nodeId, graph.nodeOfNodeId.get(nodeId));
					rootNode = currentNode;
					anOutputTree.addVertex(currentNode);
					createdTreeNode.put(pathStartingFromKeywordNodeToTheRoot.get(j), currentNode);
					rootHasInit = true;
				}

				if (!createdTreeNode.containsKey(pathStartingFromKeywordNodeToTheRoot.get(j))) {
					int nodeId = pathStartingFromKeywordNodeToTheRoot.get(j);
					String labels = "";
					for (int tokenId : graph.nodeOfNodeId.get(nodeId).getTokens()) {
						labels += StringPoolUtility.getStringOfId(tokenId).toLowerCase() + ", ";
					}
					currentNode = new ResultNode(labels, nodeId, graph.nodeOfNodeId.get(nodeId));
					anOutputTree.addVertex(currentNode);
					createdTreeNode.put(pathStartingFromKeywordNodeToTheRoot.get(j), currentNode);
				} else {
					currentNode = createdTreeNode.get(pathStartingFromKeywordNodeToTheRoot.get(j));
				}

				if (temporaryParent != null && !anOutputTree.containsEdge(temporaryParent, currentNode)) {
					RelationshipInfra relInfra = null;

					// if
					// (graph.nodeOfNodeId.get(temporaryParent.nodeId).getOutgoingRelIdOfSourceNodeId()
					// .containsKey(currentNode.nodeId)) {
					relInfra = graph.relationOfRelId.get(graph.nodeOfNodeId.get(currentNode.nodeId)
							.getOutgoingRelIdOfSourceNodeId().get(temporaryParent.nodeId));
					// } else if
					// (graph.nodeOfNodeId.get(currentNode.nodeId).getOutgoingRelIdOfSourceNodeId()
					// .containsKey(temporaryParent.nodeId)) {
					// relInfra =
					// graph.relationOfRelId.get(graph.nodeOfNodeId.get(currentNode.nodeId)
					// .getOutgoingRelIdOfSourceNodeId().get(temporaryParent.nodeId));
					// }

					anOutputTree.addEdge(currentNode, temporaryParent, relInfra);

				}

				temporaryParent = createdTreeNode.get(pathStartingFromKeywordNodeToTheRoot.get(j));

			}
			resultNodeOfKeyword.put(keyword, createdTreeNode.get(pathStartingFromKeywordNodeToTheRoot.get(0)));

		}

		this.cost = 0d;
		for (String keyword : keywordNodeIdsParticipatingInTree.keySet()) {
			int keywordNodeId = keywordNodeIdsParticipatingInTree.get(keyword).iterator().next();
			ArrayList<Integer> pathStartingFromKeywordNodeToTheRoot = pathOfRootToTheOrigin.get(keywordNodeId);

			for (int j = 0; j < (pathStartingFromKeywordNodeToTheRoot.size() - 1); j++) {
				int relId = graph.nodeOfNodeId.get(pathStartingFromKeywordNodeToTheRoot.get(j))
						.getOutgoingRelIdOfSourceNodeId().get(pathStartingFromKeywordNodeToTheRoot.get(j + 1));
				RelationshipInfra relInfra = graph.relationOfRelId.get(relId);
				this.cost += relInfra.weight;
			}
		}

		// if (this.anOutputTree.vertexSet().size() == 6 &&
		// this.anOutputTree.edgeSet().size() == 5) {
		// int i=0;
		// //1654782, 39773,1654776,46848,103747,80898
		// }

		// HashSet<Integer> nodeIdsParticipant = new HashSet<Integer>();
		// // nodeIdsParticipant
		// for (HashSet<Integer> set :
		// keywordNodeIdsParticipatingInTree.values()) {
		// nodeIdsParticipant.addAll(set);
		// }

		// for (int originId : costOfPathFromOriginId.keySet()) {
		// if (nodeIdsParticipant.contains(originId)) {
		// cost += costOfPathFromOriginId.get(originId);
		// }
		// }

		// FloydWarshallShortestPaths<ResultNode, RelationshipInfra>
		// floydWarshal = new FloydWarshallShortestPaths<ResultNode,
		// RelationshipInfra>(
		// anOutputTree);
		//
		// for (String keyword1 : resultNodeOfKeyword.keySet()) {
		// for (String keyword2 : resultNodeOfKeyword.keySet()) {
		// if (!keyword1.equals(keyword2)) {
		// cost +=
		// floydWarshal.shortestDistance(resultNodeOfKeyword.get(keyword1),
		// resultNodeOfKeyword.get(keyword2));
		// }
		// }
		// }
		//
		// cost /= 2;

	}

}
