//package pairwiseBasedKWS;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedHashSet;
//import java.util.LinkedList;
//import java.util.PriorityQueue;
//import org.jgrapht.graph.ListenableUndirectedGraph;
//import org.joda.time.DateTime;
//
//import com.couchbase.client.deps.io.netty.handler.codec.http.HttpContentEncoder.Result;
//
//import aqpeq.utilities.InfoHolder;
//import aqpeq.utilities.KWSUtilities;
//import aqpeq.utilities.StringPoolUtility;
//import aqpeq.utilities.TimeLogger;
//import aqpeq.utilities.Visualizer;
//import aqpeq.utilities.Dummy.DummyFunctions;
//import aqpeq.utilities.Dummy.DummyProperties;
//import bank.keywordSearch.DistinctRootExperiment;
//import baselines.CoOccurrence;
//import baselines.DataCloudDistinguishability;
//import dataset.BerkeleyDB.BerkleleyDB;
//import graphInfra.GraphInfraReaderArray;
//import graphInfra.RelationshipInfra;
//import incrementalEvaluation.IncEval;
//import neo4jBasedKWS.ResultNode;
//import neo4jBasedKWS.ResultTree;
//import queryExpansion.AnswerAsInput;
//import queryExpansion.CostAndNodesOfAnswersPair;
//import queryExpansion.GraphKWSExpand;
//import relevantDiversificationLibraries.Diversification;
//import tryingToTranslate.PrunedLandmarkLabeling_labelloader;
//
//public class PWQueryFinder {
//	GraphInfraReaderArray graph;
//
//	// mhn
//	// private static String graphInfraPath =
//	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/graph/";
//	// private static String keywordsPath =
//	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/queries/imdb_query_DR.txt";
//	// private static String envFilePath =
//	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/bdb/withProp/dbEnvWithProp";
//	// private String distanceIndexDB =
//	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/graph/imdb_8bits.jin";
//
//	private static String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/graph/";
//	private static String keywordsPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/queries/temp/imdb_query_PW_Test.txt";
//	private static String envFilePath = "/Users/mnamaki/AQPEQ/GraphExamples/subgraphTest/graph/";
//	private String distanceIndexDB = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/graph/imdb_8bits.jin";
//
//	// private static String graphInfraPath =
//	// "/Users/mnamaki/AQPEQ/GraphExamples/subgraphTest/graph/";
//	// private static String keywordsPath =
//	// "/Users/mnamaki/AQPEQ/GraphExamples/subgraphTest/graph/keywords.in";
//	// private static String envFilePath =
//	// "/Users/mnamaki/AQPEQ/GraphExamples/subgraphTest/graph/";
//	// private String distanceIndexDB =
//	// "/Users/mnamaki/AQPEQ/GraphExamples/subgraphTest/graph/subgraphTest_8bits.jin";
//
//	private int distanceIndexDBBitNum = 8;
//
//	static int[] maxRequiredResults = { 2 };
//	private static int[] distanceBounds = { 1 };
//	private static double[] deltas = { 0.5 };// quality bound
//	private static int[] keywordSizes = { 3 };
//
//	int maxRequiredResult;
//	private int distanceBound;
//	private double delta;//// quality bound
//
//	private PrunedLandmarkLabeling_labelloader prunedLandmarkLabeling;
//
//	private int fixedmaxRequiredResult;
//
//	static LinkedList<PairwiseAnswer> fullOutputsQueue = new LinkedList<PairwiseAnswer>();
//
//	// from bank
//	static HashMap<String, HashSet<Integer>> candidatesOfAKeyword = new HashMap<String, HashSet<Integer>>();
//	static HashSet<Integer> candidatesSet = new HashSet<Integer>();
//	static HashMap<Integer, HashSet<Integer>> nodeIdsOfToken = new HashMap<Integer, HashSet<Integer>>();
//
//	GraphKWSExpand graphKWSExpand;
//
//	private double avgNumberOfCandidates;
//	private double avgOutDegreeOfKeywordCandidates;
//
//	private double initialKWSStartTime = 0d;
//	private double initialKWSDuration = 0d;
//
//	// BDB
//	private static String database = "database";
//	private static String catDatabase = "catDatabase";
//
//	private static BerkleleyDB berkeleyDB;
//	private static boolean withProperties = true;
//
//	// experiments
//	private static int numberOfSameExperiments = 1;
//	private static boolean usingBDB = false;
//
//	private static boolean debugMode = true;
//	private static boolean visualizeMode = true;
//
//	// steps turning on/off
//	boolean keywordSuggestionOn = true;
//	// if don't want just change to false
//	boolean incEvalOn = keywordSuggestionOn && true;
//	boolean newKWSOn = keywordSuggestionOn && true;
//	boolean topkSelectionOn = keywordSuggestionOn && false;
//	boolean coOccBaseLineOn = keywordSuggestionOn && false;
//	boolean dataCloudOn = keywordSuggestionOn && false;
//
//	static boolean qualityExp = false;
//	ArrayList<String> ourKeywords = new ArrayList<String>();
//	ArrayList<String> coOccKeywords = new ArrayList<String>();
//	ArrayList<String> dataCloudKeywords = new ArrayList<String>();
//
//	private String resultName = "";
//
//	public static void main(String[] args) throws Exception {
//
//		PWQueryFinder kws = new PWQueryFinder();
//		kws.runMain(args);
//
//	}
//
//	private void runMain(String[] args) throws Exception {
//
//		for (int i = 0; i < args.length; i++) {
//			if (args[i].equals("-dataGraph")) {
//				graphInfraPath = args[++i];
//			} else if (args[i].equals("-keywordsPath")) {
//				keywordsPath = args[++i];
//			} else if (args[i].equals("-maxRequiredResults")) {
//				maxRequiredResults = DummyFunctions.getArrOutOfCSV(maxRequiredResults, args[++i]);
//			} else if (args[i].equals("-distanceBounds")) {
//				distanceBounds = DummyFunctions.getArrOutOfCSV(distanceBounds, args[++i]);
//			} else if (args[i].equals("-debugMode")) {
//				debugMode = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-visualize")) {
//				visualizeMode = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-usingBDB")) {
//				usingBDB = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-database")) {
//				database = args[++i];
//			} else if (args[i].equals("-catDatabase")) {
//				catDatabase = args[++i];
//			} else if (args[i].equals("-envFilePath")) {
//				envFilePath = args[++i];
//			} else if (args[i].equals("-distanceIndexDB")) {
//				distanceIndexDB = args[++i];
//			} else if (args[i].equals("-distanceIndexDBBitNum")) {
//				distanceIndexDBBitNum = Integer.parseInt(args[++i]);
//			} else if (args[i].equals("-withProperties")) {
//				withProperties = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-deltas")) {
//				deltas = DummyFunctions.getArrOutOfCSV(deltas, args[++i]);
//			} else if (args[i].equals("-keywordSizes")) {
//				keywordSizes = DummyFunctions.getArrOutOfCSV(keywordSizes, args[++i]);
//			} else if (args[i].equals("-keywordSuggestionOn")) {
//				keywordSuggestionOn = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-incEvalOn")) {
//				incEvalOn = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-newKWSOn")) {
//				newKWSOn = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-qualityExp")) {
//				qualityExp = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-coOccBaseLineOn")) {
//				coOccBaseLineOn = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-dataCloudOn")) {
//				dataCloudOn = Boolean.parseBoolean(args[++i]);
//			} else if (args[i].equals("-resultName")) {
//				resultName = args[++i];
//			}
//
//		}
//
//		DummyProperties.withProperties = withProperties;
//		DummyProperties.debugMode = debugMode;
//		prunedLandmarkLabeling = new PrunedLandmarkLabeling_labelloader(distanceIndexDBBitNum, distanceIndexDB);
//		runRCliqueExperiment();
//	}
//
//	private void runRCliqueExperiment() throws Exception {
//
//		boolean addBackward = true;
//
//		graph = new GraphInfraReaderArray(graphInfraPath, addBackward);
//		// if (!usingBDB) {
//		graph.read();
//		nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);
//		// } else {
//		// graph.readWithNoLabels();
//		// berkeleyDB = new BerkleleyDB(database, catDatabase, envFilePath);
//		// }
//
//		if (debugMode)
//			System.out.println("finish read");
//
//		// read all initial queries
//
//		for (int keywordSize : keywordSizes) {
//
//			ArrayList<ArrayList<String>> keywordsSet = KWSUtilities.readKeywords(keywordsPath, keywordSize);
//
//			for (int i = 0; i < keywordsSet.size(); i++) {
//				for (int distanceBound : distanceBounds) {
//					for (double delta : deltas) {
//						for (int maxRequiredResult : maxRequiredResults) {
//
//							this.delta = delta;
//							this.maxRequiredResult = maxRequiredResult;
//							fixedmaxRequiredResult = maxRequiredResult;
//							this.distanceBound = distanceBound;
//							ArrayList<String> keywords = keywordsSet.get(i);
//
//							if (qualityExp) {
//								ourKeywords.clear();
//								ourKeywords.addAll(keywords);
//
//								coOccKeywords.clear();
//								coOccKeywords.addAll(keywords);
//
//								dataCloudKeywords.clear();
//								dataCloudKeywords.addAll(keywords);
//							}
//
//							boolean hasEnoughAnswer = true;
//							boolean hasASuggestingKeyword = true;
//
//							int exp = 0;
//
//							while (exp < numberOfSameExperiments) {
//
//								String settingStr = keywords.size() + " keywords is "
//										+ DummyFunctions.getStringOutOfCollection(keywords, ";") + ", n:"
//										+ maxRequiredResult + ", distance:" + distanceBound + ", delta:" + delta;
//
//								System.out.println("SUBGRAPH: " + settingStr);
//
//								initialKWSStartTime = System.nanoTime();
//
//								if (qualityExp) {
//									numberOfSameExperiments = 2; // from
//																	// 2keywords
//																	// to 4.
//									settingStr = ourKeywords.size() + " keywords is "
//											+ DummyFunctions.getStringOutOfCollection(ourKeywords, ";") + ", n:"
//											+ maxRequiredResult + ", distance:" + distanceBound + ", delta:" + delta;
//
//									preKWS(ourKeywords, exp);
//									run(ourKeywords);
//								} else {
//
//									settingStr = keywords.size() + " keywords is "
//											+ DummyFunctions.getStringOutOfCollection(keywords, ";") + ", n:"
//											+ maxRequiredResult + ", distance:" + distanceBound + ", delta:" + delta;
//
//									preKWS(keywords, exp);
//									run(keywords);
//								}
//
//								if (fullOutputsQueue.size() < maxRequiredResult) {
//									hasEnoughAnswer = false;
//									break;
//								}
//
//								if (debugMode && visualizeMode) {
//									LinkedList<ResultTree> fullOutputsQueueTemp = new LinkedList<ResultTree>();
//
//									for (PairwiseAnswer rClique : fullOutputsQueue) {
//										fullOutputsQueueTemp.add(rClique.resultTree);
//									}
//
//									Visualizer.visualizeOutput(fullOutputsQueueTemp, graph, null, berkeleyDB, keywords);
//									System.out.println("finsihed:");
//								}
//
//								// end of initial KWS
//								initialKWSDuration = ((System.nanoTime() - initialKWSStartTime) / 1e6);
//
//								System.out.println("after initialKWS duration:" + initialKWSDuration);
//
//								int discoveredAnswersKWS = fullOutputsQueue.size();
//								int discoveredAnswersNewKWS = 0;
//								int discoveredAnswersCoOcc = 0;
//								int discoveredAnswersDataCloud = 0;
//
//								double avgoutdegcand1 = avgOutDegreeOfKeywordCandidates;
//								double avgnumberofcands1 = avgNumberOfCandidates;
//								double secondaryKWSStartTime = 0d;
//								double secondaryKWSDuration = 0d;
//								double secondaryKWSQuality = 0d;
//
//								double coOccKWSStartTime = 0d;
//								double coOccKWSDuration = 0d;
//								double coOccKWSQuality = 0d;
//
//								double dataCloudKWSStartTime = 0d;
//								double dataCloudKWSDuration = 0d;
//								double dataCloudKWSQuality = 0d;
//
//								double minSuggestedWeightMainDelta = Double.MAX_VALUE;
//								double maxSuggestedWeightMainDelta = Double.MIN_VALUE;
//								double avgSuggestedWeightMainDelta = 0d;
//
//								double minSuggestedWeightSecondDelta = Double.MAX_VALUE;
//								double maxSuggestedWeightSecondDelta = Double.MIN_VALUE;
//								double avgSuggestedWeightSecondDelta = 0d;
//
//								String sugg = "";
//
//								// read from full outputs queue
//								// transform each answer from this to
//								// AnswerAsInput
//								ArrayList<AnswerAsInput> topNAnswersTemp = tansformPairwiseAnswerIntoAnserAsInput();
//
//								// initialize the class graphKWSExpand
//								// call expand
//								ArrayList<AnswerAsInput> topNAnswers = new ArrayList<AnswerAsInput>();
//								if (topNAnswersTemp.size() > maxRequiredResult) {
//									topNAnswers.addAll(topNAnswersTemp.subList(0, maxRequiredResult));
//								} else {
//									topNAnswers.addAll(topNAnswersTemp);
//								}
//
//								HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap = null;
//								CoOccurrence coOccHandler = null;
//								DataCloudDistinguishability dataCloudHandler = null;
//
//								double incEvalTotalCostPlusInitCost = 0d;
//								IncEval incEval = null;
//
//								if (keywordSuggestionOn) {
//
//									if (qualityExp)
//										System.out.println(
//												"starting our keyword suggestion " + new DateTime() + " for keywords "
//														+ DummyFunctions.getStringOutOfCollection(keywords, ";"));
//
//									if (qualityExp) {
//
//										System.out.println("secondary delta");
//
//										double tempDelta = this.delta;
//										this.delta = this.delta * 1.5;
//										estimatedWeightOfSuggestedKeywordMap = keywordExpand(topNAnswers, keywords);
//
//										if (estimatedWeightOfSuggestedKeywordMap == null
//												|| estimatedWeightOfSuggestedKeywordMap.size() == 0
//												|| graphKWSExpand.bestKeywordInfo == null
//												|| graphKWSExpand.bestKeywordInfo.nodeId == null) {
//											hasASuggestingKeyword = false;
//											break;
//										}
//
//										int sizeOfAllSuggestedKeywords = estimatedWeightOfSuggestedKeywordMap.size();
//
//										for (int tokenId : estimatedWeightOfSuggestedKeywordMap.keySet()) {
//											avgSuggestedWeightSecondDelta += estimatedWeightOfSuggestedKeywordMap
//													.get(tokenId).cost;
//
//											minSuggestedWeightSecondDelta = Math.min(
//													estimatedWeightOfSuggestedKeywordMap.get(tokenId).cost,
//													minSuggestedWeightSecondDelta);
//
//											maxSuggestedWeightSecondDelta = Math.max(
//													estimatedWeightOfSuggestedKeywordMap.get(tokenId).cost,
//													maxSuggestedWeightSecondDelta);
//										}
//										avgSuggestedWeightSecondDelta /= (double) sizeOfAllSuggestedKeywords;
//
//										this.delta = tempDelta;
//									}
//
//									estimatedWeightOfSuggestedKeywordMap = keywordExpand(topNAnswers, keywords);
//
//									System.out.println("estimatedWeightOfSuggestedKeywordMap size: "
//											+ estimatedWeightOfSuggestedKeywordMap.size());
//
//									if (estimatedWeightOfSuggestedKeywordMap == null
//											|| estimatedWeightOfSuggestedKeywordMap.size() == 0
//											|| graphKWSExpand.bestKeywordInfo == null
//											|| graphKWSExpand.bestKeywordInfo.nodeId == null) {
//										hasASuggestingKeyword = false;
//										break;
//									}
//
//									int sizeOfAllSuggestedKeywords = estimatedWeightOfSuggestedKeywordMap.size();
//
//									for (int tokenId : estimatedWeightOfSuggestedKeywordMap.keySet()) {
//										avgSuggestedWeightMainDelta += estimatedWeightOfSuggestedKeywordMap
//												.get(tokenId).cost;
//
//										minSuggestedWeightMainDelta = Math.min(
//												estimatedWeightOfSuggestedKeywordMap.get(tokenId).cost,
//												minSuggestedWeightMainDelta);
//
//										maxSuggestedWeightMainDelta = Math.max(
//												estimatedWeightOfSuggestedKeywordMap.get(tokenId).cost,
//												maxSuggestedWeightMainDelta);
//									}
//									avgSuggestedWeightMainDelta /= (double) sizeOfAllSuggestedKeywords;
//
//									int c = 0;
//									for (int key : estimatedWeightOfSuggestedKeywordMap.keySet()) {
//										sugg += StringPoolUtility.getStringOfId(key) + ":"
//												+ estimatedWeightOfSuggestedKeywordMap.get(key).cost + "; ";
//										c++;
//										if (c > 49) {
//											break;
//										}
//									}
//
//									System.out
//											.println("after keyword suggestion:" + graphKWSExpand.getKeywordsDuration);
//									System.out
//											.println("keyword suggestion visited nodes " + graphKWSExpand.visitedNodes);
//									System.out.println(
//											"keyword suggestion visited keywords " + graphKWSExpand.visitedKeywords);
//									System.out.println("top-10: " + sugg);
//
//									//Suggest keywords based on Diversification function
//
//									Diversification div = new Diversification(graph);
//									HashSet<String> diversifiedKeywords = div.run(estimatedWeightOfSuggestedKeywordMap, topNAnswers, false, graph, 2, nodeIdsOfToken);
//									System.out.println("diversifivation suggestion: ");
//									System.out.println(diversifiedKeywords);
//
//									// incremental evaluation
//									incEval = new IncEval(graph, graphKWSExpand.bestKeywordInfo, topNAnswers,
//											distanceBound, DummyProperties.KWSSetting.SUBGRAPH);
//
//									if (incEvalOn) {
//
//										System.out.println("starting our inc eval" + new DateTime());
//
//										incEval.incEval(prunedLandmarkLabeling);
//
//										for (int p = 0; p < topNAnswers.size(); p++) {
//											incEvalTotalCostPlusInitCost += topNAnswers.get(p).getCost()
//													+ incEval.lastTripleOfKeywordMatchToTarget[p].getCost();
//										}
//
//										System.out.println("after incEval: " + incEval.incEvalDuration);
//										System.out.println("incEval cost: " + incEvalTotalCostPlusInitCost);
//									}
//
//									/// run a from-scratch KWS for new query
//
//									if (newKWSOn) {
//
//										System.out.println("starting newKWS: " + new DateTime());
//
//										secondaryKWSStartTime = System.nanoTime();
//
//										ArrayList<String> newKeywords;
//
//										if (qualityExp) {
//											newKeywords = new ArrayList<String>(ourKeywords);
//										} else {
//											newKeywords = new ArrayList<String>(keywords);
//										}
//
//										newKeywords.add(StringPoolUtility
//												.getStringOfId(graphKWSExpand.lowestWeightSuggestedKeywordId));
//
//										if (qualityExp)
//											System.out.println("testing newKWS: " + new DateTime() + " with keywords: "
//													+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));
//
//										preKWS(newKeywords, exp);
//										run(newKeywords);
//										secondaryKWSDuration = ((System.nanoTime() - secondaryKWSStartTime) / 1e6);
//
//										for (PairwiseAnswer resultTree : fullOutputsQueue) {
//											secondaryKWSQuality += resultTree.pairwiseWeight;
//										}
//
//										if (fullOutputsQueue.size() < maxRequiredResult) {
//											double addedValue = (double) graphKWSExpand.initialOveralWeight
//													/ (double) maxRequiredResult;
//											int lessResult = maxRequiredResult - fullOutputsQueue.size();
//
//											secondaryKWSQuality += lessResult * addedValue;
//										}
//
//										discoveredAnswersNewKWS = fullOutputsQueue.size();
//
//										if (debugMode && visualizeMode) {
//											LinkedList<ResultTree> fullOutputsQueueTemp = new LinkedList<ResultTree>();
//
//											for (PairwiseAnswer rClique : fullOutputsQueue) {
//												fullOutputsQueueTemp.add(rClique.resultTree);
//											}
//
//											Visualizer.visualizeOutput(fullOutputsQueueTemp, graph,
//													estimatedWeightOfSuggestedKeywordMap, berkeleyDB, keywords);
//											System.out.println("finsihed:");
//										}
//
//										if (qualityExp) {
//											ourKeywords.clear();
//											ourKeywords.addAll(newKeywords);
//										}
//
//										System.out.println("after new keyword search time: " + secondaryKWSDuration);
//										System.out.println("after new keyword search cost: " + secondaryKWSQuality);
//										System.out.println("after coOccBaseLine answers: " + discoveredAnswersNewKWS);
//										System.out.println(newKeywords.size() + " keywords was "
//												+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));
//									}
//
//									if (coOccBaseLineOn) {
//
//										int tempMaxReqResults = this.maxRequiredResult;
//										this.maxRequiredResult = 20;
//
//										if (qualityExp)
//											System.out.println("starting coOccBaseLine: " + new DateTime()
//													+ " with keywords: "
//													+ DummyFunctions.getStringOutOfCollection(coOccKeywords, ";"));
//
//										preKWS(coOccKeywords, exp);
//										run(coOccKeywords);
//
//										topNAnswersTemp = tansformPairwiseAnswerIntoAnserAsInput();
//
//										// initialize the class graphKWSExpand
//										// call expand
//										topNAnswers = new ArrayList<AnswerAsInput>();
//										if (topNAnswersTemp.size() > this.maxRequiredResult) {
//											topNAnswers.addAll(topNAnswersTemp.subList(0, this.maxRequiredResult));
//										} else {
//											topNAnswers.addAll(topNAnswersTemp);
//										}
//
//										coOccKWSStartTime = System.nanoTime();
//
//										ArrayList<String> newKeywords;
//
//										if (qualityExp) {
//											newKeywords = new ArrayList<String>(coOccKeywords);
//										} else {
//											newKeywords = new ArrayList<String>(keywords);
//										}
//
//										if (qualityExp)
//											System.out.println("testing coOccBaseLine: " + new DateTime()
//													+ " with keywords: "
//													+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));
//
//										coOccHandler = new CoOccurrence(graph, nodeIdsOfToken, topNAnswers, 1,
//												keywords);
//										coOccHandler.expand();
//
//										this.maxRequiredResult = tempMaxReqResults;
//										fullOutputsQueue.clear();
//
//										if (coOccHandler.topFrequentKeywords.size() > 0) {
//
//											if (qualityExp) {
//												System.out.println("selected keyword: "
//														+ coOccHandler.topFrequentKeywords.get(0) + " num of cand: "
//														+ nodeIdsOfToken.get(StringPoolUtility.getIdOfStringFromPool(
//																coOccHandler.topFrequentKeywords.get(0))));
//											}
//
//											newKeywords.add(coOccHandler.topFrequentKeywords.get(0));
//											preKWS(newKeywords, exp);
//											run(newKeywords);
//											coOccKWSDuration = ((System.nanoTime() - coOccKWSStartTime) / 1e6);
//
//											for (PairwiseAnswer resultTree : fullOutputsQueue) {
//												coOccKWSQuality += resultTree.pairwiseWeight;
//											}
//										}
//
//										if (fullOutputsQueue.size() < maxRequiredResult) {
//											double addedValue = (double) graphKWSExpand.initialOveralWeight
//													/ (double) maxRequiredResult;
//											int lessResult = maxRequiredResult - fullOutputsQueue.size();
//
//											coOccKWSQuality += lessResult * addedValue;
//										}
//
//										discoveredAnswersCoOcc = fullOutputsQueue.size();
//
//										if (qualityExp) {
//											coOccKeywords.clear();
//											coOccKeywords.addAll(newKeywords);
//										}
//
//										System.out.println("after coOccBaseLine time: " + coOccKWSDuration);
//										System.out.println("after coOccBaseLine cost: " + coOccKWSQuality);
//										System.out.println("after coOccBaseLine answers: " + discoveredAnswersCoOcc);
//										System.out.println(newKeywords.size() + " keywords was "
//												+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));
//									}
//
//									if (dataCloudOn) {
//
//										if (qualityExp)
//											System.out.println("starting data cloud: " + new DateTime()
//													+ " with keywords: "
//													+ DummyFunctions.getStringOutOfCollection(dataCloudKeywords, ";"));
//
//										ArrayList<String> newKeywords;
//										if (qualityExp) {
//											newKeywords = new ArrayList<String>(dataCloudKeywords);
//										} else {
//											newKeywords = new ArrayList<String>(keywords);
//										}
//										dataCloudKWSStartTime = System.nanoTime();
//										dataCloudHandler = new DataCloudDistinguishability(graph, nodeIdsOfToken,
//												topNAnswers, 1, newKeywords);
//
//										dataCloudHandler.expand();
//										fullOutputsQueue.clear();
//										if (dataCloudHandler.topFrequentKeywords.size() > 0) {
//
//											if (qualityExp) {
//												System.out.println("selected keyword: "
//														+ dataCloudHandler.topFrequentKeywords.get(0) + " num of cand: "
//														+ nodeIdsOfToken.get(StringPoolUtility.getIdOfStringFromPool(
//																dataCloudHandler.topFrequentKeywords.get(0))));
//											}
//
//											newKeywords.add(dataCloudHandler.topFrequentKeywords.get(0));
//
//											if (qualityExp)
//												System.out.println("testing data cloud: " + new DateTime()
//														+ " with keywords: "
//														+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));
//
//											preKWS(newKeywords, exp);
//											run(newKeywords);
//											dataCloudKWSDuration = ((System.nanoTime() - dataCloudKWSStartTime) / 1e6);
//
//											for (PairwiseAnswer resultTree : fullOutputsQueue) {
//												dataCloudKWSQuality += resultTree.pairwiseWeight;
//											}
//										}
//
//										if (fullOutputsQueue.size() < maxRequiredResult) {
//											double addedValue = (double) graphKWSExpand.initialOveralWeight
//													/ (double) maxRequiredResult;
//											int lessResult = maxRequiredResult - fullOutputsQueue.size();
//
//											dataCloudKWSQuality += lessResult * addedValue;
//										}
//
//										discoveredAnswersDataCloud = fullOutputsQueue.size();
//
//										if (qualityExp) {
//											dataCloudKeywords.clear();
//											dataCloudKeywords.addAll(newKeywords);
//										}
//
//										System.out.println("after dataCloud time: " + dataCloudKWSDuration);
//										System.out.println("after dataCloud cost: " + dataCloudKWSQuality);
//										System.out.println("after dataCloud answers: " + discoveredAnswersDataCloud);
//										System.out.println(newKeywords.size() + " keywords was "
//												+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));
//									}
//
//								}
//
//								settingStr = "After: discoveredAnswers:" + fullOutputsQueue.size() + " kwsTime: "
//										+ initialKWSDuration;
//
//								System.out.println("Pairwise: " + settingStr);
//
//								ArrayList<InfoHolder> timeInfos = new ArrayList<InfoHolder>();
//								timeInfos.add(new InfoHolder(0, "Setting", "Pairwise"));
//								timeInfos.add(new InfoHolder(1, "keywords",
//										DummyFunctions.getStringOutOfCollection(keywords, ";")));
//								timeInfos.add(new InfoHolder(2, "Nodes", graph.nodeOfNodeId.size()));
//								timeInfos.add(new InfoHolder(3, "Relationships", graph.relationOfRelId.size()));
//								timeInfos.add(new InfoHolder(4, "num keywords", keywords.size()));
//								timeInfos.add(new InfoHolder(5, "distance bound", distanceBound));
//								timeInfos.add(new InfoHolder(6, "delta", delta));
//								timeInfos.add(new InfoHolder(7, "n", maxRequiredResult));
//								timeInfos.add(new InfoHolder(8, "discoveredAnswers1", discoveredAnswersKWS));
//								timeInfos.add(new InfoHolder(9, "avg out deg cand1", avgoutdegcand1));
//								timeInfos.add(new InfoHolder(10, "avg cands1", avgnumberofcands1));
//								timeInfos.add(new InfoHolder(11, "initialOveralWeight",
//										graphKWSExpand != null ? graphKWSExpand.initialOveralWeight : ""));
//								timeInfos.add(new InfoHolder(12, "first KWS", initialKWSDuration));
//								timeInfos.add(new InfoHolder(13, "qExpand time",
//										graphKWSExpand != null ? graphKWSExpand.querySuggestionKWSDuration : ""));
//								timeInfos.add(new InfoHolder(14, "qExp Vis. Nodes",
//										graphKWSExpand != null ? graphKWSExpand.visitedNodes : ""));
//								timeInfos.add(new InfoHolder(15, "qExp Vis. Kywrds",
//										graphKWSExpand != null ? graphKWSExpand.visitedKeywords : ""));
//								timeInfos.add(new InfoHolder(16, "qExp getKywrds Time",
//										graphKWSExpand != null ? graphKWSExpand.getKeywordsDuration : ""));
//								timeInfos.add(new InfoHolder(17, "sugg kywrds (freq pruned)", graphKWSExpand != null
//										? graphKWSExpand.estimatedWeightOfSuggestedKeywordMap.size() : ""));
//								timeInfos.add(new InfoHolder(18, "totalWeightOfSuggestedKeywords",
//										graphKWSExpand != null ? graphKWSExpand.totalWeightOfSuggestedKeywords : ""));
//								timeInfos.add(new InfoHolder(19, "avgWeightOfSuggestedKeyword",
//										graphKWSExpand != null ? graphKWSExpand.avgQualityOfSuggestedKeyword : ""));
//
//								timeInfos.add(new InfoHolder(20, "lowest weight kywrd",
//										graphKWSExpand != null ? graphKWSExpand.lowestWeightOfSuggestedKeyword : ""));
//								timeInfos.add(new InfoHolder(21, "lowest suggested weight", graphKWSExpand != null
//										? StringPoolUtility.getStringOfId(graphKWSExpand.lowestWeightSuggestedKeywordId)
//										: ""));
//								timeInfos.add(new InfoHolder(22, "removed high freq of sugg",
//										graphKWSExpand != null ? graphKWSExpand.highFrequentKeywordsRemovedNum : ""));
//
//								timeInfos.add(new InfoHolder(23, "incEval Duration",
//										incEval != null ? incEval.incEvalDuration : ""));
//								timeInfos.add(new InfoHolder(24, "incEval visited nodes",
//										incEval != null ? incEval.visitedNodes : ""));
//								timeInfos.add(new InfoHolder(25, "incEval total cost",
//										incEvalOn ? incEvalTotalCostPlusInitCost : ""));
//								timeInfos.add(new InfoHolder(26, "new KWS time", newKWSOn ? secondaryKWSDuration : ""));
//								timeInfos.add(
//										new InfoHolder(27, "new KWS quality", newKWSOn ? secondaryKWSQuality : ""));
//								timeInfos.add(new InfoHolder(28, "discoveredAnswersNewKWS",
//										newKWSOn ? discoveredAnswersNewKWS : ""));
//								timeInfos.add(new InfoHolder(29, "avg out deg cand2",
//										newKWSOn ? avgOutDegreeOfKeywordCandidates : ""));
//
//								timeInfos.add(new InfoHolder(30, "KWS time for coOcc",
//										coOccBaseLineOn ? coOccKWSDuration : ""));
//								timeInfos.add(new InfoHolder(31, "KWS quality for coOcc",
//										coOccBaseLineOn ? coOccKWSQuality : ""));
//								timeInfos.add(new InfoHolder(32, "discoveredAnswers co occ",
//										coOccBaseLineOn ? discoveredAnswersCoOcc : ""));
//								timeInfos.add(new InfoHolder(33, "coOccHandler",
//										(coOccBaseLineOn && coOccHandler.topFrequentKeywords.size() > 0)
//												? coOccHandler.topFrequentKeywords.get(0) : ""));
//
//								timeInfos.add(new InfoHolder(34, "KWS time for dataCloud",
//										dataCloudOn ? dataCloudKWSDuration : ""));
//								timeInfos.add(new InfoHolder(35, "KWS quality for dataCloud",
//										dataCloudOn ? dataCloudKWSQuality : ""));
//								timeInfos.add(new InfoHolder(36, "discoveredAnswers dataCloud",
//										dataCloudOn ? discoveredAnswersDataCloud : ""));
//								timeInfos.add(new InfoHolder(37, "dataCloud Handler",
//										(dataCloudOn && dataCloudHandler.topFrequentKeywords.size() > 0)
//												? dataCloudHandler.topFrequentKeywords.get(0) : ""));
//
//								timeInfos.add(
//										new InfoHolder(38, "minSuggestedWeightMainDelta", minSuggestedWeightMainDelta));
//								timeInfos.add(
//										new InfoHolder(39, "avgSuggestedWeightMainDelta", avgSuggestedWeightMainDelta));
//								timeInfos.add(
//										new InfoHolder(40, "maxSuggestedWeightMainDelta", maxSuggestedWeightMainDelta));
//
//								timeInfos.add(new InfoHolder(41, "minSuggestedWeightSecondDelta",
//										minSuggestedWeightSecondDelta));
//								timeInfos.add(new InfoHolder(42, "avgSuggestedWeightSecondDelta",
//										avgSuggestedWeightSecondDelta));
//								timeInfos.add(new InfoHolder(43, "maxSuggestedWeightSecondDelta",
//										maxSuggestedWeightSecondDelta));
//
//								timeInfos.add(new InfoHolder(44, "10 sugg kywrds", sugg));
//
//								TimeLogger.LogTime(resultName + "PairwiseOutput.csv", true, timeInfos);
//
//								exp++;
//							}
//
//							if (!hasEnoughAnswer) {
//								System.out
//										.println("not enough answer for keywords " + Arrays.toString(keywords.toArray())
//												+ " the answer size is: " + fullOutputsQueue.size());
//								continue;
//							}
//							if (!hasASuggestingKeyword) {
//								System.out.println("no suggesting keyword within bound " + distanceBound + " , delta: "
//										+ delta + " , keywords: " + Arrays.toString(keywords.toArray()) + " for top-"
//										+ fullOutputsQueue.size() + " answers");
//								continue;
//							}
//
//						}
//					}
//				}
//			}
//		}
//
//	}
//
//	private void preKWS(ArrayList<String> keywords, int exp) throws Exception {
//
//		candidatesOfAKeyword = null;
//		candidatesSet = null;
//		fullOutputsQueue = null;
//
//		// BerkleleyDB.environment.evictMemory();
//		System.runFinalization();
//		System.gc();
//
//		Thread.sleep(1000);
//
//		candidatesOfAKeyword = new HashMap<String, HashSet<Integer>>();
//		candidatesSet = new HashSet<Integer>();
//
//		// if (usingBDB) {
//		// KWSUtilities.findCandidatesOfKeywordsUsingBDB(keywords, berkeleyDB,
//		// candidatesOfAKeyword, candidatesSet);
//		// } else {
//		KWSUtilities.findCandidatesOfKeywordsUsingInvertedList(keywords, nodeIdsOfToken, candidatesOfAKeyword,
//				candidatesSet);
//		// }
//
//		avgOutDegreeOfKeywordCandidates = DummyFunctions.getAvgOutDegreesOfASet(graph, candidatesSet);
//		avgNumberOfCandidates = (double) candidatesSet.size() / (double) keywords.size();
//
//		maxRequiredResult = fixedmaxRequiredResult;
//
//		fullOutputsQueue = new LinkedList<PairwiseAnswer>();
//
//	}
//
//	private void run(ArrayList<String> keywords) throws Exception {
//
//		LinkedHashSet<Integer>[] c = (LinkedHashSet<Integer>[]) new LinkedHashSet[keywords.size()];
//
//		// for i=0 to l do
//		for (int i = 0; i < keywords.size(); i++) {
//
//			// Ci <-- the set of nodes in G containing ki
//			LinkedHashSet<Integer> nodeIdsOfTheToken = new LinkedHashSet<Integer>();
//
//			// if (usingBDB) {
//			// nodeIdsOfTheToken.addAll(candidatesOfAKeyword.get(keywords.get(i)));
//			// } else {
//			nodeIdsOfTheToken.addAll(candidatesOfAKeyword.get(keywords.get(i)));
//			// }
//
//			if (DummyProperties.debugMode) {
//				System.out.println(keywords.get(i) + ":" + nodeIdsOfTheToken);
//			}
//
//			c[i] = nodeIdsOfTheToken;
//		}
//
//		// Queue <-- an empty priority queue
//		PriorityQueue<AnswerSearchSpacePair> queue = new PriorityQueue<AnswerSearchSpacePair>(maxRequiredResult,
//				new Comparator<AnswerSearchSpacePair>() {
//					public int compare(AnswerSearchSpacePair a1, AnswerSearchSpacePair a2) {
//						return Integer.compare(a1.answer.weight, a2.answer.weight);
//					}
//				});
//
//		// A <-- FindTopRankedAnswer(C, G, l, r)
//		PairwiseAnswer answer = findTopRankedAnswer(c, keywords);
//
//		// if A =empty ; then
//		if (answer != null) {
//			queue.add(new AnswerSearchSpacePair(answer, c));
//		}
//
//		// 8: while Queue is not empty
//		while (!queue.isEmpty()) {
//			// <A, S> <-- Queue.removeTop()
//			AnswerSearchSpacePair currentAnswerSearchSpacePair = queue.poll();
//
//			// adding the new disovered answer
//			fullOutputsQueue.add(currentAnswerSearchSpacePair.answer);
//			if (debugMode)
//				print(currentAnswerSearchSpacePair.answer, keywords);
//
//			// maxRequiredResult <-- maxRequiredResult - 1
//			maxRequiredResult--;
//
//			// if maxRequiredResult = 0 then
//			if (maxRequiredResult == 0)
//				break;
//
//			// ProduceSubSpaces
//			LinkedHashSet<Integer>[][] newSubspaces = produceSubSpaces(currentAnswerSearchSpacePair, keywords);
//
//			if (DummyProperties.debugMode) {
//				System.out.println();
//				System.out.println("newSubspaces at maxRequiredResult=" + maxRequiredResult);
//				for (int i = 0; i < newSubspaces.length; i++) {
//					for (int j = 0; j < newSubspaces[i].length; j++) {
//						System.out.print(newSubspaces[i][j] + " ");
//					}
//					System.out.println();
//				}
//				System.out.println();
//			}
//
//			// for i <-- 1 to l do
//			for (int i = 0; i < keywords.size(); i++) {
//
//				// check validity of a subspace:
//				boolean isAValidSubspace = true;
//				for (LinkedHashSet<Integer> newSubspace : newSubspaces[i]) {
//					if (newSubspace.isEmpty()) {
//						isAValidSubspace = false;
//						break;
//					}
//				}
//
//				if (!isAValidSubspace)
//					continue;
//
//				// // Ai <-- FindTopRankedAnswer(SBi, G, l, r)
//				PairwiseAnswer answer_i = findTopRankedAnswer(newSubspaces[i], keywords);
//				//
//				// // if A =empty ; then
//				if (answer_i != null) {
//					queue.add(new AnswerSearchSpacePair(answer_i, newSubspaces[i]));
//				}
//			}
//
//		}
//
//		if (DummyProperties.debugMode) {
//			System.out.println("\n FinalAnswers: \n");
//			for (PairwiseAnswer pairwiseAnswer : fullOutputsQueue) {
//				print(pairwiseAnswer, keywords);
//			}
//		}
//
//		// updatePairwiseAnswersWithResultTree(keywords, fullOutputsQueue);
//
//	}
//
////	private void updatePairwiseAnswersWithResultTree(ArrayList<String> keywords,
////			LinkedList<PairwiseAnswer> fullOutputsQueue) throws Exception {
////
////		ArrayList<PairwiseAnswer> localOutputsQueue = new ArrayList<PairwiseAnswer>(fullOutputsQueue);
////
////		Collections.sort(localOutputsQueue, new Comparator<PairwiseAnswer>() {
////			@Override
////			public int compare(PairwiseAnswer o1, PairwiseAnswer o2) {
////				return Double.compare(o1.pairwiseWeight, o2.pairwiseWeight);
////			}
////		});
////
////		for (PairwiseAnswer answer : localOutputsQueue) {
////
////			DistinctRootExperiment bank = new DistinctRootExperiment();
////
////			// in fact only one candidate
////			HashMap<String, HashSet<Integer>> candidatesOfAKeywordForBank = new HashMap<String, HashSet<Integer>>();
////
////			for (int i = 0; i < keywords.size(); i++) {
////				candidatesOfAKeywordForBank.putIfAbsent(keywords.get(i), new HashSet<>());
////				candidatesOfAKeywordForBank.get(keywords.get(i)).add(answer.nodeMatches.get(i));
////			}
////
////			bank.runFromOutside(keywords, candidatesOfAKeywordForBank, distanceBound, 1, graph,
////					fixedmaxRequiredResult * 3, debugMode);
////
////			ResultTree resTree = bank.fullOutputsQueue.poll();
////
////			answer.resultTree = resTree;
////
////		}
////
////	}
//
//	private LinkedHashSet<Integer>[][] produceSubSpaces(AnswerSearchSpacePair currentAnswerSearchSpacePair,
//			ArrayList<String> keywords) {
//
//		// Input: the best answer of previous step, A = <v1; v2; : : : vl>,
//		// and the sets of content nodes, S1, . . . , Sl
//		// Output: l new subspaces
//
//		// initialization of data structure
//		LinkedHashSet<Integer>[][] newSubspaces = (LinkedHashSet<Integer>[][]) new LinkedHashSet[keywords
//				.size()][keywords.size()];
//
//		for (int i = 0; i < keywords.size(); i++) {
//			for (int j = 0; j < keywords.size(); j++) {
//				newSubspaces[i][j] = new LinkedHashSet<Integer>(keywords.size());
//			}
//		}
//
//		// for i <-- 0 to l do
//		for (int i = 0; i < keywords.size(); i++) {
//			// for j <-- 0 to i-1 do
//			for (int j = 0; j <= (i - 1); j++) {
//				// SBji <-- {vj}
//				newSubspaces[i][j].add(currentAnswerSearchSpacePair.answer.nodeMatches.get(j));
//			}
//
//			// SBii <-- Si - {vi}
//			LinkedHashSet<Integer> temp_ii = new LinkedHashSet<Integer>();
//			temp_ii.addAll(currentAnswerSearchSpacePair.searchSpace[i]);
//			temp_ii.remove(currentAnswerSearchSpacePair.answer.nodeMatches.get(i));
//			newSubspaces[i][i] = temp_ii;
//
//			// for j <-- i + 1 to l do
//			for (int j = i + 1; j < keywords.size(); j++) {
//				// SBji <-- Sj
//				LinkedHashSet<Integer> temp_j = new LinkedHashSet<Integer>();
//				temp_j.addAll(currentAnswerSearchSpacePair.searchSpace[j]);
//				newSubspaces[i][j] = temp_j;
//			}
//		}
//
//		// return <SB1; : : : ; SBl> where SBi = SB1i *...* SBli
//		return newSubspaces;
//	}
//
//	private void print(PairwiseAnswer answer, ArrayList<String> keywords) {
//		String answerStr = "ANSWER:\n";
//		for (int i = 0; i < keywords.size(); i++) {
//			answerStr += keywords.get(i) + ":" + answer.nodeMatches.get(i) + ", ";
//		}
//		answerStr = answerStr.substring(0, answerStr.length() - 2);
//		System.out.println(maxRequiredResult + "=> " + answerStr + " weight: " + answer.weight + ", pairwiseWeight: "
//				+ answer.pairwiseWeight);
//	}
//
//	private PairwiseAnswer findTopRankedAnswer(LinkedHashSet<Integer>[] searchSpace, ArrayList<String> keywords) {
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
//					if (nndPair.distance <= distanceBound) {
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
//		// topAnswer <- empty
//		PairwiseAnswer topAnswer = null;
//
//		// for i 1 to l do
//		for (int i = 0; i < keywords.size(); i++) {
//			int j = 0;
//			for (Integer sji : searchSpace[i]) {
//				boolean passDistanceCriteria = true;
//				for (int k = 0; k < keywords.size(); k++) {
//					if (nearestNodeAndDistancesToSindex[i][j][k].distance > distanceBound) {
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
//						topAnswer = new PairwiseAnswer(keywords.size());
//						for (int k = 0; k < keywords.size(); k++) {
//							topAnswer.nodeMatches.add(nearestNodeAndDistancesToSindex[i][j][k].nodeId);
//						}
//						topAnswer.weight = weight;
//					}
//				}
//				j++;
//			}
//		}
//
//		if (topAnswer != null) {
//			for (int a = 0; a < topAnswer.nodeMatches.size(); a++) {
//				for (int b = a + 1; b < topAnswer.nodeMatches.size(); b++) {
//					topAnswer.pairwiseWeight += prunedLandmarkLabeling.queryDistance(topAnswer.nodeMatches.get(a),
//							topAnswer.nodeMatches.get(b));
//				}
//			}
//		}
//
//		return topAnswer;
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
//			if (nearestNodeAndDistance.nodeId == null) {
//				System.out.println("no near node from the list: src:" + sourceNodeId + " nearest:"
//						+ Arrays.toString(linkedHashSet.toArray()));
//			} else {
//				System.out.println("nearestNodeAndDistance: src:" + sourceNodeId + " nearest:"
//						+ nearestNodeAndDistance.nodeId + " dist:" + nearestNodeAndDistance.distance);
//			}
//		}
//
//		return nearestNodeAndDistance;
//	}
//
//	public ArrayList<AnswerAsInput> tansformPairwiseAnswerIntoAnserAsInput() {
//
//		ArrayList<AnswerAsInput> topNAnswers = new ArrayList<AnswerAsInput>();
//		LinkedList<PairwiseAnswer> tem = new LinkedList<PairwiseAnswer>();
//
//		while (!fullOutputsQueue.isEmpty()) {
//			PairwiseAnswer theWholeResult = fullOutputsQueue.poll();
//			tem.push(theWholeResult);
//
//			// In pairwise case, there is no root
//			int rootNodeId = -1;
//			double cost = theWholeResult.pairwiseWeight;
//
//			ArrayList<Integer> contentNodes = new ArrayList<Integer>();
//			contentNodes = theWholeResult.nodeMatches;
//
//			// In pairwise case, we may don't need allNodes
//			ArrayList<Integer> allNodes = new ArrayList<Integer>();
//
//			ListenableUndirectedGraph<ResultNode, RelationshipInfra> resultTree = new ListenableUndirectedGraph<ResultNode, RelationshipInfra>(
//					RelationshipInfra.class);
//
//			for (int nodeId : theWholeResult.nodeMatches) {
//				ResultNode resultNode = new ResultNode(" ", nodeId, graph.nodeOfNodeId.get(nodeId));
//				resultTree.addVertex(resultNode);
//			}
//
//			AnswerAsInput topNAnswer = new AnswerAsInput(rootNodeId, contentNodes, allNodes, cost);
//			topNAnswers.add(topNAnswer);
//		}
//		while (!tem.isEmpty()) {
//			PairwiseAnswer theWholeResultTree = tem.poll();
//			fullOutputsQueue.push(theWholeResultTree);
//		}
//		return topNAnswers;
//
//	}
//
//	public HashMap<Integer, CostAndNodesOfAnswersPair> keywordExpand(ArrayList<AnswerAsInput> topNAnswers,
//			ArrayList<String> keywords) throws Exception {
//
//		HashSet<Integer> keywordsSet = new HashSet<>();
//
//		for (AnswerAsInput ai : topNAnswers) {
//			for (int nodeId : ai.getContentNodes()) {
//				keywordsSet.addAll(DummyFunctions.getKeywords(graph, nodeId));
//			}
//		}
//
//		graphKWSExpand = new GraphKWSExpand(graph, topNAnswers, delta, distanceBound, berkeleyDB, keywordsSet);
//		// quality preservable keywords K ={(k′, w′), . . . }.
//		HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap = graphKWSExpand.expand();
//		return estimatedWeightOfSuggestedKeywordMap;
//	}
//}
