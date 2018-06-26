package demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import aqpeq.utilities.Dummy;
import aqpeq.utilities.MapUtil;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.neo4j.kernel.configuration.Config;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

import aqpeq.utilities.Dummy.DummyProperties;
import aqpeq.utilities.StringPoolUtility;
import bank.keywordSearch.test;
import edu.uci.ics.jung.graph.CTreeNetwork;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;
import incrementalEvaluation.IncBFSTriple;
import incrementalEvaluation.IncEval;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;
import queryExpansion.AnswerAsInput;
import queryExpansion.CostAndNodesOfAnswersPair;
import relevantDiversificationLibraries.Diversification;
import scala.sys.process.ProcessImpl.DummyProcess;

public class DR implements KWSSearch {

    //	String graphBase = "../GraphExamples/demo/";
//	String graphPath = "";
    public test dr = new test();
    int heapSize = 100;
    Configuration config;
    int treeId = 0;

    public DR(String DBName, Configuration config) throws Exception {
        this.config = config;
        //graphPath = graphBase + DBName + "/";
//		DummyProperties.withProperties = true;
//		DummyProperties.readRelType = true;
//		DummyProperties.debugMode = false;
//		dr.graphInfraPath = graphPath;
        dr.maxRequiredResult = config.answerSize;
        dr.distanceBound = config.explorationRange;
        dr.delta = config.costBound;
        ArrayList<String> kwList = new ArrayList<String>();
        for (String str : config.keywords) {
            kwList.add(str);
        }
        dr.keywords = kwList;
    }

