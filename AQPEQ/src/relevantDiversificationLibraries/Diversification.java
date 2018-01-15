package relevantDiversificationLibraries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import aqpeq.utilities.StringPoolUtility;
import dataset.BerkeleyDB.BerkleleyDB;
import graphInfra.GraphInfraReaderArray;
import queryExpansion.AnswerAsInput;
import queryExpansion.CostAndNodesOfAnswersPair;

public class Diversification {

	static double maxTfIdf = 0;
	static double maxImportance = 0;
	private GraphInfraReaderArray graph;

	public Diversification(GraphInfraReaderArray graph) throws Exception {

		this.graph = graph;

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public HashSet<String> run(HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap,
			ArrayList<AnswerAsInput> topNAnswers, boolean wrtToAnswers, GraphInfraReaderArray graph, int k,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfToken) throws Exception {

		// fill importance and tfIdf score for each CostAndNodesOfAnswersPair
		estimatedWeightOfSuggestedKeywordMap = computeRelScore(estimatedWeightOfSuggestedKeywordMap, nodeIdsOfToken,
				topNAnswers, wrtToAnswers);

		HashMap<Double, DivObject> map = getDivObjectList(estimatedWeightOfSuggestedKeywordMap, nodeIdsOfToken);

		HashSet<String> topk = getTopK(k, map);
		return topk;
	}

	public HashMap<Integer, CostAndNodesOfAnswersPair> computeRelScore(
			HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfToken, ArrayList<AnswerAsInput> topNAnswers,
			boolean wrtToAnswers) throws Exception {
		ComputingRelevancyScore relScore = new ComputingRelevancyScore(estimatedWeightOfSuggestedKeywordMap,
				topNAnswers, wrtToAnswers, nodeIdsOfToken);

		maxTfIdf = relScore.maxTfIdf;
		maxImportance = relScore.maxImportance;

		// fill importance score
		estimatedWeightOfSuggestedKeywordMap = relScore.ImportanceScore(topNAnswers, graph,
				estimatedWeightOfSuggestedKeywordMap, nodeIdsOfToken);

		// fill tfIdf score
		estimatedWeightOfSuggestedKeywordMap = relScore.TFIDFScore(graph, estimatedWeightOfSuggestedKeywordMap);
		return estimatedWeightOfSuggestedKeywordMap;

	}

	public HashMap<Double, DivObject> getDivObjectList(
			HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfToken) throws Exception {
		// key: Function score Value: keywords pair
		HashMap<Double, DivObject> map = new HashMap<Double, DivObject>();

		HashSet<Integer> visited = new HashSet<Integer>();
		// compute difference
		for (Integer keywordA : estimatedWeightOfSuggestedKeywordMap.keySet()) {
			for (Integer keywordB : estimatedWeightOfSuggestedKeywordMap.keySet()) {
				if (keywordA != keywordB) {
					// skip <k_1, k_1> ... <k_n, k_n>
					if (!visited.contains(keywordB)) {
						double diffScore = TermSimilarFunction.ComputeTermSimilar(graph, nodeIdsOfToken, keywordA,
								keywordB);
						ArrayList<Integer> newKeywords = new ArrayList<Integer>();
						newKeywords.add(keywordA);
						newKeywords.add(keywordB);
						double tfIdfScore = (estimatedWeightOfSuggestedKeywordMap.get(keywordA).getTfIdf() / maxTfIdf)
								+ (estimatedWeightOfSuggestedKeywordMap.get(keywordB).getTfIdf() / maxTfIdf);
						double importanceScore = (estimatedWeightOfSuggestedKeywordMap.get(keywordA).getImportance()
								/ maxTfIdf)
								+ (estimatedWeightOfSuggestedKeywordMap.get(keywordB).getImportance() / maxTfIdf);
						double FScore = diffScore + tfIdfScore + importanceScore;

						DivObject divObject = new DivObject(tfIdfScore, importanceScore, diffScore, newKeywords);

						map.put(FScore, divObject);
					}
				}
			}
			visited.add(keywordA);
		}
//		// compute rel
//		for (Integer keywordA : estimatedWeightOfSuggestedKeywordMap.keySet()) {
//
//			double diffScore = 0;
//			ArrayList<Integer> newKeywords = new ArrayList<Integer>();
//			newKeywords.add(keywordA);
//			double tfIdfScore = (estimatedWeightOfSuggestedKeywordMap.get(keywordA).getTfIdf() / maxTfIdf);
//			double importanceScore = (estimatedWeightOfSuggestedKeywordMap.get(keywordA).getImportance() / maxTfIdf);
//			double FScore = diffScore + tfIdfScore + importanceScore;
//
//			DivObject divObject = new DivObject(tfIdfScore, importanceScore, diffScore, newKeywords);
//
//			map.put(FScore, divObject);
//
//		}

		return map;
	}

	public HashSet<String> getTopK(int k, HashMap<Double, DivObject> map) throws Exception {
		HashSet<String> topK = new HashSet<String>();
		while (k != topK.size()) {
			double maxF = Collections.max(map.keySet());
			DivObject divObject = map.get(maxF);
			topK.add(StringPoolUtility.getStringOfId(divObject.newKeywords.get(0)));
			if (topK.size() < k) {
				topK.add(StringPoolUtility.getStringOfId(divObject.newKeywords.get(1)));
			}
			System.out.println(divObject.newKeywords + " div score: " + maxF);
			map.remove(maxF);
		}
		return topK;
	}

}
