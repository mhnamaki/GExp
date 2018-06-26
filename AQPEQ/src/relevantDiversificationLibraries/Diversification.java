package relevantDiversificationLibraries;

import java.util.*;

import aqpeq.utilities.MapUtil;
import aqpeq.utilities.StringPoolUtility;
import demo.Configuration;
import graphInfra.GraphInfraReaderArray;
import queryExpansion.AnswerAsInput;
import queryExpansion.CostAndNodesOfAnswersPair;

public class Diversification {

    public double maxTfIdf = 0;
    public double maxImportance = 0;
    // public double maxFscore = 0;
    private GraphInfraReaderArray graph;
    private HashSet<String> divFunctions = new HashSet<String>();
    double lambda = 0;
    // public HashSet<Integer> topFrequentTokens = new HashSet<Integer>();

    public Diversification(GraphInfraReaderArray graph, HashSet<String> divFunctions, double lambda) throws Exception {

        this.graph = graph;
        this.divFunctions = divFunctions;
        this.lambda = lambda;
        // this.topFrequentTokens = topFrequentTokens;

    }

    public ArrayList<Integer> run(HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap,
                                  ArrayList<AnswerAsInput> topNAnswers, boolean wrtToAnswers, int k,
                                  HashMap<Integer, HashSet<Integer>> nodeIdsOfToken) throws Exception {

        // fill importance and tfIdf score for each CostAndNodesOfAnswersPair
        estimatedWeightOfSuggestedKeywordMap = computeRelScore(estimatedWeightOfSuggestedKeywordMap, nodeIdsOfToken,
                topNAnswers, wrtToAnswers);

        ArrayList<Integer> topk = new ArrayList<Integer>();
        if (divFunctions.size() > 1) {
            HashMap<Double, DivObject> map = getDivObjectList(estimatedWeightOfSuggestedKeywordMap, nodeIdsOfToken);

            topk = getTopK(k, map);

        } else {
            String function = divFunctions.iterator().next();
            if (function.equals("TFIDF")) {
                topk = getTopKTFIDF(k, estimatedWeightOfSuggestedKeywordMap);
            } else {
                topk = getTopKImportance(k, estimatedWeightOfSuggestedKeywordMap);
            }
        }

        return topk;
    }

    private ArrayList<Integer> getTopKImportance(int k,
                                                 HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap) throws Exception {
        ArrayList<Integer> topK = new ArrayList<Integer>();

        HashMap<Integer, Double> suggestionMap = new HashMap<Integer, Double>();

        for (int suggestion : estimatedWeightOfSuggestedKeywordMap.keySet()) {
            suggestionMap.put(suggestion, estimatedWeightOfSuggestedKeywordMap.get(suggestion).getImportance());
        }

        Map<Integer, Double> sortedMap = MapUtil.sortByValueDesc(suggestionMap);

        for (Map.Entry<Integer, Double> entry : sortedMap.entrySet()) {
            if (topK.size() < k) {
                topK.add(entry.getKey());
            }
        }

        return topK;

    }

    private ArrayList<Integer> getTopKTFIDF(int k,
                                            HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap) throws Exception {
        ArrayList<Integer> topK = new ArrayList<Integer>();

        HashMap<Integer, Double> suggestionMap = new HashMap<Integer, Double>();

        for (int suggestion : estimatedWeightOfSuggestedKeywordMap.keySet()) {
            suggestionMap.put(suggestion, estimatedWeightOfSuggestedKeywordMap.get(suggestion).getTfIdf());
        }

        Map<Integer, Double> sortedMap = MapUtil.sortByValueDesc(suggestionMap);

        for (Map.Entry<Integer, Double> entry : sortedMap.entrySet()) {
            if (topK.size() < k) {
                topK.add(entry.getKey());
            }
        }

        return topK;
    }

    public HashMap<Integer, CostAndNodesOfAnswersPair> computeRelScore(
            HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap,
            HashMap<Integer, HashSet<Integer>> nodeIdsOfToken, ArrayList<AnswerAsInput> topNAnswers,
            boolean wrtToAnswers) throws Exception {
        ComputingRelevancyScore relScore = new ComputingRelevancyScore(estimatedWeightOfSuggestedKeywordMap,
                topNAnswers, wrtToAnswers, nodeIdsOfToken);

        // fill importance score
        estimatedWeightOfSuggestedKeywordMap = relScore.ImportanceScore(topNAnswers, graph,
                estimatedWeightOfSuggestedKeywordMap, nodeIdsOfToken);

        // fill tfIdf score
        estimatedWeightOfSuggestedKeywordMap = relScore.TFIDFScore(graph, estimatedWeightOfSuggestedKeywordMap);

        this.maxTfIdf = relScore.maxTfIdf;
        this.maxImportance = relScore.maxImportance;

        return estimatedWeightOfSuggestedKeywordMap;

    }

