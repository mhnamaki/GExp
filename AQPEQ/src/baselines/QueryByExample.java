//package baselines;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedHashSet;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.PriorityQueue;
//import java.util.Stack;
//import java.util.StringTokenizer;
//import java.util.Map.Entry;
//
//import org.apache.jena.ext.com.google.common.collect.MinMaxPriorityQueue;
//import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;
//import org.jgrapht.graph.DefaultEdge;
//import org.jgrapht.graph.ListenableUndirectedGraph;
//
//import aqpeq.utilities.Dummy;
//import aqpeq.utilities.MapUtil;
//import aqpeq.utilities.Visualizer;
//import aqpeq.utilities.Dummy.DummyFunctions;
//import aqpeq.utilities.Dummy.DummyProperties;
//import bank.keywordSearch.DijkstraDistanceComparator;
//import bank.keywordSearch.DijkstraRunner;
//import bank.keywordSearch.ExperimentUsingBDB;
//import dataset.BerkeleyDB.BerkleleyDB;
//import graphInfra.BackwardRelInfra;
//import graphInfra.GraphInfraReaderArray;
//import graphInfra.NodeInfra;
//import graphInfra.RelationshipInfra;
//import neo4jBasedKWS.CrossProduct;
//import neo4jBasedKWS.ResultNode;
//import neo4jBasedKWS.ResultTree;
//import neo4jBasedKWS.ResultTreeRelevanceComparator;
//import pairwiseBasedKWS.NearestNodeAndDistance;
//import pairwiseBasedKWS.PairwiseAnswer;
//import tryingToTranslate.PrunedLandmarkLabeling_labelloader;
//
//public class QueryByExample {
//
//	static GraphInfraReaderArray graph;
//
//	// xin
//	// private static String graphInfraPath =
//	// "/Users/zhangxin/Desktop/DBP/graph/";
//	// private static String keywordsPath =
//	// "/Users/zhangxin/Desktop/AQPEQ/KeywordExamples/scientistUCBaward.txt";
//	// private static String envFilePath =
//	// "/Users/zhangxin/Desktop/DBP/newdbEnv";
//
//	// mhn
//	private static String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/dbpGraphInfra/graph/";
//	private static String keywordsPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/dbpGraphInfra/keywords/scientistUCBaward.txt";
//	private static String envFilePath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/dbpGraphInfra/bdb/newdbEnv/";
//
//	private static int heapSize = 1;
//	static int maxRequiredResults = 3;
//	private static int distanceBound = 1;
//	private static boolean debugMode = true;
//	private static boolean visualizeMode = false;
//
//	static HashMap<String, HashSet<Integer>> candidatesOfAKeyword = new HashMap<String, HashSet<Integer>>();
//	static HashSet<Integer> candidatesSet = new HashSet<Integer>();
//	static HashMap<String, HashSet<Integer>> nodeIdsOfToken;
//	static HashMap<String, HashSet<Integer>> edgeIdsOfToken;
//
//	ArrayList<ListenableUndirectedGraph<ResultNode, DefaultEdge>> printedResultTrees = new ArrayList<ListenableUndirectedGraph<ResultNode, DefaultEdge>>();
//	static int totalPrintedResults = 0;
//
//	static LinkedList<ResultTree> fullOutputsQueue = new LinkedList<ResultTree>();
//
//	// BDB
//	private static String database = "database";
//	private static String catDatabase = "catDatabase";
//
//	private static BerkleleyDB berkeleyDB;
//	private static boolean withProperties;
//
//	// experiments
//	private static int numberOfSameExperiments = 1;
//	private static boolean usingBDB = true;
//	PrunedLandmarkLabeling_labelloader prunedLandmarkLabeling;
//
//	private int distanceIndexDBBitNum;
//
//	private String distanceIndexDB = "";
//
//	private static int r;
//	private static int k = 1;
//
//	HashMap<String, Integer> freqOfKeywordsOnTheAnswers = new HashMap<String, Integer>();
//
//	// top selected keywords
//	ArrayList<String> topFrequentKeywords;
//
//	public QueryByExample() {
//
//	}
//
//	public static void main(String[] args) throws Exception {
//
//		for (int i = 0; i < args.length; i++) {
//			if (args[i].equals("-dataGraph")) {
//				graphInfraPath = args[++i];
//			} else if (args[i].equals("-keywordsPath")) {
//				keywordsPath = args[++i];
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
//			} else if (args[i].equals("-envFilePath")) {
//				envFilePath = args[++i];
//			} else if (args[i].equals("-usingBDB")) {
//				usingBDB = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-withProperties")) {
//				withProperties = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-r")) {
//				r = Integer.parseInt(args[++i]);
//			} else if (args[i].equals("-k")) {
//				k = Integer.parseInt(args[++i]);
//			}
//
//		}
//
//		DummyProperties.withProperties = withProperties;
//		QueryByExample experimentUsingBDB = new QueryByExample();
//		experimentUsingBDB.runQBE();
//
//	}
//
//	private void runQBE() throws Exception {
//
//		boolean addBackward = true;
//
//		graph = new GraphInfraReaderArray(graphInfraPath, addBackward);
//
//		if (!usingBDB) {
//			graph.read();
//			nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);
//			edgeIdsOfToken = graph.indexEdgeInvertedListOfType(graph);
//			updateTheEdgeWeights(graph);
//		} else {
//			graph.readWithNoLabels();
//			berkeleyDB = new BerkleleyDB(database, catDatabase, envFilePath);
//			updateTheEdgeWeightsUsingBDB(graph, berkeleyDB);
//		}
//
//		prunedLandmarkLabeling = new PrunedLandmarkLabeling_labelloader(distanceIndexDBBitNum, distanceIndexDB);
//
//		System.out.println("finish read");
//
//		File result = new File("Result.txt");
//		FileOutputStream fosResult = new FileOutputStream(result);
//		BufferedWriter bwResult = new BufferedWriter(new OutputStreamWriter(fosResult));
//
//		ArrayList<ArrayList<String>> keywordsSet = readKeywords();
//
//		bwResult.write("distance bound: " + distanceBound + "\n");
//		bwResult.write("\n");
//
//		for (int i = 0; i < keywordsSet.size(); i++) {
//
//			ArrayList<String> keywords = keywordsSet.get(i);
//			bwResult.write("keywords: " + keywords + "\n");
//
//			ArrayList<Integer> timeInformation = new ArrayList<Integer>();
//			candidatesOfAKeyword = new HashMap<String, HashSet<Integer>>();
//			candidatesSet = new HashSet<Integer>();
//
//			int exp = 0;
//			long averageTime = 0;
//
//			if (usingBDB) {
//				findCandidatesOfKeywordsUsingBDB(keywords, berkeleyDB);
//			} else {
//				findCandidatesOfKeywordsUsingInvertedList(keywords);
//
//			}
//
//			visualizeMode = false;
//			if (exp == 0) {
//				// debug
//				Iterator<Entry<String, HashSet<Integer>>> it = candidatesOfAKeyword.entrySet().iterator();
//				while (it.hasNext()) {
//					HashMap.Entry<String, HashSet<Integer>> entry = (java.util.Map.Entry<String, HashSet<Integer>>) it
//							.next();
//					bwResult.write("keyword: " + entry.getKey() + "\n");
//					bwResult.write("candidate size: " + entry.getValue().size() + "\n");
//					bwResult.write("candidates: " + entry.getValue() + "\n");
//
//					visualizeMode = true;
//				}
//				// debug
//			}
//
//			totalPrintedResults = 0;
//			fullOutputsQueue = new LinkedList<ResultTree>();
//			Double time = run(keywords);
//			timeInformation.add(time.intValue());
//			bwResult.write((exp + 1) + " time: " + time + "\n");
//			exp++;
//
//			bwResult.write("average time: " + averageTime + " ms." + "\n");
//			bwResult.write("\n");
//			bwResult.write("----------------------------------------------");
//			bwResult.write("\n");
//		}
//
//		bwResult.close();
//
//	}
//
//	private void updateTheEdgeWeightsUsingBDB(GraphInfraReaderArray graph, BerkleleyDB berkeleyDB) throws Exception {
//
//		// w(e) = ief(e) / p(e);
//		for (RelationshipInfra rel : graph.relationOfRelId) {
//
//			HashSet<String> edgeTypes = berkeleyDB.SearchEdgeTypeByRelId(rel.relId);
//
//			HashSet<Integer> edgesWithSameLabel = berkeleyDB.SearchNodeIdsByToken(edgeTypes.iterator().next());
//
//			double iefOfEdge = Math.log((double) graph.relationOfRelId.size() / (double) edgesWithSameLabel.size());
//
//			int participationDegreeCnt = 0;
//			for (int relId : edgesWithSameLabel) {
//
//				if (rel.sourceId == graph.relationOfRelId.get(relId).sourceId
//						|| rel.destId == graph.relationOfRelId.get(relId).destId) {
//					participationDegreeCnt++;
//				}
//			}
//
//			rel.weight = (float) (iefOfEdge / participationDegreeCnt);
//		}
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
//	private double run(ArrayList<String> keywords) throws Exception {
//
//		long start = System.nanoTime();
//
//		// maybe runnign r-clique first?
//		Integer[] queryEntities = findCloseNodeMatchOfKeyword(keywords);
//
//		// m ← r/(|t|+1)
//		int m = r / (keywords.size() + 1);
//
//		// V (MQGt) ← empty ;
//		// E(MQGt) ← empty;
//		QBEGraph maximalQueryGraph = new QBEGraph();
//
//		// G ←empty;
//		ArrayList<QBEGraph> qbeGraphs = new ArrayList<QBEGraph>(keywords.size() + 1);
//
//		// foreach vi ∈ t do
//		for (int v_i : queryEntities) {
//			// Gvi ← use DFS to obtain the subgraph containing vertices (and
//			// their incident edges) that connect to other vj in t only through
//			// vi;
//			QBEGraph subgraphOfVi = getSubgraphByDFS(graph, v_i, queryEntities);
//
//			// G ← G ∪ {Gvi};
//			qbeGraphs.add(subgraphOfVi);
//
//		}
//
//		// Gcore ← use DFS to obtain the subgraph containing vertices and
//		// edges on undirected paths between query entities;
//		QBEGraph gCore = getMST(graph, queryEntities);
//
//		// G ← G ∪ {Gcore};
//		qbeGraphs.add(gCore);
//
//		// foreach G ∈ G do
//		for (QBEGraph qbeGraph : qbeGraphs) {
//
//			// step ← 1; s1 ← 0; s ← m;
//			int step = 1;
//			int s1 = 0;
//			int s2 = 0;
//			int s = m;
//
//			QBEGraph ms = null;
//
//			// while s > 0 do
//			while (s > 0) {
//
//				// Ms ← the weakly connected component found from the
//				// top-s edges of G that contains all of G’s query entities;
//				ms = getMsUsingTopSEdges(graph, qbeGraph, s, queryEntities);
//
//				// if Ms exists then
//				if (ms != null) {
//
//					// if |E(Ms)| = m then break
//					if (ms.getNumberOfEdges() == m)
//						break;
//
//					// if |E(Ms)| < m then
//					if (ms.getNumberOfEdges() < m) {
//
//						// s1 ← s;
//						s1 = s;
//
//						// if step = −1 then break
//						if (step == -1)
//							break;
//					}
//
//					// if |E(Ms)| > m then
//					if (ms.getNumberOfEdges() > m) {
//
//						// if s1 > 0 then
//						if (s1 > 0) {
//
//							// s ← s1; break;
//							s = s1;
//							break;
//						}
//
//						// s2 ← s; step ← −1;
//						s2 = s;
//						step = -1;
//
//					} // end of if ms edges > m
//
//				} // end of if ms exists
//
//				// s ← s + step;
//				s = s + step;
//
//			} // end of while
//
//			// if s = 0 then s ← s2
//			if (s == 0)
//				s = s2;
//
//			// V (MQGt) ← V (MQGt) ∪ V (Ms);
//			// E(MQGt) ← E(MQGt) ∪ E(Ms);
//			maximalQueryGraph.union(ms);
//
//		}
//
//		for (int nodeId : maximalQueryGraph.nodes) {
//			for (String keyword : DummyFunctions.getKeywords(graph, nodeId, berkeleyDB)) {
//				freqOfKeywordsOnTheAnswers.putIfAbsent(keyword, 0);
//				freqOfKeywordsOnTheAnswers.put(keyword, freqOfKeywordsOnTheAnswers.get(keyword) + 1);
//			}
//		}
//
//		Map<String, Integer> map = MapUtil.sortByValueDesc(freqOfKeywordsOnTheAnswers);
//
//		int i = 0;
//
//		topFrequentKeywords = new ArrayList<String>();
//
//		for (Map.Entry<String, Integer> entry : map.entrySet()) {
//
//			if (keywords.contains(entry.getKey()))
//				continue;
//
//			topFrequentKeywords.add(entry.getKey());
//
//			i++;
//
//			if (i == k)
//				break;
//		}
//
//		double duration = ((System.nanoTime() - start) / 1e6);
//		if (debugMode && visualizeMode) {
//			Visualizer.visualizeOutput(fullOutputsQueue, graph);
//			System.out.println("finsihed:");
//		}
//		return duration;
//	}
//
//	private QBEGraph getMsUsingTopSEdges(GraphInfraReaderArray graph, QBEGraph qbeGraph, int s,
//			Integer[] queryEntities) {
//
//		QBEGraph ms = new QBEGraph();
//
//		ArrayList<RelAndWeightPair> qbeRelsAndWeight = new ArrayList<RelAndWeightPair>();
//
//		HashSet<Integer> queryEntitiesSet = new HashSet<Integer>();
//
//		for (int relId : qbeGraph.relationships) {
//			qbeRelsAndWeight.add(new RelAndWeightPair(relId, graph.relationOfRelId.get(relId).weight));
//		}
//
//		Collections.sort(qbeRelsAndWeight, new Comparator<RelAndWeightPair>() {
//			@Override
//			public int compare(RelAndWeightPair o1, RelAndWeightPair o2) {
//				return Double.compare(o2.getWeight(), o1.getWeight());
//			}
//		});
//
//		for (int i = 0; i < s; i++) {
//			int relId = qbeRelsAndWeight.get(i).getRelId();
//			if (queryEntitiesSet.contains(graph.relationOfRelId.get(relId).sourceId)) {
//				queryEntitiesSet.remove(graph.relationOfRelId.get(relId).sourceId);
//			}
//			if (queryEntitiesSet.contains(graph.relationOfRelId.get(relId).destId)) {
//				queryEntitiesSet.remove(graph.relationOfRelId.get(relId).destId);
//			}
//		}
//
//		if (queryEntitiesSet.isEmpty()) {
//			ms.createByEdges(graph, qbeRelsAndWeight.subList(0, s));
//			return ms;
//		}
//
//		return null;
//	}
//
//	private QBEGraph getMST(GraphInfraReaderArray graph, Integer[] queryEntities) {
//		QBEGraph gCore = new QBEGraph();
//
//		HashSet<Integer> otherEntitiesSet = new HashSet<Integer>();
//
//		Stack<DFSQBENode> dfsStack = new Stack<DFSQBENode>();
//
//		for (int i = 0; i < queryEntities.length; i++) {
//			otherEntitiesSet.add(queryEntities[i]);
//			dfsStack.push(new DFSQBENode(queryEntities[i], -1, null, 0));
//		}
//
//		while (!dfsStack.isEmpty()) {
//
//			DFSQBENode currentDFSNode = dfsStack.pop();
//			int currentNodeId = currentDFSNode.getNodeId();
//
//			if (otherEntitiesSet.contains(currentNodeId)) {
//				updateQBEGraphUsingOnlyThisPath(graph, gCore, currentDFSNode);
//			}
//
//			if (currentDFSNode.getDistance() >= distanceBound)
//				continue;
//
//			for (int targetNodeId : graph.nodeOfNodeId.get(currentNodeId).getOutgoingRelIdOfSourceNodeId().keySet()) {
//				dfsStack.push(new DFSQBENode(targetNodeId,
//						graph.nodeOfNodeId.get(currentNodeId).getOutgoingRelIdOfSourceNodeId().get(targetNodeId),
//						currentDFSNode, currentDFSNode.getDistance() + 1));
//			}
//		}
//
//		return gCore;
//	}
//
//	private QBEGraph getSubgraphByDFS(GraphInfraReaderArray graph, int v_i, Integer[] queryEntities) {
//
//		HashSet<Integer> otherEntitiesSet = new HashSet<Integer>();
//		for (int i = 0; i < queryEntities.length; i++) {
//			if (queryEntities[i] != v_i)
//				otherEntitiesSet.add(queryEntities[i]);
//		}
//
//		QBEGraph qbeSubgraph = new QBEGraph();
//
//		Stack<DFSQBENode> dfsStack = new Stack<DFSQBENode>();
//		dfsStack.push(new DFSQBENode(v_i, null, 0));
//
//		while (!dfsStack.isEmpty()) {
//
//			DFSQBENode currentDFSNode = dfsStack.pop();
//			int currentNodeId = currentDFSNode.getNodeId();
//
//			if (otherEntitiesSet.contains(currentNodeId)) {
//				updateQBEGraphUsingOnlyThisPath(graph, qbeSubgraph, currentDFSNode);
//
//				otherEntitiesSet.remove(currentNodeId);
//			}
//
//			if (currentDFSNode.getDistance() >= distanceBound)
//				continue;
//
//			for (int targetNodeId : graph.nodeOfNodeId.get(currentNodeId).getOutgoingRelIdOfSourceNodeId().keySet()) {
//				dfsStack.push(new DFSQBENode(targetNodeId, currentDFSNode, currentDFSNode.getDistance() + 1));
//			}
//		}
//
//		return qbeSubgraph;
//	}
//
//	private void updateQBEGraphUsingThisPathAndIncidentEdges(GraphInfraReaderArray graph, QBEGraph qbeSubgraph,
//			DFSQBENode currentDFSNode) {
//
//		HashSet<Integer> visitedNodes = new HashSet<Integer>();
//		HashSet<Integer> visitedEdges = new HashSet<Integer>();
//
//		while (currentDFSNode != null) {
//			visitedNodes.add(currentDFSNode.getNodeId());
//
//			visitedEdges.addAll(
//					graph.nodeOfNodeId.get(currentDFSNode.getNodeId()).getOutgoingRelIdOfSourceNodeId().values());
//
//			currentDFSNode = currentDFSNode.getParentDFSNode();
//		}
//
//		qbeSubgraph.union(visitedNodes, visitedEdges);
//
//	}
//
//	private void updateQBEGraphUsingOnlyThisPath(GraphInfraReaderArray graph, QBEGraph gCore,
//			DFSQBENode currentDFSNode) {
//
//		HashSet<Integer> visitedNodes = new HashSet<Integer>();
//		HashSet<Integer> visitedEdges = new HashSet<Integer>();
//
//		while (currentDFSNode != null) {
//			visitedNodes.add(currentDFSNode.getNodeId());
//
//			visitedEdges.add(currentDFSNode.getRelId());
//
//			currentDFSNode = currentDFSNode.getParentDFSNode();
//		}
//
//		gCore.union(visitedNodes, visitedEdges);
//
//	}
//
//	private Integer[] findCloseNodeMatchOfKeyword(ArrayList<String> keywords) {
//
//		LinkedHashSet<Integer>[] searchSpace = (LinkedHashSet<Integer>[]) new LinkedHashSet[keywords.size()];
//
//		// for i=0 to l do
//		for (int i = 0; i < keywords.size(); i++) {
//
//			// Ci <-- the set of nodes in G containing ki
//			LinkedHashSet<Integer> nodeIdsOfTheToken = new LinkedHashSet<Integer>(
//					graph.nodeIdsOfToken.get(keywords.get(i)));
//
//			if (DummyProperties.debugMode) {
//				System.out.println(keywords.get(i) + ":" + nodeIdsOfTheToken);
//			}
//
//			searchSpace[i] = nodeIdsOfTheToken;
//		}
//
//		// s[i][j] -> [k]
//		NearestNodeAndDistance[][][] nearestNodeAndDistancesToSindex = new NearestNodeAndDistance[keywords.size()][][];
//
//		// for i=1 to l do
//		for (int i = 0; i < keywords.size(); i++) {
//			nearestNodeAndDistancesToSindex[i] = new NearestNodeAndDistance[searchSpace[i].size()][keywords.size()];
//			int j = 0;
//			for (Integer sji : searchSpace[i]) {
//				// d(sji, i) <-- 0
//				// n(sji, i) <-- sji
//				nearestNodeAndDistancesToSindex[i][j][i] = new NearestNodeAndDistance(sji, 0);
//				j++;
//			}
//		}
//
//		// for i<-1 to l do
//		for (int i = 0; i < keywords.size(); i++) {
//			// for j<-1 to size(Si) do
//			int j = 0;
//			for (Integer sji : searchSpace[i]) {
//				// for k<-1 to l ; k 6= i do
//				for (int k = 0; k < keywords.size(); k++) {
//					if (k == i)
//						continue;
//
//					// <dist; nearest> <-- shortest path from sji to Sk
//					NearestNodeAndDistance nndPair = getNearestNodeAndDistance(sji, searchSpace[k]);
//
//					// if dist <= r then
//					if (nndPair.distance <= r) {
//						// d(sji ; k) <-- dist
//						// n(sji ; k) <-- nearest
//						nearestNodeAndDistancesToSindex[i][j][k] = nndPair;
//					} else {
//						// d(sji ; k) <-- infinity
//						// n(sji ; k) <-- null
//						NearestNodeAndDistance nndPairInf = new NearestNodeAndDistance(null, Integer.MAX_VALUE);
//						nearestNodeAndDistancesToSindex[i][j][k] = nndPairInf;
//					}
//				}
//				j++;
//			}
//		}
//
//		// leastWeight <- infinity
//		int leastWeight = Integer.MAX_VALUE;
//
//		Integer[] nodeMatches = new Integer[keywords.size()];
//
//		// for i 1 to l do
//		for (int i = 0; i < keywords.size(); i++) {
//			int j = 0;
//			for (Integer sji : searchSpace[i]) {
//				boolean passDistanceCriteria = true;
//				for (int k = 0; k < keywords.size(); k++) {
//					if (nearestNodeAndDistancesToSindex[i][j][k].distance > r) {
//						passDistanceCriteria = false;
//						break;
//					}
//				}
//
//				if (passDistanceCriteria) {
//					// weight <-- \Sigma_{h=1}{l} d(sji ; h)
//					int weight = 0;
//					for (int h = 0; h < keywords.size(); h++) {
//						weight += nearestNodeAndDistancesToSindex[i][j][h].distance;
//					}
//
//					// if weight < leastW eight then
//					if (weight < leastWeight) {
//						// leastWeight <-- weight;
//						leastWeight = weight;
//
//						// topAnswer <--- <n(sji ; 1); : : : ; n(sji ; l)>
//						for (int k = 0; k < keywords.size(); k++) {
//							nodeMatches[k] = nearestNodeAndDistancesToSindex[i][j][k].nodeId;
//						}
//					}
//				}
//				j++;
//			}
//		}
//		return nodeMatches;
//	}
//
//	private NearestNodeAndDistance getNearestNodeAndDistance(Integer sourceNodeId,
//			LinkedHashSet<Integer> linkedHashSet) {
//
//		NearestNodeAndDistance nearestNodeAndDistance = new NearestNodeAndDistance();
//		for (Integer targetNodeId : linkedHashSet) {
//			if (prunedLandmarkLabeling.queryDistance(sourceNodeId, targetNodeId) < nearestNodeAndDistance.distance) {
//				nearestNodeAndDistance.distance = prunedLandmarkLabeling.queryDistance(sourceNodeId, targetNodeId);
//				nearestNodeAndDistance.nodeId = targetNodeId;
//			}
//		}
//
//		if (DummyProperties.debugMode) {
//			System.out.println("nearestNodeAndDistance: src:" + sourceNodeId + " nearest:"
//					+ nearestNodeAndDistance.nodeId + " dist:" + nearestNodeAndDistance.distance);
//		}
//		return nearestNodeAndDistance;
//	}
//
//	private void updateTheEdgeWeights(GraphInfraReaderArray graph) {
//
//		for (RelationshipInfra rel : graph.relationOfRelId) {
//
//			HashSet<String> edgeTypes = rel.types;
//
//			HashSet<Integer> edgesWithSameLabel = edgeIdsOfToken.get(edgeTypes.iterator().next());
//
//			double iefOfEdge = Math.log((double) graph.relationOfRelId.size() / (double) edgesWithSameLabel.size());
//
//			int participationDegreeCnt = 0;
//			for (int relId : edgesWithSameLabel) {
//
//				if (rel.sourceId == graph.relationOfRelId.get(relId).sourceId
//						|| rel.destId == graph.relationOfRelId.get(relId).destId) {
//					participationDegreeCnt++;
//				}
//			}
//
//			rel.weight = (float) (iefOfEdge / participationDegreeCnt);
//		}
//	}
//
//	public void findCandidatesOfKeywordsUsingBDB(ArrayList<String> keywords, BerkleleyDB berkeleyDB) throws Exception {
//		for (String keyword : keywords) {
//			ArrayList<String> keywordList = new ArrayList<String>();
//			for (String token : Dummy.DummyFunctions.getTokens(keyword)) {
//				keywordList.add(token);
//			}
//			HashSet<Integer> candidate = berkeleyDB.SearchNodeIdsByKeyword(keywordList);
//			candidatesOfAKeyword.put(keyword, candidate);
//			candidatesSet.addAll(candidate);
//		}
//
//		// debug
//		for (String keyword : candidatesOfAKeyword.keySet()) {
//			System.out.println("keyword: " + keyword + ", candidate set: " + candidatesOfAKeyword.get(keyword));
//		}
//	}
//
//	public void findCandidatesOfKeywordsUsingInvertedList(ArrayList<String> keywords) throws Exception {
//
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
//}