    @Override
    public LinkedList<DemoResultTree> Search(boolean incSearch) throws Exception {

        if (!incSearch || config.KWChoose.equals("")) {

            ArrayList<String> keywordsArr = new ArrayList<String>();
            keywordsArr.addAll(config.keywords);
            dr.nodeIdsOfToken = config.nodeIdsOfToken;
            dr.graph = config.graph;
            dr.preKWS(keywordsArr, 1);
            LinkedList<ResultTree> resultTem = dr.runFromOutside(keywordsArr, dr.candidatesOfAKeyword,
                    config.explorationRange, config.answerSize, config.graph, heapSize, Dummy.DummyProperties.debugMode);

            LinkedList<DemoResultTree> result = new LinkedList<DemoResultTree>();
//            int treeId = 0;
            for (ResultTree tree : resultTem) {
                DemoResultTree newTree = new DemoResultTree(treeId++, tree);
                result.add(newTree);
            }
            return result;
        } else {
            // TODO: incremental search
            // DRHandler.dr.rootKWSExpand.bestKeywordInfo
            // After we click the new keyword, we want to directly show the
            // results tree
            config.keywords.add(config.KWChoose);
            int KWChooseId = StringPoolUtility.getIdOfStringFromPool(config.KWChoose);
            CostAndNodesOfAnswersPair chose = config.estimatedWeightOfSuggestedKeywordMap.get(KWChooseId);
            for (int nodeId : chose.nodeId) {
                dr.candidatesSet.add(nodeId);
            }
            LinkedList<ResultTree> choseResults = new LinkedList<ResultTree>();
            choseResults.addAll(config.choseResults);
            ArrayList<AnswerAsInput> answerAsInput = dr.tansformResultTreeIntoAnswerAsInput(choseResults);
            dr.incEval = new IncEval(dr.graph, chose, answerAsInput, config.explorationRange,
                    DummyProperties.KWSSetting.DISTINCTROOT);
            dr.incEval.incEval();
            LinkedList<DemoResultTree> updatedResultTrees = updateResultTrees(dr.graph, choseResults, chose,
                    dr.incEval);
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
        ArrayList<AnswerAsInput> answerAsInput = dr.tansformResultTreeIntoAnswerAsInput(choseResults);
        ArrayList<String> keywords = new ArrayList<String>();
        for (String str : config.keywords) {
            keywords.add(str);
        }
        HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap = dr
                .keywordExpand(answerAsInput, keywords, config.costBound, config.explorationRange);

        System.out.println("size of estimatedWeightOfSuggestedKeywordMap = " + estimatedWeightOfSuggestedKeywordMap.size());

        config.estimatedWeightOfSuggestedKeywordMap = estimatedWeightOfSuggestedKeywordMap;

        ArrayList<CostAndNodesOfAnswersPair> rankedSuggestions = new ArrayList<CostAndNodesOfAnswersPair>();
        HashSet<Integer> suggestionIds = new HashSet<Integer>();

        if (config.relevanceFunction.size() > 0) {
            // rank by relevance function

            int bound = 1000;

            if(config.relevanceFunction.size()>1){
                bound =200;
            }

            HashMap<Integer, CostAndNodesOfAnswersPair> newMap = new HashMap<Integer, CostAndNodesOfAnswersPair>();

            if (estimatedWeightOfSuggestedKeywordMap.size() > bound) {
                //get top-1000 tokens
                System.out.println("filter estimatedWeightOfSuggestedKeywordMap");


                HashMap<Integer, Integer> freqOfSelectedTokenIds = new HashMap<>();
                for (int tokenId : estimatedWeightOfSuggestedKeywordMap.keySet()) {
                    freqOfSelectedTokenIds.put(tokenId, dr.graph.nodeOfNodeId.size());
                }

                Map<Integer, Integer> freqMap = MapUtil.sortByValueDesc(freqOfSelectedTokenIds);


                for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
                    if (newMap.size() < bound) {
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


            Diversification div = new Diversification(dr.graph, config.relevanceFunction, config.lambda);
            System.out.println("size of newMap = " + newMap.size());
            // debug for wrtToAnswers;
            boolean wrtToAnswers = false;
            ArrayList<Integer> rankedSuggestionsStr = div.run(newMap, answerAsInput,
                    wrtToAnswers, config.expandedQueriess, dr.nodeIdsOfToken);
            //Integer max = rankedSuggestionsStr.get(0);
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
                    //                   String function = config.relevanceFunction.iterator().next();
//                    if (function.equals("TFIDF")) {
                    newMap.get(index)
                            .setTfIdf(newMap.get(index).getTfIdf() / maxTFIDF);
                    newMap.get(index).setImportance(
                            newMap.get(index).getImportance() / maxImportance);
                    //                   }
//                    else {
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

        HashMap<Integer, HashSet<Integer>> urlIdsOfLabel = dr.graph.urlIdsOfLabel;

        for (CostAndNodesOfAnswersPair token : rankedSuggestions) {
            if (urlIdsOfLabel.containsKey(token.keywordIndex)) {

                groupedSuggestions.putIfAbsent(token.keywordIndex, new HashSet<>());
                groupedSuggestions.get(token.keywordIndex).add(token);
            } else {
                for (int nodeId : token.nodeId) {
                    if(!dr.graph.nodeOfNodeId.get(nodeId).labels.isEmpty()) {
                        for (int labelId : dr.graph.nodeOfNodeId.get(nodeId).labels) {
                            if (groupedSuggestions.containsKey(labelId)) {
                                groupedSuggestions.get(labelId).add(token);
                            } else {
                                groupedSuggestions.putIfAbsent(labelId, new HashSet<>());
                                groupedSuggestions.get(labelId).add(token);
                            }
                        }
                    }
                    else{
                        int unknownTokenId = StringPoolUtility.insertIntoStringPool("unknown");
                        groupedSuggestions.putIfAbsent(unknownTokenId, new HashSet<>());
                        groupedSuggestions.get(unknownTokenId).add(token);
                    }
                }
            }
        }

//        //debug
//        System.out.println(StringPoolUtility.getStringOfIds(groupedSuggestions.keySet()));
//        System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
//        for (int labelId : groupedSuggestions.keySet()) {
//        	System.out.println(StringPoolUtility.getStringOfId(labelId));
//        	for (CostAndNodesOfAnswersPair pair : groupedSuggestions.get(labelId)) {
//        		System.out.print(StringPoolUtility.getStringOfId(pair.keywordIndex) + ",");
//        	}
//        	System.out.println();
//        }

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

            ResultNode currentNode;
            ResultNode parentNode;
            IncBFSTriple currentTriple = target;
            IncBFSTriple currentParentTriple = target.getParentBFSTriple();
            resultTrees.get(treeIndex).cost += target.getCost();

            while (currentParentTriple != null) {

                if (resultTrees.get(treeIndex).createdTreeNode.containsKey(currentTriple.getNodeId())) {
                    currentNode = resultTrees.get(treeIndex).createdTreeNode.get(currentTriple.getNodeId());
                } else {
                    currentNode = new ResultNode(
                            StringPoolUtility
                                    .getStringOfTokenIds(graph.nodeOfNodeId.get(currentTriple.getNodeId()).tokens),
                            currentTriple.getNodeId(), graph.nodeOfNodeId.get(currentTriple.getNodeId()));
                    resultTrees.get(treeIndex).anOutputTree.addVertex(currentNode);
                    resultTrees.get(treeIndex).createdTreeNode.put(currentTriple.getNodeId(), currentNode);
                }

                if (resultTrees.get(treeIndex).createdTreeNode.containsKey(currentParentTriple.getNodeId())) {
                    parentNode = resultTrees.get(treeIndex).createdTreeNode.get(currentParentTriple.getNodeId());
                } else {
                    parentNode = new ResultNode(
                            StringPoolUtility.getStringOfTokenIds(
                                    graph.nodeOfNodeId.get(currentParentTriple.getNodeId()).tokens),
                            currentParentTriple.getNodeId(), graph.nodeOfNodeId.get(currentParentTriple.getNodeId()));
                    resultTrees.get(treeIndex).anOutputTree.addVertex(parentNode);
                    resultTrees.get(treeIndex).createdTreeNode.put(currentParentTriple.getNodeId(), parentNode);
                }

                if ((!resultTrees.get(treeIndex).anOutputTree.containsEdge(currentNode, parentNode))
                        && (!resultTrees.get(treeIndex).anOutputTree.containsEdge(parentNode, currentNode))) {

                    RelationshipInfra relInfra = null;

                    if (graph.nodeOfNodeId.get(parentNode.nodeId).getOutgoingRelIdOfSourceNodeId()
                            .containsKey(currentNode.nodeId)) {
                        relInfra = graph.relationOfRelId.get(graph.nodeOfNodeId.get(currentNode.nodeId)
                                .getOutgoingRelIdOfSourceNodeId().get(parentNode.nodeId));
                    } else if (graph.nodeOfNodeId.get(currentNode.nodeId).getOutgoingRelIdOfSourceNodeId()
                            .containsKey(parentNode.nodeId)) {
                        relInfra = graph.relationOfRelId.get(graph.nodeOfNodeId.get(currentNode.nodeId)
                                .getOutgoingRelIdOfSourceNodeId().get(parentNode.nodeId));
                    }

                    resultTrees.get(treeIndex).anOutputTree.addEdge(currentNode, parentNode, relInfra);
                }

                currentTriple = currentParentTriple;
                currentParentTriple = currentTriple.getParentBFSTriple();
            }

        }

        HashSet<NodeInfra> newNodes = new HashSet<NodeInfra>();
        HashSet<RelationshipInfra> newEdges = new HashSet<RelationshipInfra>();

//        int treeId = 0;
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
