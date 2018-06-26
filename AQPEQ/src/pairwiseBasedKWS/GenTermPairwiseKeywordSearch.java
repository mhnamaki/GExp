package pairwiseBasedKWS;

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

import com.couchbase.client.deps.io.netty.handler.codec.http.HttpContentEncoder.Result;

import aqpeq.utilities.InfoHolder;
import aqpeq.utilities.KWSUtilities;
import aqpeq.utilities.StringPoolUtility;
import aqpeq.utilities.TimeLogger;
import aqpeq.utilities.Visualizer;
import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import bank.keywordSearch.DistinctRootExperiment;
import baselines.CoOccurrence;
import baselines.DataCloudDistinguishability;
import graphInfra.GraphInfraReaderArray;
import graphInfra.RelationshipInfra;
import incrementalEvaluation.IncEval;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;
import queryExpansion.AnswerAsInput;
import queryExpansion.CostAndNodesOfAnswersPair;
import queryExpansion.DivQKWSExpand;
import queryExpansion.GraphKWSExpand;
import queryExpansion.IndexGenTemKWSExpand;
import queryExpansion.ObjectiveHandler;
import queryExpansion.SteinerKWSExpansion;
import steiner.keywordSearch.StinerbasedKWS;
import tryingToTranslate.PrunedLandmarkLabeling;

public class GenTermPairwiseKeywordSearch {
	GraphInfraReaderArray graph;

	// mhn
	// private static String graphInfraPath =
	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/graph/";
	// private static String keywordsPath =
	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/queries/imdb_query_DR.txt";
	// private static String envFilePath =
	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/bdb/withProp/dbEnvWithProp";
	// private String distanceIndexDB =
	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/graph/imdb_8bits.jin";

	private static String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/graph/";
	private static String keywordsPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/queries/l3/imdb_query_PW.txt";
	private String distanceIndexDB = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/graph/imdb_8bits.jin";

	// private static String graphInfraPath =
	// "/Users/mnamaki/AQPEQ/GraphExamples/subgraphTest/graph/";
	// private static String keywordsPath =
	// "/Users/mnamaki/AQPEQ/GraphExamples/subgraphTest/graph/keywords.in";
	// private static String envFilePath =
	// "/Users/mnamaki/AQPEQ/GraphExamples/subgraphTest/graph/";
	// private String distanceIndexDB =
	// "/Users/mnamaki/AQPEQ/GraphExamples/subgraphTest/graph/subgraphTest_8bits.jin";

	private int distanceIndexDBBitNum = 8;

	static int[] maxRequiredResults = { 3, 5, 10 };
	private static int[] distanceBounds = { 2, 3 };
	private static double[] deltas = { 2 };//// quality bound
	private static int[] keywordSizes = { 2, 3 };
	private static int[] numberOfQueries = { 3, 5, 8 };
	private static double[] lambdas = { 0.5d };
	private static double[] epsilons = { 0.1d, 0.2d, 0.3d };
	private static int[] maxTokensForNode = { 30 };

	int maxRequiredResult;
	private int distanceBound;
	private double delta;//// quality bound

	private PrunedLandmarkLabeling prunedLandmarkLabeling;

	private int fixedmaxRequiredResult;

	static LinkedList<PairwiseAnswer> fullOutputsQueue = new LinkedList<PairwiseAnswer>();

	// from bank
	static HashMap<String, HashSet<Integer>> candidatesOfAKeyword = new HashMap<String, HashSet<Integer>>();
	static HashSet<Integer> candidatesSet = new HashSet<Integer>();
	static HashMap<Integer, HashSet<Integer>> nodeIdsOfToken = new HashMap<Integer, HashSet<Integer>>();

	GraphKWSExpand graphKWSExpand;

	private double avgNumberOfCandidates;
	private double avgOutDegreeOfKeywordCandidates;

	private double initialKWSStartTime = 0d;
	private double initialKWSDuration = 0d;


	private static boolean withProperties = true;

	// experiments
	private static int numberOfSameExperiments = 1;

	private static boolean debugMode = false;
	private static boolean visualizeMode = false;

	// steps turning on/off
	boolean keywordSuggestionOn = true;
	// if don't want just change to false
	boolean incEvalOn = keywordSuggestionOn && false;
	boolean newKWSOn = keywordSuggestionOn && false;
	boolean topkSelectionOn = keywordSuggestionOn && false;
	boolean coOccBaseLineOn = keywordSuggestionOn && true;
	boolean dataCloudOn = keywordSuggestionOn && true;

