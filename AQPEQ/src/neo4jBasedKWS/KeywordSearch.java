package neo4jBasedKWS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

import org.apache.jena.ext.com.google.common.collect.MinMaxPriorityQueue;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import aqpeq.utilities.Dummy;
import aqpeq.utilities.Dummy.DummyFunctions;
import dataset.BerkeleyDB.BerkleleyDB;
import string.transformations.TransformationObject;
import aqpeq.utilities.TreeNode;
import aqpeq.utilities.Visualizer;
//
//// TODO: implementing backward edges including its weighting schemes
//
public class KeywordSearch {
//
//	// private static String dataGraphPath =
//	// "/Users/mnamaki/AQPEQ/GraphExamples/k1";
//	// private static String keywordsPath =
//	// "/Users/mnamaki/AQPEQ/GraphExamples/k1/keywords.txt";
//	// private static String dataGraphPath =
//	// "/Users/zhangxin/Desktop/dbp_3_2_1_sample";
//	private static String dataGraphPath = "/Users/zhangxin/Desktop/Summer/dbp_3_2_1";
//	private static String keywordsPath = "/Users/zhangxin/Desktop/keywords.txt";
//	private static int heapSize = 1;
//	ArrayList<String> keywords = new ArrayList<String>();
//	HashMap<String, HashMap<String, Double>> similarKeywords = new HashMap<String, HashMap<String, Double>>();
//	HashMap<String, HashSet<Long>> candidatesOfAKeyword = new HashMap<String, HashSet<Long>>();
//	HashSet<Long> candidatesSet = new HashSet<Long>();
//
//	HashMap<String, ArrayList<Long>> nodeIdsOfALabel = new HashMap<String, ArrayList<Long>>();
//	HashMap<String, String> cleanedNodeIdOfLabel = new HashMap<String, String>();
//	HashMap<String, ArrayList<Long>> nodeIdsOfAAttributeKey = new HashMap<String, ArrayList<Long>>();
//	HashMap<String, ArrayList<Long>> nodeIdsOfAAttributeValues = new HashMap<String, ArrayList<Long>>();
//
//	int totalPrintedResults = 0;
//	static int maxRequiredResults = 3;
//	static GraphDatabaseService dataGraph;
//	private static int distanceBound = 3;
//	private static boolean debugMode = true;
//	private static boolean visualizeMode = true;
//	
//	private static String database = "database";
//	private static String catDatabase = "catDatabase";
//	private static String enrichDatabase = "enrichDatabase";
//	private static String enrichCatDatabase = "enrichCatDatabase";
//	private static String envFilePath = "dbEnv";
//
//	LinkedList<ListenableDirectedGraph<Long, DefaultEdge>> fullOutputsQueue = new LinkedList<ListenableDirectedGraph<Long, DefaultEdge>>();
//
//	public static void main(String[] args) throws Exception {
//
//		for (int i = 0; i < args.length; i++) {
//			if (args[i].equals("-dataGraph")) {
//				dataGraphPath = args[++i];
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
//			} else if (args[i].equals("-database")) {
//				database = args[++i];
//			} else if (args[i].equals("-catDatabase")) {
//				catDatabase = args[++i];
//			} else if (args[i].equals("-enrichDatabase")) {
//				enrichDatabase = args[++i];
//			} else if (args[i].equals("-enrichCatDatabase")) {
//				enrichCatDatabase = args[++i];
//			} else if (args[i].equals("-envFilePath")) {
//				envFilePath = args[++i];
//			}
//
//		}
//
//		KeywordSearchChange keywordSearch = new KeywordSearchChange();
//
//		keywordSearch.readKeywords();
//
//		File storeDir = new File(dataGraphPath);
//		dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
//				.setConfig(GraphDatabaseSettings.pagecache_memory, "4g")
//				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();
//
//		Transaction tx1 = dataGraph.beginTx();
//		
//		keywordSearch.indexNodeLabelsAndProperties();
//
//		keywordSearch.findCandidatesOfKeywords();
//
//		keywordSearch.run();
//
//		tx1.success();
//		tx1.close();
//	}
//
//	private void run() {
//
//		HashMap<Long, ExtendedNode> createdExtendedNodeOfNodeId = new HashMap<Long, ExtendedNode>();
//		// HashMap<Integer, List<Long>> crossProductResult = new
//		// HashMap<Integer, List<Long>>();
//
//		// TODO: to be implemented
//		ResultTreeRelevanceComparator resultTreeRelevanceComparator = new ResultTreeRelevanceComparator();
//
//		DijkstraDistanceComparator dijkstraDistanceComparator = new DijkstraDistanceComparator();
//
//		PriorityQueue<DijkstraRunner> iteratorHeap = new PriorityQueue<>(dijkstraDistanceComparator);
//
//		MinMaxPriorityQueue<ResultTree> outputHeap = MinMaxPriorityQueue.orderedBy(resultTreeRelevanceComparator)
//				.maximumSize(heapSize).create();
//		
//		long startInitialTime=System.currentTimeMillis();
//		for (String keyword : keywords) { // keywords is an ArrayList - enhanced
//											// for-loop that goes through each
//											// keyword
//			// initialize an instance of DijkstraRunner from each source
//			// candidate
//			for (Long sourceNodeId : candidatesOfAKeyword.get(keyword)) {
//
//				DijkstraRunner dijkstraRunner = new DijkstraRunner(dataGraph, createdExtendedNodeOfNodeId, sourceNodeId,
//						keyword, distanceBound, debugMode, candidatesSet, cleanedNodeIdOfLabel);
//				iteratorHeap.add(dijkstraRunner);
//				// visitedNodesByAllDijkstras.add(sourceNodeId);
//			}
//		}
//		
//		long endInitialTime=System.currentTimeMillis();
//		long initialTime = (endInitialTime - startInitialTime);
//		System.out.println("initial time: " + initialTime + " ms.");
//
//		int cnt = 0;
//		// while IteratorHeap is not empty and more results required
//		System.out.println("iteratorHeap: " + iteratorHeap.size());
//		System.out.println();
//		long startExtendTime=System.currentTimeMillis();
//		while (!iteratorHeap.isEmpty() && (totalPrintedResults + outputHeap.size()) < maxRequiredResults) {
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
//			// if (debugMode)
//			// System.out.println("dijkstraRunner origin:" +
//			// dijkstraRunner.getKeyword() + ", dijkstraRunnerOriginId:"
//			// + dijkstraRunner.originNodeId + ", nextExtendedNode:" +
//			// nextExtendedNode.node.getId());
//			//
//			// // for debug start
//			// if (debugMode) {
//			// for (Long originId :
//			// nextExtendedNode.distanceFromOriginId.keySet()) {
//			// System.out.println(
//			// "originId:" + originId + " dist:" +
//			// nextExtendedNode.distanceFromOriginId.get(originId));
//			// }
//			// for (Long originId :
//			// nextExtendedNode.costOfPathFromOriginId.keySet()) {
//			// System.out.println(
//			// "originId:" + originId + " cost:" +
//			// nextExtendedNode.costOfPathFromOriginId.get(originId));
//			// }
//			// for (Long originId :
//			// nextExtendedNode.pathOfRootToTheOrigin.keySet()) {
//			// System.out.println("originId:" + originId + " path:"
//			// +
//			// Arrays.toString(nextExtendedNode.pathOfRootToTheOrigin.get(originId).toArray()));
//			// }
//			// System.out.println();
//			// // for debug end
//			// }
//
//			if (dijkstraRunner.hasMoreNodesToOutput()) {
//				iteratorHeap.add(dijkstraRunner);
//			}
//
//			nextExtendedNode.originsOfKeywords.putIfAbsent(dijkstraRunner.getKeyword(), new ArrayList<Long>());
//			nextExtendedNode.originsOfKeywords.get(dijkstraRunner.getKeyword()).add(dijkstraRunner.originNodeId);
//			// visitedNodesByAllDijkstras.add(nextExtendedNode.node.getId());
//			// if (debugMode) {
//			// System.out.println(
//			// nextExtendedNode.node.getId() + ".originsOfKeywords: " +
//			// nextExtendedNode.originsOfKeywords);
//			// System.out.println();
//			// }
//			// }
//
//			// cross product
//			long startCrossTime=System.currentTimeMillis();
//			if (nextExtendedNode.originsOfKeywords.keySet().size() == keywords.size()) {
//				ArrayList<HashMap<String, ArrayList<Long>>> crossedProductResults = CrossProduct
//						.crossProduct(nextExtendedNode.originsOfKeywords);
//
//				// for each tuple in CrossProduct
//				// create ResultTree from tuple
//
//				for (HashMap<String, ArrayList<Long>> keywordNodeIdsParticipatingInTree : crossedProductResults) {
//					ResultTree newResultTree = new ResultTree();
//					newResultTree.resultTree(keywordNodeIdsParticipatingInTree, nextExtendedNode.originsOfKeywords,
//							nextExtendedNode.pathOfRootToTheOrigin);
//
//					if (outputHeap.size() == heapSize) {
//						ResultTree resultTreeToBeRemovedAndPrinted = outputHeap.poll();
//						print(resultTreeToBeRemovedAndPrinted);
//					}
//					outputHeap.add(newResultTree);
//				}
//			}
//		}
//		long endCrossTime=System.currentTimeMillis();
//		long crossExtendTime = (endCrossTime - startExtendTime);
//		System.out.println("entend + cross Product time: " + crossExtendTime + " ms.");
//
//		if (debugMode && visualizeMode) {
//			Visualizer.visualizeOutput(fullOutputsQueue);
//			System.out.println("finsihed:");
//		}
//
//	}
//
//	private void print(ResultTree resultTreeToBePrinted) {
//		if (resultTreeToBePrinted.treeNode.children.size() == 1)
//			return;
//
//		ListenableDirectedGraph<Long, DefaultEdge> anOutputTree = null;
//		if (visualizeMode)
//			anOutputTree = new ListenableDirectedGraph<Long, DefaultEdge>(DefaultEdge.class);
//
//		System.out.println("OUTPUT TREE: " + totalPrintedResults++);
//		TreeNode<Long> currentNode = resultTreeToBePrinted.treeNode;
//		HashSet<TreeNode<Long>> nodesSet = new HashSet<TreeNode<Long>>();
//		LinkedList<TreeNode<Long>> nodesList = new LinkedList<TreeNode<Long>>();
//		nodesList.add(currentNode);
//
//		if (visualizeMode)
//			anOutputTree.addVertex(currentNode.getData());
//
//		while (!nodesList.isEmpty()) {
//			currentNode = nodesList.poll();
//
//			nodesSet.add(currentNode);
//			System.out.print(currentNode.data + ", label: " + dataGraph.getNodeById(currentNode.data).getLabels());
//
//			if (currentNode.children.size() > 0) {
//				System.out.print(" children: ");
//
//				for (TreeNode<Long> child : currentNode.children) {
//					System.out.print(child.data + ", " + "label: " + dataGraph.getNodeById(child.data).getLabels()
//							+ ", relation: "
//							+ Dummy.DummyFunctions.getRelationshipOfPairNodes(dataGraph, currentNode.data, child.data));
//
//					if (!nodesSet.contains(child)) {
//						nodesList.add(child);
//
//						if (visualizeMode)
//							anOutputTree.addVertex(child.getData());
//					}
//
//					if (visualizeMode && !anOutputTree.containsEdge(currentNode.data, child.data)) {
//						anOutputTree.addEdge(currentNode.data, child.data);
//					}
//				}
//			}
//
//			System.out.println();
//		}
//
//		System.out.println();
//
//		if (visualizeMode)
//			fullOutputsQueue.add(anOutputTree);
//
//	}
//
//	private void findCandidatesOfKeywords() throws Exception {
//		long startFindTime=System.currentTimeMillis();
//		
//		//TODO: First search in database
//		//Second search in enrich database
//		//BerkleleyDB.CreateDatabase(database, catDatabase, envFilePath);
//		//BerkleleyDB.CreateDatabase(enrichDatabase, enrichCatDatabase, envFilePath);
//		
//		for(String keyword : keywords) {
//			//open database
//			BerkleleyDB.CreateDatabase(database, catDatabase, envFilePath);
//			if (BerkleleyDB.Search(keyword) != null) {
//				MyObject result = BerkleleyDB.Search(keyword);
//				similarKeywords.put(keyword, result.synonym);
////				similarKeywords.put(keyword, result.abbreviation);
////				similarKeywords.put(keyword, result.otherTransormation);
////				candidatesOfAKeyword.putIfAbsent(keyword, (HashSet<Long>) result.similar.keySet());
////				candidatesSet.addAll(candidatesOfAKeyword.get(keyword));
//				candidatesOfAKeyword.putIfAbsent(keyword, new HashSet<Long>());
//				HashMap<String, Double> map = similarKeywords.get(keyword);
//				for (String similarKeyword: similarKeywords.get(keyword).keySet()){
//					if (cleanedNodeIdOfLabel.containsKey(similarKeyword)) {
//						candidatesOfAKeyword.get(similarKeyword)
//								.addAll(nodeIdsOfALabel.get(cleanedNodeIdOfLabel.get(similarKeyword)));
//					}
//					candidatesSet.addAll(candidatesOfAKeyword.get(similarKeyword));
//				}
//			} else {
//				//close database
//				BerkleleyDB.CloseDatabase();
//				//open enrich
//				BerkleleyDB.CreateDatabase(enrichDatabase, enrichCatDatabase, envFilePath);
//				if (BerkleleyDB.Search(keyword) == null) {
//					continue;
//				} else {
//					MyObject result = BerkleleyDB.Search(keyword);
//					similarKeywords.put(keyword, result.synonym);
////					similarKeywords.put(keyword, result.abbreviation);
////					similarKeywords.put(keyword, result.otherTransormation);
////					candidatesOfAKeyword.putIfAbsent(keyword, (HashSet<Long>) result.similar.keySet());
////					candidatesSet.addAll(candidatesOfAKeyword.get(keyword));
//					candidatesOfAKeyword.putIfAbsent(keyword, new HashSet<Long>());
//					HashMap<String, Double> map = similarKeywords.get(keyword);
//					for (String similarKeyword: similarKeywords.get(keyword).keySet()){
//						if (cleanedNodeIdOfLabel.containsKey(similarKeyword)) {
//							candidatesOfAKeyword.get(similarKeyword)
//									.addAll(nodeIdsOfALabel.get(cleanedNodeIdOfLabel.get(similarKeyword)));
//						}
//						candidatesSet.addAll(candidatesOfAKeyword.get(similarKeyword));
//					}
//				}
//				//close enrich
//				BerkleleyDB.CloseDatabase();
//			}
//			//close database
//			BerkleleyDB.CloseDatabase();
//		}
//		
//		// debug
//		Iterator it = candidatesOfAKeyword.entrySet().iterator();
//		while (it.hasNext()) {
//			HashMap.Entry<String, HashSet<Long>> entry = (java.util.Map.Entry<String, HashSet<Long>>) it.next();
//			System.out.println("keyword: " + entry.getKey());
//			System.out.println("candidate size: " + entry.getValue().size());
//			System.out.println("candidates: " + entry.getValue());
//			System.out.println();
//		}
//		// debug
//		long endFindTime=System.currentTimeMillis();
//		long findTime = (endFindTime - startFindTime);
//		System.out.println("index node label time: " + findTime + " ms.");
//
//	}
//
//	private void indexNodeLabelsAndProperties() {
//		long startIndexTime=System.currentTimeMillis();
//		for (Node node : dataGraph.getAllNodes()) {
//			// index labels
//			for (Label lbl : node.getLabels()) {
//				String cleanLbl = DummyFunctions.getCleanedString(lbl.name());
//				for (String token : DummyFunctions.getTokens(cleanLbl)) {
//					nodeIdsOfALabel.putIfAbsent(token, new ArrayList<Long>());
//					nodeIdsOfALabel.get(token).add(node.getId());
//					String newToken = "";
//					if (token.contains("uri_")) {
//						String[] tem = token.trim().split("_");
//						for (int i = 1; i < tem.length; i++) {
//							newToken = newToken + " " + tem[i];
//						}
//					} else {
//						newToken = token;
//					}
//					cleanedNodeIdOfLabel.put(newToken.trim(), token);
//				}
//			}
//
//			// // index property keys and values
//			// Map<String, Object> props = node.getAllProperties();
//			//
//			// // index property keys
//			// for (String key : props.keySet()) {
//			// String cleanKey = DummyFunctions.getCleanedString(key);
//			// for (String token : DummyFunctions.getTokens(cleanKey)) {
//			// nodeIdsOfAAttributeKey.putIfAbsent(token, new ArrayList<Long>());
//			// nodeIdsOfAAttributeKey.get(token).add(node.getId());
//			// }
//			// }
//			//
//			// // index property values
//			// for (String key : props.keySet()) {
//			// String cleanValue =
//			// DummyFunctions.getCleanedString(props.get(key).toString());
//			// for (String token : DummyFunctions.getTokens(cleanValue)) {
//			// nodeIdsOfAAttributeValues.putIfAbsent(token, new
//			// ArrayList<Long>());
//			// nodeIdsOfAAttributeValues.get(token).add(node.getId());
//			// }
//			// }
//		}
//		long endIndexTime=System.currentTimeMillis();
//		long indexTime = (endIndexTime - startIndexTime);
//		System.out.println("find candidate set time: " + indexTime + " ms.");
//
//	}
//
//	private void readKeywords() throws Exception {
//		FileInputStream fis = new FileInputStream(keywordsPath);
//		// Construct BufferedReader from InputStreamReader
//		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
//		String keywordsLine = br.readLine();
//		br.close();
//
//		// keywordsLine = "wenfei fan yinghui wu";
//		StringTokenizer stringTokenizer = new StringTokenizer(keywordsLine, ",", false);
//		System.out.println(stringTokenizer.countTokens());
//		while (stringTokenizer.hasMoreElements()) {
//			String nextToken = DummyFunctions.getCleanedString(stringTokenizer.nextElement().toString());
//
//			if (debugMode)
//				System.out.println("nextToken: " + nextToken);
//
//			keywords.add(nextToken);
//		}
//	}
//
}
