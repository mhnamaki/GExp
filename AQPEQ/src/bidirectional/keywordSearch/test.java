package bidirectional.keywordSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import javax.xml.bind.annotation.XmlAccessorOrder;

import org.apache.jena.ext.com.google.common.collect.MinMaxPriorityQueue;

import aqpeq.utilities.KWSUtilities;
import aqpeq.utilities.Dummy.DummyProperties;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import neo4jBasedKWS.ResultTree;
import neo4jBasedKWS.ResultTreeRelevanceComparator;

public class test {

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

	static int dMax = 5;
	boolean spreadActivation = true; // true -> incoming outgoing -> outgoing
	HashMap<Integer, Node> visitedNode = new HashMap<Integer, Node>();

	int cnt = 1;

	public test() {

	}

	public static void main(String[] args) throws Exception {
		test bidirectionalKWS = new test();
		bidirectionalKWS.run(bidirectionalKWS);
	}

	private void run(test bidirectionalKWS) throws Exception {

		boolean addBackward = true;

		graph = new GraphInfraReaderArray(graphInfraPath, addBackward);
		graph.read();
		nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);

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
					// TODO: follows seeting in paper
					// double activation = (double)
					// graph.nodeOfNodeId.get(nodeId).getDegree() /
					// setS.get(ti).size();
					double activation = (double) 1 / setS.get(ti).size();
					// double activation = (double)
					// graph.nodeOfNodeId.get(nodeId).inDegree /
					// setS.get(ti).size();
					HashMap<String, Double> activationMap = new HashMap<String, Double>();
					activationMap.put(ti, activation);
					HashMap<String, Double> distUI = new HashMap<String, Double>();
					for (String tI : setS.keySet()) {
						if (tI.equals(ti)) {
							distUI.put(tI, 0.0);
						} else {
							distUI.put(tI, Double.POSITIVE_INFINITY);
						}
					}
					HashSet<Integer> incoming = new HashSet<Integer>(
							graph.nodeOfNodeId.get(nodeId).getIncomingRelIdOfSourceNodeId(graph).keySet());
					HashSet<Integer> outgoing = new HashSet<Integer>(
							graph.nodeOfNodeId.get(nodeId).getOutgoingRelIdOfSourceNodeId().keySet());
					// TODO: in the paper, spUI initial is infinity
					ArrayList<Integer> path = new ArrayList<Integer>();
					path.add(nodeId);
					HashMap<String, ArrayList<Integer>> spUI = new HashMap<String, ArrayList<Integer>>();
					for (String tI : setS.keySet()) {
						if (tI.equals(ti)) {
							spUI.put(tI, path);
						} else {
							spUI.put(tI, new ArrayList<Integer>());
						}
					}
					// initialize
					// int nodeId, double activation, int depth, HashSet<Node>
					// incoming
					Node node = new Node(nodeId, 1, incoming, outgoing);
					node.setActivationMap(activationMap);
					node.setSpUI(spUI);
					node.setDistUI(distUI);
					qIn.add(node);
					visitedNode.put(node.nodeId, node);
				}
			}
			bidirectionalKWS.BidirExpSearch();
		}

	}

	private void BidirExpSearch() {
		System.out.println("Begin biridirection search");
		while (!qIn.isEmpty() || !qOut.isEmpty()) {
			System.out.println();
			System.out.println(cnt);
			Node nodeFromQIn = null;
			Node nodeFromQOut = null;

			// if (!qIn.isEmpty()) {
			// System.out.println("qIn");
			// PriorityQueue<Node> tem = new PriorityQueue<Node>(qIn);
			// while (!tem.isEmpty()) {
			// System.out.print(tem.poll().nodeId + ", ");
			// }
			// System.out.println();
			// }
			//
			// if (!xIn.isEmpty()) {
			// System.out.println("xIn");
			// for (Node node : xIn) {
			// System.out.print(node.nodeId + ", ");
			// }
			// System.out.println();
			// }
			//
			// if (!qOut.isEmpty()) {
			// System.out.println("qOut");
			// PriorityQueue<Node> tem = new PriorityQueue<Node>(qOut);
			// while (!tem.isEmpty()) {
			// System.out.print(tem.poll().nodeId + ", ");
			//
			// }
			// System.out.println();
			// }
			//
			// if (!xOut.isEmpty()) {
			// System.out.println("xOut");
			// for (Node node : xOut) {
			// System.out.print(node.nodeId + ", ");
			// }
			// System.out.println();
			// }

			if (!qIn.isEmpty()) {
				nodeFromQIn = qIn.poll();
			}
			if (!qOut.isEmpty()) {
				nodeFromQOut = qOut.poll();
			}

			if (nodeFromQIn == null) {
				// qIn is empty
				Node u = nodeFromQOut;
				xOut.add(u);

				if (IsComplete(u)) {
					// u is root
					Emit(u);
				}

				if (u.depth < dMax) {
					// for all outgoing[v]
					for (int nodeVID : u.outgoing) {
						System.out.println("v -> " + nodeVID);
						// if (u.getpU().containsKey(nodeVID)) {
						// System.out.println("do not go back to " + nodeVID);
						// continue;
						// } else {
						Node v = null;
						if (visitedNode.containsKey(nodeVID)) {
							v = visitedNode.get(nodeVID);
						} else {

							HashSet<Integer> incoming = new HashSet<Integer>(
									graph.nodeOfNodeId.get(nodeVID).getIncomingRelIdOfSourceNodeId(graph).keySet());
							HashSet<Integer> outgoing = new HashSet<Integer>(
									graph.nodeOfNodeId.get(nodeVID).getOutgoingRelIdOfSourceNodeId().keySet());
							HashMap<String, ArrayList<Integer>> spUI = new HashMap<String, ArrayList<Integer>>();
							for (String tI : setS.keySet()) {
								spUI.put(tI, new ArrayList<Integer>());
							}
							v = new Node(nodeVID, 1, incoming, outgoing);
							v.setSpUI(spUI);
						}
						spreadActivation = false;
						ExploreEdge(u, v);
						HashMap<Integer, Node> pU = u.getpU();
						pU.put(v.nodeId, v);
						u.setpU(pU);
						HashMap<Integer, Node> pUV = v.getpU();
						pUV.put(u.nodeId, u);
						v.setpU(pUV);
						if (visitedNode.containsKey(v.nodeId)) {
							Update(v);
						}
						if (!xOut.contains(v)) {
							System.out.println("node from qOut");
							v.depth = u.depth + 1;
							xOut.add(v);
						}
					}
				}

				// }
			} else if (nodeFromQOut == null) {
				// qOut is empty
				System.out.println("qOut is empty");
				Node v = nodeFromQIn;
				xIn.add(v);

				System.out.println("the " + cnt++ + " round, node v comes from Q_in: " + v.nodeId + ", activation: "
						+ v.activation);

				if (IsComplete(v)) {
					// v is root
					Emit(v);
				}

				if (v.depth < dMax) {
					// for all incoming[v]
					for (int nodeUID : v.incoming) {
						System.out.println("u -> " + nodeUID);
						// if (v.getpU().containsKey(nodeUID)) {
						// System.out.println("do not go back to " + nodeUID);
						// continue;
						// } else {
						Node u = null;
						if (v.getpU().containsKey(nodeUID)) {
							u = v.getpU().get(nodeUID);
							System.out.println(v.nodeId + " contains " + u.nodeId);
						} else {
							HashSet<Integer> incoming = new HashSet<Integer>(
									graph.nodeOfNodeId.get(nodeUID).getIncomingRelIdOfSourceNodeId(graph).keySet());
							HashSet<Integer> outgoing = new HashSet<Integer>(
									graph.nodeOfNodeId.get(nodeUID).getOutgoingRelIdOfSourceNodeId().keySet());
							HashMap<String, ArrayList<Integer>> spUI = new HashMap<String, ArrayList<Integer>>();
							u = new Node(nodeUID, 1, incoming, outgoing);
						}
						spreadActivation = true;
						ExploreEdge(u, v);
						HashMap<Integer, Node> pU = v.getpU();
						pU.put(u.nodeId, u);
						v.setpU(pU);
						HashMap<Integer, Node> pUU = u.getpU();
						pUU.put(v.nodeId, v);
						u.setpU(pUU);

						if (visitedNode.containsKey(u.nodeId)) {
							u.depth = v.depth + 1;
							///////
							u = Update(u);
							HashMap<Integer, Node> pUV = v.getpU();
							pUV.replace(u.nodeId, u);
							v.setpU(pUV);
						}
						if (!contentNodes.contains(u.nodeId)) {
							if (!xIn.contains(u)) {
								u.depth = v.depth + 1;
								qIn.add(u);
							}
						} else {
							UpdateQin(u);
						}

					}
				}
				System.out.println("qIn head -> " + qIn.peek().nodeId + " activation :" + qIn.peek().activation);
				if (!xOut.contains(v)) {
					qOut.add(v);
				}
				// }
			} // change switch to if-else
			else if (nodeFromQIn.activation == nodeFromQOut.activation) {
				// choose node from Q_in
				qOut.add(nodeFromQOut);
				Node v = nodeFromQIn;
				xIn.add(v);

				System.out.println("the " + cnt++ + " round, node v comes from Q_in: " + v.nodeId + ", activation: "
						+ v.activation);

				if (IsComplete(v)) {
					// v is root
					Emit(v);
				}

				if (v.depth < dMax) {
					// for all incoming[v]
					for (int nodeUID : v.incoming) {
						System.out.println("u -> " + nodeUID);
						// if (v.getpU().containsKey(nodeUID)) {
						// System.out.println("do not go back to " + nodeUID);
						// continue;
						// } else {
						Node u = null;
						if (v.getpU().containsKey(nodeUID)) {
							u = v.getpU().get(nodeUID);
						} else {
							HashSet<Integer> incoming = new HashSet<Integer>(
									graph.nodeOfNodeId.get(nodeUID).getIncomingRelIdOfSourceNodeId(graph).keySet());
							HashSet<Integer> outgoing = new HashSet<Integer>(
									graph.nodeOfNodeId.get(nodeUID).getOutgoingRelIdOfSourceNodeId().keySet());
							u = new Node(nodeUID, 1, incoming, outgoing);
						}
						spreadActivation = true;
						ExploreEdge(u, v);
						// TODO: P_u = Set of nodes u such that edge (u, v)
						// has been explored
						HashMap<Integer, Node> pU = v.getpU();
						pU.put(u.nodeId, u);
						v.setpU(pU);
						HashMap<Integer, Node> pUU = u.getpU();
						pUU.put(v.nodeId, v);
						u.setpU(pUU);

						if (visitedNode.containsKey(u.nodeId)) {
							u.depth = v.depth + 1;
							u = Update(u);
							HashMap<Integer, Node> pUV = v.getpU();
							pUV.replace(u.nodeId, u);
							v.setpU(pUV);
						}
						if (!contentNodes.contains(u.nodeId)) {
							if (!xIn.contains(u)) {
								u.depth = v.depth + 1;
								qIn.add(u);
							}
						} else {
							UpdateQin(u);
						}
						visitedNode.put(u.nodeId, u);
					}
				}
				if (!xOut.contains(v)) {
					qOut.add(v);
				}
				// }
			} else if (nodeFromQIn.activation > nodeFromQOut.activation) {
				// Q_in has node with highest activation
				// Pop best v from Q_in and insert in X_in
				qOut.add(nodeFromQOut);
				Node v = nodeFromQIn;
				xIn.add(v);

				System.out.println("the " + cnt++ + " round, node v comes from Q_in: " + v.nodeId + ", activation: "
						+ v.activation);

				if (IsComplete(v)) {
					// v is root
					Emit(v);
				}

				if (v.depth < dMax) {
					// for all incoming[v]
					for (int nodeUID : v.incoming) {
						System.out.println("u -> " + nodeUID);
						// if (v.getpU().containsKey(nodeUID)) {
						// System.out.println("do not go back to " + nodeUID);
						// continue;
						// } else {
						Node u = null;
						// TODO: when node 27 spread node 9, we should
						// update node 9's information
						if (v.getpU().containsKey(nodeUID)) {
							u = v.getpU().get(nodeUID);
							System.out.println("contains " + nodeUID);
						} else {
							HashSet<Integer> incoming = new HashSet<Integer>(
									graph.nodeOfNodeId.get(nodeUID).getIncomingRelIdOfSourceNodeId(graph).keySet());
							HashSet<Integer> outgoing = new HashSet<Integer>(
									graph.nodeOfNodeId.get(nodeUID).getOutgoingRelIdOfSourceNodeId().keySet());
							u = new Node(nodeUID, 1, incoming, outgoing);
						}
						spreadActivation = true;
						ExploreEdge(u, v);
						// TODO: P_u = Set of nodes u such that edge (u, v)
						// has
						// been explored
						HashMap<Integer, Node> pU = v.getpU();
						pU.put(u.nodeId, u);
						v.setpU(pU);
						HashMap<Integer, Node> pUU = u.getpU();
						pUU.put(v.nodeId, v);
						u.setpU(pUU);

						if (visitedNode.containsKey(u.nodeId)) {
							u.depth = v.depth + 1;
							u = Update(u);
							HashMap<Integer, Node> pUV = v.getpU();
							pUV.replace(u.nodeId, u);
							v.setpU(pUV);
						}
						if (!contentNodes.contains(u.nodeId)) {
							if (!xIn.contains(u)) {
								u.depth = v.depth + 1;
								qIn.add(u);
							}
						} else {
							UpdateQin(u);
						}
						visitedNode.put(u.nodeId, u);
					}
				}
				if (!xOut.contains(v)) {
					qOut.add(v);
				}
				// }
			} else {
				// Q_out has node with highest activation
				// Pop best v from Q_out and insert in X_out
				qIn.add(nodeFromQIn);
				Node u = nodeFromQOut;
				xOut.add(u);

				System.out.println("the " + cnt++ + " round, node u comes from Q_out: " + u.nodeId + ", activation: "
						+ u.activation);

				if (IsComplete(u)) {
					// u is root
					Emit(u);
				}

				if (u.depth < dMax) {
					// for all outgoing[v]
					for (int nodeVID : u.outgoing) {
						// if (u.getpU().containsKey(nodeVID)) {
						// System.out.println("do not go back to " + nodeVID);
						// continue;
						// } else {
						Node v = null;
						if (u.getpU().containsKey(nodeVID)) {
							v = u.getpU().get(nodeVID);
							System.out.println("contains " + nodeVID);
						} else {
							// TODO: if u.Pu contains nodeUID, do not need
							// to
							// initialize
							// if u.Pu doesn't contains nodeUID, initialize
							// v
							System.out.println("v -> " + nodeVID);
							HashSet<Integer> incoming = new HashSet<Integer>(
									graph.nodeOfNodeId.get(nodeVID).getIncomingRelIdOfSourceNodeId(graph).keySet());
							HashSet<Integer> outgoing = new HashSet<Integer>(
									graph.nodeOfNodeId.get(nodeVID).getOutgoingRelIdOfSourceNodeId().keySet());
							v = new Node(nodeVID, 1, incoming, outgoing);
						}
						spreadActivation = false;
						ExploreEdge(u, v);
						// TODO: may not need
						HashMap<Integer, Node> pU = u.getpU();
						pU.put(v.nodeId, v);
						u.setpU(pU);
						HashMap<Integer, Node> pUV = v.getpU();
						pUV.put(u.nodeId, u);
						v.setpU(pUV);
						if (!xOut.contains(v)) {
							v.depth = u.depth + 1;
							xOut.add(v);
						}
					}
				}
			}
			// }

		}
	}

	private void ExploreEdge(Node u, Node v) {
		System.out.println("ExploreEdge");
		for (String keyword : setS.keySet()) {
			System.out.println(keyword);
			System.out.println(u.nodeId + "' distUI: ");
			System.out.println(u.getDistUI());
			System.out.println(u.nodeId + "'s spUI: " + u.getpU().get(keyword));
			System.out.println(v.nodeId + "v's spUI: ");
			System.out.println(v.getPathToOrigin().get(keyword));
			System.out.println(v.nodeId + "'s spUI: " + v.getpU().get(keyword));
			System.out.println();

			if (v.getPathToOrigin().get(keyword).isEmpty()) {
				continue;
			} else {
				// if u has a better path to t_i via v
				if (u.getDistUI().isEmpty()) {
					// when node u is a new node
					// System.out.println(v.nodeId + "'s getSpUI().get(keyword):
					// " + v.getSpUI().get(keyword));
					ArrayList<Integer> betterPath = new ArrayList<Integer>(v.getPathToOrigin().get(keyword));
					betterPath.add(u.nodeId);
					HashMap<String, ArrayList<Integer>> spUI = new HashMap<String, ArrayList<Integer>>();
					for (String tI : setS.keySet()) {
						if (tI.equals(keyword)) {
							spUI.put(tI, betterPath);
						} else {
							spUI.put(tI, new ArrayList<Integer>());
						}
					}
					u.setSpUI(spUI);
					// System.out.println(u.nodeId + "'s better path -> " +
					// spUI.get(keyword));
					Attach(u, keyword, betterPath);
				} else {
					if (u.getPathToOrigin().get(keyword).size() < (v.getPathToOrigin().get(keyword).size() + 1)) {
						ArrayList<Integer> betterPath = new ArrayList<Integer>(v.getPathToOrigin().get(keyword));
						betterPath.add(u.nodeId);
						HashMap<String, ArrayList<Integer>> spUI = u.getPathToOrigin();
						spUI.put(keyword, betterPath);
						u.setSpUI(spUI);
						// System.out.println(u.nodeId + "'s better path -> " +
						// spUI.get(keyword));
						Attach(u, keyword, betterPath);
					}
				}
				if (IsComplete(u)) {
					Emit(u);
				}

				// if v spreads more activation to u from t_i
				double newActivation = computeNewActivation(v, u, keyword);
				Activate(u, keyword, newActivation);

			}
		}
	}

	// Update priority of v if it is present in Q_in
	// Propagate change in cost dist_vk
	private void Attach(Node v, String k, ArrayList<Integer> betterPath) {
		System.out.println("attach");

		if (v.getDistUI().isEmpty()) {
			// initialize the dist_ui
			HashMap<String, Double> distUI = new HashMap<String, Double>();
			for (String ti : keywords) {
				if (ti.equals(k)) {
					distUI.put(ti, (double) betterPath.size() - 1);
				} else {
					distUI.put(ti, Double.POSITIVE_INFINITY);
				}
			}
			v.setDistUI(distUI);
			// HashMap<String, Double>
			System.out.println(v.nodeId + "'s distUI: " + v.getDistUI());
			System.out.println();
		} else {
			HashMap<String, Double> distUI = v.getDistUI();
			distUI.replace(k, (double) betterPath.size() - 1);
			v.setDistUI(distUI);
			System.out.println(v.nodeId + "'s distUI: " + v.getDistUI());
			System.out.println();
		}

		if (qIn.contains(v)) {

			// TODO: update priority fir Qin

		}

		// TODO: propagation to all reached-ancesetors
	}

	// Update priority of v if it is present in Q_in
	// Propagate change in cost activation a_vk
	private void Activate(Node v, String k, double newActivation) {

		if (v.getActivationMap().isEmpty()) {
			HashMap<String, Double> activationMap = new HashMap<String, Double>();
			activationMap.put(k, newActivation);
			v.setActivationMap(activationMap);
			System.out.println(v.nodeId + ".activation: " + v.activation);
		} else {
			if (!v.getActivationMap().containsKey(k)) {
				HashMap<String, Double> activationMap = v.getActivationMap();
				activationMap.put(k, newActivation);
				v.setActivationMap(activationMap);
				System.out.println(v.nodeId + ".activation: " + v.activation);
			} else {
				HashMap<String, Double> activationMap = v.getActivationMap();
				newActivation = activationMap.get(keywords) + newActivation;
				activationMap.replace(k, newActivation);
				v.setActivationMap(activationMap);
				System.out.println(v.nodeId + ".activation: " + v.activation);
			}
		}

		System.out.println("activate");

		if (qIn.contains(v)) {
			// TODO: update priority
		}

		// TODO: propagation

		System.out.println();
	}

	// TODO: add input parameter for this method
	// construct result tree rooted at u
	// add it to result heap
	private void Emit(Node u) {
		System.out.println("find one result at round " + cnt);
		System.out.println("result tree depth: " + u.depth);
		System.out.println(u.getPathToOrigin());
		System.out.println("------------------------------");
		System.out.println();
		ResultTree newResultTree = new ResultTree();
		outputHeap.add(newResultTree);

	}

	private boolean IsComplete(Node u) {
		boolean isComplete = true;
		if (u.getDistUI().containsValue(Double.POSITIVE_INFINITY)) {
			// No path to t_i
			isComplete = false;
		}
		return isComplete;
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
		System.out.println("before: " + u.nodeId + " activation" + visitedNode.get(u.nodeId).activation);
		Node result = null;
		for (Node node : visitedNode.values()) {
			if (node.nodeId == u.nodeId) {
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
				node.setSpUI(spUI);
				visitedNode.replace(node.nodeId, node);
				result = node;
				System.out.println("after: " + node.nodeId + " activation" + visitedNode.get(node.nodeId).activation);
				System.out.println();
				break;
			}
		}
		if (IsComplete(result)) {
			Emit(result);
		}
		return result;
	}

	private void UpdateQin(Node u) {
		PriorityQueue<Node> tem = new PriorityQueue<Node>(comparator);
		while (!qIn.isEmpty()) {
			Node node = qIn.poll();
			if (node.nodeId == u.nodeId) {
				node = visitedNode.get(node.nodeId);
			}
			tem.add(node);
		}
		qIn = tem;
	}

}
