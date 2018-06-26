package demo;

import aqpeq.utilities.Dummy.DummyProperties;
import aqpeq.utilities.MapUtil;
import aqpeq.utilities.StringPoolUtility;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;
import incrementalEvaluation.IncBFSTriple;
import incrementalEvaluation.IncEval;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;
import pairwiseBasedKWS.PairwiseKeywordSearchDemo;
import queryExpansion.AnswerAsInput;
import queryExpansion.CostAndNodesOfAnswersPair;
import relevantDiversificationLibraries.Diversification;
import steiner.keywordSearch.StinerbasedKWSDemo;
import tryingToTranslate.PrunedLandmarkLabeling;

import java.util.*;
import java.util.Map.Entry;

public class SG implements KWSSearch {

    private PrunedLandmarkLabeling prunedLandmarkLabeling;
    //	String graphBase = "../GraphExamples/demo/";
//	String graphPath = "";
    public PairwiseKeywordSearchDemo sg = new PairwiseKeywordSearchDemo();


    int heapSize = 100;
    Configuration config;
    ArrayList<String> kwList;

    public SG(String DBName, Configuration config) throws Exception {
        this.config = config;

        if (config.prunedLandmarkLabelingOfDB.containsKey(config.DBName)) {
            sg.prunedLandmarkLabeling = config.prunedLandmarkLabelingOfDB.get(config.DBName);
        } else {
            config.prunedLandmarkLabelingOfDB.clear();
            PrunedLandmarkLabeling prunedLandmarkLabeling = new PrunedLandmarkLabeling(8, config.graphPath + config.DBName + ".jin");
            config.prunedLandmarkLabelingOfDB.put(config.DBName, prunedLandmarkLabeling);
            sg.prunedLandmarkLabeling = prunedLandmarkLabeling;
        }

        kwList = new ArrayList<String>();
        for (String str : config.keywords) {
            kwList.add(str);
        }
    }

    @Override
    public LinkedList<DemoResultTree> Search(boolean incSearch) throws Exception {

        if (!incSearch || config.KWChoose.equals("")) {
            sg.nodeIdsOfToken = config.nodeIdsOfToken;
            sg.graph = config.graph;
            sg.preKWS(kwList, 1);
            LinkedList<ResultTree> resultTem = sg.runFromOutside(kwList, sg.candidatesOfAKeyword,
                    config.explorationRange, config.answerSize, config.graph, DummyProperties.debugMode);

            LinkedList<DemoResultTree> result = new LinkedList<DemoResultTree>();
            int treeId = 0;
            for (ResultTree tree : resultTem) {
                DemoResultTree newTree = new DemoResultTree(treeId++, tree);
                result.add(newTree);
            }
            return result;
        } else {
            // TODO: incremental search
            // DRHandler.sg.rootKWSExpand.bestKeywordInfo
            // After we click the new keyword, we want to directly show the
            // results tree
            config.keywords.add(config.KWChoose);
            int KWChooseId = StringPoolUtility.getIdOfStringFromPool(config.KWChoose);
            CostAndNodesOfAnswersPair chose = config.estimatedWeightOfSuggestedKeywordMap.get(KWChooseId);
            for (int nodeId : chose.nodeId) {
                sg.candidatesSet.add(nodeId);
            }
            LinkedList<ResultTree> choseResults = new LinkedList<ResultTree>();
            choseResults.addAll(config.choseResults);
            ArrayList<AnswerAsInput> answerAsInput = sg.tansformResultTreeIntoAnswerAsInput(choseResults);
            sg.incEvalWithIndex = new IncEval(sg.graph, chose, answerAsInput, config.explorationRange,
                    DummyProperties.KWSSetting.SUBGRAPH);
            sg.incEvalWithIndex.incEval(sg.prunedLandmarkLabeling);
            LinkedList<DemoResultTree> updatedResultTrees = updateResultTrees(sg.graph, choseResults, chose,
                    sg.incEvalWithIndex);
            config.choseResults = new HashSet<ResultTree>();

            return updatedResultTrees;
        }
    }