    public HashMap<Double, DivObject> getDivObjectList(
            HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap,
            HashMap<Integer, HashSet<Integer>> nodeIdsOfToken) throws Exception {
        System.out.println("Begin creating div Object.");
        // key: Function score Value: keywords pair
        HashMap<Double, DivObject> map = new HashMap<Double, DivObject>();

        HashSet<Integer> visited = new HashSet<Integer>();

        // ArrayList<Integer> tokenIds = new
        // ArrayList<Integer>(estimatedWeightOfSuggestedKeywordMap.keySet());
        //
        // ArrayList<String> tokenPairs = new ArrayList<String>();
        //
        // for (int keywordA: tokenIds) {
        // for (Integer keywordB :
        // estimatedWeightOfSuggestedKeywordMap.keySet()) {
        // if (keywordA != keywordB) {
        // // skip <k_1, k_1> ... <k_n, k_n>
        // if (!visited.contains(keywordB)) {
        // String tokenPair = String.valueOf(keywordA) + "," +
        // String.valueOf(keywordB);
        // tokenPairs.add(tokenPair);
        // }
        // }
        // }
        // }

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

                        ArrayList<Double> scores = new ArrayList<Double>();
                        scores.add(diffScore);
                        // System.out.println("~~~~~Debug in DIV~~~~");
                        // System.out.println("diff = " + diffScore);

                        double RScore = 0.0;

                        double tfIdfScore = 0.0;
                        double importanceScore = 0.0;

                        for (String function : divFunctions) {
                            if (function.equals("TFIDF")) {
                                tfIdfScore = (estimatedWeightOfSuggestedKeywordMap.get(keywordA).getTfIdf() / maxTfIdf)
                                        + (estimatedWeightOfSuggestedKeywordMap.get(keywordB).getTfIdf() / maxTfIdf);
                                tfIdfScore = tfIdfScore / 2;
                                scores.add(tfIdfScore);
                                RScore += tfIdfScore;
                                // System.out.println("~~~~~Debug in DIV~~~~");
                                // System.out.println("tfidf = " + tfIdfScore);
                            } else if (function.equals("IMPORTANCE")) {
                                importanceScore = (estimatedWeightOfSuggestedKeywordMap.get(keywordA).getImportance()
                                        / maxImportance)
                                        + (estimatedWeightOfSuggestedKeywordMap.get(keywordB).getImportance()
                                        / maxImportance);
                                importanceScore = importanceScore / 2;
                                scores.add(importanceScore);
                                RScore += importanceScore;
                                // System.out.println("~~~~~Debug in DIV~~~~");
                                // System.out.println("importance = " +
                                // importanceScore);
                            }
                        }

                        double FScore = ((1 - lambda) * RScore) + (lambda * diffScore);

                        DivObject divObject = new DivObject(scores, newKeywords);

                        map.put(FScore, divObject);

                        estimatedWeightOfSuggestedKeywordMap.get(keywordA).FScore = FScore;
                        estimatedWeightOfSuggestedKeywordMap.get(keywordB).FScore = FScore;

                    }

                }
            }
            visited.add(keywordA);
        }

        return map;
    }

    public ArrayList<Integer> getTopK(int k, HashMap<Double, DivObject> map) throws Exception {

        System.out.println("Begin geting top k.");

        HashSet<Integer> visited = new HashSet<Integer>();
        ArrayList<Integer> topK = new ArrayList<Integer>();

        while ((topK.size() < k) && (!map.isEmpty())) {
            double maxF = Collections.max(map.keySet());
            System.out.println("max f = " + maxF);
            DivObject divObject = map.get(maxF);
            // String str =
            // StringPoolUtility.getStringOfId(divObject.newKeywords.get(0));
            int strId = divObject.newKeywords.get(0);
            if (!visited.contains(strId)) {
                topK.add(strId);
                visited.add(strId);
            }
            if (topK.size() < k) {
                int str2Id = divObject.newKeywords.get(1);
                // String str2 =
                // StringPoolUtility.getStringOfId(divObject.newKeywords.get(1));
                if (!visited.contains(str2Id)) {
                    topK.add(str2Id);
                    visited.add(str2Id);
                }
            }
            map.remove(maxF);
        }

        return topK;
    }

}
