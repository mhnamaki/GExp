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
import dataset.BerkeleyDB.BerkleleyDB;
import graphInfra.GraphInfraReaderArray;
import graphInfra.RelationshipInfra;
import incrementalEvaluation.IncEval;
import neo4jBasedKWS.CrossProduct;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;
import neo4jBasedKWS.ResultTreeRelevanceComparator;
import queryExpansion.AnswerAsInput;

import queryExpansion.CostAndNodesOfAnswersPair;
import queryExpansion.RootKWSExpand;

public class DREQueryFinder {

	GraphInfraReaderArray graph;

	// xin
	// private static String graphInfraPath =
	// "/Users/zhangxin/Desktop/IMDB/sample/";
	// private static String keywordsPath =
	// "/Users/zhangxin/Desktop/keyword/imdbSample2.txt";
	// private static String envFilePath =
	// "/Users/zhangxin/Desktop/IMDB/sample/dbEnvNoProp";

	// // mhn
	private static String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/dbpGraphInfra/graph/dbp/";
	private static String keywordsPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/query/test/dbp_query_DR.txt";
	private static String envFilePath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/bdb/withProp/dbEnvWithProp";

	static int[] maxRequiredResults = { 2 };
	private static int[] distanceBounds = { 4 };
	private static double[] deltas = { 0.5 };//// quality bound
	private static int[] keywordSizes = { 2 };

	int maxRequiredResult;
	private int distanceBound;
	private double delta;//// quality bound

	int heapSize = 0;

	HashMap<String, HashSet<Integer>> candidatesOfAKeyword = new HashMap<String, HashSet<Integer>>();
	HashSet<Integer> candidatesSet = new HashSet<Integer>();
	HashMap<Integer, HashSet<Integer>> nodeIdsOfToken = new HashMap<Integer, HashSet<Integer>>();

	ArrayList<ListenableUndirectedGraph<ResultNode, RelationshipInfra>> printedResultTrees = new ArrayList<ListenableUndirectedGraph<ResultNode, RelationshipInfra>>();

	RootKWSExpand rootKWSExpand;

	private double avgNumberOfCandidates;
	private double avgOutDegreeOfKeywordCandidates;
	static int totalPrintedResults = 0;

	private double initialKWSStartTime = 0d;
	private double initialKWSDuration = 0d;

	public LinkedList<ResultTree> fullOutputsQueue = new LinkedList<ResultTree>();

	// BDB
	private static String database = "database";
	private static String catDatabase = "catDatabase";

	private static BerkleleyDB berkeleyDB;
	private static boolean withProperties = false;

	// experiments
	private static int numberOfSameExperiments = 1;
	private static boolean usingBDB = false;

	private static boolean debugMode = false;
	private static boolean visualizeMode = false;

	// steps turning on/off
	static boolean keywordSuggestionOn = true;
	// if don't want just change to false
	static boolean incEvalOn = keywordSuggestionOn && true;
	static boolean newKWSOn = keywordSuggestionOn && true;
	static boolean topkSelectionOn = keywordSuggestionOn && true;
	static boolean coOccBaseLineOn = keywordSuggestionOn && true;
	static boolean dataCloudOn = keywordSuggestionOn && true;

	static boolean qualityExp = true;
	ArrayList<String> ourKeywords = new ArrayList<String>();
	ArrayList<String> coOccKeywords = new ArrayList<String>();
	ArrayList<String> dataCloudKeywords = new ArrayList<String>();

	boolean timeOut = false;
	double maximumTimeBound = 180000;