    @Override
    public HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>> Explore() throws Exception {
        System.out.println("Explore");

        // get suggestions
        LinkedList<ResultTree> choseResults = new LinkedList<ResultTree>();
        choseResults.addAll(config.choseResults);
        ArrayList<AnswerAsInput> answerAsInput = sg.tansformResultTreeIntoAnswerAsInput(choseResults);
        LinkedHashSet<String> keywords = new LinkedHashSet<String>();
        for (String str : config.keywords) {
            keywords.add(str);
        }
        HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap = sg
                .keywordExpand(answerAsInput, kwList, config.costBound, config.explorationRange);


        System.out.println("size of estimatedWeightOfSuggestedKeywordMap = " + estimatedWeightOfSuggestedKeywordMap.size());

//        System.out.println(StringPoolUtility.getStringOfIds(estimatedWeightOfSuggestedKeywordMap.keySet()));

        config.estimatedWeightOfSuggestedKeywordMap = estimatedWeightOfSuggestedKeywordMap;

        ArrayList<CostAndNodesOfAnswersPair> rankedSuggestions = new ArrayList<CostAndNodesOfAnswersPair>();
        HashSet<Integer> suggestionIds = new HashSet<Integer>();

        if (config.relevanceFunction.size() > 0) {
            // rank by relevance function

            HashMap<Integer, CostAndNodesOfAnswersPair> newMap = new HashMap<Integer, CostAndNodesOfAnswersPair>();

            if (estimatedWeightOfSuggestedKeywordMap.size() > 1000) {
                //get top-1000 tokens
                System.out.println("filter estimatedWeightOfSuggestedKeywordMap");


                HashMap<Integer, Integer> freqOfSelectedTokenIds = new HashMap<>();
                for (int tokenId : estimatedWeightOfSuggestedKeywordMap.keySet()) {
                    freqOfSelectedTokenIds.put(tokenId, sg.graph.nodeOfNodeId.size());
                }

                Map<Integer, Integer> freqMap = MapUtil.sortByValueDesc(freqOfSelectedTokenIds);


                for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
                    if (newMap.size() < 1000) {
                        newMap.put(entry.getKey(), estimatedWeightOfSuggestedKeywordMap.get(entry
                                .getKey()));
                    } else {
                        break;
                    }
                }
            } else {
                System.out.println("did not filter estimatedWeightOfSuggestedKeywordMap");
                newMap = estimatedWeightOfSuggestedKeywordMap;
            }

            Diversification div = new Diversification(sg.graph, config.relevanceFunction, config.lambda);
            System.out.println("size of newMap = " + newMap.size());
            // debug for wrtToAnswers;
            boolean wrtToAnswers = false;
            ArrayList<Integer> rankedSuggestionsStr = div.run(newMap, answerAsInput,
                    wrtToAnswers, config.expandedQueriess, sg.nodeIdsOfToken);

            //Integer max = rankedSuggestionsStr.size() > 0 ? rankedSuggestionsStr.get(0) : Integer.MAX_VALUE;
            //double maxFscore = newMap.get(max).FScore;
            double maxTFIDF = div.maxTfIdf;
            double maxImportance = div.maxImportance;
            for (int index : rankedSuggestionsStr) {

                if (config.relevanceFunction.size() > 1) {
//                    newMap
//                            .get(index).FScore = newMap.get(index).FScore / maxFscore;
                    newMap.get(index)
                            .setTfIdf(newMap.get(index).getTfIdf() / maxTFIDF);
                    newMap.get(index).setImportance(
                            newMap.get(index).getImportance() / maxImportance);
                } else {
//                    String function = config.relevanceFunction.iterator().next();
//                    if (function.equals("TFIDF")) {
                    newMap.get(index)
                            .setTfIdf(newMap.get(index).getTfIdf() / maxTFIDF);
                    newMap.get(index).setImportance(
                            newMap.get(index).getImportance() / maxImportance);
//                    } else {
//                        estimatedWeightOfSuggestedKeywordMap.get(index).setImportance(
//                                estimatedWeightOfSuggestedKeywordMap.get(index).getImportance() / maxImportance);
//                    }
                }

                newMap.get(index).keywordIndex = index;
                rankedSuggestions.add(newMap.get(index));
            }
            // config.rankedSuggestions = rankedSuggestions;

            for (CostAndNodesOfAnswersPair pair : rankedSuggestions) {
                suggestionIds.add(pair.keywordIndex);
            }

        } else {

            // rank by cost

            for (int sug : estimatedWeightOfSuggestedKeywordMap.keySet()) {
                CostAndNodesOfAnswersPair tem = estimatedWeightOfSuggestedKeywordMap.get(sug);
                tem.keywordIndex = sug;
                rankedSuggestions.add(tem);

            }
            CostComparator comparator = new CostComparator();
            rankedSuggestions.sort(comparator);

            //System.out.println(StringPoolUtility.getStringOfIds(estimatedWeightOfSuggestedKeywordMap.keySet()));


            if (config.expandedQueriess != 0) {
                int cnt = 0;
                ArrayList<CostAndNodesOfAnswersPair> suggestions = new ArrayList<CostAndNodesOfAnswersPair>();
                for (CostAndNodesOfAnswersPair tem : rankedSuggestions) {
                    if (cnt < config.expandedQueriess) {
                        suggestions.add(tem);
                    } else {
                        break;
                    }
                    cnt++;
                }

                int cryptoId = StringPoolUtility.getIdOfStringFromPool("cryptography");
                if (estimatedWeightOfSuggestedKeywordMap.containsKey(cryptoId)) {
                    suggestions.add(estimatedWeightOfSuggestedKeywordMap.get(cryptoId));
                }
                // config.rankedSuggestions = suggestions;
                rankedSuggestions = suggestions;



                for (CostAndNodesOfAnswersPair pair : rankedSuggestions) {
                    suggestionIds.add(pair.keywordIndex);
                }

            } else {

                // config.rankedSuggestions = rankedSuggestions;
                suggestionIds.addAll(estimatedWeightOfSuggestedKeywordMap.keySet());

            }


        }

