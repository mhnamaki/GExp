package bidirectional.keywordSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;
import org.apache.jena.ext.com.google.common.collect.MinMaxPriorityQueue;

import aqpeq.utilities.KWSUtilities;
import aqpeq.utilities.Dummy.DummyProperties;
import graphInfra.GraphInfraReaderArray;
import neo4jBasedKWS.ResultTree;
import neo4jBasedKWS.ResultTreeRelevanceComparator;

public class test2 {

	GraphInfraReaderArray graph;
	private static String graphInfraPath = "/Users/mnamaki/AQPEQ/GraphExamples/simpler/graph/";
	private static String keywordsPath = "/Users/mnamaki/AQPEQ/KeywordExamples/ABC.in";
	private int keywordSize = 3;
	private static int[] keywordSizes = { 1 };
	private double mu = 0.5;

	// A priority queue of nodes in backward expanding fringe
	// initialize = candidate set
	NodeComparator comparator = new NodeComparator();
	PriorityQueue<Node> qIn = new PriorityQueue<Node>(comparator);
	// PriorityQueue<Integer> queueIn;
	// A priority queue of node in forward expanding fringe
	// initialize = empty
	PriorityQueue<Node> qOut = new PriorityQueue<Node>(comparator);
	// Set of nodes expanded for incoming paths
	// initialize = empty
	HashSet<Node> xIn = new HashSet<Node>();
	// Set of nodes expanded for outgoing paths
	// initialize = empty
	HashSet<Node> xOut = new HashSet<Node>();

	// keywords
	ArrayList<String> keywords = new ArrayList<String>();
	// content nodes
	HashSet<Integer> contentNodes = new HashSet<Integer>();
	// value: keyword t_i value: S_i
	static HashMap<String, HashSet<Integer>> setS = new HashMap<String, HashSet<Integer>>();
	HashMap<Integer, HashSet<Integer>> nodeIdsOfToken = new HashMap<Integer, HashSet<Integer>>();

	static int heapSize = 1;
	static ResultTreeRelevanceComparator resultTreeRelevanceComparator = new ResultTreeRelevanceComparator();
	static MinMaxPriorityQueue<ResultTree> outputHeap = MinMaxPriorityQueue.orderedBy(resultTreeRelevanceComparator)
			.maximumSize(heapSize).create();

	static int dMax = 4;
	boolean spreadActivation = true; // true -> incoming outgoing -> outgoing
	HashMap<Integer, Node> constructedNodes = new HashMap<Integer, Node>();

	int iteration = 1;

	public test2() {

	}

	public static void main(String[] args) throws Exception {
		test2 bidirectionalKWS = new test2();
		bidirectionalKWS.run(bidirectionalKWS);
	}

	private void run(test2 bidirectionalKWS) throws Exception {

		boolean addBackward = true;

		graph = new GraphInfraReaderArray(graphInfraPath, addBackward);
		graph.read();
		nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);

		// TODO: debugMode
		DummyProperties.debugMode = true;

		ArrayList<ArrayList<String>> keywordsSet = KWSUtilities.readKeywords(keywordsPath, keywordSize);
		for (int i = 0; i < keywordsSet.size(); i++) {
			keywords = keywordsSet.get(i);
			KWSUtilities.findCandidatesOfKeywordsUsingInvertedList(keywords, nodeIdsOfToken, setS, contentNodes);
		}

