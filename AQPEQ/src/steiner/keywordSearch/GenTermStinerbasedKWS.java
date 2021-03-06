//TODO: maybe we need to take care of the case if in the merge or growth we encounter nodes matching the keywords.

package steiner.keywordSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.joda.time.DateTime;

import aqpeq.utilities.Dummy;
import aqpeq.utilities.InfoHolder;
import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import baselines.CoOccurrence;
import baselines.DataCloudDistinguishability;
import aqpeq.utilities.KWSUtilities;
import aqpeq.utilities.StringPoolUtility;
import aqpeq.utilities.TimeLogger;
import aqpeq.utilities.Visualizer;

import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;
import incrementalEvaluation.IncEval;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;
import queryExpansion.AnswerAsInput;
import queryExpansion.CostAndNodesOfAnswersPair;
import queryExpansion.DivQKWSExpand;
import queryExpansion.IndexGenTemKWSExpand;
import queryExpansion.SteinerKWSExpansion;
import tryingToTranslate.PrunedLandmarkLabeling;

public class GenTermStinerbasedKWS {

	GraphInfraReaderArray graph;
	IndexGenTemKWSExpand streamDivQExpand;
	DivQKWSExpand divQExpand;

	// xin
	// private static String graphInfraPath =
	// "/Users/zhangxin/Desktop/Summer/synthetic-100-nodes/";
	// "/Users/zhangxin/Desktop/Summer/synthetic-100-nodes/keywords.txt";

	// mhn
	private static String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/graph/";
	private static String keywordsPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/queries/l3/imdb_query_S.txt";
	private static String indexPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/graph/imdb_8bits.jin";

	static int[] maxRequiredResults = { 3, 5, 10 };
	private static int[] distanceBounds = { 2, 3 };
	private static double[] deltas = { 2 };//// quality bound
	private static int[] keywordSizes = { 2, 3 };
	private static int[] numberOfQueries = { 3, 5, 8 };
	private static double[] lambdas = { 0.5d };
	private static double[] epsilons = { 0.1d, 0.2d, 0.3d };
	private static int[] maxTokensForNode = { 30 };

	int defaultSizeOfQueue = 10;

	int maxRequiredResult;
	private int distanceBound;
	private double delta;//// quality bound

	HashMap<String, HashSet<Integer>> candidatesOfAKeyword = new HashMap<String, HashSet<Integer>>();
	HashSet<Integer> candidatesSet = new HashSet<Integer>();
	HashMap<Integer, HashSet<Integer>> nodeIdsOfToken = new HashMap<Integer, HashSet<Integer>>();

	// BDB
	private static boolean usingBDB = false;

	private static String database = "database";
	private static String catDatabase = "catDatabase";

	// experiments
	private static int numberOfSameExperiments = 1;

	// key is root and p
	HashMap<String, SteinerTree> treeOfRootedAtAndContains = new HashMap<String, SteinerTree>();

	// key is root
	HashMap<Integer, HashSet<SteinerTree>> treesOfRootedAt = new HashMap<Integer, HashSet<SteinerTree>>();

	int printRecursiveCallDebug = 0;
	int treeIndex = 0;

	ArrayList<ListenableUndirectedGraph<ResultNode, RelationshipInfra>> printedResultTrees = new ArrayList<ListenableUndirectedGraph<ResultNode, RelationshipInfra>>();

	SteinerKWSExpansion steinerKWSExpand;

	private double avgNumberOfCandidates;
	private double avgOutDegreeOfKeywordCandidates;

	private double initialKWSStartTime = 0d;
	private double initialKWSDuration = 0d;

	private static boolean debugMode = true;
	private static boolean visualizeMode = true;

	// steps turning on/off
	static boolean keywordSuggestionOn = true;
	// if don't want just change to false
	static boolean incEvalOn = keywordSuggestionOn && false;
	static boolean newKWSOn = keywordSuggestionOn && false;
	static boolean topkSelectionOn = keywordSuggestionOn && false;
	static boolean coOccBaseLineOn = keywordSuggestionOn && false;
	static boolean dataCloudOn = keywordSuggestionOn && false;

	private static boolean withProperties = true;

	static LinkedList<ResultTree> fullOutputsQueue = new LinkedList<ResultTree>();

	static boolean qualityExp = false;
	private static PrunedLandmarkLabeling pl;