        HashMap<Integer, HashSet<CostAndNodesOfAnswersPair>> groupedSuggestions = new HashMap<Integer, HashSet<CostAndNodesOfAnswersPair>>();

        HashMap<Integer, HashSet<Integer>> urlIdsOfLabel = sg.graph.urlIdsOfLabel;

        for (CostAndNodesOfAnswersPair token : rankedSuggestions) {
            if (urlIdsOfLabel.containsKey(token.keywordIndex)) {

                groupedSuggestions.putIfAbsent(token.keywordIndex, new HashSet<>());
                groupedSuggestions.get(token.keywordIndex).add(token);
            } else {
                for (int nodeId : token.nodeId) {
                    if (!sg.graph.nodeOfNodeId.get(nodeId).labels.isEmpty()) {
                        for (int labelId : sg.graph.nodeOfNodeId.get(nodeId).labels) {
                            if (groupedSuggestions.containsKey(labelId)) {
                                groupedSuggestions.get(labelId).add(token);
                            } else {
                                groupedSuggestions.putIfAbsent(labelId, new HashSet<>());
                                groupedSuggestions.get(labelId).add(token);
                            }
                        }
                    } else {
                        int unknownTokenId = StringPoolUtility.insertIntoStringPool("unknown");
                        groupedSuggestions.putIfAbsent(unknownTokenId, new HashSet<>());
                        groupedSuggestions.get(unknownTokenId).add(token);
                    }
                }
            }
        }

        HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>> groupedRankSuggestions = new HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>>();
        if (config.relevanceFunction.size() > 0) {
            // rank by relevance function

            if (config.relevanceFunction.size() > 1) {
                //rank by div score
                for (int labelId : groupedSuggestions.keySet()) {
                    HashSet<CostAndNodesOfAnswersPair> urlToken = groupedSuggestions.get(labelId);
                    ArrayList<CostAndNodesOfAnswersPair> rank = new ArrayList<CostAndNodesOfAnswersPair>();
                    rank.addAll(urlToken);
                    DivComparator comparator = new DivComparator();
                    rank.sort(comparator);
                    groupedRankSuggestions.put(labelId, rank);
                }

            } else {
                String function = config.relevanceFunction.iterator().next();
                if (function.equals("TFIDF")) {
                    //rank by tfidf
                    for (int labelId : groupedSuggestions.keySet()) {
                        HashSet<CostAndNodesOfAnswersPair> urlToken = groupedSuggestions.get(labelId);
                        ArrayList<CostAndNodesOfAnswersPair> rank = new ArrayList<CostAndNodesOfAnswersPair>();
                        rank.addAll(urlToken);
                        TFIDFComparator comparator = new TFIDFComparator();
                        rank.sort(comparator);
                        groupedRankSuggestions.put(labelId, rank);
                    }
                } else {
                    //rank by importance
                    for (int labelId : groupedSuggestions.keySet()) {
                        HashSet<CostAndNodesOfAnswersPair> urlToken = groupedSuggestions.get(labelId);
                        ArrayList<CostAndNodesOfAnswersPair> rank = new ArrayList<CostAndNodesOfAnswersPair>();
                        rank.addAll(urlToken);
                        ImportanceComparator comparator = new ImportanceComparator();
                        rank.sort(comparator);
                        groupedRankSuggestions.put(labelId, rank);
                    }
                }
            }

        } else {

            // rank by cost
            for (int labelId : groupedSuggestions.keySet()) {
                HashSet<CostAndNodesOfAnswersPair> urlToken = groupedSuggestions.get(labelId);
                ArrayList<CostAndNodesOfAnswersPair> rank = new ArrayList<CostAndNodesOfAnswersPair>();
                rank.addAll(urlToken);
                CostComparator comparator = new CostComparator();
                rank.sort(comparator);
                groupedRankSuggestions.put(labelId, rank);
            }

        }

