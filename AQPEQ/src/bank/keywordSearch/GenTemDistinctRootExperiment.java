// TODO: just find distinct root answers
// TODO: comparator

package bank.keywordSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import org.apache.jena.ext.com.google.common.collect.MinMaxPriorityQueue;
import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.joda.time.DateTime;

import aqpeq.utilities.Visualizer;
import aqpeq.utilities.Dummy;
import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import baselines.CoOccurrence;
import baselines.DataCloudDistinguishability;
import aqpeq.utilities.InfoHolder;
import aqpeq.utilities.KWSUtilities;
import aqpeq.utilities.StringPoolUtility;
import aqpeq.utilities.TimeLogger;
import graphInfra.GraphInfraReaderArray;
import graphInfra.RelationshipInfra;
import incrementalEvaluation.IncEval;
import neo4jBasedKWS.CrossProduct;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;
import neo4jBasedKWS.ResultTreeRelevanceComparator;
import queryExpansion.AnswerAsInput;
import queryExpansion.DivQKWSExpand;
import queryExpansion.IndexGenTemKWSExpand;
import queryExpansion.ObjectiveHandler;
import tryingToTranslate.PrunedLandmarkLabeling;

public class GenTemDistinctRootExperiment {

	GraphInfraReaderArray graph;

	// xin
	// private static String graphInfraPath =
	// "/Users/zhangxin/AQPEQ/GraphExamples/GenTemTest/graph2/";
	// private static String keywordsPath =
	// "/Users/zhangxin/AQPEQ/GraphExamples/GenTemTest/keyword.txt";

	// mhn DBPedia
	// private static String graphInfraPath =
	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/dbpGraphInfra/graph/dbp/";
	// private static String keywordsPath =
	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/dbpGraphInfra/keywords/knuthPrize.txt";
	// private static String indexPath =
	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/dbpGraphInfra/distanceIndex/dbp_8bits.jin";

	private static boolean withProperties = true;

	// mhn IMDB
	private static String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/graph/";
	private static String keywordsPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/queries/l3/imdb_query_DR.txt";
	private static String indexPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/graph/imdb_8bits.jin";

	static int[] maxRequiredResults = { 10 };
	private static int[] distanceBounds = { 3 };
	private static double[] deltas = { 2 };//// quality bound
	private static int[] keywordSizes = { 3 };
	private static int[] numberOfQueries = { 5 };
	private static double[] lambdas = { 0.5d };
	private static double[] epsilons = { 0.1d, 0.2d, 0.3d };
	private static int[] maxTokensForNode = { 30 };

	int maxRequiredResult;
	private int distanceBound;
	private double delta;//// quality bound

	int heapSize = 0;

	HashMap<String, HashSet<Integer>> candidatesOfAKeyword = new HashMap<String, HashSet<Integer>>();
	HashSet<Integer> candidatesSet = new HashSet<Integer>();
	HashMap<Integer, HashSet<Integer>> nodeIdsOfToken = new HashMap<Integer, HashSet<Integer>>();

	ArrayList<ListenableUndirectedGraph<ResultNode, RelationshipInfra>> printedResultTrees = new ArrayList<ListenableUndirectedGraph<ResultNode, RelationshipInfra>>();

	// RootKWSExpand rootKWSExpand;
	// GenTemRootKWSExpand genTemRootKWSExpand;
	IndexGenTemKWSExpand streamDivQExpand;
	DivQKWSExpand divQExpand;

	private double avgNumberOfCandidates;
	private double avgOutDegreeOfKeywordCandidates;
	static int totalPrintedResults = 0;

	private double initialKWSStartTime = 0d;
	private double initialKWSDuration = 0d;

	public LinkedList<ResultTree> fullOutputsQueue = new LinkedList<ResultTree>();

	// experiments
	private static int numberOfSameExperiments = 1;
	private static boolean usingBDB = false;

	private static boolean debugMode = false;
	private static boolean visualizeMode = false;

	// steps turning on/off
	static boolean keywordSuggestionOn = true;
	// if don't want just change to false
	static boolean incEvalOn = keywordSuggestionOn && false;
	static boolean newKWSOn = keywordSuggestionOn && false;
	static boolean topkSelectionOn = keywordSuggestionOn && false;
	static boolean coOccBaseLineOn = keywordSuggestionOn && true;
	static boolean dataCloudOn = keywordSuggestionOn && true;

	static boolean qualityExp = false;

	private static PrunedLandmarkLabeling pl;

	ArrayList<String> ourKeywords = new ArrayList<String>();
	ArrayList<String> coOccKeywords = new ArrayList<String>();
	ArrayList<String> dataCloudKeywords = new ArrayList<String>();

	boolean timeOut = false;
	double maximumTimeBound = 240000;

	public GenTemDistinctRootExperiment() {

	}