	static boolean qualityExp = false;
	ArrayList<String> ourKeywords = new ArrayList<String>();
	ArrayList<String> coOccKeywords = new ArrayList<String>();
	ArrayList<String> dataCloudKeywords = new ArrayList<String>();

	IndexGenTemKWSExpand streamDivQExpand;
	DivQKWSExpand divQExpand;

	public static void main(String[] args) throws Exception {

		GenTermPairwiseKeywordSearch kws = new GenTermPairwiseKeywordSearch();
		kws.runMain(args);

	}

	private void runMain(String[] args) throws Exception {

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
			} else if (args[i].equals("-distanceIndexDB")) {
				distanceIndexDB = args[++i];
			} else if (args[i].equals("-distanceIndexDBBitNum")) {
				distanceIndexDBBitNum = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-withProperties")) {
				withProperties = Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-deltas")) {
				deltas = DummyFunctions.getArrOutOfCSV(deltas, args[++i]);
			} else if (args[i].equals("-keywordSizes")) {
				keywordSizes = DummyFunctions.getArrOutOfCSV(keywordSizes, args[++i]);
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
			} else if (args[i].equals("-maxTokensForNode")) {
				maxTokensForNode = DummyFunctions.getArrOutOfCSV(maxTokensForNode, args[++i]);
			}

		}

		DummyProperties.withProperties = withProperties;
		DummyProperties.debugMode = debugMode;
		prunedLandmarkLabeling = new PrunedLandmarkLabeling(distanceIndexDBBitNum, distanceIndexDB);
		runRCliqueExperiment();
	}

	private void runRCliqueExperiment() throws Exception {

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

	public void runBasedOnParam(ArrayList<ArrayList<String>> keywordsSet, int indexOfKeyword, int distanceBound,
			double epsilon, int maxRequiredResult, int numberOfQuery, int mTokenOfNode, double lambda)
			throws Exception {

		this.maxRequiredResult = maxRequiredResult;
		fixedmaxRequiredResult = maxRequiredResult;
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

			String settingStr = keywords.size() + " keywords is "
					+ DummyFunctions.getStringOutOfCollection(keywords, ";") + ", n:" + maxRequiredResult
					+ ", distance:" + distanceBound + ", delta:" + delta;

			System.out.println("SUBGRAPH: " + settingStr);

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
			} else {

				settingStr = keywords.size() + " keywords is " + DummyFunctions.getStringOutOfCollection(keywords, ";")
						+ ", n:" + maxRequiredResult + ", distance:" + distanceBound + ", delta:" + delta;

				preKWS(keywords, exp);
				run(keywords);
			}

			if (fullOutputsQueue.size() < maxRequiredResult) {
				hasEnoughAnswer = false;
				break;
			}

			if (debugMode && visualizeMode) {
				LinkedList<ResultTree> fullOutputsQueueTemp = new LinkedList<ResultTree>();

				for (PairwiseAnswer rClique : fullOutputsQueue) {
					fullOutputsQueueTemp.add(rClique.resultTree);
				}

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
			ArrayList<AnswerAsInput> topNAnswersTemp = tansformPairwiseAnswerIntoAnserAsInput();

			// initialize the class graphKWSExpand
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

				// if (coOccBaseLineOn) {
				//
				// int tempMaxReqResults = this.fixedmaxRequiredResult;
				// this.maxRequiredResult = 20;
				// this.fixedmaxRequiredResult = this.maxRequiredResult;
				//
				// if (qualityExp)
				// System.out.println("starting coOccBaseLine: " + new
				// DateTime() + " with keywords: "
				// + DummyFunctions.getStringOutOfCollection(coOccKeywords,
				// ";"));
				//
				// preKWS(coOccKeywords, exp);
				// run(coOccKeywords);
				//
				// topNAnswersTemp = tansformPairwiseAnswerIntoAnserAsInput();
				//
				// // initialize the class graphKWSExpand
				// // call expand
				// topNAnswers = new ArrayList<AnswerAsInput>();
				// if (topNAnswersTemp.size() > this.fixedmaxRequiredResult) {
				// topNAnswers.addAll(topNAnswersTemp.subList(0,
				// this.fixedmaxRequiredResult));
				// } else {
				// topNAnswers.addAll(topNAnswersTemp);
				// }
				//
				// coOccKWSStartTime = System.nanoTime();
				//
				// ArrayList<String> newKeywords;
				//
				// if (qualityExp) {
				// newKeywords = new ArrayList<String>(coOccKeywords);
				// } else {
				// newKeywords = new ArrayList<String>(keywords);
				// }
				//
				// if (qualityExp)
				// System.out.println("testing coOccBaseLine: " + new DateTime()
				// + " with keywords: "
				// + DummyFunctions.getStringOutOfCollection(newKeywords, ";"));
				//
				// if (qualityExp) {
				// coOccHandler = new CoOccurrence(graph, nodeIdsOfToken,
				// topNAnswers, 1, coOccKeywords);
				// } else {
				// coOccHandler = new CoOccurrence(graph, nodeIdsOfToken,
				// topNAnswers, 1, keywords);
				// }
				//
				// coOccHandler.expand();
				//
				// this.maxRequiredResult = tempMaxReqResults;
				// this.fixedmaxRequiredResult = tempMaxReqResults;
				//
				// fullOutputsQueue.clear();
				//
				// HashSet<Integer> qTCoOcc = new HashSet<Integer>();
				// for (int p = 0; p < numberOfQuery; p++) {
				// if (p < coOccHandler.topFrequentKeywordsInt.size()) {
				// qTCoOcc.add(coOccHandler.topFrequentKeywordsInt.get(p));
				// }
				// }
				// ObjectiveHandler objectiveHanlder = new
				// ObjectiveHandler(numberOfQuery, epsilon, lambda);
				// coOccF = objectiveHanlder.computeFFromScratch(qTCoOcc,
				// topNAnswers.subList(0, this.maxRequiredResult),
				// prunedLandmarkLabeling, nodeIdsOfToken,
				// distanceBound);
				//
				// discoveredAnswersCoOcc = fullOutputsQueue.size();
				//
				// if (qualityExp) {
				// coOccKeywords.clear();
				// coOccKeywords.addAll(newKeywords);
				// }
				//
				// System.out.println("after coOccBaseLine time: " +
				// coOccKWSDuration);
				// System.out.println("after coOccBaseLine cost: " +
				// coOccKWSQuality);
				// System.out.println("after coOccBaseLine answers: " +
				// discoveredAnswersCoOcc);
				// System.out.println(newKeywords.size() + " keywords was "
				// + DummyFunctions.getStringOutOfCollection(newKeywords, ";"));
				// }

				// if (dataCloudOn) {
				//
				// if (qualityExp)
				// System.out.println("starting data cloud: " + new DateTime() +
				// " with keywords: "
				// + DummyFunctions.getStringOutOfCollection(dataCloudKeywords,
				// ";"));
				//
				// ArrayList<String> newKeywords;
				// if (qualityExp) {
				// newKeywords = new ArrayList<String>(dataCloudKeywords);
				// } else {
				// newKeywords = new ArrayList<String>(keywords);
				// }
				// dataCloudKWSStartTime = System.nanoTime();
				// dataCloudHandler = new DataCloudDistinguishability(graph,
				// nodeIdsOfToken, topNAnswers, 1,
				// newKeywords);
				//
				// dataCloudHandler.expand();
				// fullOutputsQueue.clear();
				//
				// HashSet<Integer> qTDC = new HashSet<Integer>();
				// for (int p = 0; p < numberOfQuery; p++) {
				// if (p < dataCloudHandler.topFrequentKeywordsInt.size()) {
				// qTDC.add(dataCloudHandler.topFrequentKeywordsInt.get(p));
				// }
				// }
				// ObjectiveHandler objectiveHanlder = new
				// ObjectiveHandler(numberOfQuery, epsilon, lambda);
				// dataCloudF = objectiveHanlder.computeFFromScratch(qTDC,
				// topNAnswers.subList(0, this.maxRequiredResult),
				// prunedLandmarkLabeling, nodeIdsOfToken,
				// distanceBound);
				//
				// discoveredAnswersDataCloud = fullOutputsQueue.size();
				//
				// if (qualityExp) {
				// dataCloudKeywords.clear();
				// dataCloudKeywords.addAll(newKeywords);
				// }
				//
				// System.out.println("after dataCloud time: " +
				// dataCloudKWSDuration);
				// System.out.println("after dataCloud cost: " +
				// dataCloudKWSQuality);
				// System.out.println("after dataCloud answers: " +
				// discoveredAnswersDataCloud);
				// System.out.println(newKeywords.size() + " keywords was "
				// + DummyFunctions.getStringOutOfCollection(newKeywords, ";"));
				// }

			}

			settingStr = "After: discoveredAnswers:" + fullOutputsQueue.size() + " kwsTime: " + initialKWSDuration;

			System.out.println("Pairwise: " + settingStr);

			ArrayList<InfoHolder> timeInfos = new ArrayList<InfoHolder>();
			timeInfos.add(new InfoHolder(0, "Setting", "Pairwise"));
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

			TimeLogger.LogTime("PairwiseOutput.csv", true, timeInfos);

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

	private void preKWS(ArrayList<String> keywords, int exp) throws Exception {

		candidatesOfAKeyword = null;
		candidatesSet = null;
		fullOutputsQueue = null;

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

		maxRequiredResult = fixedmaxRequiredResult;

		fullOutputsQueue = new LinkedList<PairwiseAnswer>();

	}

	private void run(ArrayList<String> keywords) throws Exception {

		LinkedHashSet<Integer>[] c = (LinkedHashSet<Integer>[]) new LinkedHashSet[keywords.size()];

		// for i=0 to l do
		for (int i = 0; i < keywords.size(); i++) {

			// Ci <-- the set of nodes in G containing ki
			LinkedHashSet<Integer> nodeIdsOfTheToken = new LinkedHashSet<Integer>();

			// if (usingBDB) {
			// nodeIdsOfTheToken.addAll(candidatesOfAKeyword.get(keywords.get(i)));
			// } else {
			nodeIdsOfTheToken.addAll(candidatesOfAKeyword.get(keywords.get(i)));
			// }

			if (DummyProperties.debugMode) {
				System.out.println(keywords.get(i) + ":" + nodeIdsOfTheToken);
			}

			c[i] = nodeIdsOfTheToken;
		}

		// Queue <-- an empty priority queue
		PriorityQueue<AnswerSearchSpacePair> queue = new PriorityQueue<AnswerSearchSpacePair>(maxRequiredResult,
				new Comparator<AnswerSearchSpacePair>() {
					public int compare(AnswerSearchSpacePair a1, AnswerSearchSpacePair a2) {
						return Integer.compare(a1.answer.weight, a2.answer.weight);
					}
				});

		// A <-- FindTopRankedAnswer(C, G, l, r)
		PairwiseAnswer answer = findTopRankedAnswer(c, keywords);

		// if A =empty ; then
		if (answer != null) {
			queue.add(new AnswerSearchSpacePair(answer, c));
		}

		// 8: while Queue is not empty
		while (!queue.isEmpty()) {
			// <A, S> <-- Queue.removeTop()
			AnswerSearchSpacePair currentAnswerSearchSpacePair = queue.poll();

			// adding the new disovered answer
			fullOutputsQueue.add(currentAnswerSearchSpacePair.answer);
			if (debugMode)
				print(currentAnswerSearchSpacePair.answer, keywords);

			// maxRequiredResult <-- maxRequiredResult - 1
			maxRequiredResult--;

			// if maxRequiredResult = 0 then
			if (maxRequiredResult == 0)
				break;

			// ProduceSubSpaces
			LinkedHashSet<Integer>[][] newSubspaces = produceSubSpaces(currentAnswerSearchSpacePair, keywords);

			if (DummyProperties.debugMode) {
				System.out.println();
				System.out.println("newSubspaces at maxRequiredResult=" + maxRequiredResult);
				for (int i = 0; i < newSubspaces.length; i++) {
					for (int j = 0; j < newSubspaces[i].length; j++) {
						System.out.print(newSubspaces[i][j] + " ");
					}
					System.out.println();
				}
				System.out.println();
			}

			// for i <-- 1 to l do
			for (int i = 0; i < keywords.size(); i++) {

				// check validity of a subspace:
				boolean isAValidSubspace = true;
				for (LinkedHashSet<Integer> newSubspace : newSubspaces[i]) {
					if (newSubspace.isEmpty()) {
						isAValidSubspace = false;
						break;
					}
				}

				if (!isAValidSubspace)
					continue;

				// // Ai <-- FindTopRankedAnswer(SBi, G, l, r)
				PairwiseAnswer answer_i = findTopRankedAnswer(newSubspaces[i], keywords);
				//
				// // if A =empty ; then
				if (answer_i != null) {
					queue.add(new AnswerSearchSpacePair(answer_i, newSubspaces[i]));
				}
			}

		}

		// if (DummyProperties.debugMode) {
		System.out.println("\n FinalAnswers: \n");
		for (PairwiseAnswer pairwiseAnswer : fullOutputsQueue) {
			print(pairwiseAnswer, keywords);
		}
		// }

		// updatePairwiseAnswersWithSteinerResultTree(keywords,
		// fullOutputsQueue);

	}

	private void updatePairwiseAnswersWithResultTree(ArrayList<String> keywords,
			LinkedList<PairwiseAnswer> fullOutputsQueue) throws Exception {

		ArrayList<PairwiseAnswer> localOutputsQueue = new ArrayList<PairwiseAnswer>(fullOutputsQueue);

		Collections.sort(localOutputsQueue, new Comparator<PairwiseAnswer>() {
			@Override
			public int compare(PairwiseAnswer o1, PairwiseAnswer o2) {
				return Double.compare(o1.pairwiseWeight, o2.pairwiseWeight);
			}
		});

		for (PairwiseAnswer answer : localOutputsQueue) {

			DistinctRootExperiment bank = new DistinctRootExperiment();

			// in fact only one candidate
			HashMap<String, HashSet<Integer>> candidatesOfAKeywordForBank = new HashMap<String, HashSet<Integer>>();

			for (int i = 0; i < keywords.size(); i++) {
				candidatesOfAKeywordForBank.putIfAbsent(keywords.get(i), new HashSet<>());
				candidatesOfAKeywordForBank.get(keywords.get(i)).add(answer.nodeMatches.get(i));
			}

			bank.runFromOutside(keywords, candidatesOfAKeywordForBank, distanceBound, 1, graph,
					fixedmaxRequiredResult * 3, debugMode);

			ResultTree resTree = bank.fullOutputsQueue.poll();

			answer.resultTree = resTree;
			bank.print(answer.resultTree);

		}

	}

	private void updatePairwiseAnswersWithSteinerResultTree(ArrayList<String> keywords,
			LinkedList<PairwiseAnswer> fullOutputsQueue) throws Exception {

		ArrayList<PairwiseAnswer> localOutputsQueue = new ArrayList<PairwiseAnswer>(fullOutputsQueue);

		Collections.sort(localOutputsQueue, new Comparator<PairwiseAnswer>() {
			@Override
			public int compare(PairwiseAnswer o1, PairwiseAnswer o2) {
				return Double.compare(o1.pairwiseWeight, o2.pairwiseWeight);
			}
		});

		for (PairwiseAnswer answer : localOutputsQueue) {

			StinerbasedKWS steiner = new StinerbasedKWS();

			// in fact only one candidate
			HashMap<String, HashSet<Integer>> candidatesOfAKeywordForBank = new HashMap<String, HashSet<Integer>>();

			for (int i = 0; i < keywords.size(); i++) {
				candidatesOfAKeywordForBank.putIfAbsent(keywords.get(i), new HashSet<>());
				candidatesOfAKeywordForBank.get(keywords.get(i)).add(answer.nodeMatches.get(i));
			}

			steiner.runFromOutside(keywords, candidatesOfAKeywordForBank, distanceBound, 1, graph,
					fixedmaxRequiredResult * 3, debugMode);

		}

	}

	private LinkedHashSet<Integer>[][] produceSubSpaces(AnswerSearchSpacePair currentAnswerSearchSpacePair,
			ArrayList<String> keywords) {

		// Input: the best answer of previous step, A = <v1; v2; : : : vl>,
		// and the sets of content nodes, S1, . . . , Sl
		// Output: l new subspaces

		// initialization of data structure
		LinkedHashSet<Integer>[][] newSubspaces = (LinkedHashSet<Integer>[][]) new LinkedHashSet[keywords
				.size()][keywords.size()];

		for (int i = 0; i < keywords.size(); i++) {
			for (int j = 0; j < keywords.size(); j++) {
				newSubspaces[i][j] = new LinkedHashSet<Integer>(keywords.size());
			}
		}

		// for i <-- 0 to l do
		for (int i = 0; i < keywords.size(); i++) {
			// for j <-- 0 to i-1 do
			for (int j = 0; j <= (i - 1); j++) {
				// SBji <-- {vj}
				newSubspaces[i][j].add(currentAnswerSearchSpacePair.answer.nodeMatches.get(j));
			}

			// SBii <-- Si - {vi}
			LinkedHashSet<Integer> temp_ii = new LinkedHashSet<Integer>();
			temp_ii.addAll(currentAnswerSearchSpacePair.searchSpace[i]);
			temp_ii.remove(currentAnswerSearchSpacePair.answer.nodeMatches.get(i));
			newSubspaces[i][i] = temp_ii;

			// for j <-- i + 1 to l do
			for (int j = i + 1; j < keywords.size(); j++) {
				// SBji <-- Sj
				LinkedHashSet<Integer> temp_j = new LinkedHashSet<Integer>();
				temp_j.addAll(currentAnswerSearchSpacePair.searchSpace[j]);
				newSubspaces[i][j] = temp_j;
			}
		}

		// return <SB1; : : : ; SBl> where SBi = SB1i *...* SBli
		return newSubspaces;
	}

	private void print(PairwiseAnswer answer, ArrayList<String> keywords) {
		String answerStr = "ANSWER:\n";
		for (int i = 0; i < keywords.size(); i++) {
			answerStr += keywords.get(i) + ":" + answer.nodeMatches.get(i) + ", ";
		}
		answerStr = answerStr.substring(0, answerStr.length() - 2);
		System.out.println(maxRequiredResult + "=> " + answerStr + " weight: " + answer.weight + ", pairwiseWeight: "
				+ answer.pairwiseWeight);
	}

	private PairwiseAnswer findTopRankedAnswer(LinkedHashSet<Integer>[] searchSpace, ArrayList<String> keywords) {

		// s[i][j] -> [k]
		NearestNodeAndDistance[][][] nearestNodeAndDistancesToSindex = new NearestNodeAndDistance[keywords.size()][][];

		// for i=1 to l do
		for (int i = 0; i < keywords.size(); i++) {
			nearestNodeAndDistancesToSindex[i] = new NearestNodeAndDistance[searchSpace[i].size()][keywords.size()];
			int j = 0;
			for (Integer sji : searchSpace[i]) {
				// d(sji, i) <-- 0
				// n(sji, i) <-- sji
				nearestNodeAndDistancesToSindex[i][j][i] = new NearestNodeAndDistance(sji, 0);
				j++;
			}
		}

		// for i<-1 to l do
		for (int i = 0; i < keywords.size(); i++) {
			// for j<-1 to size(Si) do
			int j = 0;
			for (Integer sji : searchSpace[i]) {
				// for k<-1 to l ; k 6= i do
				for (int k = 0; k < keywords.size(); k++) {
					if (k == i)
						continue;

					// <dist; nearest> <-- shortest path from sji to Sk
					NearestNodeAndDistance nndPair = getNearestNodeAndDistance(sji, searchSpace[k]);

					// if dist <= r then
					if (nndPair.distance <= distanceBound) {
						// d(sji ; k) <-- dist
						// n(sji ; k) <-- nearest
						nearestNodeAndDistancesToSindex[i][j][k] = nndPair;
					} else {
						// d(sji ; k) <-- infinity
						// n(sji ; k) <-- null
						NearestNodeAndDistance nndPairInf = new NearestNodeAndDistance(null, Integer.MAX_VALUE);
						nearestNodeAndDistancesToSindex[i][j][k] = nndPairInf;
					}
				}
				j++;
			}
		}

		// leastWeight <- infinity
		int leastWeight = Integer.MAX_VALUE;

		// topAnswer <- empty
		PairwiseAnswer topAnswer = null;

		// for i 1 to l do
		for (int i = 0; i < keywords.size(); i++) {
			int j = 0;
			for (Integer sji : searchSpace[i]) {
				boolean passDistanceCriteria = true;
				for (int k = 0; k < keywords.size(); k++) {
					if (nearestNodeAndDistancesToSindex[i][j][k].distance > distanceBound) {
						passDistanceCriteria = false;
						break;
					}
				}

				if (passDistanceCriteria) {
					// weight <-- \Sigma_{h=1}{l} d(sji ; h)
					int weight = 0;
					for (int h = 0; h < keywords.size(); h++) {
						weight += nearestNodeAndDistancesToSindex[i][j][h].distance;
					}

					// if weight < leastW eight then
					if (weight < leastWeight) {
						// leastWeight <-- weight;
						leastWeight = weight;

						// topAnswer <--- <n(sji ; 1); : : : ; n(sji ; l)>
						topAnswer = new PairwiseAnswer(keywords.size());
						for (int k = 0; k < keywords.size(); k++) {
							topAnswer.nodeMatches.add(nearestNodeAndDistancesToSindex[i][j][k].nodeId);
						}
						topAnswer.weight = weight;
					}
				}
				j++;
			}
		}

		if (topAnswer != null) {
			for (int a = 0; a < topAnswer.nodeMatches.size(); a++) {
				for (int b = a + 1; b < topAnswer.nodeMatches.size(); b++) {
					topAnswer.pairwiseWeight += prunedLandmarkLabeling.queryDistance(topAnswer.nodeMatches.get(a),
							topAnswer.nodeMatches.get(b));
				}
			}
		}

		return topAnswer;
	}

	private NearestNodeAndDistance getNearestNodeAndDistance(Integer sourceNodeId,
			LinkedHashSet<Integer> linkedHashSet) {

		NearestNodeAndDistance nearestNodeAndDistance = new NearestNodeAndDistance();
		for (Integer targetNodeId : linkedHashSet) {
			if (prunedLandmarkLabeling.queryDistance(sourceNodeId, targetNodeId) < nearestNodeAndDistance.distance) {
				nearestNodeAndDistance.distance = prunedLandmarkLabeling.queryDistance(sourceNodeId, targetNodeId);
				nearestNodeAndDistance.nodeId = targetNodeId;
			}
		}

		if (DummyProperties.debugMode) {
			if (nearestNodeAndDistance.nodeId == null) {
				System.out.println("no near node from the list: src:" + sourceNodeId + " nearest:"
						+ Arrays.toString(linkedHashSet.toArray()));
			} else {
				System.out.println("nearestNodeAndDistance: src:" + sourceNodeId + " nearest:"
						+ nearestNodeAndDistance.nodeId + " dist:" + nearestNodeAndDistance.distance);
			}
		}

		return nearestNodeAndDistance;
	}

	public ArrayList<AnswerAsInput> tansformPairwiseAnswerIntoAnserAsInput() {

		ArrayList<AnswerAsInput> topNAnswers = new ArrayList<AnswerAsInput>();
		LinkedList<PairwiseAnswer> tem = new LinkedList<PairwiseAnswer>();

		while (!fullOutputsQueue.isEmpty()) {
			PairwiseAnswer theWholeResult = fullOutputsQueue.poll();
			tem.push(theWholeResult);

			// In pairwise case, there is no root
			int rootNodeId = -1;
			double cost = theWholeResult.pairwiseWeight;

			ArrayList<Integer> contentNodes = new ArrayList<Integer>();
			contentNodes = theWholeResult.nodeMatches;

			// In pairwise case, we may don't need allNodes
			ArrayList<Integer> allNodes = new ArrayList<Integer>();
			allNodes.addAll(contentNodes);

			ListenableUndirectedGraph<ResultNode, RelationshipInfra> resultTree = new ListenableUndirectedGraph<ResultNode, RelationshipInfra>(
					RelationshipInfra.class);

			for (int nodeId : theWholeResult.nodeMatches) {
				ResultNode resultNode = new ResultNode(" ", nodeId, graph.nodeOfNodeId.get(nodeId));
				resultTree.addVertex(resultNode);
			}

			AnswerAsInput topNAnswer = new AnswerAsInput(rootNodeId, contentNodes, allNodes, cost);
			topNAnswers.add(topNAnswer);
		}
		while (!tem.isEmpty()) {
			PairwiseAnswer theWholeResultTree = tem.poll();
			fullOutputsQueue.push(theWholeResultTree);
		}
		return topNAnswers;

	}

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
				epsilon, lambda, prunedLandmarkLabeling, numberOfQuery, nodeIdsOfToken, mTokenOfNode);
		streamDivQExpand.expand();
		HashMap<Integer, Double> distanceOfTerms = streamDivQExpand.termDistance;

		return distanceOfTerms;
	}
}