        config.groupedSuggestions = groupedRankSuggestions;

        return groupedRankSuggestions;

    }

    public LinkedList<DemoResultTree> updateResultTrees(GraphInfraReaderArray graph, LinkedList<ResultTree> resultTrees,
                                                        CostAndNodesOfAnswersPair bestKeywordInfo, IncEval incEval) throws Exception {

        LinkedList<DemoResultTree> newResultTrees = new LinkedList<DemoResultTree>();
        HashSet<NodeInfra> oldNodes = new HashSet<NodeInfra>();
        HashSet<RelationshipInfra> oldEdges = new HashSet<RelationshipInfra>();

        for (ResultTree tree : resultTrees) {
            for (ResultNode node : tree.anOutputTree.vertexSet()) {
                oldNodes.add(node.node);
            }
            for (RelationshipInfra edge : tree.anOutputTree.edgeSet()) {
                oldEdges.add(edge);
            }
        }

        System.out.println("Debug in update");
        for (int i : bestKeywordInfo.nodeId) {
            System.out.println(i);
        }

        for (int treeIndex = 0; treeIndex < resultTrees.size(); treeIndex++) {

            IncBFSTriple target = incEval.lastTripleOfKeywordMatchToTarget[treeIndex];
            resultTrees.get(treeIndex).cost += target.getCost();

            ResultNode newResNode = new ResultNode(StringPoolUtility
                    .getStringOfTokenIds(graph.nodeOfNodeId.get(target.getNodeId()).tokens),
                    target.getNodeId(), graph.nodeOfNodeId.get(target.getNodeId()));


            resultTrees.get(treeIndex).anOutputTree.addVertex(newResNode);

            for (ResultNode oldResNode : resultTrees.get(treeIndex).anOutputTree.vertexSet()) {
                if (oldResNode.nodeId != newResNode.nodeId) {
                    RelationshipInfra newRelInfra = new RelationshipInfra(++graph.tempMaxRelIdForDemo, oldResNode.nodeId, newResNode.nodeId);
                    int distance = sg.prunedLandmarkLabeling.queryDistance(oldResNode.nodeId, newResNode.nodeId);
                    int tokenId = StringPoolUtility.insertIntoStringPool(String.valueOf(distance));
                    newRelInfra.addType(tokenId);
                    resultTrees.get(treeIndex).anOutputTree.addEdge(oldResNode, newResNode, newRelInfra);
                }
            }
        }

        HashSet<NodeInfra> newNodes = new HashSet<NodeInfra>();
        HashSet<RelationshipInfra> newEdges = new HashSet<RelationshipInfra>();

        int treeId = 0;
        for (ResultTree rt : resultTrees) {
            DemoResultTree tree = new DemoResultTree(treeId++, rt);
            newResultTrees.add(tree);

            for (ResultNode node : rt.anOutputTree.vertexSet()) {
                if (!oldNodes.contains(node.node)) {
                    newNodes.add(node.node);
                }
            }
            tree.newNodes = newNodes;

            for (RelationshipInfra edge : rt.anOutputTree.edgeSet()) {
                if (!oldEdges.contains(edge)) {
                    newEdges.add(edge);
                }
            }
            tree.newEdges = newEdges;

        }
        return newResultTrees;
    }

}