	LinkedHashSet<String> ourKeywords = new LinkedHashSet<String>();
	LinkedHashSet<String> coOccKeywords = new LinkedHashSet<String>();
	LinkedHashSet<String> dataCloudKeywords = new LinkedHashSet<String>();

	boolean timeOut = false;
	double maximumTimeBound = 1000000;

	public GenTermStinerbasedKWS() {

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
		DummyProperties.debugMode = debugMode;
		DummyProperties.withProperties = withProperties;
		GenTermStinerbasedKWS stinerbasedKWS = new GenTermStinerbasedKWS();
		stinerbasedKWS.runOptimalSteinerExperiment();

	}

	private void runOptimalSteinerExperiment() throws Exception {

		boolean addBackward = true;

		graph = new GraphInfraReaderArray(graphInfraPath, addBackward);
		// if (!usingBDB) {
		graph.read();
		nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);

		int higherWeight = 0;
		for (RelationshipInfra rel : graph.relationOfRelId) {
			if (rel.weight > 1) {
				higherWeight++;

			}

			rel.weight = 1.0f;
		}
		System.out.println("higherWeight:" + higherWeight);

		// } else {
		// graph.readWithNoLabels();
		// berkeleyDB = new BerkleleyDB(database, catDatabase, envFilePath);
		// }

		if (debugMode)
			System.out.println("finish read");

		// read all initial queries
		// read all initial queries
		pl.LoadIndex(indexPath);

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

