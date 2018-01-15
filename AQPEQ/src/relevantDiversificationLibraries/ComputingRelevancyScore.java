package relevantDiversificationLibraries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import aqpeq.utilities.StringPoolUtility;
import dataset.BerkeleyDB.BerkleleyDB;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import queryExpansion.AnswerAsInput;
import queryExpansion.CostAndNodesOfAnswersPair;

public class ComputingRelevancyScore {

	static HashMap<Integer, HashSet<Integer>> nodeIDsMap = new HashMap<Integer, HashSet<Integer>>();
	static HashSet<Integer> allNodeIdOfTopNAnswers = new HashSet<Integer>();
	static boolean wrtToAnswers;
	public double maxTfIdf = 0;
	public double maxImportance = 0;

	// input: given top-n answers, graph, estimatedWeightOfSuggestedKeywordMap,
	// etc.
	// output: top-k keyword maximizing the objective function including their
	// score.
	public ComputingRelevancyScore(HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap,
			ArrayList<AnswerAsInput> topNAnswers, boolean wrtToAnswers,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfToken) throws Exception {

		for (Integer keyword : estimatedWeightOfSuggestedKeywordMap.keySet()) {
			HashSet<Integer> nodeIDs = nodeIdsOfToken.get(keyword);
			nodeIDsMap.put(keyword, nodeIDs);
		}

		for (AnswerAsInput answer : topNAnswers) {
			allNodeIdOfTopNAnswers.addAll(answer.getAllNodes());
		}
		ComputingRelevancyScore.wrtToAnswers = wrtToAnswers;
	}

	public HashMap<Integer, CostAndNodesOfAnswersPair> ImportanceScore(ArrayList<AnswerAsInput> topNAnswers,
			GraphInfraReaderArray graph,
			HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfToken) throws Exception {

		for (Integer keyword : estimatedWeightOfSuggestedKeywordMap.keySet()) {
			// importance score come from degree, but later we may normalize it
			double importance = 0;
			int degree = 0;

			if (wrtToAnswers) {
				HashSet<Integer> nodeIDsWrtAnswer = new HashSet<Integer>(nodeIDsMap.get(keyword));
				nodeIDsWrtAnswer.retainAll(allNodeIdOfTopNAnswers);
				if (!nodeIDsWrtAnswer.isEmpty()) {
					for (int nodeId : nodeIDsWrtAnswer) {
						NodeInfra node = graph.nodeOfNodeId.get(nodeId);
						degree += node.getDegree();
					}
				} else {
					degree = 0;
				}
			} else {
				HashSet<Integer> nodeIDs = nodeIDsMap.get(keyword);
				for (int nodeId : nodeIDs) {
					NodeInfra node = graph.nodeOfNodeId.get(nodeId);
					degree += node.getDegree();
				}
			}

			
			importance = (double) degree;
			if (importance > maxImportance) {
				maxImportance = importance;
			}

			// Update cost
			CostAndNodesOfAnswersPair cost = estimatedWeightOfSuggestedKeywordMap.get(keyword);
			cost.setImportance(importance);
			estimatedWeightOfSuggestedKeywordMap.replace(keyword, cost);
		}
		return estimatedWeightOfSuggestedKeywordMap;
	}

	public HashMap<Integer, CostAndNodesOfAnswersPair> TFIDFScore(GraphInfraReaderArray graph,
			HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap) throws Exception {
		for (Integer keyword : estimatedWeightOfSuggestedKeywordMap.keySet()) {
			double tfIdf = 0;
			double tf = 0;
			double idf = 0;

			HashSet<Integer> nodeIDs = nodeIDsMap.get(keyword);

			// idf = log(1 + |V|/C(k))
			idf = 1 + (double) graph.nodeOfNodeId.size() / nodeIDs.size();
			idf = Math.log(idf);

			// tf = term frequency in answers and graph

			HashSet<Integer> nodeIDsWrtAnswer = new HashSet<Integer>(nodeIDsMap.get(keyword));
			if (wrtToAnswers) {// based on top-n answers
				if (nodeIDsWrtAnswer.size() == 0) {
					tf = 0;
				} else {
					tf = (double) nodeIDsWrtAnswer.size();
					tf = 1 + Math.log(tf);
				}
			} else {// base on graph
				tf = (double) nodeIDsWrtAnswer.size();
				tf = 1 + Math.log(tf);
			}

			// tdIdf = tf * idf
			tfIdf = tf * idf;
			if (tfIdf > maxTfIdf) {
				maxTfIdf = maxTfIdf;
			}

			// Update cost
			CostAndNodesOfAnswersPair cost = estimatedWeightOfSuggestedKeywordMap.get(keyword);
			cost.setTfIdf(tfIdf);
			estimatedWeightOfSuggestedKeywordMap.replace(keyword, cost);

		}
		return estimatedWeightOfSuggestedKeywordMap;

	}

}

// boolean wrtToAnswers = false;
// ComputingRelevancyScore relScore = new ComputingRelevancyScore(
// estimatedWeightOfSuggestedKeywordMap, berkeleyDB, topNAnswers,
// wrtToAnswers);
// estimatedWeightOfSuggestedKeywordMap = relScore.TFIDFScore(graph,
// estimatedWeightOfSuggestedKeywordMap);
// estimatedWeightOfSuggestedKeywordMap = relScore.ImportanceScore(topNAnswers,
// graph, estimatedWeightOfSuggestedKeywordMap, berkeleyDB);
//
// for (String keyword : estimatedWeightOfSuggestedKeywordMap.keySet()) {
// CostAndNodesOfAnswersPair cost = estimatedWeightOfSuggestedKeywordMap
// .get(keyword);
// System.out.println(keyword + "'s cost of tf•Idf" + cost.getTfIdf());
// }
//
// for (String keyword : estimatedWeightOfSuggestedKeywordMap.keySet()) {
// CostAndNodesOfAnswersPair cost = estimatedWeightOfSuggestedKeywordMap
// .get(keyword);
// System.out.println(keyword + "'s cost of importance" + cost.getImportance());
// }
