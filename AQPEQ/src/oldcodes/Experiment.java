package oldcodes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

import org.apache.jena.ext.com.google.common.collect.MinMaxPriorityQueue;
import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableUndirectedGraph;

import com.couchbase.client.deps.com.fasterxml.jackson.databind.deser.std.DateDeserializers.CalendarDeserializer;

import aqpeq.utilities.Visualizer;
import aqpeq.utilities.Dummy;
import aqpeq.utilities.Dummy.DummyFunctions;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import neo4jBasedKWS.CrossProduct;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;
import neo4jBasedKWS.ResultTreeRelevanceComparator;
//
public class Experiment {
//	static GraphInfraReaderArray graph;
//
//	private static String graphInfraPath = "/Users/mnamaki/AQPEQ/GraphExamples/k2Infra/";
//	private static String keywordsPath = "/Users/mnamaki/AQPEQ/KeywordExamples/ABC.in";
//	private static int heapSize = 4;
//	static int maxRequiredResults = 6;
//	private static int distanceBound = 4;
//	private static boolean debugMode = true;
//	private static boolean visualizeMode = false;
//
//	// without using BerkeleyDB
//	static HashMap<String, HashSet<Integer>> candidatesOfAKeyword = new HashMap<String, HashSet<Integer>>();
//
//	static HashSet<Integer> candidatesSet = new HashSet<Integer>();
//	static HashMap<String, HashSet<Integer>> nodeIdsOfToken = new HashMap<String, HashSet<Integer>>();
//
//	HashMap<String, ArrayList<Integer>> nodeIdsOfAAttributeKey = new HashMap<String, ArrayList<Integer>>();
//	HashMap<String, ArrayList<Integer>> nodeIdsOfAAttributeValues = new HashMap<String, ArrayList<Integer>>();
//	ArrayList<ListenableUndirectedGraph<ResultNode, DefaultEdge>> printedResultTrees = new ArrayList<ListenableUndirectedGraph<ResultNode, DefaultEdge>>();
//	static int totalPrintedResults = 0;
//
//	static LinkedList<ResultTree> fullOutputsQueue = new LinkedList<ResultTree>();
//
//	public static void main(String[] args) throws Exception {
//
//		for (int i = 0; i < args.length; i++) {
//			if (args[i].equals("-dataGraph")) {
//				graphInfraPath = args[++i];
//			} else if (args[i].equals("-keywordsPath")) {
//				keywordsPath = args[++i];
//			} else if (args[i].equals("-heapSize")) {
//				heapSize = Integer.parseInt(args[++i]);
//			} else if (args[i].equals("-maxRequiredResults")) {
//				maxRequiredResults = Integer.parseInt(args[++i]);
//			} else if (args[i].equals("-distanceBound")) {
//				distanceBound = Integer.parseInt(args[++i]);
//			} else if (args[i].equals("-debugMode")) {
//				debugMode = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-visualize")) {
//				visualizeMode = Boolean.parseBoolean(args[++i]);
//			}
//		}
//
//		graph = new GraphInfraReaderArray(graphInfraPath, Dummy.DummyProperties.addBackward);
//		graph.read();
//		System.out.println("finish read");
//		Experiment experiment = new Experiment();
//		// experiment.indexNodeLabelsAndProperties();
//		nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);
//
//		File result = new File("Result.txt");
//		FileOutputStream fosResult = new FileOutputStream(result);
//		BufferedWriter bwResult = new BufferedWriter(new OutputStreamWriter(fosResult));
//		
//		ArrayList<ArrayList<String>> keywordsSet = experiment.readKeywords();
//		
//		bwResult.write("distance bound: " + distanceBound + "\n");
//		bwResult.write("\n");
//		for (int i = 0; i < keywordsSet.size(); i++) {
//			// distanceBound = 2;
//			// if (i == 0) {
//			// distanceBound = 1;
//			// }
//			ArrayList<String> keywords = keywordsSet.get(i);
//			bwResult.write("keywords: " + keywords + "\n");
//			ArrayList<Long> timeInformation = new ArrayList<Long>();
//			candidatesOfAKeyword = new HashMap<String, HashSet<Integer>>();
//			candidatesSet = new HashSet<Integer>();
//			totalPrintedResults = 0;
//			int count = 0;
//			long averageTime = 0;
//			fullOutputsQueue = new LinkedList<ResultTree>();
//			experiment.findCandidatesOfKeywords(keywords);
//			while (count < 5) {
//				visualizeMode = false;
//				if (count == 0) {
//					// debug
//					Iterator it = candidatesOfAKeyword.entrySet().iterator();
//					while (it.hasNext()) {
//						HashMap.Entry<String, HashSet<Integer>> entry = (java.util.Map.Entry<String, HashSet<Integer>>) it
//								.next();
//						bwResult.write("keyword: " + entry.getKey() + "\n");
//						bwResult.write("candidate size: " + entry.getValue().size() + "\n");
//						bwResult.write("candidates: " + entry.getValue() + "\n");
//						// System.out.println("keyword: " + entry.getKey());
//						// System.out.println("candidate size: " +
//						// entry.getValue().size());
//						// System.out.println("candidates: " +
//						// entry.getValue());
//						// System.out.println();
//						visualizeMode = true;
//					}
//					// debug
//				}
//				long time = experiment.run(keywords);
//				timeInformation.add(time);
//				bwResult.write((count + 1) + " time: " + time + "\n");
//				count++;
//			}
//			for (long time : timeInformation) {
//				averageTime = averageTime + time;
//			}
//			averageTime = averageTime / 5;
//			bwResult.write("average time: " + averageTime + " ms." + "\n");
//			bwResult.write("\n");
//			bwResult.write("----------------------------------------------");
//			bwResult.write("\n");
//		}
//
//		bwResult.close();
//	}
//
//	private ArrayList<ArrayList<String>> readKeywords() throws Exception {
//		ArrayList<ArrayList<String>> keywordsSet = new ArrayList<ArrayList<String>>();
//		FileInputStream fis = new FileInputStream(keywordsPath);
//		// Construct BufferedReader from InputStreamReader
//		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
//		String keywordsLine = "";
//		while ((keywordsLine = br.readLine()) != null) {
//			ArrayList<String> keywords = new ArrayList<String>();
//			StringTokenizer stringTokenizer = new StringTokenizer(keywordsLine, ",", false);
//			System.out.println(stringTokenizer.countTokens());
//			while (stringTokenizer.hasMoreElements()) {
//				String nextToken = DummyFunctions.getCleanedString(stringTokenizer.nextElement().toString());
//
//				if (debugMode)
//					System.out.println("nextToken: " + nextToken);
//
//				keywords.add(nextToken);
//			}
//			keywordsSet.add(keywords);
//		}
//		br.close();
//		return keywordsSet;
//	}
//
//	private Long run(ArrayList<String> keywords) {
//		HashMap<Integer, ExtendedNode> createdExtendedNodeOfNodeId = new HashMap<Integer, ExtendedNode>();
//
//		ResultTreeRelevanceComparator resultTreeRelevanceComparator = new ResultTreeRelevanceComparator();
//
//		DijkstraDistanceComparator dijkstraDistanceComparator = new DijkstraDistanceComparator();
//
//		PriorityQueue<DijkstraRunner> iteratorHeap = new PriorityQueue<>(dijkstraDistanceComparator);
//
//		MinMaxPriorityQueue<ResultTree> outputHeap = MinMaxPriorityQueue.orderedBy(resultTreeRelevanceComparator)
//				.maximumSize(heapSize).create();
//		long start = System.currentTimeMillis();
//		HashMap<Integer, DijkstraRunner> dijkstraRunnerOfNodeId = new HashMap<Integer, DijkstraRunner>();
//		for (String keyword : keywords) { // keywords is an ArrayList - enhanced
//											// for-loop that goes through each
//											// keyword
//			// initialize an instance of DijkstraRunner from each source
//			// candidate
//			for (int sourceNodeId : candidatesOfAKeyword.get(keyword)) {
//				if (!dijkstraRunnerOfNodeId.containsKey(sourceNodeId)) {
//					DijkstraRunner dijkstraRunner = new DijkstraRunner(graph, createdExtendedNodeOfNodeId, sourceNodeId,
//							keyword, distanceBound, debugMode, candidatesSet);
//					iteratorHeap.add(dijkstraRunner);
//					// visitedNodesByAllDijkstras.add(sourceNodeId);
//					dijkstraRunnerOfNodeId.put(sourceNodeId, dijkstraRunner);
//				} else {
//					dijkstraRunnerOfNodeId.get(sourceNodeId).frontier.peek().originsOfKeywords.put(keyword,
//							new HashSet<>());
//					dijkstraRunnerOfNodeId.get(sourceNodeId).frontier.peek().originsOfKeywords.get(keyword)
//							.add(sourceNodeId);
//				}
//			}
//
//		}
//
//		while (!iteratorHeap.isEmpty() && (totalPrintedResults + outputHeap.size()) < maxRequiredResults) {
//
//			// Iterator = remove top iterator from IteratorHeap
//
//			DijkstraRunner dijkstraRunner = iteratorHeap.poll();
//
//			// if (debugMode)
//			// System.out.println("cnt: " + cnt++);
//
//			// v = Get next node from Iterator
//			ExtendedNode nextExtendedNode = dijkstraRunner.getNextExtendedNode();
//
//			if (dijkstraRunner.hasMoreNodesToOutput()) {
//				iteratorHeap.add(dijkstraRunner);
//			}
//
//			// cross product
//			if (nextExtendedNode.originsOfKeywords.keySet().size() == keywords.size()) {
//				ArrayList<HashMap<String, HashSet<Integer>>> crossedProductResults = CrossProduct
//						.crossProduct(nextExtendedNode.originsOfKeywords);
//
//				// for each tuple in CrossProduct
//				// create ResultTree from tuple
//
//				for (HashMap<String, HashSet<Integer>> keywordNodeIdsParticipatingInTree : crossedProductResults) {
//					ResultTree newResultTree = new ResultTree();
//					newResultTree.resultTree(graph, keywordNodeIdsParticipatingInTree,
//							nextExtendedNode.originsOfKeywords, nextExtendedNode.pathOfRootToTheOrigin);
//
//					if (outputHeap.size() == heapSize) {
//						// if (totalPrintedResults < maxRequiredResults) {
//						ResultTree resultTreeToBeRemovedAndPrinted = outputHeap.poll();
//						print(resultTreeToBeRemovedAndPrinted);
//					}
//					outputHeap.add(newResultTree);
//				}
//			}
//		}
//
//		while (!outputHeap.isEmpty()) {
//			ResultTree resultTreeToBeRemovedAndPrinted = outputHeap.poll();
//			print(resultTreeToBeRemovedAndPrinted);
//		}
//
//		long end = System.currentTimeMillis() - start;
//		if (debugMode && visualizeMode) {
//			Visualizer.visualizeOutput(fullOutputsQueue, graph);
//			System.out.println("finsihed:");
//		}
//		return end;
//	}
//
//	private void print(ResultTree resultTreeToBePrinted) {
//
//		if (!isomorphisToPreviouslyPrinted(printedResultTrees, resultTreeToBePrinted.anOutputTree)) {
//
//			System.out.println("OUTPUT TREE: " + totalPrintedResults++ + ", cost: " + resultTreeToBePrinted.cost);
//
//			System.out.println("Vertices");
//			for (ResultNode resNod : resultTreeToBePrinted.anOutputTree.vertexSet()) {
//				System.out.println(resNod.nodeId + ", label: " + graph.nodeOfNodeId.get(resNod.nodeId).getLabels());
//			}
//			System.out.println("Edges");
//			for (DefaultEdge e : resultTreeToBePrinted.anOutputTree.edgeSet()) {
//				System.out.println(resultTreeToBePrinted.anOutputTree.getEdgeSource(e) + " -> "
//						+ resultTreeToBePrinted.anOutputTree.getEdgeTarget(e));
//				int sourceID = resultTreeToBePrinted.anOutputTree.getEdgeSource(e).nodeId;
//				int targetID = resultTreeToBePrinted.anOutputTree.getEdgeTarget(e).nodeId;
//				System.out.println(
//						"relationship: " + Dummy.DummyFunctions.getRelationshipOfPairNodes(graph, sourceID, targetID));
//			}
//			System.out.println("Neighbor information");
//			HashSet<Integer> nodeIdsInResultTree = new HashSet<Integer>();
//			for (ResultNode resultNode : resultTreeToBePrinted.anOutputTree.vertexSet()) {
//				nodeIdsInResultTree.add(resultNode.nodeId);
//			}
//			for (ResultNode resNod : resultTreeToBePrinted.anOutputTree.vertexSet()) {
//				HashMap<String, Integer> freqOfNeighborTokenType = new HashMap<String, Integer>();
//				for (int otherNodeId : resNod.node.getRelationshipsOfNodeId().keySet()) {
//					NodeInfra otherNode = graph.nodeOfNodeId.get(otherNodeId);
//
//					if (nodeIdsInResultTree.contains(otherNode.nodeId))
//						continue;
//
//					for (String lbl : otherNode.getLabels()) {
//						String cleanLbl = DummyFunctions.getCleanedString(lbl);
//						for (String token : DummyFunctions.getTokens(cleanLbl)) {
//							freqOfNeighborTokenType.putIfAbsent(token, 0);
//							freqOfNeighborTokenType.put(token, freqOfNeighborTokenType.get(token) + 1);
//						}
//					}
//				}
//				if (freqOfNeighborTokenType.keySet().size() > 20 || freqOfNeighborTokenType.keySet().size() == 0) {
//					continue;
//				} else {
//					System.out.println("node: " + resNod.nodeId + ", neighbor information ->");
//					System.out.println(freqOfNeighborTokenType);
//				}
//			}
//
//			printedResultTrees.add(resultTreeToBePrinted.anOutputTree);
//
//			if (visualizeMode)
//				fullOutputsQueue.add(resultTreeToBePrinted);
//
//			System.out.println();
//		}
//
//	}
//
//	private boolean isomorphisToPreviouslyPrinted(
//			ArrayList<ListenableUndirectedGraph<ResultNode, DefaultEdge>> printedResultTrees2,
//			ListenableUndirectedGraph<ResultNode, DefaultEdge> anOutputTree) {
//
//		for (ListenableUndirectedGraph<ResultNode, DefaultEdge> graph1 : printedResultTrees2) {
//
//			VF2GraphIsomorphismInspector<ResultNode, DefaultEdge> vf2Checker = new VF2GraphIsomorphismInspector<ResultNode, DefaultEdge>(
//					graph1, anOutputTree, new Comparator<ResultNode>() {
//						@Override
//						public int compare(ResultNode o1, ResultNode o2) {
//							return Long.compare(o1.nodeId, o2.nodeId);
//						}
//					}, null);
//			if (vf2Checker.isomorphismExists())
//				return true;
//
//		}
//
//		return false;
//	}
//
//	public void findCandidatesOfKeywords(ArrayList<String> keywords) throws Exception {
//		// for (String keyword : keywords) {
//		// if (cleanedNodeIdOfLabel.containsKey(keyword)) {
//		// candidatesOfAKeyword.putIfAbsent(keyword, new HashSet<Integer>());
//		// candidatesOfAKeyword.get(keyword).addAll(nodeIdsOfALabel.get(cleanedNodeIdOfLabel.get(keyword)));
//		// }
//		// candidatesSet.addAll(candidatesOfAKeyword.get(keyword));
//		// }
//		System.out.println("nodeIdsOfToken: " + nodeIdsOfToken.size());
//		for (String keyword : keywords) {
//			HashSet<Integer> candidate = new HashSet<Integer>();
//			for (String token : Dummy.DummyFunctions.getTokens(keyword)) {
//				if (nodeIdsOfToken.containsKey(token)) {
//					HashSet<Integer> candidateTem = new HashSet<Integer>();
//					candidateTem.addAll(nodeIdsOfToken.get(token));
//					if (candidate.isEmpty()) {
//						candidate = candidateTem;
//					} else {
//						candidate.retainAll(candidateTem);
//					}
//				}
//			}
//			candidatesOfAKeyword.put(keyword, candidate);
//			candidatesSet.addAll(candidatesOfAKeyword.get(keyword));
//		}
//
//		// debug
//		for (String keyword : candidatesOfAKeyword.keySet()) {
//			System.out.println("keyword: " + keyword + ", candidate set: " + candidatesOfAKeyword.get(keyword));
//		}
//	}
//
//	// public void indexNodeLabelsAndProperties() {
//	// nodeIdsOfToken = new HashMap<>();
//	//
//	// long startIndexTime = System.currentTimeMillis();
//	// for (Node node : graph..getAllNodes()) {
//	// // index labels
//	// for (Label lbl : node.getLabels()) {
//	// String token = DummyFunctions.getCleanedString(lbl.name()).toLowerCase();
//	// nodeIdsOfALabel.putIfAbsent(token, new ArrayList<Long>());
//	// nodeIdsOfALabel.get(token).add(node.getId());
//	// String newToken = "";
//	// if (token.contains("uri_")) {
//	// String[] tem = token.trim().split("_");
//	// for (int i = 1; i < tem.length; i++) {
//	// newToken = newToken.trim() + " " + tem[i];
//	// }
//	// } else {
//	// newToken = token;
//	// }
//	// cleanedNodeIdOfLabel.put(newToken.trim(), token);
//	// }
//	//
//	// }
//	// long endIndexTime = System.currentTimeMillis();
//	// long indexTime = (endIndexTime - startIndexTime);
//	// System.out.println("find candidate set time: " + indexTime + " ms.");
//	//
//	// }
//
}
