package demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import aqpeq.utilities.Dummy.DummyProperties;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;
import queryExpansion.CostAndNodesOfAnswersPair;
import tryingToTranslate.PrunedLandmarkLabeling;

public class Configuration implements Cloneable {


    final double DEFAULT_LAMBDA = 0.5;
    final double DEFAULT_COST_BOUND = 1;
    final int DEFAULT_EXPLORAITON_RANGE = 2;
    final int DEFAULT_ANSWERS_SIZE = 3;
    final int DEFAULT_EXPANSION_SIZE = 5;


    String graphBase = "../GraphExamples/demo/";
    String graphPath = "";
    public HashMap<String, PrunedLandmarkLabeling> prunedLandmarkLabelingOfDB = new HashMap<String, PrunedLandmarkLabeling>();
    //public String DBName = "sample";
    public String DBName = "DBPKW";
    // public String DBName = "IMDB1"; //Versionmovie, Duane
    public String KWS = "DR";
    //	public String KWS = "SG";
    public HashSet<String> keywords = new HashSet<String>();
    public GraphInfraReaderArray graph;
    HashMap<Integer, HashSet<Integer>> nodeIdsOfToken;
    public int answerSize = DEFAULT_ANSWERS_SIZE;
    public int expandedQueriess = DEFAULT_EXPANSION_SIZE;
    public int explorationRange = DEFAULT_EXPLORAITON_RANGE;
    public double costBound = DEFAULT_COST_BOUND;
    public HashSet<String> relevanceFunction = new HashSet<String>();
    public double lambda = DEFAULT_LAMBDA;
    public boolean searchInc = false;
    public String KWChoose = "";
    public LinkedList<DemoResultTree> resultTrees = new LinkedList<DemoResultTree>();
    public HashSet<ResultTree> choseResults = new HashSet<ResultTree>();
    public HashSet<NodeInfra> originalNodes = new HashSet<NodeInfra>();
    public HashSet<RelationshipInfra> originalEdges = new HashSet<RelationshipInfra>();
    public HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap = new HashMap<Integer, CostAndNodesOfAnswersPair>();
    // public ArrayList<CostAndNodesOfAnswersPair> rankedSuggestions = new
    // ArrayList<CostAndNodesOfAnswersPair>();
    HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>> groupedSuggestions = new HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>>();
//	public Stack<Configuration> states = new Stack<Configuration>();

    public Configuration() throws Exception {

    }

    public Configuration(String DBName) throws Exception {
        this.DBName = DBName;
    }

    public void readTheDataset() throws Exception {
        DummyProperties.withProperties = true;
        DummyProperties.readRelType = true;
        DummyProperties.debugMode = false;
        graph = null;
        graphPath = graphBase + DBName + "/";
        graph = new GraphInfraReaderArray(graphPath, true);
        graph.read();
        nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);
        PrunedLandmarkLabeling prunedLandmarkLabeling = new PrunedLandmarkLabeling(8, this.graphPath + this.DBName + ".jin");
        prunedLandmarkLabelingOfDB.clear();
        prunedLandmarkLabelingOfDB.put(this.DBName, prunedLandmarkLabeling);
        System.out.println("the " + DBName + " has been loaded!");
    }

    @Override
    public Object clone() {
        Configuration config = null;
        try {
            config = new Configuration();
            //these are expensive objects and also not needed to renew
            //START
            config.graph = this.graph;
            config.nodeIdsOfToken = this.nodeIdsOfToken;
            //END

            config.graphPath = this.graphPath;
            config.graphBase = this.graphBase;
            config.DBName = this.DBName;
            config.KWS = this.KWS;
            config.answerSize = this.answerSize;
            config.expandedQueriess = this.expandedQueriess;
            config.explorationRange = this.explorationRange;
            config.costBound = this.costBound;
            config.lambda = this.lambda;
            config.searchInc = this.searchInc;
            config.KWChoose = this.KWChoose;


            config.keywords = new HashSet<String>();
            config.keywords.addAll(this.keywords);

            config.relevanceFunction = new HashSet<String>();
            config.relevanceFunction.addAll(this.relevanceFunction);

            config.originalNodes = new HashSet<NodeInfra>();
            config.originalNodes.addAll(this.originalNodes);

            config.originalEdges = new HashSet<RelationshipInfra>();
            config.originalEdges.addAll(this.originalEdges);

            config.estimatedWeightOfSuggestedKeywordMap = new HashMap<Integer, CostAndNodesOfAnswersPair>();
            for (int keywordId : this.estimatedWeightOfSuggestedKeywordMap.keySet()) {
                CostAndNodesOfAnswersPair costPair = this.estimatedWeightOfSuggestedKeywordMap.get(keywordId);
                CostAndNodesOfAnswersPair newOne = new CostAndNodesOfAnswersPair(costPair.nodeId, costPair.cost);
                newOne.keywordIndex = costPair.keywordIndex;
                newOne.setImportance(costPair.getImportance());
                newOne.setTfIdf(costPair.getTfIdf());
                config.estimatedWeightOfSuggestedKeywordMap.put(keywordId, newOne);
            }

            config.groupedSuggestions = new HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>>();
            for (int labelId : this.groupedSuggestions.keySet()) {
                ArrayList<CostAndNodesOfAnswersPair> arr = new ArrayList<CostAndNodesOfAnswersPair>();
                for (CostAndNodesOfAnswersPair costPairOfLabelId : this.groupedSuggestions.get(labelId)) {
                    arr.add(config.estimatedWeightOfSuggestedKeywordMap.get(costPairOfLabelId.keywordIndex));
                }
                config.groupedSuggestions.put(labelId, arr);
            }

            config.resultTrees = new LinkedList<DemoResultTree>();

            //keeping a map for chosenResult
            HashMap<ResultTree, ResultTree> newOfOldResultTree = new HashMap<>();

            for (DemoResultTree originalResTree : this.resultTrees) {
                ResultTree newResTree = new ResultTree();
                newOfOldResultTree.put(originalResTree.tree, newResTree);

                newResTree.rootNode = originalResTree.tree.rootNode;
                newResTree.cost = originalResTree.tree.cost;

                for (int id : originalResTree.tree.createdTreeNode.keySet()) {
                    newResTree.createdTreeNode.put(id, originalResTree.tree.createdTreeNode.get(id));
                }

                for (ResultNode resNode : originalResTree.tree.anOutputTree.vertexSet()) {
                    newResTree.anOutputTree.addVertex(resNode);
                }

                for (RelationshipInfra relInfra : originalResTree.tree.anOutputTree.edgeSet()) {
                    newResTree.anOutputTree.addEdge(originalResTree.tree.anOutputTree.getEdgeSource(relInfra), originalResTree.tree.anOutputTree.getEdgeTarget(relInfra), relInfra);
                }

                DemoResultTree newDemoResTree = new DemoResultTree(originalResTree.treeId, newResTree);
                newDemoResTree.newEdges.addAll(originalResTree.newEdges);
                newDemoResTree.newNodes.addAll(originalResTree.newNodes);


                config.resultTrees.add(newDemoResTree);
            }


            config.choseResults = new HashSet<ResultTree>();
            for (ResultTree thisChosenResTree : this.choseResults) {
                config.choseResults.add(newOfOldResultTree.get(thisChosenResTree));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return config;
    }
}