	public static void main(String[] args) throws Exception {

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dataGraph")) {
				graphInfraPath = args[++i];
			} else if (args[i].equals("-keywordsPath")) {
				keywordsPath = args[++i];
			} else if (args[i].equals("-maxRequiredResults")) {
				maxRequiredResults = DummyFunctions.getArrOutOfCSV(maxRequiredResults, args[++i]);
			} else if (args[i].equals("-distanceBounds")) {
				distanceBounds = DummyFunctions.getArrOutOfCSV(distanceBounds, args[++i]);
			} else if (args[i].equals("-debugMode")) {
				debugMode = Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-visualize")) {
				visualizeMode = Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-numberOfSameExperiments")) {
				numberOfSameExperiments = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-withProperties")) {
				withProperties = Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-deltas")) {
				deltas = DummyFunctions.getArrOutOfCSV(deltas, args[++i]);
			} else if (args[i].equals("-keywordSizes")) {
				keywordSizes = DummyFunctions.getArrOutOfCSV(keywordSizes, args[++i]);
			} else if (args[i].equals("-MaxNumberOfVisitedNodes")) {
				DummyProperties.MaxNumberOfVisitedNodes = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-keywordSuggestionOn")) {
				keywordSuggestionOn = Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-incEvalOn")) {
				incEvalOn = Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-newKWSOn")) {
				newKWSOn = Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-qualityExp")) {
				qualityExp = Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-coOccBaseLineOn")) {
				coOccBaseLineOn = Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-dataCloudOn")) {
				dataCloudOn = Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-lambdas")) {
				lambdas = DummyFunctions.getArrOutOfCSV(lambdas, args[++i]);
			} else if (args[i].equals("-epsilons")) {
				epsilons = DummyFunctions.getArrOutOfCSV(epsilons, args[++i]);
			} else if (args[i].equals("-numberOfQueries")) {
				numberOfQueries = DummyFunctions.getArrOutOfCSV(numberOfQueries, args[++i]);
			} else if (args[i].equals("-indexPath")) {
				indexPath = args[++i];
			} else if (args[i].equals("-maxTokensForNode")) {
				maxTokensForNode = DummyFunctions.getArrOutOfCSV(maxTokensForNode, args[++i]);
			}
			

		}
		pl = new PrunedLandmarkLabeling(8);
		DummyProperties.withProperties = withProperties;
		DummyProperties.debugMode = debugMode;
		GenTemDistinctRootExperiment experimentUsingBDB = new GenTemDistinctRootExperiment();
		experimentUsingBDB.runBankExperiment();

	}

	private void runBankExperiment() throws Exception {

		boolean addBackward = true;

		graph = new GraphInfraReaderArray(graphInfraPath, addBackward);
		// if (!usingBDB) {
		graph.read();
		nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);

		// For EXP: set weights to 1:
		int higherWeight = 0;
		for (RelationshipInfra rel : graph.relationOfRelId) {
			if (rel.weight > 1) {
				higherWeight++;

			}

			rel.weight = 1.0f;
		}
		System.out.println("higherWeight:" + higherWeight);

		// }
		// else {
		// graph.readWithNoLabels();
		// berkeleyDB = new BerkleleyDB(database, catDatabase, envFilePath);
		// }

		if (debugMode)
			System.out.println("finish read");

		pl.LoadIndex(indexPath);

		// read all initial queries

		for (int keywordSize : keywordSizes) {

			ArrayList<ArrayList<String>> keywordsSet = KWSUtilities.readKeywords(keywordsPath, keywordSize);

			for (int indexOfKeywords = 0; indexOfKeywords < keywordsSet.size(); indexOfKeywords++) {

				for (int distanceBound : distanceBounds) {
					for (double epsilon : epsilons) {
						for (double lambda : lambdas) {
							for (int maxRequiredResult : maxRequiredResults) {
								for (int numberOfQuery : numberOfQueries) {
									for (int mTokenOfNode : maxTokensForNode) {
										runBasedOnParam(keywordsSet, indexOfKeywords, distanceBound, epsilon,
												maxRequiredResult, numberOfQuery, mTokenOfNode, lambda);
									}
								}
							}

						} // end of all keywords
					}
				}
			}
		}
	}

	private void preKWS(ArrayList<String> keywords, int exp) throws Exception {

		candidatesOfAKeyword = null;
		candidatesSet = null;
		fullOutputsQueue = null;
		printedResultTrees = null;

		// if (usingBDB)
		// BerkleleyDB.environment.evictMemory();

		System.runFinalization();
		System.gc();

		Thread.sleep(1000);

		candidatesOfAKeyword = new HashMap<String, HashSet<Integer>>();
		candidatesSet = new HashSet<Integer>();

		// if (usingBDB) {
		// KWSUtilities.findCandidatesOfKeywordsUsingBDB(keywords, berkeleyDB,
		// candidatesOfAKeyword, candidatesSet);
		//
		// if (debugMode) {
		// // 1654776, 72705, 46339, 135488, 30212, 139719
		// // int[] ids = new int[] { 1654776, 72705, 46339, 135488, 30212,
		// // 139719 };
		// // HashSet<Integer> idSet = new HashSet<Integer>();
		// // for (int i : ids) {
		// // idSet.add(i);
		// // }
		// //
		// // for (String keyword : candidatesOfAKeyword.keySet()) {
		// // Iterator<Integer> itr =
		// // candidatesOfAKeyword.get(keyword).iterator();
		// // while (itr.hasNext()) {
		// // int id = itr.next();
		// // if (!idSet.contains(id)) {
		// // itr.remove();
		// // }
		// // }
		// // }
		// //
		// // HashMap<String, HashSet<String>> typeOfCandidates = new
		// // HashMap<String, HashSet<String>>();
		// // for (String keyword : candidatesOfAKeyword.keySet()) {
		// // typeOfCandidates.put(keyword, new HashSet<String>());
		// // for (int nodeId : candidatesOfAKeyword.get(keyword)) {
		// // for (String lbl :
		// // berkeleyDB.SearchNodeInfoWithPro(nodeId).getLabels()) {
		// // typeOfCandidates.get(keyword).add(lbl);
		// // }
		// // }
		// // }
		// // for (String keyword : typeOfCandidates.keySet()) {
		// // System.out.println("keyword:" + keyword);
		// // for (String type : typeOfCandidates.get(keyword)) {
		// // System.out.print(type + ", ");
		// // }
		// // System.out.println();
		// // }
		// }
		//
		// } else {
		KWSUtilities.findCandidatesOfKeywordsUsingInvertedList(keywords, nodeIdsOfToken, candidatesOfAKeyword,
				candidatesSet);

		// }

		avgOutDegreeOfKeywordCandidates = DummyFunctions.getAvgOutDegreesOfASet(graph, candidatesSet);
		avgNumberOfCandidates = (double) candidatesSet.size() / (double) keywords.size();

		totalPrintedResults = 0;

		fullOutputsQueue = new LinkedList<ResultTree>();

		printedResultTrees = new ArrayList<>();

	}

	public void runFromOutside(ArrayList<String> keywords, HashMap<String, HashSet<Integer>> candidatesOfAKeyword,
			int distanceBound, int maxRequiredResult, GraphInfraReaderArray graph, int heapSize, boolean debugMode)
			throws Exception {
		// candidatesSet
		candidatesSet = new HashSet<Integer>();
		for (String keyword : candidatesOfAKeyword.keySet()) {
			candidatesSet.addAll(candidatesOfAKeyword.get(keyword));
		}

		this.candidatesOfAKeyword = candidatesOfAKeyword;

		this.distanceBound = distanceBound;
		this.maxRequiredResult = maxRequiredResult;
		this.graph = graph;
		this.heapSize = heapSize;
		DummyProperties.debugMode = debugMode;
		keywordSuggestionOn = false;

		totalPrintedResults = 0;
		fullOutputsQueue = new LinkedList<ResultTree>();
		printedResultTrees = new ArrayList<>();

		run(keywords);
		// if (timeOut) {
		// continue;
		// }

	}

	private boolean run(ArrayList<String> keywords) throws Exception {

		timeOut = false;
		double startingTimeOfAlg = System.nanoTime();

		if (debugMode) {
			System.out.println("maxRequiredResult:" + maxRequiredResult);
			System.out.println("heapSeize:" + heapSize);
		}

		HashSet<Integer> createdRootSet = new HashSet<Integer>();

		HashMap<Integer, ExtendedNode> createdExtendedNodeOfNodeId = new HashMap<Integer, ExtendedNode>();

		ResultTreeRelevanceComparator resultTreeRelevanceComparator = new ResultTreeRelevanceComparator();

		DijkstraDistanceComparator dijkstraDistanceComparator = new DijkstraDistanceComparator();

		PriorityQueue<DijkstraRunner> iteratorHeap = new PriorityQueue<>(dijkstraDistanceComparator);

		MinMaxPriorityQueue<ResultTree> outputHeap = MinMaxPriorityQueue.orderedBy(resultTreeRelevanceComparator)
				.maximumSize(heapSize).create();

		if (debugMode) {
			System.out.println("keyword size: " + keywords.size());
		}

		HashMap<Integer, DijkstraRunner> dijkstraRunnerOfNodeId = new HashMap<Integer, DijkstraRunner>();
		for (String keyword : keywords) { // keywords is an ArrayList - enhanced
											// for-loop that goes through each
											// keyword
			// initialize an instance of DijkstraRunner from each source
			// candidate
			for (int sourceNodeId : candidatesOfAKeyword.get(keyword)) {
				if (!dijkstraRunnerOfNodeId.containsKey(sourceNodeId)) {
					DijkstraRunner dijkstraRunner = new DijkstraRunner(graph, createdExtendedNodeOfNodeId, sourceNodeId,
							keyword, distanceBound, debugMode, candidatesSet);
					iteratorHeap.add(dijkstraRunner);

					dijkstraRunnerOfNodeId.put(sourceNodeId, dijkstraRunner);
				} else {
					dijkstraRunnerOfNodeId.get(sourceNodeId).frontier.peek().originsOfKeywords.put(keyword,
							new HashSet<>());
					dijkstraRunnerOfNodeId.get(sourceNodeId).frontier.peek().originsOfKeywords.get(keyword)
							.add(sourceNodeId);
				}
			}

		}

		int cnt = 0;

		double soFarTime = 0d;

		while (!iteratorHeap.isEmpty() && totalPrintedResults < maxRequiredResult) {

			soFarTime = ((System.nanoTime() - startingTimeOfAlg) / 1e6);

			if (soFarTime > maximumTimeBound) {
				timeOut = true;
				return false;
			}

			cnt++;

			if (debugMode) {
				if (cnt % 10000 == 0) {
					System.out.println("cnt: " + cnt);
					System.out.println("iteratorHeap: " + iteratorHeap.size());
					System.out.println("totalPrintedResults: " + totalPrintedResults);
				}
			}

			// Iterator = remove top iterator from IteratorHeap

			DijkstraRunner dijkstraRunner = iteratorHeap.poll();

			// if (debugMode)
			// System.out.println("cnt: " + cnt++);

			// v = Get next node from Iterator
			ExtendedNode nextExtendedNode = dijkstraRunner.getNextExtendedNode();

			// if (debugMode)
			// System.out.println("dijkstraRunner origin:" +
			// dijkstraRunner.getKeyword() + ", dijkstraRunnerOriginId:"
			// + dijkstraRunner.originNodeId + ", nextExtendedNode:" +
			// nextExtendedNode.node.nodeId);
			//
			// // for debug start
			// if (debugMode) {
			// for (int originId :
			// nextExtendedNode.distanceFromOriginId.keySet()) {
			// System.out.println(
			// "originId:" + originId + " dist:" +
			// nextExtendedNode.distanceFromOriginId.get(originId));
			// }
			// for (int originId :
			// nextExtendedNode.costOfPathFromOriginId.keySet()) {
			// System.out.println(
			// "originId:" + originId + " cost:" +
			// nextExtendedNode.costOfPathFromOriginId.get(originId));
			// }
			// for (int originId :
			// nextExtendedNode.pathOfRootToTheOrigin.keySet()) {
			// System.out.println("originId:" + originId + " path:"
			// +
			// Arrays.toString(nextExtendedNode.pathOfRootToTheOrigin.get(originId).toArray()));
			// }
			// System.out.println();
			// // for debug end
			// }
			//
			// if (debugMode)
			// System.out.println("next extended node: " +
			// nextExtendedNode.node.nodeId);

			if (dijkstraRunner.hasMoreNodesToOutput()) {
				iteratorHeap.add(dijkstraRunner);
			}

			if (createdRootSet.contains(nextExtendedNode.node.nodeId))
				continue;

			// cross product
			if (nextExtendedNode.originsOfKeywords.keySet().size() == keywords.size()) {
				ArrayList<HashMap<String, HashSet<Integer>>> crossedProductResults = CrossProduct
						.crossProduct(nextExtendedNode.originsOfKeywords);

				// for each tuple in CrossProduct
				// create ResultTree from tuple

				for (HashMap<String, HashSet<Integer>> keywordNodeIdsParticipatingInTree : crossedProductResults) {

					// boolean validAnswer = true;
					// for (int nId :
					// nextExtendedNode.pathOfRootToTheOrigin.keySet()) {
					// if
					// (nextExtendedNode.pathOfRootToTheOrigin.get(nId).size() >
					// (distanceBound + 1)) {
					// validAnswer = false;
					// break;
					// }
					// }
					// if (!validAnswer)
					// continue;

					ResultTree newResultTree = new ResultTree();
					newResultTree.resultTree(graph, keywordNodeIdsParticipatingInTree,
							nextExtendedNode.originsOfKeywords, nextExtendedNode.pathOfRootToTheOrigin,
							nextExtendedNode.costOfPathFromOriginId);

					int rootDegree = newResultTree.anOutputTree.degreeOf(newResultTree.rootNode);

					if (rootDegree <= 1)
						continue;

					if (outputHeap.size() == heapSize) {
						ResultTree resultTreeToBeRemovedAndPrinted = outputHeap.poll();
						print(resultTreeToBeRemovedAndPrinted);

						if (totalPrintedResults >= maxRequiredResult)
							break;
					}

					// if
					// (!createdRootSet.contains(nextExtendedNode.node.nodeId))
					outputHeap.add(newResultTree);

					createdRootSet.add(nextExtendedNode.node.nodeId);

				}
			}
		}

		if (debugMode) {
			System.out.println("out of while of DR");
		}

		while (!outputHeap.isEmpty() && totalPrintedResults < maxRequiredResult) {
			ResultTree resultTreeToBeRemovedAndPrinted = outputHeap.poll();
			print(resultTreeToBeRemovedAndPrinted);
		}

		createdRootSet = null;
		createdExtendedNodeOfNodeId = null;
		resultTreeRelevanceComparator = null;
		dijkstraDistanceComparator = null;
		iteratorHeap = null;
		outputHeap = null;
		dijkstraRunnerOfNodeId = null;

		return true;
	}

	public void print(ResultTree resultTreeToBePrinted) throws Exception {

		if (!isomorphisToPreviouslyPrinted(printedResultTrees, resultTreeToBePrinted.anOutputTree)) {

			if (debugMode) {
				System.out.println("OUTPUT TREE: " + totalPrintedResults + ",cost: " + resultTreeToBePrinted.cost);

				System.out.println("Vertices");
				for (ResultNode resNod : resultTreeToBePrinted.anOutputTree.vertexSet()) {
					System.out.println(resNod.nodeId + ", label: " + graph.nodeOfNodeId.get(resNod.nodeId).getTokens());
				}

				System.out.println("Edges");
				for (RelationshipInfra e : resultTreeToBePrinted.anOutputTree.edgeSet()) {
					System.out.println(resultTreeToBePrinted.anOutputTree.getEdgeSource(e) + " -> "
							+ resultTreeToBePrinted.anOutputTree.getEdgeTarget(e));
					int sourceID = resultTreeToBePrinted.anOutputTree.getEdgeSource(e).nodeId;
					int targetID = resultTreeToBePrinted.anOutputTree.getEdgeTarget(e).nodeId;
					System.out.println("relationship: "
							+ Dummy.DummyFunctions.getRelationshipOfPairNodes(graph, sourceID, targetID));
				}
			}
			//
			// if (debugMode)
			// System.out.println("Neighbor information");

			// HashSet<Integer> nodeIdsInResultTree = new HashSet<Integer>();
			// for (ResultNode resultNode :
			// resultTreeToBePrinted.anOutputTree.vertexSet()) {
			// nodeIdsInResultTree.add(resultNode.nodeId);
			// }
			// for (ResultNode resNod :
			// resultTreeToBePrinted.anOutputTree.vertexSet()) {
			// HashMap<String, Integer> freqOfNeighborTokenType = new
			// HashMap<String, Integer>();
			// for (int otherNodeId :
			// resNod.node.getOutgoingRelIdOfSourceNodeId().keySet()) {
			// NodeInfra otherNode = graph.nodeOfNodeId.get(otherNodeId);
			//
			// if (nodeIdsInResultTree.contains(otherNode.nodeId))
			// continue;
			//
			// for (int tokenId : otherNode.getTokens()) {
			//
			// String cleanLbl =
			// DummyFunctions.getCleanedString(StringPoolUtility.getStringOfId(tokenId));
			// for (String token : DummyFunctions.getTokens(cleanLbl)) {
			// freqOfNeighborTokenType.putIfAbsent(token, 0);
			// freqOfNeighborTokenType.put(token,
			// freqOfNeighborTokenType.get(token) + 1);
			// }
			// }
			// }
			// if (freqOfNeighborTokenType.keySet().size() > 20 ||
			// freqOfNeighborTokenType.keySet().size() == 0) {
			// continue;
			// } else {
			// System.out.println("node: " + resNod.nodeId + ", neighbor
			// information ->");
			// System.out.println(freqOfNeighborTokenType);
			// }
			// }

			printedResultTrees.add(resultTreeToBePrinted.anOutputTree);

			// if (visualizeMode)
			fullOutputsQueue.add(resultTreeToBePrinted);
			totalPrintedResults++;

			// System.out.println();
		}
		// }

	}

	private boolean isomorphisToPreviouslyPrinted(
			ArrayList<ListenableUndirectedGraph<ResultNode, RelationshipInfra>> printedResultTrees2,
			ListenableUndirectedGraph<ResultNode, RelationshipInfra> anOutputTree) {

		for (ListenableUndirectedGraph<ResultNode, RelationshipInfra> graph1 : printedResultTrees2) {

			VF2GraphIsomorphismInspector<ResultNode, RelationshipInfra> vf2Checker = new VF2GraphIsomorphismInspector<ResultNode, RelationshipInfra>(
					graph1, anOutputTree, new Comparator<ResultNode>() {
						@Override
						public int compare(ResultNode o1, ResultNode o2) {
							return Long.compare(o1.nodeId, o2.nodeId);
						}
					}, null);
			if (vf2Checker.isomorphismExists())
				return true;

		}

		return false;
	}

	// private int rootNodeId;
	// private ArrayList<Integer> contentNodes;
	// private ArrayList<Integer> allNodes;

	// private double cost; // quality
	public ArrayList<AnswerAsInput> tansformResultTreeIntoAnswerAsInput() {
		ArrayList<AnswerAsInput> topNAnswers = new ArrayList<AnswerAsInput>();
		LinkedList<ResultTree> tem = new LinkedList<ResultTree>();
		while (!fullOutputsQueue.isEmpty()) {
			ResultTree theWholeResultTree = fullOutputsQueue.poll();
			tem.push(theWholeResultTree);
			int rootNodeId = theWholeResultTree.rootNode.nodeId;
			double cost = theWholeResultTree.cost;

			ArrayList<Integer> contentNodes = new ArrayList<Integer>();
			ListenableUndirectedGraph<ResultNode, RelationshipInfra> resultTree = theWholeResultTree.anOutputTree;
			ArrayList<Integer> allNodes = new ArrayList<Integer>();
			for (ResultNode node : resultTree.vertexSet()) {
				allNodes.add(node.nodeId);
				// TODO: the order should be consistent with keywords order
				if (candidatesSet.contains(node.nodeId)) {
					contentNodes.add(node.nodeId);
				}
			}
			AnswerAsInput topNAnswer = new AnswerAsInput(rootNodeId, contentNodes, allNodes, cost);
			topNAnswers.add(topNAnswer);
		}
		while (!tem.isEmpty()) {
			ResultTree theWholeResultTree = tem.poll();
			fullOutputsQueue.push(theWholeResultTree);
		}
		return topNAnswers;

	}

	public void runBasedOnParam(ArrayList<ArrayList<String>> keywordsSet, int indexOfKeyword, int distanceBound,
			double epsilon, int maxRequiredResult, int numberOfQuery, int mTokenOfNode, double lambda)
			throws Exception {

		this.maxRequiredResult = maxRequiredResult;
		// heapSize = 3 * maxRequiredResult;
		heapSize = 200;
		this.distanceBound = distanceBound;

		ArrayList<String> keywords = keywordsSet.get(indexOfKeyword);

		if (qualityExp) {
			ourKeywords.clear();
			ourKeywords.addAll(keywords);

			coOccKeywords.clear();
			coOccKeywords.addAll(keywords);

			dataCloudKeywords.clear();
			dataCloudKeywords.addAll(keywords);
		}

		boolean hasEnoughAnswer = true;
		boolean hasASuggestingKeyword = true;

		int exp = 0;

		while (exp < numberOfSameExperiments) {
			if (timeOut) {
				timeOut = false;
				exp = Integer.MAX_VALUE;
				continue;
			}

			initialKWSStartTime = System.nanoTime();

			String settingStr = "";

			if (qualityExp) {
				numberOfSameExperiments = 2; // from
												// 2keywords
												// to 4.
				settingStr = ourKeywords.size() + " keywords is "
						+ DummyFunctions.getStringOutOfCollection(ourKeywords, ";") + ", n:" + maxRequiredResult
						+ ", distance:" + distanceBound + ", delta:" + delta;

				preKWS(ourKeywords, exp);
				run(ourKeywords);

				if (timeOut) {
					continue;
				}
			} else {

				settingStr = keywords.size() + " keywords is " + DummyFunctions.getStringOutOfCollection(keywords, ";")
						+ ", n:" + maxRequiredResult + ", distance:" + distanceBound + ", delta:" + delta;

				preKWS(keywords, exp);
				run(keywords);

				if (timeOut) {
					continue;
				}
			}

			System.out.println("BANK: " + settingStr);

			if (fullOutputsQueue.size() < maxRequiredResult) {
				hasEnoughAnswer = false;
				break;
			}

			if (debugMode && visualizeMode) {
				LinkedList<ResultTree> fullOutputsQueueTemp = new LinkedList<ResultTree>(fullOutputsQueue);
				Visualizer.visualizeOutput(fullOutputsQueueTemp, graph,  null, keywords);
				System.out.println("finsihed:");
			}

			// end of initial KWS
			initialKWSDuration = ((System.nanoTime() - initialKWSStartTime) / 1e6);

			System.out.println("after initialKWS duration:" + initialKWSDuration);

			int discoveredAnswersKWS = fullOutputsQueue.size();
			int discoveredAnswersNewKWS = 0;
			int discoveredAnswersCoOcc = 0;
			int discoveredAnswersDataCloud = 0;

			double avgoutdegcand1 = avgOutDegreeOfKeywordCandidates;
			double avgnumberofcands1 = avgNumberOfCandidates;
			double secondaryKWSStartTime = 0d;
			double secondaryKWSDuration = 0d;
			double secondaryKWSQuality = 0d;

			double coOccKWSStartTime = 0d;
			double coOccKWSDuration = 0d;
			double coOccKWSQuality = 0d;

			double coOccF = 0d;

			double dataCloudKWSStartTime = 0d;
			double dataCloudKWSDuration = 0d;
			double dataCloudKWSQuality = 0d;

			double dataCloudF = 0d;

			String suggDivQ = "";
			String suggStream = "";

			// read from full outputs queue
			// transform each answer from this to
			// AnswerAsInput
			ArrayList<AnswerAsInput> topNAnswersTemp = tansformResultTreeIntoAnswerAsInput();

			// initialize the class RootKWSExpand
			// call expand
			ArrayList<AnswerAsInput> topNAnswers = new ArrayList<AnswerAsInput>();
			if (topNAnswersTemp.size() > maxRequiredResult) {
				topNAnswers.addAll(topNAnswersTemp.subList(0, maxRequiredResult));
			} else {
				topNAnswers.addAll(topNAnswersTemp);
			}

			// HashMap<Integer, CostAndNodesOfAnswersPair>
			// estimatedWeightOfSuggestedKeywordMap = null;
			HashMap<Integer, Double> termDistance = null;
			CoOccurrence coOccHandler = null;
			DataCloudDistinguishability dataCloudHandler = null;

			if (keywordSuggestionOn) {

				if (qualityExp)
					System.out.println("starting our keyword suggestion " + new DateTime() + " for keywords "
							+ DummyFunctions.getStringOutOfCollection(keywords, ";"));

				// if (qualityExp) {
				//
				// System.out.println("secondary delta");
				//
				// double tempDelta = this.delta;
				// this.delta = this.delta * 2;
				// termDistance = keywordExpand(topNAnswers,
				// keywords);
				//
				// if (termDistance == null ||
				// termDistance.size() == 0
				// || genTemRootKWSExpand.bestKeywordInfo ==
				// null
				// ||
				// genTemRootKWSExpand.bestKeywordInfo.nodeId
				// == null) {
				// hasASuggestingKeyword = false;
				// break;
				// }
				//
				// int sizeOfAllSuggestedKeywords =
				// termDistance.size();
				//
				// for (int tokenId : termDistance.keySet())
				// {
				// avgSuggestedWeightSecondDelta +=
				// termDistance.get(tokenId);
				//
				// // TODO: didn't compute
				// minSuggestedWeightSecondDelta = 0;
				//
				// maxSuggestedWeightSecondDelta = 0;
				// }
				// avgSuggestedWeightSecondDelta /= (double)
				// sizeOfAllSuggestedKeywords;
				//
				// this.delta = tempDelta;
				// }

				// DIVQ
				{
					termDistance = keywordExpandDivQ(topNAnswers, keywords, epsilon, lambda, mTokenOfNode,
							numberOfQuery);

					if (termDistance == null || termDistance.size() == 0) {
						hasASuggestingKeyword = false;
						break;
					}

					int sizeOfAllSuggestedKeywords = termDistance.size();

					int c = 0;
					for (Integer key : termDistance.keySet()) {
						suggDivQ += StringPoolUtility.getStringOfId(key) + ":" + termDistance.get(key) + "; ";
						c++;
						if (c > 9) {
							break;
						}
					}

					System.out.println("after keyword suggestion divQExpand: " + divQExpand.querySuggestionKWSDuration);
					System.out.println("keyword suggestion num: " + divQExpand.termDistance.size());
					System.out.println("keyword suggestion visited keywords divQExpand: " + divQExpand.visitedKeywords);

				}

				// StreamQ
				{
					termDistance = keywordExpandStreamQ(topNAnswers, keywords, epsilon, lambda, mTokenOfNode,
							numberOfQuery);

					if (termDistance == null || termDistance.size() == 0) {
						hasASuggestingKeyword = false;
						break;
					}

					int sizeOfAllSuggestedKeywords = termDistance.size();

					int c = 0;
					for (Integer key : termDistance.keySet()) {
						suggStream += StringPoolUtility.getStringOfId(key) + ":" + termDistance.get(key) + "; ";
						c++;
						if (c > 9) {
							break;
						}
					}

					System.out.println("after keyword suggestion streamDivQExpand: "
							+ streamDivQExpand.querySuggestionKWSDuration);
					System.out.println("keyword suggestion num: " + streamDivQExpand.termDistance.size());
					System.out.println("keyword suggestion visited keywords streamDivQExpand: "
							+ streamDivQExpand.visitedKeywords);

				}

				// incremental evaluation
				// incEval = new IncEval(graph,
				// genTemRootKWSExpand.bestKeywordInfo,
				// topNAnswers,
				// distanceBound,
				// DummyProperties.KWSSetting.DISTINCTROOT);
				//
				// if (incEvalOn) {
				//
				// System.out.println("starting our inc eval
				// " + new DateTime());
				//
				// incEval.incEval();
				//
				// for (int p = 0; p < topNAnswers.size();
				// p++) {
				// incEvalTotalCostPlusInitCost +=
				// topNAnswers.get(p).getCost()
				// +
				// incEval.lastTripleOfKeywordMatchToTarget[p].getCost();
				//
				// System.out.println("inc eval p:" + p + ",
				// nodeId: "
				// +
				// incEval.lastTripleOfKeywordMatchToTarget[p].getNodeId());
				// }
				//
				// System.out.println("after incEval: " +
				// incEval.incEvalDuration);
				// System.out.println("incEval cost: " +
				// incEvalTotalCostPlusInitCost);
				// System.out.println("selected keyword
				// nodeIds were: "
				// +
				// Arrays.toString(genTemRootKWSExpand.bestKeywordInfo.nodeId));
				// System.out.println("lowest weight
				// suggested keywrd: " + StringPoolUtility
				// .getStringOfId(genTemRootKWSExpand.lowestWeightSuggestedKeywordId));
				//
				// }

				/// run a from-scratch KWS for new query

				// if (newKWSOn) {
				//
				// int tempHeapSize = heapSize;
				// heapSize = 50;
				//
				// secondaryKWSStartTime =
				// System.nanoTime();
				// ArrayList<String> newKeywords;
				//
				// if (qualityExp) {
				// newKeywords = new
				// ArrayList<String>(ourKeywords);
				// } else {
				// newKeywords = new
				// ArrayList<String>(keywords);
				// }
				//
				// newKeywords.add(StringPoolUtility
				// .getStringOfId(genTemRootKWSExpand.lowestWeightSuggestedKeywordId));
				//
				// if (qualityExp)
				// System.out.println("testing newKWS: " +
				// new DateTime() + " with keywords: "
				// +
				// DummyFunctions.getStringOutOfCollection(newKeywords,
				// ";"));
				//
				// preKWS(newKeywords, exp);
				// run(newKeywords);
				// if (timeOut) {
				// continue;
				// }
				// secondaryKWSDuration =
				// ((System.nanoTime() -
				// secondaryKWSStartTime) / 1e6);
				//
				// for (ResultTree resultTree :
				// fullOutputsQueue) {
				// secondaryKWSQuality += resultTree.cost;
				// }
				//
				// if (fullOutputsQueue.size() <
				// maxRequiredResult) {
				// double addedValue = (double)
				// genTemRootKWSExpand.initialOveralWeight
				// / (double) maxRequiredResult;
				// int lessResult = maxRequiredResult -
				// fullOutputsQueue.size();
				//
				// secondaryKWSQuality += lessResult *
				// addedValue;
				//
				// }
				//
				// discoveredAnswersNewKWS =
				// fullOutputsQueue.size();
				//
				// if (debugMode && visualizeMode) {
				// LinkedList<ResultTree>
				// fullOutputsQueueTemp = new
				// LinkedList<ResultTree>(
				// fullOutputsQueue);
				// Visualizer.visualizeOutput(fullOutputsQueueTemp,
				// graph, null, null,
				// newKeywords);
				// }
				//
				// if (qualityExp) {
				// ourKeywords.clear();
				// ourKeywords.addAll(newKeywords);
				// }
				//
				// System.out.println("after new keyword
				// search time: " + secondaryKWSDuration);
				// System.out.println("after new keyword
				// search cost: " + secondaryKWSQuality);
				// System.out.println(
				// "after new keyword search answers: " +
				// discoveredAnswersNewKWS);
				// System.out.println(newKeywords.size() + "
				// keywords was "
				// +
				// DummyFunctions.getStringOutOfCollection(newKeywords,
				// ";"));
				//
				// heapSize = tempHeapSize;
				// }

				if (coOccBaseLineOn) {

					int tempMaxReqResults = this.maxRequiredResult;
					this.maxRequiredResult = 20;

					if (qualityExp) {
						System.out.println("starting coOccBaseLine: " + new DateTime() + " with keywords: "
								+ DummyFunctions.getStringOutOfCollection(coOccKeywords, ";"));

						preKWS(coOccKeywords, exp);
						run(coOccKeywords);

						if (timeOut) {
							continue;
						}
					} else {
						preKWS(keywords, exp);
						run(keywords);
						if (timeOut) {
							continue;
						}
					}

					topNAnswersTemp = tansformResultTreeIntoAnswerAsInput();

					// initialize the class RootKWSExpand
					// call expand
					topNAnswers = new ArrayList<AnswerAsInput>();
					if (topNAnswersTemp.size() > this.maxRequiredResult) {
						topNAnswers.addAll(topNAnswersTemp.subList(0, this.maxRequiredResult));
					} else {
						topNAnswers.addAll(topNAnswersTemp);
					}

					coOccKWSStartTime = System.nanoTime();

					ArrayList<String> newKeywords;

					if (qualityExp) {
						newKeywords = new ArrayList<String>(coOccKeywords);
					} else {
						newKeywords = new ArrayList<String>(keywords);
					}

					if (qualityExp)
						System.out.println("testing coOccBaseLine: " + new DateTime() + " with keywords: "
								+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));

					if (qualityExp) {
						coOccHandler = new CoOccurrence(graph, nodeIdsOfToken, topNAnswers, 1, coOccKeywords);
					} else {
						coOccHandler = new CoOccurrence(graph, nodeIdsOfToken, topNAnswers, 1, keywords);
					}

					coOccHandler.expand();

					this.maxRequiredResult = tempMaxReqResults;
					fullOutputsQueue.clear();

					HashSet<Integer> qTCoOcc = new HashSet<Integer>();
					for (int p = 0; p < numberOfQuery; p++) {
						if (p < coOccHandler.topFrequentKeywordsInt.size()) {
							qTCoOcc.add(coOccHandler.topFrequentKeywordsInt.get(p));
						}
					}
					ObjectiveHandler objectiveHanlder = new ObjectiveHandler(numberOfQuery, epsilon, lambda);
					coOccF = objectiveHanlder.computeFFromScratch(qTCoOcc,
							topNAnswers.subList(0, this.maxRequiredResult), pl, nodeIdsOfToken, distanceBound);

					// if (coOccHandler.topFrequentKeywords.size() > 0) {
					//
					// if (qualityExp) {
					// System.out.println(
					// "selected keyword: " +
					// coOccHandler.topFrequentKeywords.get(0) + " num of cand:
					// "
					// + nodeIdsOfToken.get(StringPoolUtility
					// .getIdOfStringFromPool(coOccHandler.topFrequentKeywords.get(0)))
					// .size());
					// }
					//
					// newKeywords.add(coOccHandler.topFrequentKeywords.get(0));
					// preKWS(newKeywords, exp);
					// run(newKeywords);
					// if (timeOut) {
					// continue;
					// }
					// coOccKWSDuration = ((System.nanoTime() -
					// coOccKWSStartTime) / 1e6);
					//
					// for (ResultTree resultTree : fullOutputsQueue) {
					// coOccKWSQuality += resultTree.cost;
					// }
					//
					// if (debugMode && visualizeMode) {
					// LinkedList<ResultTree> fullOutputsQueueTemp = new
					// LinkedList<ResultTree>(fullOutputsQueue);
					// Visualizer.visualizeOutput(fullOutputsQueueTemp, graph,
					// null, null, newKeywords);
					// }
					// }

					// if (fullOutputsQueue.size() <
					// maxRequiredResult) {
					// double addedValue = (double)
					// genTemRootKWSExpand.initialOveralWeight
					// / (double) maxRequiredResult;
					// int lessResult = maxRequiredResult -
					// fullOutputsQueue.size();
					//
					// coOccKWSQuality += lessResult *
					// addedValue;
					// }

					discoveredAnswersCoOcc = fullOutputsQueue.size();

					if (qualityExp) {
						coOccKeywords.clear();
						coOccKeywords.addAll(newKeywords);
					}

					System.out.println("after coOccBaseLine time: " + coOccKWSDuration);
					System.out.println("after coOccBaseLine cost: " + coOccKWSQuality);
					System.out.println("after coOccBaseLine answers: " + discoveredAnswersCoOcc);
					System.out.println(newKeywords.size() + " keywords was "
							+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));
				}

				if (dataCloudOn) {

					if (qualityExp)
						System.out.println("starting data cloud: " + new DateTime() + " with keywords: "
								+ DummyFunctions.getStringOutOfCollection(dataCloudKeywords, ";"));

					ArrayList<String> newKeywords;
					if (qualityExp) {
						newKeywords = new ArrayList<String>(dataCloudKeywords);
					} else {
						newKeywords = new ArrayList<String>(keywords);
					}
					dataCloudKWSStartTime = System.nanoTime();
					dataCloudHandler = new DataCloudDistinguishability(graph, nodeIdsOfToken, topNAnswers, 1,
							newKeywords);

					dataCloudHandler.expand();
					fullOutputsQueue.clear();

					HashSet<Integer> qTDC = new HashSet<Integer>();
					for (int p = 0; p < numberOfQuery; p++) {
						if (p < dataCloudHandler.topFrequentKeywordsInt.size()) {
							qTDC.add(dataCloudHandler.topFrequentKeywordsInt.get(p));
						}
					}
					ObjectiveHandler objectiveHanlder = new ObjectiveHandler(numberOfQuery, epsilon, lambda);
					dataCloudF = objectiveHanlder.computeFFromScratch(qTDC,
							topNAnswers.subList(0, this.maxRequiredResult), pl, nodeIdsOfToken, distanceBound);

					// if (dataCloudHandler.topFrequentKeywords.size() > 0) {
					//
					// if (qualityExp) {
					// System.out.println("selected keyword: " +
					// dataCloudHandler.topFrequentKeywords.get(0)
					// + " num of cand: "
					// + nodeIdsOfToken
					// .get(StringPoolUtility
					// .getIdOfStringFromPool(dataCloudHandler.topFrequentKeywords.get(0)))
					// .size());
					// }
					//
					// newKeywords.add(dataCloudHandler.topFrequentKeywords.get(0));
					//
					// if (qualityExp)
					// System.out.println("testing data cloud: " + new
					// DateTime() + " with keywords: "
					// + DummyFunctions.getStringOutOfCollection(newKeywords,
					// ";"));
					//
					// preKWS(newKeywords, exp);
					// run(newKeywords);
					// if (timeOut) {
					// continue;
					// }
					// dataCloudKWSDuration = ((System.nanoTime() -
					// dataCloudKWSStartTime) / 1e6);
					//
					// for (ResultTree resultTree : fullOutputsQueue) {
					// dataCloudKWSQuality += resultTree.cost;
					// }
					//
					// if (debugMode && visualizeMode) {
					// LinkedList<ResultTree> fullOutputsQueueTemp = new
					// LinkedList<ResultTree>(fullOutputsQueue);
					// Visualizer.visualizeOutput(fullOutputsQueueTemp, graph,
					// null, null, newKeywords);
					// }
					// }

					// if (fullOutputsQueue.size() <
					// maxRequiredResult) {
					// double addedValue = (double)
					// genTemRootKWSExpand.initialOveralWeight
					// / (double) maxRequiredResult;
					// int lessResult = maxRequiredResult -
					// fullOutputsQueue.size();
					//
					// dataCloudKWSQuality += lessResult *
					// addedValue;
					// }

					discoveredAnswersDataCloud = fullOutputsQueue.size();

					if (qualityExp) {
						dataCloudKeywords.clear();
						dataCloudKeywords.addAll(newKeywords);
					}

					System.out.println("after dataCloud time: " + dataCloudKWSDuration);
					System.out.println("after dataCloud cost: " + dataCloudKWSQuality);
					System.out.println("after dataCloud answers: " + discoveredAnswersDataCloud);
					System.out.println(newKeywords.size() + " keywords was "
							+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));
				}

				// greedy selection of top-k keywords out of
				// estimatedWeightOfSuggestedKeywordMap
				// input: given top-n answers, graph,
				// estimatedWeightOfSuggestedKeywordMap,
				// etc.
				// output: top-k keyword maximizing the
				// objective
				// function
				// including their score.

			}

			settingStr = "After All: discoveredAnswers:" + fullOutputsQueue.size() + " kwsTime: " + initialKWSDuration;

			System.out.println("BANK: " + settingStr);

			ArrayList<InfoHolder> timeInfos = new ArrayList<InfoHolder>();
			timeInfos.add(new InfoHolder(0, "Setting", "DistinctRoot"));
			timeInfos.add(new InfoHolder(1, "keywords", DummyFunctions.getStringOutOfCollection(keywords, ";")));
			timeInfos.add(new InfoHolder(2, "Nodes", graph.nodeOfNodeId.size()));
			timeInfos.add(new InfoHolder(3, "Relationships", graph.relationOfRelId.size()));
			timeInfos.add(new InfoHolder(4, "num keywords", keywords.size()));
			timeInfos.add(new InfoHolder(5, "distance bound", distanceBound));
			timeInfos.add(new InfoHolder(6, "number of queries", numberOfQuery));
			timeInfos.add(new InfoHolder(7, "epsilon", epsilon));
			timeInfos.add(new InfoHolder(8, "lambda", lambda));
			timeInfos.add(new InfoHolder(9, "mToken L", mTokenOfNode));
			// timeInfos.add(new InfoHolder(6, "delta",
			// delta));
			timeInfos.add(new InfoHolder(10, "n", maxRequiredResult));
			timeInfos.add(new InfoHolder(11, "discoveredAnswersKWS", discoveredAnswersKWS));
			timeInfos.add(new InfoHolder(12, "avg out deg cand1", avgoutdegcand1));
			timeInfos.add(new InfoHolder(13, "avg cands1", avgnumberofcands1));
			timeInfos.add(new InfoHolder(14, "first KWS", initialKWSDuration));
			timeInfos.add(new InfoHolder(15, "divQ Time", divQExpand.querySuggestionKWSDuration));
			timeInfos.add(new InfoHolder(16, "streamDivQ Time", streamDivQExpand.querySuggestionKWSDuration));

			timeInfos.add(new InfoHolder(17, "divQ F", divQExpand.qT_F));
			timeInfos.add(new InfoHolder(18, "streamDivQ F", streamDivQExpand.qT_F));

			timeInfos.add(new InfoHolder(19, "coOcc F", coOccF));
			timeInfos.add(new InfoHolder(20, "dataCloud F", dataCloudF));

			timeInfos.add(new InfoHolder(21, "divQ VisitedKeywords", divQExpand.visitedKeywords));
			timeInfos.add(new InfoHolder(22, "stream VisitedKeywords ", streamDivQExpand.visitedKeywords));

			timeInfos.add(new InfoHolder(23, "divQ Visited Nodes", divQExpand.visitedNodes));
			timeInfos.add(new InfoHolder(24, "stream Visited Nodes", streamDivQExpand.visitedNodes));

			timeInfos.add(new InfoHolder(25, "divQ number of suggested", divQExpand.termDistance.size()));
			timeInfos.add(new InfoHolder(26, "stream number of suggested ", streamDivQExpand.termDistance.size()));

			timeInfos.add(new InfoHolder(27, "DivQ diversification duration ", divQExpand.diviersificationDuration));
			timeInfos.add(new InfoHolder(28, "StreamDivQ diversification duration ",
					streamDivQExpand.diviersificationDuration));
			timeInfos.add(new InfoHolder(29, "StreamDivQ index duration ", streamDivQExpand.indexComputationDuration));
			timeInfos.add(new InfoHolder(30, "StreamDivQ UB_LB  duration ",
					streamDivQExpand.upperboundlowerboundcomputationDuration));

			timeInfos.add(new InfoHolder(31, "numberOfDistanceQuerying", streamDivQExpand.numberOfDistaneQuerying));
			timeInfos.add(
					new InfoHolder(32, "avgDistanceQueryingDuration ", streamDivQExpand.avgDistanceQueryingDuration));

			timeInfos.add(new InfoHolder(33, "10 sugg kywrds suggDivQ", suggDivQ));
			timeInfos.add(new InfoHolder(34, "10 sugg kywrds suggStream", suggStream));

			TimeLogger.LogTime("bankOutput.csv", true, timeInfos);

			exp++;
		}

		if (!hasEnoughAnswer) {
			System.out.println("not enough answer for keywords " + Arrays.toString(keywords.toArray())
					+ " the answer size is: " + fullOutputsQueue.size());
			return;
		}
		if (!hasASuggestingKeyword) {
			System.out.println(
					"no suggesting keyword within bound " + distanceBound + " , delta: " + delta + " , keywords: "
							+ Arrays.toString(keywords.toArray()) + " for top-" + fullOutputsQueue.size() + " answers");
			return;
		}

	}

	// return dMap HashMap<Integer, TermInfo>
	public HashMap<Integer, Double> keywordExpandDivQ(ArrayList<AnswerAsInput> topNAnswers, ArrayList<String> keywords,
			double epsilon, double lambda, int mTokenOfNode, int numberOfQuery) throws Exception {

		HashSet<Integer> keywordsSet = new HashSet<Integer>();

		for (int m = 0; m < keywords.size(); m++) {
			keywordsSet.add(StringPoolUtility.getIdOfStringFromPool(keywords.get(m)));
		}

		divQExpand = new DivQKWSExpand(graph, keywordsSet, topNAnswers, distanceBound, epsilon, lambda, numberOfQuery,
				nodeIdsOfToken, mTokenOfNode);

		divQExpand.expand();
		HashMap<Integer, Double> distanceOfTerms = divQExpand.termDistance;

		return distanceOfTerms;
	}

	public HashMap<Integer, Double> keywordExpandStreamQ(ArrayList<AnswerAsInput> topNAnswers,
			ArrayList<String> keywords, double epsilon, double lambda, int mTokenOfNode, int numberOfQuery)
			throws Exception {

		HashSet<Integer> keywordsSet = new HashSet<Integer>();

		for (int m = 0; m < keywords.size(); m++) {
			keywordsSet.add(StringPoolUtility.getIdOfStringFromPool(keywords.get(m)));
		}

		streamDivQExpand = new IndexGenTemKWSExpand(graph, keywordsSet, topNAnswers, keywords.size(), distanceBound,
				epsilon, lambda, pl, numberOfQuery, nodeIdsOfToken, mTokenOfNode);
		streamDivQExpand.expand();
		HashMap<Integer, Double> distanceOfTerms = streamDivQExpand.termDistance;

		return distanceOfTerms;
	}
}
