package queryGeneration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import aqpeq.utilities.MapUtil;
import aqpeq.utilities.StringPoolUtility;
import dataset.BerkeleyDB.BerkleleyDB;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import queryExpansion.BFSTriple;

public class randomWalkGen {

	private static GraphInfraReaderArray graph;

	// // mhn
	private static String graphPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/sampledIMDB/graph/";
	private static String resultPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/sampledIMDB/query/";

	// xin
	// private static String graphPath = "/Users/zhangxin/Desktop/IMDB/graph/";
	// private static String envPath =
	// "/Users/zhangxin/Desktop/IMDB/dbEnvWithProp";
	// private static String resultPath = "/Users/zhangxin/Desktop/keyword/";

	// private static HashSet<Integer> stopWord =
	// DummyFunctions.getStopwordsSet();
	private static int lFrom = 2;
	private static int lTo = 3;
	private static int bFrom = 2;
	private static int bTo = 2;

	private static int maxDegreeOfStartingNode = 100;
	private static int selectedQueries = 0;
	static Random rnd = new Random();

	private static HashMap<Integer, HashSet<Integer>> nodeIdsOfToken;

	public randomWalkGen() {

	}

	public static void main(String[] args) throws Exception {

		int keywordNum = 2;
		int queryNum = 40;
		int distanceBound = 2;
		int frequencyBound = DummyProperties.MaxFrequencyBoundForKeywordSelection;
		String dataset = "citation";

		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("-dataGraph")) {
				graphPath = args[++i];
			} else if (args[i].equals("-dataset")) {
				dataset = args[++i];
			} else if (args[i].equals("-resultPath")) {
				resultPath = args[++i];
			} else if (args[i].equals("-queryNum")) {
				queryNum = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-frequencyBound")) {
				frequencyBound = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-lFrom")) {
				lFrom = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-lTo")) {
				lTo = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-bFrom")) {
				bFrom = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-bTo")) {
				bTo = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-withProperties")) {
				DummyProperties.withProperties = Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-debugMode")) {
				DummyProperties.debugMode = Boolean.parseBoolean(args[++i]);
			}
		}

		DummyProperties.withProperties = true;
		DummyProperties.readProperties = true;
		graph = new GraphInfraReaderArray(graphPath, true);
		graph.read();
		nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);

		randomWalkGen randomWalkGen = new randomWalkGen();

		// from minimum number of keywords to the maximum ones
		for (int l = lFrom; l <= lTo; l++) {

			keywordNum = l;

			// from minimum distance between keywords to the maximum ones
			for (int b = bFrom; b <= bTo; b++) {

				distanceBound = b;

				String resultName = dataset + "_query_l_" + l + "_b_" + b;

				File result = new File(resultPath + resultName + ".txt");
				FileOutputStream fosResult = new FileOutputStream(result);
				BufferedWriter bwResult = new BufferedWriter(new OutputStreamWriter(fosResult));

				// selecting a starting node of search

				selectedQueries = 0;
				HashSet<Integer> alreadySelectedRootsId = new HashSet<Integer>();

				while (selectedQueries < queryNum) {

					int rootNodeId = 0;

					do {
						rootNodeId = rnd.nextInt(graph.nodeOfNodeId.size());
					} while (graph.nodeOfNodeId.get(rootNodeId).getDegree() > maxDegreeOfStartingNode
							|| alreadySelectedRootsId.contains(rootNodeId));

					alreadySelectedRootsId.add(rootNodeId);

					if (DummyProperties.debugMode) {
						System.out.println(resultName);
						System.out.print("root info: ");
						NodeInfra node = graph.nodeOfNodeId.get(rootNodeId);
						System.out.print(node.nodeId + ", ");
						System.out.print(node.inDegree + ", ");
						System.out.print(node.outDegree + ", ");
						System.out.println(node.outgoingRelIdOfSourceNodeId.keySet());
					}

					if (DummyProperties.debugMode)
						System.out.println("bfs started " + new java.util.Date());

					HashSet<Integer> visitedNodesSet = randomWalkGen.boundedBFS(distanceBound, rootNodeId);

					if (DummyProperties.debugMode)
						System.out.println("bfs finished " + new java.util.Date());

					if (DummyProperties.debugMode)
						System.out.println("visitedNodesSet size: " + visitedNodesSet.size());

					if (DummyProperties.debugMode)
						System.out.println("query gen started at " + new java.util.Date());

					randomWalkGen.queryGenerator(visitedNodesSet, keywordNum, queryNum, frequencyBound, rootNodeId,
							bwResult);

					if (DummyProperties.debugMode)
						System.out.println("query gen finished at " + new java.util.Date());

				}

				bwResult.close();
			}
		}

	}

	public HashSet<Integer> boundedBFS(int distanceBound, int rootNodeId) {

		// queue L := ∅, set visited nodes S := ∅;
		LinkedList<BFSTriple> queue = new LinkedList<BFSTriple>();

		HashSet<Integer> visitedNodesSet = new HashSet<Integer>();

		// BFSTriple(nodeID, distance, cost), we set all cost = 0 heres
		queue.add(new BFSTriple(rootNodeId, 0, 0d));

		// while (L , ∅) do
		while (!queue.isEmpty()) {
			/* Picking the next node in a FIFO fashion */
			// ⟨v, d, c⟩ := L.poll ();
			BFSTriple currentBFSTriple = queue.poll();

			int v = currentBFSTriple.getNodeId();
			int d = currentBFSTriple.getDistance();

			// S = S ∪ {v };
			visitedNodesSet.add(v);

			// if d ≥ b then continue
			if (d >= distanceBound)
				continue;

			// for each each edge e = (u, v) in G′ do
			for (int targetNodeId : graph.nodeOfNodeId.get(v).getOutgoingRelIdOfSourceNodeId().keySet()) {

				// if u ∈ S then continue
				if (visitedNodesSet.contains(targetNodeId))
					continue;

				// L := L ∪ ⟨u, d + 1, c′⟩
				queue.add(new BFSTriple(targetNodeId, d + 1, 0));
			}

		}

		return visitedNodesSet;
	}

	public void queryGenerator(HashSet<Integer> visitedNodesSet, int keywordNum, int queryNum, int frequencyBound,
			int rootNodeId, BufferedWriter bwResult) throws Exception {

		// do not select from root keywords
		HashSet<String> rootKeywordsSet = new HashSet<String>();

		NodeInfra rootNodeInfra = graph.nodeOfNodeId.get(rootNodeId);
		// try {
		// rootNodeInfra = berkeleyDB.SearchNodeInfoWithPro(rootNodeId);
		// } catch (Exception exc) {
		// System.err.println("rootNodeId:" + rootNodeId);
		// System.err.println(exc.getMessage());
		// return;
		// }

		for (Integer label : rootNodeInfra.tokens) {
			// for (String t : DummyFunctions.getTokensOfALabel(label)) {
			// if (!stopWord.contains(t)) {
			rootKeywordsSet.add(StringPoolUtility.getStringOfId(label));
			// }
			// }
		}
		if (rootNodeInfra.getProperties() != null) {
			for (Integer prop : rootNodeInfra.getProperties()) {
				// for (String t : DummyFunctions.getTokensOfALabel(prop)) {
				// if (!stopWord.contains(t)) {
				rootKeywordsSet.add(StringPoolUtility.getStringOfId(prop));
				// }
				// }
			}
		}

		if (DummyProperties.debugMode)
			System.out.println("rootKeywordsSet: " + rootKeywordsSet.size());

		// get all possible keywords from visitedNodesSet

		HashMap<Integer, Integer> freqOfToken = new HashMap<Integer, Integer>();

		for (int nodeId : visitedNodesSet) {
			// System.out.println("node id -> " + nodeId);
			NodeInfra node = graph.nodeOfNodeId.get(nodeId);
			// try {
			// node = berkeleyDB.SearchNodeInfoWithPro(nodeId);
			// } catch (Exception exc) {
			// System.err.println("nodeId:" + nodeId);
			// System.err.println(exc.getMessage());
			// continue;
			// }
			for (int label : node.tokens) {
				// for (String t : DummyFunctions.getTokensOfALabel(label)) {
				// if (!stopWord.contains(t) && t.length() >= 3) {
				freqOfToken.putIfAbsent(label, 0);
				freqOfToken.put(label, freqOfToken.get(label) + 1);
				// }
				// }
			}
			if (node.getProperties() != null) {
				for (int propId : node.getProperties()) {
					freqOfToken.putIfAbsent(propId, 0);
					freqOfToken.put(propId, freqOfToken.get(propId) + 1);
				}
			}

		}

		if (DummyProperties.debugMode)
			System.out.println("freqOfToken: " + freqOfToken.size());

		HashMap<Integer, Integer> freqOfTokenInG = new HashMap<Integer, Integer>();

		// pruning keywords based on frequency or contain in root of search
		HashSet<Integer> keywordCandidateAlternative = new HashSet<Integer>(freqOfToken.keySet());
		for (Integer k : keywordCandidateAlternative) {

			boolean highFreqInG = false;

			try {
				HashSet<Integer> nodeIDs = nodeIdsOfToken.get(k);
				if (nodeIDs.size() > frequencyBound) {
					highFreqInG = true;
				} else {
					freqOfTokenInG.put(k, nodeIDs.size());
				}
			} catch (Exception exc) {
				System.err.println("get node ids of token " + k);
				System.err.println(exc.getMessage());
				highFreqInG = true;
			}

			if (highFreqInG || freqOfToken.get(k) > frequencyBound || rootKeywordsSet.contains(freqOfToken.get(k))
					|| StringPoolUtility.getStringOfId(k).contains("(")
					|| StringPoolUtility.getStringOfId(k).contains("�")
					|| StringPoolUtility.getStringOfId(k).length() < 3) {
				freqOfToken.remove(k);
			}
		}

		if (DummyProperties.debugMode)
			System.out.println("freqOfToken after pruning high-freq and root keywords: " + freqOfToken.size());

		keywordCandidateAlternative = new HashSet<Integer>(freqOfToken.keySet());
		for (Integer k : keywordCandidateAlternative) {
			try {
				// if it's only a number
				Double.parseDouble(StringPoolUtility.getStringOfId(k));
				freqOfToken.remove(k);
			} catch (Exception exc) {

			}
		}

		if (DummyProperties.debugMode)
			System.out.println("freqOfToken after pruning numbers: " + freqOfToken.size());

		HashMap<Integer, Double> tfIDFOfKeyword = new HashMap<Integer, Double>();
		for (int k : freqOfToken.keySet()) {
			tfIDFOfKeyword.put(k, getTfIdf(graph, nodeIdsOfToken, k, visitedNodesSet));
		}

		int howManyQueryFromThis = rnd.nextInt(Math.max(queryNum - selectedQueries, 0)) + 1;

		if (DummyProperties.debugMode)
			System.out.println("howManyQueryFromThis: " + howManyQueryFromThis);

		Map<Integer, Double> sortedMap = MapUtil.sortByValueDesc(tfIDFOfKeyword);

		// sorted keywords based on their tf-idf values
		ArrayList<String> sortedKeywords = new ArrayList<String>();

		int cnt0 = 0;
		int cntMax = 10;
		System.out.println("sorted-tf-idf: ");
		for (Entry<Integer, Double> entry : sortedMap.entrySet()) {
			sortedKeywords.add(StringPoolUtility.getStringOfId(entry.getKey()));

			if (DummyProperties.debugMode && cnt0 < cntMax) {
				System.out.print(entry.getKey() + ":" + entry.getValue() + ", ");
				cnt0++;
			}

		}
		if (DummyProperties.debugMode)
			System.out.println();

		int selectingKeyword = 0;

		// until we did not get the number of queries we want
		for (int h = 0; h < howManyQueryFromThis; h++) {

			// init a new query
			HashSet<String> query = new HashSet<String>();

			// until we did not reach to the request query size
			while (keywordNum > query.size()) {

				// if we ran out of the keyword!
				if (selectingKeyword >= sortedKeywords.size())
					break;

				query.add(sortedKeywords.get(selectingKeyword));

				selectingKeyword++;

			}

			// if it's a valid query
			if (query.size() == keywordNum) {
				int cnt = 0;
				for (String keyword : query) {
					cnt++;
					bwResult.write(
							keyword + ":" + freqOfTokenInG.get(StringPoolUtility.getIdOfStringFromPool(keyword)));
					if (cnt < query.size()) {
						bwResult.write(",");
					}
				}
				selectedQueries++;
				bwResult.write("\n");
			} else
				break;
		}

		if (DummyProperties.debugMode)
			System.out.println(selectedQueries + " queries were selected so far");
	}

	private double getTfIdf(GraphInfraReaderArray graph, HashMap<Integer, HashSet<Integer>> nodeIdsOfToken,
			Integer keyword, HashSet<Integer> visitedNodesSet) throws Exception {

		double tf = 0;
		double idf = 0;

		HashSet<Integer> nodeIDs = nodeIdsOfToken.get(keyword);

		// idf = log(1 + |V|/C(k))
		idf = 1 + (double) graph.nodeOfNodeId.size() / (double) nodeIDs.size();
		idf = Math.log(idf);

		// tf = term frequency in answers
		HashSet<Integer> nodeIDsWrtAnswer = new HashSet<Integer>(nodeIDs);
		nodeIDsWrtAnswer.retainAll(visitedNodesSet);
		if (nodeIDsWrtAnswer.size() == 0) {
			tf = 0;
		} else {
			tf = (double) nodeIDsWrtAnswer.size();
			tf = 1 + Math.log(tf);
		}

		// tdIdf = tf * idf
		return tf * idf;
	}

}