		for (int i = 0; i < keywordsSet.size(); i++) {
			// initialize
			for (String ti : setS.keySet()) {
				for (int nodeId : setS.get(ti)) {

					HashMap<String, Double> activationMap = new HashMap<String, Double>();
					HashMap<String, Double> distUI = new HashMap<String, Double>();
					HashMap<String, Integer> spUI = new HashMap<String, Integer>();
					HashMap<String, ArrayList<Integer>> pathOfOrigin = new HashMap<String, ArrayList<Integer>>();

					double activation = (double) graph.nodeOfNodeId.get(nodeId).getDegree()
							/ (double) setS.get(ti).size();
					activationMap.put(ti, activation);

					for (String tI : setS.keySet()) {
						if (tI.equals(ti)) {
							distUI.put(tI, 0.0);
							spUI.put(tI, Integer.MAX_VALUE);
						} else {
							distUI.put(tI, Double.POSITIVE_INFINITY);
						}
					}

					ArrayList<Integer> path = new ArrayList<Integer>();
					path.add(nodeId);
					pathOfOrigin.put(ti, path);

					// initialize
					Node node = new Node(nodeId, graph.nodeOfNodeId.get(nodeId).getDegree(), 0);
					node.setActivationMap(activationMap);
					node.setSpUI(spUI);
					node.setDistUI(distUI);
					node.setPathToOrigin(pathOfOrigin);
					qIn.add(node);
					constructedNodes.put(node.nodeId, node);
				}
			}

			bidirectionalKWS.BidirExpSearch();

		}

	}

	private void BidirExpSearch() {
		System.out.println("Begin biridirection search");

		// while Qin or Qout are non-empty
		while (!qIn.isEmpty() || !qOut.isEmpty()) {
			System.out.println("\n\n");
			System.out.println("NEW ITERATION: " + iteration);
			System.out.println("qIn:" + qIn);
			System.out.println("xIn:" + xIn);
			System.out.println("qOut:" + qOut);
			System.out.println("xOut:" + xOut);

			for (int nodeId : constructedNodes.keySet()) {
				Node node = constructedNodes.get(nodeId);
				System.out.println("getDistUI:");
				for (String keyword : node.getDistUI().keySet()) {
					System.out.print(
							"{dist u=" + nodeId + ", k=" + keyword + "}:" + node.getDistUI().get(keyword) + "; ");
				}
				System.out.println();

				System.out.println("getActivation:");
				for (String keyword : node.getActivationMap().keySet()) {
					System.out.print(
							"{act u=" + nodeId + ", k=" + keyword + "}:" + node.getActivationMap().get(keyword) + "; ");
				}
				System.out.println();

				System.out.println("getActivation:");
				for (String keyword : node.getSpUI().keySet()) {
					System.out.print("{sp u=" + nodeId + ", k=" + keyword + "}:" + node.getSpUI().get(keyword) + "; ");
				}
				System.out.println();

			}
			System.out.println();

			if (qOut.isEmpty() || qIn.peek().activation >= qOut.peek().activation) {

				// qOut is empty
				System.out.println("qOut is empty or qIn.peek().activation >= qOut.peek().activation");

				// Pop best v from Qin
				Node v = qIn.poll();

				// insert in Xin
				xIn.add(v);

				System.out.println("the " + iteration++ + " round, node v comes from Q_in: " + v.nodeId
						+ ", activation: " + v.activation);

				// if is-Complete(v)
				if (IsComplete(v)) {
					// v is root
					Emit(v);
				}

				// if depthv < dmax then
				if (v.depth < dMax) {

					// for all u \in incoming[v]
					for (int nodeUID : getIncomingOfNodeId(v.nodeId)) {
						System.out.println(nodeUID + "-> " + v.nodeId);
						// if (v.getpU().containsKey(nodeUID)) {
						// System.out.println("do not go back to " + nodeUID);
						// continue;
						// } else {
						Node u = null;
						if (constructedNodes.containsKey(nodeUID)) {
							u = constructedNodes.get(nodeUID);
							System.out.println("already seen node visited again u=" + nodeUID);
						} else {
							u = new Node(nodeUID, graph.nodeOfNodeId.get(nodeUID).getDegree(), v.depth + 1);
							constructedNodes.put(u.nodeId, u);
						}

						// just for calling a right API later in compute
						// activation
						spreadActivation = true;

						ExploreEdge(u, v);

						HashMap<Integer, Node> pV = v.getpU();
						pV.put(u.nodeId, u);
						v.setpU(pV);
						HashMap<Integer, Node> pU = u.getpU();
						pU.put(v.nodeId, v);
						u.setpU(pU);

						if (!xIn.contains(u)) {

							u.depth = v.depth + 1;

							if (constructedNodes.containsKey(u.nodeId)) {
								UpdateQin(u);
							}

							else {
								qIn.add(u);
							}

						}
					}

					System.out.println("qIn head -> " + qIn.peek().nodeId + " activation :" + qIn.peek().activation);
					if (!xOut.contains(v)) {
						qOut.add(v);
					}
				}
				// }
			} else if (qIn.isEmpty() || qOut.peek().activation > qIn.peek().activation) {

				System.out.println("qIn is empty or qOut.peek().activation >= qIn.peek().activation");

				// qIn is empty
				Node u = qOut.poll();
				xOut.add(u);

				System.out.println("the " + iteration++ + " round, node u comes from Q_out: " + u.nodeId
						+ ", activation: " + u.activation);

				if (IsComplete(u)) {
					// u is root
					Emit(u);
				}

				if (u.depth < dMax) {
					// for all outgoing[v]
					for (int nodeVID : getOutgoingOfNodeId(u.nodeId)) {
						System.out.println(u.nodeId + "-> " + nodeVID);
						// if (u.getpU().containsKey(nodeVID)) {
						// System.out.println("do not go back to " + nodeVID);
						// continue;
						// } else {
						Node v = null;
						if (constructedNodes.containsKey(nodeVID)) {
							v = constructedNodes.get(nodeVID);
						} else {
							// HashMap<String, ArrayList<Integer>> spUI = new
							// HashMap<String, ArrayList<Integer>>();
							v = new Node(nodeVID, graph.nodeOfNodeId.get(nodeVID).getDegree(), u.depth + 1);
							constructedNodes.put(v.nodeId, v);
						}
						spreadActivation = false;
						ExploreEdge(u, v);
						HashMap<Integer, Node> pU = u.getpU();
						pU.put(v.nodeId, v);
						u.setpU(pU);
						HashMap<Integer, Node> pV = v.getpU();
						pV.put(u.nodeId, u);
						v.setpU(pV);

						if (!xOut.contains(v)) {
							v.depth = u.depth + 1;

							if (constructedNodes.containsKey(v.nodeId)) {
								UpdateQout(v);
							} else {
								qOut.add(v);
							}
						}
					}
				}
			}
		}
	}

	private void ExploreEdge(Node u, Node v) {

		System.out.println("ExploreEdge");

		// for each keyword i
		for (String keyword : setS.keySet()) {
			System.out.println(keyword);
			System.out.println(u.nodeId + "->" + v.nodeId);
			System.out.println("{dist u=" + u.nodeId + ", k=" + keyword + "}:" + u.getDistUI().get(keyword) + "; ");
			System.out.println("{dist v=" + v.nodeId + ", k=" + keyword + "}:" + v.getDistUI().get(keyword) + "; ");
			// System.out.println("getPathToOrigin:" +
			// Arrays.toString(v.getPathToOrigin().get(keyword).toArray()));
			System.out.println();

			if (!v.getDistUI().containsKey(keyword) || v.getDistUI().get(keyword).isInfinite()) {
				continue;
			} else {

				// if u has a better path to t_i via v
				if (u.getDistUI().isEmpty() || !u.getDistUI().containsKey(keyword)
						|| u.getDistUI().get(keyword) > v.getDistUI().get(keyword) + 1) {
					// spu,i ← v;
					u.getSpUI().put(keyword, v.nodeId);

					// update distu,i with this new dist
					u.getDistUI().put(keyword, v.getDistUI().get(keyword) + 1);

					Attach(u, v, keyword);

					if (IsComplete(u)) {
						Emit(u);
					}
				}

				// if v spreads more activation to u from t_i
				double newActivation = computeNewActivation(v, u, keyword);

				if (constructedNodes.get(u.nodeId).getActivationMap().get(keyword) == null
						|| constructedNodes.get(u.nodeId).getActivationMap().get(keyword) < newActivation) {
					// update au,i with this new activation
					constructedNodes.get(u.nodeId).getActivationMap().put(keyword, newActivation);

					Activate(u, keyword, newActivation);
				}

			}
		}
	}

	private void Attach(Node u, Node v, String keyword) {
		// update priority of v if it is present in Qin
		if (qIn.contains(u)) {
			double updateValue = computeNewActivation(v, u, keyword);
			Double oldActValue = u.getActivationMap().get(keyword);
			u.getActivationMap().put(keyword, oldActValue == null ? 0 : oldActValue + updateValue);
			u.setActivationMap(u.getActivationMap());
		}

		// Propagate change in cost dist_{vk}
		// to all its reached-ancestors in best first manner

		// TODO: it should be a priority queue
		LinkedList<Integer> ancestorQueue = new LinkedList<Integer>();
		ancestorQueue.add(u.nodeId);
		HashSet<Integer> updatedNodes = new HashSet<Integer>();

		while (!ancestorQueue.isEmpty()) {
			int currentNode = ancestorQueue.poll();

			updatedNodes.add(currentNode);

			if (constructedNodes.get(currentNode).getDistUI().get(keyword) >= dMax)
				continue;

			for (int srcNodeId : getIncomingOfNodeId(currentNode)) {

				if (!constructedNodes.containsKey(srcNodeId))
					continue;

				if (!constructedNodes.get(srcNodeId).getDistUI().containsKey(keyword) || constructedNodes.get(srcNodeId)
						.getDistUI().get(keyword) > (constructedNodes.get(currentNode).getDistUI().get(keyword) + 1)) {
					constructedNodes.get(srcNodeId).getDistUI().put(keyword,
							constructedNodes.get(currentNode).getDistUI().get(keyword) + 1);

					if (!updatedNodes.contains(srcNodeId))
						ancestorQueue.add(srcNodeId);
				}
			}
			for (int targetNodeId : getOutgoingOfNodeId(currentNode)) {

				if (!constructedNodes.containsKey(targetNodeId))
					continue;

				if (!constructedNodes.get(targetNodeId).getDistUI().containsKey(keyword)
						|| constructedNodes.get(targetNodeId).getDistUI()
								.get(keyword) > (constructedNodes.get(currentNode).getDistUI().get(keyword) + 1)) {
					constructedNodes.get(targetNodeId).getDistUI().put(keyword,
							constructedNodes.get(currentNode).getDistUI().get(keyword) + 1);

					if (!updatedNodes.contains(targetNodeId))
						ancestorQueue.add(targetNodeId);
				}
			}

		}

	}

	private void Activate(Node u, String keyword, double newActivation) {

		boolean tempSpreadActivation = spreadActivation;

		// update priority of v if it is present in Qin
		if (qIn.contains(u)) {
			u.getActivationMap().put(keyword, u.getActivationMap().get(keyword) + newActivation);
			u.setActivationMap(u.getActivationMap());
		}

		// Propagate change in activation avk
		// to all its reached-ancestors in best first manner
		LinkedList<Integer> ancestorQueue = new LinkedList<Integer>();
		ancestorQueue.add(u.nodeId);
		HashSet<Integer> updatedNodes = new HashSet<Integer>();

		while (!ancestorQueue.isEmpty()) {
			int currentNode = ancestorQueue.poll();

			updatedNodes.add(currentNode);

			if (constructedNodes.get(currentNode).getDistUI().get(keyword) >= dMax)
				continue;

			for (int srcNodeId : getIncomingOfNodeId(currentNode)) {

				if (!constructedNodes.containsKey(srcNodeId))
					continue;

				if (!constructedNodes.get(srcNodeId).getActivationMap().containsKey(keyword)
						|| constructedNodes.get(srcNodeId).getActivationMap().get(keyword) < constructedNodes
								.get(currentNode).getActivationMap().get(keyword)) {

					spreadActivation = true;
					Double actValue = constructedNodes.get(srcNodeId).getActivationMap().get(keyword);
					constructedNodes.get(srcNodeId).getActivationMap().put(keyword,
							actValue == null ? 0
									: actValue + computeNewActivation(constructedNodes.get(srcNodeId),
											constructedNodes.get(currentNode), keyword));

					if (!updatedNodes.contains(srcNodeId))
						ancestorQueue.add(srcNodeId);
				}
			}
			for (int targetNodeId : getOutgoingOfNodeId(currentNode)) {
				if (!constructedNodes.containsKey(targetNodeId))
					continue;

				if (!constructedNodes.get(targetNodeId).getActivationMap().containsKey(keyword)
						|| constructedNodes.get(targetNodeId).getActivationMap().get(keyword) > constructedNodes
								.get(currentNode).getActivationMap().get(keyword)) {

					spreadActivation = false;

					Double actValue = constructedNodes.get(targetNodeId).getActivationMap().get(keyword);

					constructedNodes.get(targetNodeId).getActivationMap().put(keyword,
							actValue == null ? 0
									: actValue + computeNewActivation(constructedNodes.get(targetNodeId),
											constructedNodes.get(currentNode), keyword));

					if (!updatedNodes.contains(targetNodeId))
						ancestorQueue.add(targetNodeId);
				}
			}

		}

		spreadActivation = tempSpreadActivation;
	}

	// Update priority of v if it is present in Q_in
	// Propagate change in cost dist_vk
	// private void Attach(Node v, String k, ArrayList<Integer> betterPath) {
	// System.out.println("attach");
	//
	// if (v.getDistUI().isEmpty()) {
	// // initialize the dist_ui
	// HashMap<String, Double> distUI = new HashMap<String, Double>();
	// for (String ti : keywords) {
	// if (ti.equals(k)) {
	// distUI.put(ti, (double) betterPath.size() - 1);
	// } else {
	// distUI.put(ti, Double.POSITIVE_INFINITY);
	// }
	// }
	// v.setDistUI(distUI);
	// // HashMap<String, Double>
	// System.out.println(v.nodeId + "'s distUI: " + v.getDistUI());
	// System.out.println();
	// } else {
	// HashMap<String, Double> distUI = v.getDistUI();
	// distUI.replace(k, (double) betterPath.size() - 1);
	// v.setDistUI(distUI);
	// System.out.println(v.nodeId + "'s distUI: " + v.getDistUI());
	// System.out.println();
	// }
	//
	// if (qIn.contains(v)) {
	//
	// // TODO: update priority fir Qin
	//
	// }
	//
	// // TODO: propagation to all reached-ancesetors
	// }

	// Update priority of v if it is present in Q_in
	// Propagate change in cost activation a_vk
	// private void Activate(Node v, String k, double newActivation) {
	//
	// if (v.getActivationMap().isEmpty()) {
	// HashMap<String, Double> activationMap = new HashMap<String, Double>();
	// activationMap.put(k, newActivation);
	// v.setActivationMap(activationMap);
	// System.out.println(v.nodeId + ".activation: " + v.activation);
	// } else {
	// if (!v.getActivationMap().containsKey(k)) {
	// HashMap<String, Double> activationMap = v.getActivationMap();
	// activationMap.put(k, newActivation);
	// v.setActivationMap(activationMap);
	// System.out.println(v.nodeId + ".activation: " + v.activation);
	// } else {
	// HashMap<String, Double> activationMap = v.getActivationMap();
	// newActivation = activationMap.get(keywords) + newActivation;
	// activationMap.replace(k, newActivation);
	// v.setActivationMap(activationMap);
	// System.out.println(v.nodeId + ".activation: " + v.activation);
	// }
	// }
	//
	// System.out.println("activate");
	//
	// if (qIn.contains(v)) {
	// // TODO: update priority
	// }
	//
	// // TODO: propagation
	//
	// System.out.println();
	// }

	// TODO: add input parameter for this method
	// construct result tree rooted at u
	// add it to result heap
	private void Emit(Node u) {
		// TODO: creating the result tree.
		System.out.println("find one result at round " + iteration);
		System.out.println("result tree depth: " + u.depth);
		System.out.println(u.getPathToOrigin());
		System.out.println("------------------------------");
		System.out.println();
		ResultTree newResultTree = new ResultTree();
		outputHeap.add(newResultTree);

	}

	private boolean IsComplete(Node u) {

		if (u.getDistUI().containsValue(Double.POSITIVE_INFINITY) || u.getDistUI().size() != setS.size()) {
			return false;/* No path to ti */
		}
		return true;
	}

	private double computeNewActivation(Node v, Node u, String keyword) {
		double newActivation = v.getActivationMap().get(keyword) * mu;

		float vuWeight = 0f;
		float weightSum = 0f;
		int vuEdgeId = 0;

		// spreadActivation: true -> incoming f -> outgoing
		if (spreadActivation) {
			// spread to incoming
			vuEdgeId = graph.nodeOfNodeId.get(v.nodeId).getIncomingRelIdOfSourceNodeId(graph).get(u.nodeId);
			// get outgoing edges of node v
			for (int relId : graph.nodeOfNodeId.get(v.nodeId).getIncomingRelIdOfSourceNodeId(graph).values()) {
				if (relId == vuEdgeId) {
					vuWeight = graph.relationOfRelId.get(relId).weight;
					weightSum += vuWeight;
				} else {
					weightSum += graph.relationOfRelId.get(relId).weight;
				}

			}
		} else {
			// spread to outgoing
			vuEdgeId = graph.nodeOfNodeId.get(v.nodeId).outgoingRelIdOfSourceNodeId.get(u.nodeId);
			// get outgoing edges of node v
			for (int relId : graph.nodeOfNodeId.get(v.nodeId).outgoingRelIdOfSourceNodeId.values()) {
				if (relId == vuEdgeId) {
					vuWeight = graph.relationOfRelId.get(relId).weight;
					weightSum += vuWeight;
				} else {
					weightSum += graph.relationOfRelId.get(relId).weight;
				}

			}
		}
		newActivation = (double) vuWeight / weightSum * newActivation;

		return newActivation;
	}

	private Node Update(Node u) {
		// TODO: update node u's information
		// using an iterator to find node u, then update
		// its information
		System.out.println("Update information of node" + u.nodeId);
		System.out.println("before: " + u.nodeId + " activation" + constructedNodes.get(u.nodeId).activation);
		Node result = null;

		// for (Node node : visitedNode.values()) {
		// if (node.nodeId == u.nodeId) {

		Node node = constructedNodes.get(u.nodeId);
		// begin update
		if (node.depth < u.depth) {
			node.depth = u.depth;
		}
		HashMap<String, Double> activationMap = new HashMap<String, Double>();
		HashMap<Integer, Node> pUNode = new HashMap<Integer, Node>();
		HashMap<String, ArrayList<Integer>> spUI = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, Double> distUI = new HashMap<String, Double>();
		for (String t : keywords) {
			if (node.getActivationMap().get(t) != u.getActivationMap().get(t)) {
				if (node.getActivationMap().get(t) == null) {
					activationMap = node.getActivationMap();
					activationMap.put(t, u.getActivationMap().get(t));
				}
			}
			if (node.getDistUI().get(t) != u.getDistUI().get(t)) {
				if (node.getDistUI().get(t) == Double.POSITIVE_INFINITY && u.getDistUI().get(t) != null) {
					distUI = node.getDistUI();
					distUI.replace(t, u.getDistUI().get(t));
				}
			}
			if (node.getPathToOrigin().get(t).size() < u.getPathToOrigin().get(t).size()) {
				spUI = node.getPathToOrigin();
				spUI.replace(t, u.getPathToOrigin().get(t));
			}
		}
		pUNode = node.getpU();
		pUNode.putAll(u.getpU());
		node.setActivationMap(activationMap);
		node.setDistUI(distUI);
		node.setpU(pUNode);

		constructedNodes.replace(node.nodeId, node);
		result = node;
		System.out.println("after: " + node.nodeId + " activation" + constructedNodes.get(node.nodeId).activation);
		System.out.println();
		// break;
		// }
		// }
		if (IsComplete(result)) {
			Emit(result);
		}
		return result;
	}

	private void UpdateQin(Node u) {
		qIn.remove(u);
		qIn.add(u);
	}

	private void UpdateQout(Node u) {
		qOut.remove(u);
		qOut.add(u);
	}

	// private void UpdateQin(Node u) {
	// PriorityQueue<Node> tem = new PriorityQueue<Node>(comparator);
	// while (!qIn.isEmpty()) {
	// Node node = qIn.poll();
	// if (node.nodeId == u.nodeId) {
	// node = constructedNodes.get(node.nodeId);
	// }
	// tem.add(node);
	// }
	// qIn = tem;
	// }

	private Set<Integer> getIncomingOfNodeId(int nodeId) {
		return graph.nodeOfNodeId.get(nodeId).getIncomingRelIdOfSourceNodeId(graph).keySet();
	}

	private Set<Integer> getOutgoingOfNodeId(int nodeId) {
		return graph.nodeOfNodeId.get(nodeId).getOutgoingRelIdOfSourceNodeId().keySet();
	}

}