	public DREQueryFinder() {

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
			} else if (args[i].equals("-database")) {
				database = args[++i];
			} else if (args[i].equals("-catDatabase")) {
				catDatabase = args[++i];
			} else if (args[i].equals("-envFilePath")) {
				envFilePath = args[++i];
			} else if (args[i].equals("-numberOfSameExperiments")) {
				numberOfSameExperiments = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-usingBDB")) {
				usingBDB = Boolean.parseBoolean(args[++i]);
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
			}

		}
		DummyProperties.withProperties = withProperties;
		DummyProperties.debugMode = debugMode;
		DREQueryFinder experimentUsingBDB = new DREQueryFinder();
		experimentUsingBDB.runBankExperiment();

	}

	private void runBankExperiment() throws Exception {

		boolean addBackward = true;

		graph = new GraphInfraReaderArray(graphInfraPath, addBackward);
		// if (!usingBDB) {
		graph.read();
		nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);
		// }
		// else {
		// graph.readWithNoLabels();
		// berkeleyDB = new BerkleleyDB(database, catDatabase, envFilePath);
		// }

		if (debugMode)
			System.out.println("finish read");

		// read all initial queries

		for (int keywordSize : keywordSizes) {

			ArrayList<ArrayList<String>> keywordsSet = KWSUtilities.readKeywords(keywordsPath, keywordSize);

			for (int i = 0; i < keywordsSet.size(); i++) {

				for (int distanceBound : distanceBounds) {
					for (double delta : deltas) {
						for (int maxRequiredResult : maxRequiredResults) {

							this.delta = delta;
							this.maxRequiredResult = maxRequiredResult;
							heapSize = maxRequiredResult;
							this.distanceBound = distanceBound;

							ArrayList<String> keywords = keywordsSet.get(i);

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
											+ DummyFunctions.getStringOutOfCollection(ourKeywords, ";") + ", n:"
											+ maxRequiredResult + ", distance:" + distanceBound + ", delta:" + delta;

									preKWS(ourKeywords, exp);
									run(ourKeywords);

									if (timeOut) {
										continue;
									}
								} else {

									settingStr = keywords.size() + " keywords is "
											+ DummyFunctions.getStringOutOfCollection(keywords, ";") + ", n:"
											+ maxRequiredResult + ", distance:" + distanceBound + ", delta:" + delta;

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
									LinkedList<ResultTree> fullOutputsQueueTemp = new LinkedList<ResultTree>(
											fullOutputsQueue);
									Visualizer.visualizeOutput(fullOutputsQueueTemp, graph, null, berkeleyDB, keywords);
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

								double dataCloudKWSStartTime = 0d;
								double dataCloudKWSDuration = 0d;
								double dataCloudKWSQuality = 0d;

								double minSuggestedWeightMainDelta = Double.MAX_VALUE;
								double maxSuggestedWeightMainDelta = Double.MIN_VALUE;
								double avgSuggestedWeightMainDelta = 0d;

								double minSuggestedWeightSecondDelta = Double.MAX_VALUE;
								double maxSuggestedWeightSecondDelta = Double.MIN_VALUE;
								double avgSuggestedWeightSecondDelta = 0d;

								String sugg = "";

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

								HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap = null;

								CoOccurrence coOccHandler = null;
								DataCloudDistinguishability dataCloudHandler = null;

								double incEvalTotalCostPlusInitCost = 0d;
								IncEval incEval = null;

								if (keywordSuggestionOn) {

									if (qualityExp)
										System.out.println(
												"starting our keyword suggestion " + new DateTime() + " for keywords "
														+ DummyFunctions.getStringOutOfCollection(keywords, ";"));

									if (qualityExp) {

										System.out.println("secondary delta");

										double tempDelta = this.delta;
										this.delta = this.delta * 2;
										estimatedWeightOfSuggestedKeywordMap = keywordExpand(topNAnswers, keywords);

										if (estimatedWeightOfSuggestedKeywordMap == null
												|| estimatedWeightOfSuggestedKeywordMap.size() == 0
												|| rootKWSExpand.bestKeywordInfo == null
												|| rootKWSExpand.bestKeywordInfo.nodeId == null) {
											hasASuggestingKeyword = false;
											break;
										}

										int sizeOfAllSuggestedKeywords = estimatedWeightOfSuggestedKeywordMap.size();

										for (int tokenId : estimatedWeightOfSuggestedKeywordMap.keySet()) {
											avgSuggestedWeightSecondDelta += estimatedWeightOfSuggestedKeywordMap
													.get(tokenId).cost;

											minSuggestedWeightSecondDelta = Math.min(
													estimatedWeightOfSuggestedKeywordMap.get(tokenId).cost,
													minSuggestedWeightSecondDelta);

											maxSuggestedWeightSecondDelta = Math.max(
													estimatedWeightOfSuggestedKeywordMap.get(tokenId).cost,
													maxSuggestedWeightSecondDelta);
										}
										avgSuggestedWeightSecondDelta /= (double) sizeOfAllSuggestedKeywords;

										this.delta = tempDelta;
									}

									estimatedWeightOfSuggestedKeywordMap = keywordExpand(topNAnswers, keywords);

									if (estimatedWeightOfSuggestedKeywordMap == null
											|| estimatedWeightOfSuggestedKeywordMap.size() == 0
											|| rootKWSExpand.bestKeywordInfo == null
											|| rootKWSExpand.bestKeywordInfo.nodeId == null) {
										hasASuggestingKeyword = false;
										break;
									}

									int sizeOfAllSuggestedKeywords = estimatedWeightOfSuggestedKeywordMap.size();

									for (int tokenId : estimatedWeightOfSuggestedKeywordMap.keySet()) {
										avgSuggestedWeightMainDelta += estimatedWeightOfSuggestedKeywordMap
												.get(tokenId).cost;

										minSuggestedWeightMainDelta = Math.min(
												estimatedWeightOfSuggestedKeywordMap.get(tokenId).cost,
												minSuggestedWeightMainDelta);

										maxSuggestedWeightMainDelta = Math.max(
												estimatedWeightOfSuggestedKeywordMap.get(tokenId).cost,
												maxSuggestedWeightMainDelta);
									}
									avgSuggestedWeightMainDelta /= (double) sizeOfAllSuggestedKeywords;

									int c = 0;
									for (Integer key : estimatedWeightOfSuggestedKeywordMap.keySet()) {
										sugg += StringPoolUtility.getStringOfId(key) + ":"
												+ estimatedWeightOfSuggestedKeywordMap.get(key).cost + "; ";
										c++;
										if (c > 9) {
											break;
										}
									}

									System.out.println("after keyword suggestion:" + rootKWSExpand.getKeywordsDuration);
									System.out
											.println("keyword suggestion visited nodes " + rootKWSExpand.visitedNodes);
									System.out.println(
											"keyword suggestion visited keywords " + rootKWSExpand.visitedKeywords);

									// incremental evaluation
									incEval = new IncEval(graph, rootKWSExpand.bestKeywordInfo, topNAnswers,
											distanceBound, DummyProperties.KWSSetting.DISTINCTROOT);

									if (incEvalOn) {

										System.out.println("starting our inc eval " + new DateTime());

										incEval.incEval();

										for (int p = 0; p < topNAnswers.size(); p++) {
											incEvalTotalCostPlusInitCost += topNAnswers.get(p).getCost()
													+ incEval.lastTripleOfKeywordMatchToTarget[p].getCost();
										}

										System.out.println("after incEval: " + incEval.incEvalDuration);
										System.out.println("incEval cost: " + incEvalTotalCostPlusInitCost);
									}

									/// run a from-scratch KWS for new query

									if (newKWSOn) {

										secondaryKWSStartTime = System.nanoTime();
										ArrayList<String> newKeywords;

										if (qualityExp) {
											newKeywords = new ArrayList<String>(ourKeywords);
										} else {
											newKeywords = new ArrayList<String>(keywords);
										}

										newKeywords.add(StringPoolUtility
												.getStringOfId(rootKWSExpand.lowestWeightSuggestedKeywordId));

										if (qualityExp)
											System.out.println("testing newKWS: " + new DateTime() + " with keywords: "
													+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));

										preKWS(newKeywords, exp);
										run(newKeywords);
										if (timeOut) {
											continue;
										}
										secondaryKWSDuration = ((System.nanoTime() - secondaryKWSStartTime) / 1e6);

										for (ResultTree resultTree : fullOutputsQueue) {
											secondaryKWSQuality += resultTree.cost;
										}

										if (fullOutputsQueue.size() < maxRequiredResult) {
											double addedValue = (double) rootKWSExpand.initialOveralWeight
													/ (double) maxRequiredResult;
											int lessResult = maxRequiredResult - fullOutputsQueue.size();

											secondaryKWSQuality += lessResult * addedValue;

										}

										discoveredAnswersNewKWS = fullOutputsQueue.size();

										if (debugMode && visualizeMode) {
											LinkedList<ResultTree> fullOutputsQueueTemp = new LinkedList<ResultTree>(
													fullOutputsQueue);
											Visualizer.visualizeOutput(fullOutputsQueueTemp, graph, null, berkeleyDB,
													newKeywords);
										}

										if (qualityExp) {
											ourKeywords.clear();
											ourKeywords.addAll(newKeywords);
										}

										System.out.println("after new keyword search time: " + secondaryKWSDuration);
										System.out.println("after new keyword search cost: " + secondaryKWSQuality);
										System.out.println(
												"after new keyword search answers: " + discoveredAnswersNewKWS);
										System.out.println(newKeywords.size() + " keywords was "
												+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));
									}

									if (coOccBaseLineOn) {

										// int tempMaxReqResults =
										// this.maxRequiredResult;
										// this.maxRequiredResult = 20;

										if (qualityExp) {
											System.out.println("starting coOccBaseLine: " + new DateTime()
													+ " with keywords: "
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
											System.out.println("testing coOccBaseLine: " + new DateTime()
													+ " with keywords: "
													+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));

										coOccHandler = new CoOccurrence(graph, nodeIdsOfToken, topNAnswers, 1,
												keywords);
										coOccHandler.expand();

										// this.maxRequiredResult =
										// tempMaxReqResults;
										fullOutputsQueue.clear();

										if (coOccHandler.topFrequentKeywords.size() > 0) {

											if (qualityExp) {
												System.out.println("selected keyword: "
														+ coOccHandler.topFrequentKeywords.get(0) + " num of cand: "
														+ nodeIdsOfToken
																.get(StringPoolUtility.getIdOfStringFromPool(
																		coOccHandler.topFrequentKeywords.get(0)))
																.size());
											}

											newKeywords.add(coOccHandler.topFrequentKeywords.get(0));
											preKWS(newKeywords, exp);
											run(newKeywords);
											if (timeOut) {
												continue;
											}
											coOccKWSDuration = ((System.nanoTime() - coOccKWSStartTime) / 1e6);

											for (ResultTree resultTree : fullOutputsQueue) {
												coOccKWSQuality += resultTree.cost;
											}

											if (debugMode && visualizeMode) {
												LinkedList<ResultTree> fullOutputsQueueTemp = new LinkedList<ResultTree>(
														fullOutputsQueue);
												Visualizer.visualizeOutput(fullOutputsQueueTemp, graph, null,
														berkeleyDB, newKeywords);
											}
										}

										if (fullOutputsQueue.size() < maxRequiredResult) {
											double addedValue = (double) rootKWSExpand.initialOveralWeight
													/ (double) maxRequiredResult;
											int lessResult = maxRequiredResult - fullOutputsQueue.size();

											coOccKWSQuality += lessResult * addedValue;
										}

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
											System.out.println("starting data cloud: " + new DateTime()
													+ " with keywords: "
													+ DummyFunctions.getStringOutOfCollection(dataCloudKeywords, ";"));

										ArrayList<String> newKeywords;
										if (qualityExp) {
											newKeywords = new ArrayList<String>(dataCloudKeywords);
										} else {
											newKeywords = new ArrayList<String>(keywords);
										}
										dataCloudKWSStartTime = System.nanoTime();
										dataCloudHandler = new DataCloudDistinguishability(graph, nodeIdsOfToken,
												topNAnswers, 1, newKeywords);

										dataCloudHandler.expand();
										fullOutputsQueue.clear();
										if (dataCloudHandler.topFrequentKeywords.size() > 0) {

											if (qualityExp) {
												System.out.println("selected keyword: "
														+ dataCloudHandler.topFrequentKeywords.get(0) + " num of cand: "
														+ nodeIdsOfToken
																.get(StringPoolUtility.getIdOfStringFromPool(
																		dataCloudHandler.topFrequentKeywords.get(0)))
																.size());
											}

											newKeywords.add(dataCloudHandler.topFrequentKeywords.get(0));

											if (qualityExp)
												System.out.println("testing data cloud: " + new DateTime()
														+ " with keywords: "
														+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));

											preKWS(newKeywords, exp);
											run(newKeywords);
											if (timeOut) {
												continue;
											}
											dataCloudKWSDuration = ((System.nanoTime() - dataCloudKWSStartTime) / 1e6);

											for (ResultTree resultTree : fullOutputsQueue) {
												dataCloudKWSQuality += resultTree.cost;
											}

											if (debugMode && visualizeMode) {
												LinkedList<ResultTree> fullOutputsQueueTemp = new LinkedList<ResultTree>(
														fullOutputsQueue);
												Visualizer.visualizeOutput(fullOutputsQueueTemp, graph, null,
														berkeleyDB, newKeywords);
											}
										}

										if (fullOutputsQueue.size() < maxRequiredResult) {
											double addedValue = (double) rootKWSExpand.initialOveralWeight
													/ (double) maxRequiredResult;
											int lessResult = maxRequiredResult - fullOutputsQueue.size();

											dataCloudKWSQuality += lessResult * addedValue;
										}

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

								settingStr = "After All: discoveredAnswers:" + fullOutputsQueue.size() + " kwsTime: "
										+ initialKWSDuration;

								System.out.println("BANK: " + settingStr);

								ArrayList<InfoHolder> timeInfos = new ArrayList<InfoHolder>();
								timeInfos.add(new InfoHolder(0, "Setting", "DistinctRoot"));
								timeInfos.add(new InfoHolder(1, "keywords",
										DummyFunctions.getStringOutOfCollection(keywords, ";")));
								timeInfos.add(new InfoHolder(2, "Nodes", graph.nodeOfNodeId.size()));
								timeInfos.add(new InfoHolder(3, "Relationships", graph.relationOfRelId.size()));
								timeInfos.add(new InfoHolder(4, "num keywords", keywords.size()));
								timeInfos.add(new InfoHolder(5, "distance bound", distanceBound));
								timeInfos.add(new InfoHolder(6, "delta", delta));
								timeInfos.add(new InfoHolder(7, "n", maxRequiredResult));
								timeInfos.add(new InfoHolder(8, "discoveredAnswersKWS", discoveredAnswersKWS));
								timeInfos.add(new InfoHolder(9, "avg out deg cand1", avgoutdegcand1));
								timeInfos.add(new InfoHolder(10, "avg cands1", avgnumberofcands1));
								timeInfos.add(new InfoHolder(11, "initialOveralWeight",
										rootKWSExpand != null ? rootKWSExpand.initialOveralWeight : ""));
								timeInfos.add(new InfoHolder(12, "first KWS", initialKWSDuration));
								timeInfos.add(new InfoHolder(13, "qExpand time",
										rootKWSExpand != null ? rootKWSExpand.querySuggestionKWSDuration : ""));
								timeInfos.add(new InfoHolder(14, "qExp Vis. Nodes",
										rootKWSExpand != null ? rootKWSExpand.visitedNodes : ""));
								timeInfos.add(new InfoHolder(15, "qExp Vis. Kywrds",
										rootKWSExpand != null ? rootKWSExpand.visitedKeywords : ""));
								timeInfos.add(new InfoHolder(16, "qExp getKywrds Time",
										rootKWSExpand != null ? rootKWSExpand.getKeywordsDuration : ""));
								timeInfos.add(new InfoHolder(17, "sugg kywrds (freq pruned)", rootKWSExpand != null
										? rootKWSExpand.estimatedWeightOfSuggestedKeywordMap.size() : ""));
								timeInfos.add(new InfoHolder(18, "totalWeightOfSuggestedKeywords",
										rootKWSExpand != null ? rootKWSExpand.totalWeightOfSuggestedKeywords : ""));
								timeInfos.add(new InfoHolder(19, "avgWeightOfSuggestedKeyword",
										rootKWSExpand != null ? rootKWSExpand.avgQualityOfSuggestedKeyword : ""));

								timeInfos.add(new InfoHolder(20, "lowest weight kywrd",
										rootKWSExpand != null ? rootKWSExpand.lowestWeightOfSuggestedKeyword : ""));
								timeInfos.add(new InfoHolder(21, "lowest suggested weight", rootKWSExpand != null
										? StringPoolUtility.getStringOfId(rootKWSExpand.lowestWeightSuggestedKeywordId)
										: ""));
								timeInfos.add(new InfoHolder(22, "removed high freq of sugg",
										rootKWSExpand != null ? rootKWSExpand.highFrequentKeywordsRemovedNum : ""));

								timeInfos.add(new InfoHolder(23, "incEval Duration",
										incEval != null ? incEval.incEvalDuration : ""));
								timeInfos.add(new InfoHolder(24, "incEval visited nodes",
										incEval != null ? incEval.visitedNodes : ""));
								timeInfos.add(new InfoHolder(25, "incEval total cost",
										incEvalOn ? incEvalTotalCostPlusInitCost : ""));
								timeInfos.add(new InfoHolder(26, "new KWS time", newKWSOn ? secondaryKWSDuration : ""));
								timeInfos.add(
										new InfoHolder(27, "new KWS quality", newKWSOn ? secondaryKWSQuality : ""));
								timeInfos.add(new InfoHolder(28, "discoveredAnswersNewKWS",
										newKWSOn ? discoveredAnswersNewKWS : ""));
								timeInfos.add(new InfoHolder(29, "avg out deg cand2",
										newKWSOn ? avgOutDegreeOfKeywordCandidates : ""));

								timeInfos.add(new InfoHolder(30, "KWS time for coOcc",
										coOccBaseLineOn ? coOccKWSDuration : ""));
								timeInfos.add(new InfoHolder(31, "KWS quality for coOcc",
										coOccBaseLineOn ? coOccKWSQuality : ""));
								timeInfos.add(new InfoHolder(32, "discoveredAnswers co occ",
										coOccBaseLineOn ? discoveredAnswersCoOcc : ""));
								timeInfos.add(new InfoHolder(33, "coOccHandler",
										(coOccBaseLineOn && coOccHandler.topFrequentKeywords.size() > 0)
												? coOccHandler.topFrequentKeywords.get(0) : ""));

								timeInfos.add(new InfoHolder(34, "KWS time for dataCloud",
										dataCloudOn ? dataCloudKWSDuration : ""));
								timeInfos.add(new InfoHolder(35, "KWS quality for dataCloud",
										dataCloudOn ? dataCloudKWSQuality : ""));
								timeInfos.add(new InfoHolder(36, "discoveredAnswers dataCloud",
										dataCloudOn ? discoveredAnswersDataCloud : ""));
								timeInfos.add(new InfoHolder(37, "dataCloud Handler",
										(dataCloudOn && dataCloudHandler.topFrequentKeywords.size() > 0)
												? dataCloudHandler.topFrequentKeywords.get(0) : ""));

								timeInfos.add(
										new InfoHolder(38, "minSuggestedWeightMainDelta", minSuggestedWeightMainDelta));
								timeInfos.add(
										new InfoHolder(39, "avgSuggestedWeightMainDelta", avgSuggestedWeightMainDelta));
								timeInfos.add(
										new InfoHolder(40, "maxSuggestedWeightMainDelta", maxSuggestedWeightMainDelta));

								timeInfos.add(new InfoHolder(41, "minSuggestedWeightSecondDelta",
										minSuggestedWeightSecondDelta));
								timeInfos.add(new InfoHolder(42, "avgSuggestedWeightSecondDelta",
										avgSuggestedWeightSecondDelta));
								timeInfos.add(new InfoHolder(43, "maxSuggestedWeightSecondDelta",
										maxSuggestedWeightSecondDelta));

								timeInfos.add(new InfoHolder(44, "10 sugg kywrds", sugg));
								// timeInfos.add(new InfoHolder(32, "nodes in
								// Gr",
								// rootKWSExpand != null ? "" :
								// rootKWSExpand.G_r_Nodes.size()));
								// timeInfos.add(new InfoHolder(33, "edges in
								// Gr",
								// rootKWSExpand != null ? "" :
								// rootKWSExpand.G_r_Edges.size()));
								// timeInfos.add(new InfoHolder(34, "total
								// visited nodes in Gr",
								// rootKWSExpand != null ? "" :
								// rootKWSExpand.totalNumberOfNodesVisitedInGr));
								// timeInfos.add(new InfoHolder(35, "total
								// visited edges in Gr",
								// rootKWSExpand != null ? "" :
								// rootKWSExpand.totalNumberOfEdgesVisitedInGr));
								// timeInfos.add(new InfoHolder(36, "nodes in
								// GQ_DeltaGQ",
								// rootKWSExpand != null ? "" :
								// rootKWSExpand.Q_G_DELTA_Q_G_Nodes.size()));
								// timeInfos.add(new InfoHolder(37, "edges in
								// GQ_DeltaGQ",
								// rootKWSExpand != null ? "" :
								// rootKWSExpand.Q_G_DELTA_Q_G_Edges.size()));

								TimeLogger.LogTime("bankOutput.csv", true, timeInfos);

								exp++;
							}

							if (!hasEnoughAnswer) {
								System.out
										.println("not enough answer for keywords " + Arrays.toString(keywords.toArray())
												+ " the answer size is: " + fullOutputsQueue.size());
								continue;
							}
							if (!hasASuggestingKeyword) {
								System.out.println("no suggesting keyword within bound " + distanceBound + " , delta: "
										+ delta + " , keywords: " + Arrays.toString(keywords.toArray()) + " for top-"
										+ fullOutputsQueue.size() + " answers");
								continue;
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

	// public void runFromOutside(ArrayList<String> keywords, HashMap<String,
	// HashSet<Integer>> candidatesOfAKeyword,
	// int distanceBound, int maxRequiredResult, GraphInfraReaderArray graph,
	// int heapSize, boolean debugMode)
	// throws Exception {
	// // candidatesSet
	// candidatesSet = new HashSet<Integer>();
	// for (String keyword : candidatesOfAKeyword.keySet()) {
	// candidatesSet.addAll(candidatesOfAKeyword.get(keyword));
	// }
	//
	// this.candidatesOfAKeyword = candidatesOfAKeyword;
	//
	// this.distanceBound = distanceBound;
	// this.maxRequiredResult = maxRequiredResult;
	// this.graph = graph;
	// this.heapSize = heapSize;
	// this.debugMode = debugMode;
	// keywordSuggestionOn = false;
	//
	// totalPrintedResults = 0;
	// fullOutputsQueue = new LinkedList<ResultTree>();
	// printedResultTrees = new ArrayList<>();
	//
	// run(keywords);
	// if (timeOut) {
	// continue;
	// }
	//
	// }

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

	private void print(ResultTree resultTreeToBePrinted) throws Exception {

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

	// GraphInfraReaderArray graph, ArrayList<AnswerAsInput> topNAnswers, double
	// delta, int b,
	// BerkleleyDB berkeleyDB)
	public HashMap<Integer, CostAndNodesOfAnswersPair> keywordExpand(ArrayList<AnswerAsInput> topNAnswers,
			ArrayList<String> keywords) throws Exception {

		HashSet<Integer> keywordsSet = new HashSet<Integer>();

		// also keywords on the answer nodes????
		for (AnswerAsInput ai : topNAnswers) {
			for (int nodeId : ai.getContentNodes()) {
				keywordsSet.addAll(DummyFunctions.getKeywords(graph, nodeId));
			}
		}

		rootKWSExpand = new RootKWSExpand(graph, topNAnswers, delta, distanceBound, keywordsSet);
		// quality preservable keywords K ={(k′, w′), . . . }.
		HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap = rootKWSExpand.expand();
		// rootKWSExpand.updateNumberOfNodesAndEdgesInGr();
		return estimatedWeightOfSuggestedKeywordMap;
	}
}