	private void runBasedOnParam(ArrayList<ArrayList<String>> keywordsSet, int indexOfKeywords, int distanceBound,
			double epsilon, int maxRequiredResult, int numberOfQuery, int mTokenOfNode, double lambda)
			throws Exception {

		this.maxRequiredResult = maxRequiredResult;
		this.distanceBound = distanceBound;

		LinkedHashSet<String> keywords = new LinkedHashSet<String>(keywordsSet.get(indexOfKeywords));

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

			String settingStr = keywords.size() + " keywords is "
					+ DummyFunctions.getStringOutOfCollection(keywords, ";") + ", n:" + maxRequiredResult
					+ ", distance:" + distanceBound + ", delta:" + delta;

			System.out.println("STEINER: " + settingStr);

			initialKWSStartTime = System.nanoTime();

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
					System.out.println("time out occurred in our keywords");
					continue;
				}
			} else {

				settingStr = keywords.size() + " keywords is " + DummyFunctions.getStringOutOfCollection(keywords, ";")
						+ ", n:" + maxRequiredResult + ", distance:" + distanceBound + ", delta:" + delta;

				preKWS(keywords, exp);
				run(keywords);
				if (timeOut) {
					System.out.println("time out occurred in keywords");
					continue;
				}
			}

			System.out.println("STEINER: " + settingStr);

			if (fullOutputsQueue.size() < maxRequiredResult) {
				hasEnoughAnswer = false;
				break;
			}

			if (debugMode && visualizeMode) {
				LinkedList<ResultTree> fullOutputsQueueTemp = new LinkedList<ResultTree>(fullOutputsQueue);
				Visualizer.visualizeOutput(fullOutputsQueueTemp, graph, null, keywords);
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

			// initialize the class steinerKWSExpand
			// call expand
			ArrayList<AnswerAsInput> topNAnswers = new ArrayList<AnswerAsInput>();
			if (topNAnswersTemp.size() > maxRequiredResult) {
				topNAnswers.addAll(topNAnswersTemp.subList(0, maxRequiredResult));
			} else {
				topNAnswers.addAll(topNAnswersTemp);
			}

			HashMap<Integer, Double> termDistance = null;
			CoOccurrence coOccHandler = null;
			DataCloudDistinguishability dataCloudHandler = null;

			double incEvalTotalCostPlusInitCost = 0d;
			IncEval incEval = null;

			if (keywordSuggestionOn) {

				if (qualityExp)
					System.out.println("starting our keyword suggestion " + new DateTime() + " for keywords "
							+ DummyFunctions.getStringOutOfCollection(keywords, ";"));

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

				if (coOccBaseLineOn) {

					int tempMaxReqResults = this.maxRequiredResult;
					this.maxRequiredResult = 10;

					if (qualityExp)
						System.out.println("starting coOccBaseLine: " + new DateTime() + " with keywords: "
								+ DummyFunctions.getStringOutOfCollection(coOccKeywords, ";"));

					preKWS(coOccKeywords, exp);
					run(coOccKeywords);
					if (timeOut) {
						continue;
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

					HashSet<String> newKeywords;

					if (qualityExp) {
						newKeywords = new HashSet<String>(coOccKeywords);
					} else {
						newKeywords = new HashSet<String>(keywords);
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

					if (coOccHandler.topFrequentKeywords.size() > 0) {

						if (qualityExp) {
							System.out.println("selected keyword: " + coOccHandler.topFrequentKeywords.get(0)
									+ " num of cand: " + nodeIdsOfToken.get(StringPoolUtility
											.getIdOfStringFromPool(coOccHandler.topFrequentKeywords.get(0))));
						}

						newKeywords.add(coOccHandler.topFrequentKeywords.get(0));
						preKWS(newKeywords, exp);
						run(newKeywords);
						if (timeOut) {
							System.out.println("time out occurred at coOcc");
							continue;
						}
						coOccKWSDuration = ((System.nanoTime() - coOccKWSStartTime) / 1e6);

						for (ResultTree resultTree : fullOutputsQueue) {
							coOccKWSQuality += resultTree.cost;
						}
					}

					if (fullOutputsQueue.size() < maxRequiredResult) {
						double addedValue = (double) steinerKWSExpand.initialOveralWeight / (double) maxRequiredResult;
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
						System.out.println("starting data cloud: " + new DateTime() + " with keywords: "
								+ DummyFunctions.getStringOutOfCollection(dataCloudKeywords, ";"));

					HashSet<String> newKeywords;
					if (qualityExp) {
						newKeywords = new HashSet<String>(dataCloudKeywords);
					} else {
						newKeywords = new HashSet<String>(keywords);
					}
					dataCloudKWSStartTime = System.nanoTime();

					if (qualityExp) {
						dataCloudHandler = new DataCloudDistinguishability(graph, nodeIdsOfToken, topNAnswers, 1,
								dataCloudKeywords);
					} else {
						dataCloudHandler = new DataCloudDistinguishability(graph, nodeIdsOfToken, topNAnswers, 1,
								newKeywords);
					}

					dataCloudHandler.expand();
					fullOutputsQueue.clear();
					if (dataCloudHandler.topFrequentKeywords.size() > 0) {

						if (qualityExp) {
							System.out.println("selected keyword: " + dataCloudHandler.topFrequentKeywords.get(0)
									+ " num of cand: " + nodeIdsOfToken.get(StringPoolUtility
											.getIdOfStringFromPool(dataCloudHandler.topFrequentKeywords.get(0))));
						}

						newKeywords.add(dataCloudHandler.topFrequentKeywords.get(0));

						if (qualityExp)
							System.out.println("testing data cloud: " + new DateTime() + " with keywords: "
									+ DummyFunctions.getStringOutOfCollection(newKeywords, ";"));

						preKWS(newKeywords, exp);
						run(newKeywords);
						if (timeOut) {
							System.out.println("time out occurred at dataCloud");
							continue;
						}
						dataCloudKWSDuration = ((System.nanoTime() - dataCloudKWSStartTime) / 1e6);

						for (ResultTree resultTree : fullOutputsQueue) {
							dataCloudKWSQuality += resultTree.cost;
						}
					}

					if (fullOutputsQueue.size() < maxRequiredResult) {
						double addedValue = (double) steinerKWSExpand.initialOveralWeight / (double) maxRequiredResult;
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
			}

			settingStr = "After: discoveredAnswers:" + fullOutputsQueue.size() + " kwsTime: " + initialKWSDuration;

			System.out.println("STEINTER: " + settingStr);

			ArrayList<InfoHolder> timeInfos = new ArrayList<InfoHolder>();
			timeInfos.add(new InfoHolder(0, "Setting", "Steiner"));
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

			TimeLogger.LogTime("steinerOutput.csv", true, timeInfos);

			exp++;
		}

		if (!hasEnoughAnswer) {
			System.out.println("not enough answer for keywords " + Arrays.toString(keywords.toArray())
					+ " the answer size is: " + fullOutputsQueue.size());
			return;
		}
		if (!hasASuggestingKeyword) {
			System.out.println(
					"no suggesting keyword withing bound " + distanceBound + " , delta: " + delta + " , keywords: "
							+ Arrays.toString(keywords.toArray()) + " for top-" + fullOutputsQueue.size() + " answers");
			return;
		}

	}

	private void run(HashSet<String> keywords) throws Exception {

		timeOut = false;
		double startingTimeOfAlg = System.nanoTime();

		// Let Q_T be a priority queue sorted in the increasing order of costs
		// of trees;
		// Q_T ← ∅;
		PriorityQueue<SteinerTree> treesQueue = new PriorityQueue<SteinerTree>(defaultSizeOfQueue,
				new Comparator<SteinerTree>() {
					@Override
					public int compare(SteinerTree o1, SteinerTree o2) {
						return Double.compare(o1.cost, o2.cost);
					}
				});

		// for each v ∈ V (G) do
		// if v contains keywords p then
		// enqueue T(v,p) into Q_T ;
		for (String pi : candidatesOfAKeyword.keySet()) {
			for (Integer nodeId : candidatesOfAKeyword.get(pi)) {

				SteinerTree initialTree = getInitialTreeRootedAt(nodeId);
				if (initialTree == null) {
					initialTree = new InitialSteinerTree(nodeId, pi);
					initialTree.treeIndex = treeIndex++;

					treesOfRootedAt.putIfAbsent(nodeId, new HashSet<>());
					treesOfRootedAt.get(nodeId).add(initialTree);

					HashSet<String> pSet = new HashSet<String>();
					pSet.add(pi);
					treeOfRootedAtAndContains.put(DummyFunctions.getKeyForIDAndHashSet(nodeId, pSet), initialTree);

					treesQueue.add(initialTree);

				} else {
					initialTree.addRelatedKeyword(pi);
				}
			}
		}

		int i = 0;
		double soFarTime = 0d;

		// while QT != ∅ do
		while (!treesQueue.isEmpty()) {

			soFarTime = ((System.nanoTime() - startingTimeOfAlg) / 1e6);

			if (soFarTime > maximumTimeBound) {
				timeOut = true;
				return;
			}

			// System.out.println(Math.random() + " treesQueue size: " +
			// treesQueue.size());

			// dequeue QT to T(v,p);
			SteinerTree currentBestTree = treesQueue.poll();

			int v = currentBestTree.rootNodeId;
			HashSet<String> p = currentBestTree.p;

			// if p = P then
			if (DummyFunctions.areTwoSetsEqual(p, keywords)) {
				// output T (v, p); i ← i + 1;
				print(currentBestTree);
				i++;

				// terminate if i = k;
				if (i == maxRequiredResult)
					break;
			}

			// for each u ∈ N(v) do
			for (int u : graph.nodeOfNodeId.get(v).outgoingRelIdOfSourceNodeId.keySet()) {

				// if T(v,p) ⊕ (v, u) < T(u, p) then
				SteinerTree grownTree = treeGrowth(currentBestTree, u);
				SteinerTree treeRootedAtUContainP = getTreeRootedAtContain(u, p);

				if (treeRootedAtUContainP == null || grownTree.cost < treeRootedAtUContainP.cost) {
					// T(u, p) ← T(v, p) ⊕ (v, u);
					// update QT with the new T(u,p);
					setTreeRootedAtContain(u, p, grownTree, treesQueue);
				}

			}

			// p1 ← p;
			HashSet<String> p1 = currentBestTree.p;

			HashSet<SteinerTree> treesRootedAtVSet = new HashSet<SteinerTree>(getTreesRootedAt(v));

			// for each p2 s.t. p1 ∩ p2 = ∅ do
			for (SteinerTree treeP2 : treesRootedAtVSet) {

				if (currentBestTree == treeP2)
					continue;

				// there is some elements in common.
				if (!Collections.disjoint(p1, treeP2.p))
					continue;

				// if T(v,p1) ⊕ T(v,p2) < T(v,p1 ∪ p2) then
				SteinerTree mergedTree = treeMerge(v, currentBestTree, treeP2);

				HashSet<String> unionOfP1AndP2 = new HashSet<String>(p1);
				unionOfP1AndP2.addAll(treeP2.p);

				SteinerTree treeRootedAtUContainP = getTreeRootedAtContain(v, unionOfP1AndP2);
				if (treeRootedAtUContainP == null || mergedTree.cost < treeRootedAtUContainP.cost) {
					// T(v,p1 ∪ p2) ← T(v,p1) ⊕ T(v, p2);
					// update QT with the new T(v,p1 ∪ p2);
					setTreeRootedAtContain(v, unionOfP1AndP2, mergedTree, treesQueue);
				}
			}
		}

	}

	private SteinerTree getInitialTreeRootedAt(int nodeId) throws Exception {
		if (!treesOfRootedAt.containsKey(nodeId))
			return null;

		if (treesOfRootedAt.get(nodeId).size() > 1) {
			throw new Exception(treesOfRootedAt.get(nodeId).size() + " initial trees rooted at " + nodeId);
		}

		return treesOfRootedAt.get(nodeId).iterator().next();
	}

	private void setTreeRootedAtContain(int u, HashSet<String> p, SteinerTree newTree,
			PriorityQueue<SteinerTree> treesQueue) {

		String key = DummyFunctions.getKeyForIDAndHashSet(u, p);
		SteinerTree oldTree;

		if (treeOfRootedAtAndContains.containsKey(key)) {
			oldTree = treeOfRootedAtAndContains.get(key);

			// remove from set of treesOfRootedAt
			treesOfRootedAt.get(u).remove(oldTree);

			// remove from queue
			treesQueue.remove(oldTree); // O(size of PQ to find it forst + log
										// size of PQ to update it!)

		}

		// TODO: check max queue size:
		if (treesQueue.size() > DummyProperties.MaxNumberOfVisitedNodes)
			return;

		// use the newTree
		treesQueue.add(newTree);
		treeOfRootedAtAndContains.put(key, newTree);
		treesOfRootedAt.putIfAbsent(u, new HashSet<SteinerTree>());
		treesOfRootedAt.get(u).add(newTree);

	}

	private SteinerTree treeGrowth(SteinerTree currentBestTree, int targetNodeId) {
		// currentBestTree + rel
		RelationshipInfra relInfra = graph.relationOfRelId
				.get(graph.nodeOfNodeId.get(currentBestTree.rootNodeId).outgoingRelIdOfSourceNodeId.get(targetNodeId));
		double cost = currentBestTree.cost + relInfra.weight;
		SteinerTree grownTree = new GrownSteinerTree(targetNodeId, currentBestTree, relInfra, cost,
				currentBestTree.rootNodeId);
		grownTree.treeIndex = treeIndex++;

		return grownTree;
	}

	private SteinerTree treeMerge(int v, SteinerTree currentBestTree, SteinerTree treeP2) {
		// is it possible that we cannot do cost1+cost2? are the trees are
		// edge-disjoint too?
		double cost = currentBestTree.cost + treeP2.cost;
		SteinerTree mergedTree = new MergedSteinerTree(v, currentBestTree, treeP2, cost);
		mergedTree.treeIndex = treeIndex++;

		// if(mergedTree.treeIndex == 39){
		// System.out.println();
		// }

		return mergedTree;
	}

	private HashSet<SteinerTree> getTreesRootedAt(int rootNodeId) {
		return treesOfRootedAt.get(rootNodeId);
	}

	private SteinerTree getTreeRootedAtContain(int v, HashSet<String> unionOfP1AndP2) {

		String result = DummyFunctions.getKeyForIDAndHashSet(v, unionOfP1AndP2);

		return treeOfRootedAtAndContains.get(result);
	}

	private void print(SteinerTree currentBestTree) throws Exception {

		ListenableUndirectedGraph<ResultNode, RelationshipInfra> anOutputTree = new ListenableUndirectedGraph<ResultNode, RelationshipInfra>(
				RelationshipInfra.class);

		HashMap<Integer, ResultNode> resultNodeOfNodeId = new HashMap<Integer, ResultNode>();

		getReconstructedTree(currentBestTree, anOutputTree,
				resultNodeOfNodeId /* , printRecursiveCallDebug++ + "_print" */);

		ResultTree resultTree = new ResultTree();
		resultTree.anOutputTree = anOutputTree;
		resultTree.cost = currentBestTree.cost;
		resultTree.rootNode = resultNodeOfNodeId.get(currentBestTree.rootNodeId);
		fullOutputsQueue.add(resultTree);

		print(resultTree);

	}

	private void print(ResultTree resultTree) {

		System.out.println("OUTPUT TREE: cost: " + resultTree.cost);

		System.out.println("Vertices");
		for (ResultNode resNod : resultTree.anOutputTree.vertexSet()) {
			System.out.println(resNod.nodeId + ", label: " + graph.nodeOfNodeId.get(resNod.nodeId).getTokens());
		}

		System.out.println("Edges");
		for (RelationshipInfra e : resultTree.anOutputTree.edgeSet()) {
			System.out.println(
					resultTree.anOutputTree.getEdgeSource(e) + " -> " + resultTree.anOutputTree.getEdgeTarget(e));
			int sourceID = resultTree.anOutputTree.getEdgeSource(e).nodeId;
			int targetID = resultTree.anOutputTree.getEdgeTarget(e).nodeId;
			System.out.println(
					"relationship: " + Dummy.DummyFunctions.getRelationshipOfPairNodes(graph, sourceID, targetID));
		}
	}

	private ListenableUndirectedGraph<ResultNode, RelationshipInfra> growAConcreteTreesWithEdge(
			ListenableUndirectedGraph<ResultNode, RelationshipInfra> tree1, RelationshipInfra rel, int targetNodeId,
			int rootNodeId, HashMap<Integer, ResultNode> resultNodeOfNodeId) throws Exception {

		ResultNode newNode = getOrInitAResultNode(resultNodeOfNodeId, targetNodeId);
		tree1.addVertex(newNode);
		tree1.addEdge(resultNodeOfNodeId.get(targetNodeId), resultNodeOfNodeId.get(rootNodeId), rel);

		return tree1;

	}

	private ListenableUndirectedGraph<ResultNode, RelationshipInfra> getReconstructedTree(SteinerTree tree,
			ListenableUndirectedGraph<ResultNode, RelationshipInfra> anOutputTree,
			HashMap<Integer, ResultNode> resultNodeOfNodeId/*
															 * , String
															 * iterAndCaller
															 */) throws Exception {

		// System.out.println(iterAndCaller + " tree: " + tree);

		if (tree instanceof InitialSteinerTree) {
			ResultNode newNode = getOrInitAResultNode(resultNodeOfNodeId, ((InitialSteinerTree) tree).rootNodeId);
			anOutputTree.addVertex(newNode);
			return anOutputTree;
		}

		else if (tree instanceof MergedSteinerTree) {

			MergedSteinerTree mergedTree = (MergedSteinerTree) tree;

			getReconstructedTree(mergedTree.t1, anOutputTree,
					resultNodeOfNodeId/*
										 * , printRecursiveCallDebug++ +
										 * "_merge_t1"
										 */);

			getReconstructedTree(mergedTree.t2, anOutputTree,
					resultNodeOfNodeId/*
										 * , printRecursiveCallDebug++ +
										 * "_merge_t2"
										 */);

			return anOutputTree;

		} else if (tree instanceof GrownSteinerTree) {

			GrownSteinerTree grownTree = (GrownSteinerTree) tree;

			ListenableUndirectedGraph<ResultNode, RelationshipInfra> outputTree1 = getReconstructedTree(grownTree.t1,
					anOutputTree,
					resultNodeOfNodeId /*
										 * , printRecursiveCallDebug ++ +
										 * "_growth"
										 */);

			return growAConcreteTreesWithEdge(outputTree1, grownTree.growingEdge, grownTree.targetNodeId,
					grownTree.sourceNodeId, resultNodeOfNodeId);
		}

		return anOutputTree;

	}

//	public void findCandidatesOfKeywordsUsingBDB(HashSet<String> keywords, BerkleleyDB berkeleyDB) throws Exception {
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

	public void findCandidatesOfKeywordsUsingInvertedList(HashSet<String> keywords) throws Exception {

		System.out.println("nodeIdsOfToken: " + nodeIdsOfToken.size());
		for (String keyword : keywords) {
			HashSet<Integer> candidate = new HashSet<Integer>();
			for (String token : Dummy.DummyFunctions.getTokens(keyword)) {
				if (nodeIdsOfToken.containsKey(token)) {
					HashSet<Integer> candidateTem = new HashSet<Integer>();
					candidateTem.addAll(nodeIdsOfToken.get(token));
					if (candidate.isEmpty()) {
						candidate = candidateTem;
					} else {
						candidate.retainAll(candidateTem);
					}
				}
			}
			candidatesOfAKeyword.put(keyword, candidate);
			candidatesSet.addAll(candidatesOfAKeyword.get(keyword));
		}

		// debug
		for (String keyword : candidatesOfAKeyword.keySet()) {
			System.out.println("keyword: " + keyword + ", candidate set: " + candidatesOfAKeyword.get(keyword));
		}
	}

	private ResultNode getOrInitAResultNode(HashMap<Integer, ResultNode> resultNodeOfNodeId, int nodeId)
			throws Exception {
		if (resultNodeOfNodeId.containsKey(nodeId))
			return resultNodeOfNodeId.get(nodeId);

		NodeInfra nodeInfra = graph.nodeOfNodeId.get(nodeId);

		String lbls = "";
		for (int tokenId : nodeInfra.getTokens()) {
			lbls += StringPoolUtility.getStringOfId(tokenId);
		}
		// String lbls = String.join(",", );
		ResultNode newNode = new ResultNode(lbls, nodeId, nodeInfra);
		resultNodeOfNodeId.put(nodeId, newNode);
		return newNode;
	}

	private void preKWS(HashSet<String> keywords, int exp) throws Exception {

		treeOfRootedAtAndContains = null;
		treesOfRootedAt = null;
		printRecursiveCallDebug = 0;
		treeIndex = 0;

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
		// } else {
		KWSUtilities.findCandidatesOfKeywordsUsingInvertedList(keywords, nodeIdsOfToken, candidatesOfAKeyword,
				candidatesSet);
		// }

		avgOutDegreeOfKeywordCandidates = DummyFunctions.getAvgOutDegreesOfASet(graph, candidatesSet);
		avgNumberOfCandidates = (double) candidatesSet.size() / (double) keywords.size();

		fullOutputsQueue = new LinkedList<ResultTree>();

		printedResultTrees = new ArrayList<>();

		treeOfRootedAtAndContains = new HashMap<>();
		treesOfRootedAt = new HashMap<>();

	}

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

		DummyProperties.debugMode = debugMode;
		keywordSuggestionOn = false;

		fullOutputsQueue = new LinkedList<ResultTree>();
		printedResultTrees = new ArrayList<>();

		treeOfRootedAtAndContains = new HashMap<>();
		treesOfRootedAt = new HashMap<>();

		HashSet<String> keywordsSet = new HashSet<String>(keywords);
		run(keywordsSet);
		// if (timeOut) {
		// continue;
		// }

	}

	public HashMap<Integer, Double> keywordExpandDivQ(ArrayList<AnswerAsInput> topNAnswers,
			LinkedHashSet<String> keywords, double epsilon, double lambda, int mTokenOfNode, int numberOfQuery)
			throws Exception {

		HashSet<Integer> keywordsSet = new HashSet<Integer>();

		for (String keyword : keywords) {
			keywordsSet.add(StringPoolUtility.getIdOfStringFromPool(keyword));
		}

		divQExpand = new DivQKWSExpand(graph, keywordsSet, topNAnswers, distanceBound, epsilon, lambda, numberOfQuery,
				nodeIdsOfToken, mTokenOfNode);

		divQExpand.expand();
		HashMap<Integer, Double> distanceOfTerms = divQExpand.termDistance;

		return distanceOfTerms;
	}

	public HashMap<Integer, Double> keywordExpandStreamQ(ArrayList<AnswerAsInput> topNAnswers,
			LinkedHashSet<String> keywords, double epsilon, double lambda, int mTokenOfNode, int numberOfQuery)
			throws Exception {

		HashSet<Integer> keywordsSet = new HashSet<Integer>();

		for (String keyword : keywords) {
			keywordsSet.add(StringPoolUtility.getIdOfStringFromPool(keyword));
		}

		streamDivQExpand = new IndexGenTemKWSExpand(graph, keywordsSet, topNAnswers, keywords.size(), distanceBound,
				epsilon, lambda, pl, numberOfQuery, nodeIdsOfToken, mTokenOfNode);
		streamDivQExpand.expand();
		HashMap<Integer, Double> distanceOfTerms = streamDivQExpand.termDistance;

		return distanceOfTerms;
	}

}
