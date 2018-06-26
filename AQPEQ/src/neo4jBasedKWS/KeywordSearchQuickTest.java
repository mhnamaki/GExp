//package neo4jBasedKWS;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.PriorityQueue;
//import java.util.StringTokenizer;
//
//import org.apache.jena.ext.com.google.common.collect.MinMaxPriorityQueue;
//import org.jgrapht.alg.FloydWarshallShortestPaths;
//import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;
//import org.jgrapht.graph.DefaultEdge;
//import org.jgrapht.graph.ListenableDirectedGraph;
//import org.jgrapht.graph.ListenableUndirectedGraph;
//import org.neo4j.graphdb.GraphDatabaseService;
//import org.neo4j.graphdb.Label;
//import org.neo4j.graphdb.Node;
//import org.neo4j.graphdb.Transaction;
//import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//import org.neo4j.graphdb.factory.GraphDatabaseSettings;
//
//import aqpeq.utilities.Dummy;
//import aqpeq.utilities.Dummy.DummyFunctions;
//import aqpeq.utilities.TreeNode;
//import aqpeq.utilities.Visualizer;
//import dataset.BerkeleyDB.BerkleleyDB;
//
//// TODO: implementing backward edges including its weighting schemes
//
//public class KeywordSearchQuickTest {
//
//////	private static String dataGraphPath = "/Users/mnamaki/AQPEQ/GraphExamples/k2";
//////	private static String keywordsPath = "/Users/mnamaki/AQPEQ/KeywordExamples/ABC.txt";
////	private static String dataGraphPath = "/Users/zhangxin/Desktop/Summer/dbp_3_2_1";
////	private static String keywordsPath = "/Users/zhangxin/Desktop/AQPEQ/KeywordExamples/UCBUSCAward.txt";
////
////	private static int heapSize = 6;
////	ArrayList<String> keywords = new ArrayList<String>();
////	// HashMap<String, Set<String>> similarKeywords = new HashMap<String,
////	// Set<String>>();
////	HashMap<String, HashSet<Long>> candidatesOfAKeyword = new HashMap<String, HashSet<Long>>();
////	HashSet<Long> candidatesSet = new HashSet<Long>();
////
////	HashMap<String, ArrayList<Long>> nodeIdsOfALabel = new HashMap<String, ArrayList<Long>>();
////	HashMap<String, String> cleanedNodeIdOfLabel = new HashMap<String, String>();
////	HashMap<String, ArrayList<Long>> nodeIdsOfAAttributeKey = new HashMap<String, ArrayList<Long>>();
////	HashMap<String, ArrayList<Long>> nodeIdsOfAAttributeValues = new HashMap<String, ArrayList<Long>>();
////	ArrayList<ListenableUndirectedGraph<ResultNode, DefaultEdge>> printedResultTrees = new ArrayList<ListenableUndirectedGraph<ResultNode, DefaultEdge>>();
////
////	int totalPrintedResults = 0;
////	static int maxRequiredResults = 10;
////	static GraphDatabaseService dataGraph;
////	private static int distanceBound = 2;
////	private static boolean debugMode = true;
////	private static boolean visualizeMode = true;
////
////	// BerkeleyDB
////	private static boolean usingBDB = false;
////	private static String basePath = "/Users/zhangxin/Desktop/AQPEQ/AQPEQ/BerkeleyDB";
////	private static String databaseName = "DBPedia";
////
////	private static BerkleleyDB berkeleyDB;
////	private static BerkleleyDB berkeleyEnrichDB;
////	private static BerkleleyDB berkeleySimilarDB;
////	private static String database = "databaseBuffalo";
////	private static String catDatabase = "catDatabaseBuffalo";
////	private static String envFilePath = "dbEnvBuffalo";
////	private static String enrichDatabase = "enrichDatabaseBuffalo";
////	private static String enrichCatDatabase = "enrichCatDatabaseBuffalo";
////	private static String enrichEnvFilePath = "enrichDBEnvBuffalo";
////	private static String similarDatabase = "similarDatabase";
////	private static String similarCatDatabase = "similarCatDatabase";
////	private static String similarEnvFilePath = "similarEnvFilePath";
////
////	LinkedList<ResultTree> fullOutputsQueue = new LinkedList<ResultTree>();
////
////	public static void main(String[] args) throws Exception {
////
////		for (int i = 0; i < args.length; i++) {
////			if (args[i].equals("-dataGraph")) {
////				dataGraphPath = args[++i];
////			} else if (args[i].equals("-keywordsPath")) {
////				keywordsPath = args[++i];
////			} else if (args[i].equals("-heapSize")) {
////				heapSize = Integer.parseInt(args[++i]);
////			} else if (args[i].equals("-maxRequiredResults")) {
////				maxRequiredResults = Integer.parseInt(args[++i]);
////			} else if (args[i].equals("-distanceBound")) {
////				distanceBound = Integer.parseInt(args[++i]);
////			} else if (args[i].equals("-debugMode")) {
////				debugMode = Boolean.parseBoolean(args[++i]);
////			} else if (args[i].equals("-visualize")) {
////				visualizeMode = Boolean.parseBoolean(args[++i]);
////			} else if (args[i].equals("-usingBDB")) {
////				usingBDB = Boolean.parseBoolean(args[++i]);
////			} else if (args[i].equals("-basePath")) {
////				basePath = args[++i];
////			} else if (args[i].equals("-databaseName")) {
////				databaseName = args[++i];
////			}
////
////		}
////
////		KeywordSearchQuickTest keywordSearch = new KeywordSearchQuickTest();
////
////		keywordSearch.readKeywords();
////
////		File storeDir = new File(dataGraphPath);
////		dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
////				.setConfig(GraphDatabaseSettings.pagecache_memory, "4g")
////				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();
////
////		Transaction tx1 = dataGraph.beginTx();
////
////		keywordSearch.indexNodeLabelsAndProperties();
////
////		keywordSearch.findCandidatesOfKeywords();
////
////		if (usingBDB) {
////			System.out.println("Using BerkeleyDB");
////			envFilePath = basePath + "/" + databaseName + "/" + envFilePath;
////			enrichEnvFilePath = basePath + "/" + databaseName + "/" + enrichEnvFilePath;
////			similarEnvFilePath = basePath + "/" + databaseName + "/" + similarEnvFilePath;
////		} else {
////			System.out.println("Not using BerkeleyDB");
////			keywordSearch.findCandidatesOfKeywords();
////		}
////
////		keywordSearch.run();
////
////		tx1.success();
////		tx1.close();
////	}
////
////	private void run() {
////
////		HashMap<Long, ExtendedNode> createdExtendedNodeOfNodeId = new HashMap<Long, ExtendedNode>();
////
////		// TODO: to be implemented
////		ResultTreeRelevanceComparator resultTreeRelevanceComparator = new ResultTreeRelevanceComparator();
////
////		DijkstraDistanceComparator dijkstraDistanceComparator = new DijkstraDistanceComparator();
////
////		PriorityQueue<DijkstraRunner> iteratorHeap = new PriorityQueue<>(dijkstraDistanceComparator);
////
////		MinMaxPriorityQueue<ResultTree> outputHeap = MinMaxPriorityQueue.orderedBy(resultTreeRelevanceComparator)
////				.maximumSize(heapSize).create();
////		long start = System.currentTimeMillis();
////		long startInitialTime = System.currentTimeMillis();
////
////		HashMap<Long, DijkstraRunner> dijkstraRunnerOfNodeId = new HashMap<Long, DijkstraRunner>();
////		for (String keyword : keywords) { // keywords is an ArrayList - enhanced
////											// for-loop that goes through each
////											// keyword
////			// initialize an instance of DijkstraRunner from each source
////			// candidate
////			for (Long sourceNodeId : candidatesOfAKeyword.get(keyword)) {
////				if (!dijkstraRunnerOfNodeId.containsKey(sourceNodeId)) {
////					DijkstraRunner dijkstraRunner = new DijkstraRunner(dataGraph, createdExtendedNodeOfNodeId,
////							sourceNodeId, keyword, distanceBound, debugMode, candidatesSet, cleanedNodeIdOfLabel);
////					iteratorHeap.add(dijkstraRunner);
////					// visitedNodesByAllDijkstras.add(sourceNodeId);
////					dijkstraRunnerOfNodeId.put(sourceNodeId, dijkstraRunner);
////				} else {
////					dijkstraRunnerOfNodeId.get(sourceNodeId).frontier.peek().originsOfKeywords.put(keyword,
////							new HashSet<>());
////					dijkstraRunnerOfNodeId.get(sourceNodeId).frontier.peek().originsOfKeywords.get(keyword)
////							.add(sourceNodeId);
////				}
////			}
////
////		}
////
////		long endInitialTime = System.currentTimeMillis();
////		long initialTime = (endInitialTime - startInitialTime);
////		System.out.println("initial time: " + initialTime + " ms.");
////
////		int cnt = 0;
////		// while IteratorHeap is not empty and more results required
////		System.out.println("iteratorHeap: " + iteratorHeap.size());
////		System.out.println();
////		long startExtendTime = System.currentTimeMillis();
////		while (!iteratorHeap.isEmpty() && (totalPrintedResults + outputHeap.size()) < maxRequiredResults) {
////			// Iterator = remove top iterator from IteratorHeap
////
////			DijkstraRunner dijkstraRunner = iteratorHeap.poll();
////
////			// if (debugMode)
////			// System.out.println("cnt: " + cnt++);
////
////			// v = Get next node from Iterator
////			ExtendedNode nextExtendedNode = dijkstraRunner.getNextExtendedNode();
////
////			// if (debugMode)
////			// System.out.println("dijkstraRunner origin:" +
////			// dijkstraRunner.getKeyword() + ", dijkstraRunnerOriginId:"
////			// + dijkstraRunner.originNodeId + ", nextExtendedNode:" +
////			// nextExtendedNode.node.getId());
////			//
////			// // for debug start
////			// if (debugMode) {
////			// for (Long originId :
////			// nextExtendedNode.distanceFromOriginId.keySet()) {
////			// System.out.println(
////			// "originId:" + originId + " dist:" +
////			// nextExtendedNode.distanceFromOriginId.get(originId));
////			// }
////			// for (Long originId :
////			// nextExtendedNode.costOfPathFromOriginId.keySet()) {
////			// System.out.println(
////			// "originId:" + originId + " cost:" +
////			// nextExtendedNode.costOfPathFromOriginId.get(originId));
////			// }
////			// for (Long originId :
////			// nextExtendedNode.pathOfRootToTheOrigin.keySet()) {
////			// System.out.println("originId:" + originId + " path:"
////			// +
////			// Arrays.toString(nextExtendedNode.pathOfRootToTheOrigin.get(originId).toArray()));
////			// }
////			// System.out.println();
////			// // for debug end
////			// }
////
////			if (dijkstraRunner.hasMoreNodesToOutput()) {
////				iteratorHeap.add(dijkstraRunner);
////			}
////
////			// // cross product
////			long startCrossTime = System.currentTimeMillis();
////			if (nextExtendedNode.originsOfKeywords.keySet().size() == keywords.size()) {
////				ArrayList<HashMap<String, HashSet<Integer>>> crossedProductResults = CrossProduct
////						.crossProduct(nextExtendedNode.originsOfKeywords);
////
////				// for each tuple in CrossProduct
////				// create ResultTree from tuple
////
////				for (HashMap<String, HashSet<Long>> keywordNodeIdsParticipatingInTree : crossedProductResults) {
////					ResultTree newResultTree = new ResultTree();
////					newResultTree.resultTree(graph, keywordNodeIdsParticipatingInTree,
////							nextExtendedNode.originsOfKeywords, nextExtendedNode.pathOfRootToTheOrigin);
////
////					if (outputHeap.size() == heapSize) {
////						ResultTree resultTreeToBeRemovedAndPrinted = outputHeap.poll();
////						print(resultTreeToBeRemovedAndPrinted);
////					}
////					outputHeap.add(newResultTree);
////				}
////			}
////		}
////		long endCrossTime = System.currentTimeMillis();
////		long crossExtendTime = (endCrossTime - startExtendTime);
////		System.out.println("entend + cross Product time: " + crossExtendTime + " ms.");
////
////		long end = System.currentTimeMillis();
////		long time = end - start;
////		System.out.println("The total run time is: " + time + " ms.");
////
////		System.out.println("number of results: " + fullOutputsQueue.size());
////
////		if (debugMode && visualizeMode) {
////			Visualizer.visualizeOutput(fullOutputsQueue);
////			System.out.println("finsihed:");
////		}
////	}
////
////
////	private void print(ResultTree resultTreeToBePrinted) {
////
////		if (!isomorphisToPreviouslyPrinted(printedResultTrees, resultTreeToBePrinted.anOutputTree)) {
////			System.out.println("cost: " + resultTreeToBePrinted.cost);
////			System.out.println("OUTPUT TREE: " + totalPrintedResults++);
////
////			System.out.println("Vertices");
////			for (ResultNode resNod : resultTreeToBePrinted.anOutputTree.vertexSet()) {
////				System.out.println(resNod.nodeId + ", label: " + dataGraph.getNodeById(resNod.nodeId).getLabels());
////			}
////			System.out.println("Edges");
////			for (DefaultEdge e : resultTreeToBePrinted.anOutputTree.edgeSet()) {
////				System.out.println(resultTreeToBePrinted.anOutputTree.getEdgeSource(e) + "->"
////						+ resultTreeToBePrinted.anOutputTree.getEdgeTarget(e));
////				int sourceID = resultTreeToBePrinted.anOutputTree.getEdgeSource(e).nodeId;
////				int targetID = resultTreeToBePrinted.anOutputTree.getEdgeTarget(e).nodeId;
////				System.out.println("relationship: "
////						+ Dummy.DummyFunctions.getRelationshipOfPairNodes(graph, sourceID, targetID));
////			}
////
////			printedResultTrees.add(resultTreeToBePrinted.anOutputTree);
////
////			if (visualizeMode)
////				fullOutputsQueue.add(resultTreeToBePrinted);
////		}
////
////	}
////
////	private boolean isomorphisToPreviouslyPrinted(
////			ArrayList<ListenableUndirectedGraph<ResultNode, DefaultEdge>> printedResultTrees2,
////			ListenableUndirectedGraph<ResultNode, DefaultEdge> anOutputTree) {
////
////		for (ListenableUndirectedGraph<ResultNode, DefaultEdge> graph1 : printedResultTrees2) {
////
////			VF2GraphIsomorphismInspector<ResultNode, DefaultEdge> vf2Checker = new VF2GraphIsomorphismInspector<ResultNode, DefaultEdge>(
////					graph1, anOutputTree, new Comparator<ResultNode>() {
////						@Override
////						public int compare(ResultNode o1, ResultNode o2) {
////							return Long.compare(o1.nodeId, o2.nodeId);
////						}
////					}, null);
////			if (vf2Checker.isomorphismExists())
////				return true;
////
////		}
////
////		return false;
////	}
////
////	public void findCandidatesOfKeywords() throws Exception {
////		long startFindTime = System.currentTimeMillis();
////
////		for (String keyword : keywords) {
////			if (cleanedNodeIdOfLabel.containsKey(keyword)) {
////				candidatesOfAKeyword.putIfAbsent(keyword, new HashSet<Long>());
////				candidatesOfAKeyword.get(keyword).addAll(nodeIdsOfALabel.get(cleanedNodeIdOfLabel.get(keyword)));
////			}
////			candidatesSet.addAll(candidatesOfAKeyword.get(keyword));
////		}
////
////		// debug
////		Iterator it = candidatesOfAKeyword.entrySet().iterator();
////		while (it.hasNext()) {
////			HashMap.Entry<String, HashSet<Long>> entry = (java.util.Map.Entry<String, HashSet<Long>>) it.next();
////			System.out.println("keyword: " + entry.getKey());
////			System.out.println("candidate size: " + entry.getValue().size());
////			System.out.println("candidates: " + entry.getValue());
////			System.out.println();
////		}
////		// debug
////		long endFindTime = System.currentTimeMillis();
////		long findTime = (endFindTime - startFindTime);
////		System.out.println("index node label time: " + findTime + " ms.");
////
////	}
////
////	public void indexNodeLabelsAndProperties() {
////		long startIndexTime = System.currentTimeMillis();
////		for (Node node : dataGraph.getAllNodes()) {
////			// index labels
////			for (Label lbl : node.getLabels()) {
////				String token = DummyFunctions.getCleanedString(lbl.name()).toLowerCase();
////				nodeIdsOfALabel.putIfAbsent(token, new ArrayList<Long>());
////				nodeIdsOfALabel.get(token).add(node.getId());
////				String newToken = "";
////				if (token.contains("uri_")) {
////					String[] tem = token.trim().split("_");
////					for (int i = 1; i < tem.length; i++) {
////						newToken = newToken.trim() + " " + tem[i];
////					}
////				} else {
////					newToken = token;
////				}
////				cleanedNodeIdOfLabel.put(newToken.trim(), token);
////			}
////
////		}
////		long endIndexTime = System.currentTimeMillis();
////		long indexTime = (endIndexTime - startIndexTime);
////		System.out.println("find candidate set time: " + indexTime + " ms.");
////
////	}
////
////	private void readKeywords() throws Exception {
////		FileInputStream fis = new FileInputStream(keywordsPath);
////		// Construct BufferedReader from InputStreamReader
////		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
////		String keywordsLine = br.readLine();
////		br.close();
////
////		// keywordsLine = "wenfei fan yinghui wu";
////		StringTokenizer stringTokenizer = new StringTokenizer(keywordsLine, ",", false);
//////		System.out.println(stringTokenizer.countTokens());
////		while (stringTokenizer.hasMoreElements()) {
////			String nextToken = DummyFunctions.getCleanedString(stringTokenizer.nextElement().toString());
////
////			if (debugMode)
////				System.out.println("nextToken: " + nextToken);
////
////			keywords.add(nextToken);
////		}
////	}
//
//}
