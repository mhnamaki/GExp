package demo;

import java.util.*;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import graphInfra.NodeInfra;
import org.jgrapht.graph.ListenableUndirectedGraph;

import graphInfra.RelationshipInfra;
import neo4jBasedKWS.ResultNode;
import queryExpansion.CostAndNodesOfAnswersPair;

public class KWSHandler {

    public DR DRHandler;
    public ST STHnadler;
    public SG SGHnadler;

//    private LinkedList<DemoResultTree> resultTrees = new LinkedList<DemoResultTree>();
//    private HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>> sugKeywords = new HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>>();

    public Configuration config;
    String KWS = "";

    public KWSHandler(Configuration config) throws Exception {

        this.config = config;

        System.out.println("Initializing KWSHandler");

        this.KWS = config.KWS;
        String DBName = config.DBName;

        switch (KWS) {
            case "DR":
                this.DRHandler = new DR(DBName, config);
                break;
            case "ST":
                this.STHnadler = new ST(DBName, config);
                break;
            case "SG":
                this.SGHnadler = new SG(DBName, config);
                break;
        }

    }

    public LinkedList<DemoResultTree> Search() throws Exception {
    	LinkedList<DemoResultTree> resultTrees = new LinkedList<DemoResultTree>();
        switch (config.KWS) {
            case "DR":
                resultTrees = DRHandler.Search(config.searchInc);
                System.out.println("Debug in DRHandler");
                System.out.println("----------------");
                for (DemoResultTree rt : resultTrees) {
                    ListenableUndirectedGraph<ResultNode, RelationshipInfra> anOutputTree = rt.tree.anOutputTree;
                    for (ResultNode node : anOutputTree.vertexSet()) {
                        System.out.println(node.nodeId + ": " + node.labels);
                    }
                }
                break;
            case "ST":
                resultTrees = STHnadler.Search(config.searchInc);
                System.out.println("Debug in STHandler");
                System.out.println("----------------");
                for (DemoResultTree rt : resultTrees) {
                    ListenableUndirectedGraph<ResultNode, RelationshipInfra> anOutputTree = rt.tree.anOutputTree;
                    for (ResultNode node : anOutputTree.vertexSet()) {
                        System.out.println(node.nodeId + ": " + node.labels);
                    }
                }
                break;
            case "SG":
                resultTrees = SGHnadler.Search(config.searchInc);
                System.out.println("Debug in SGHandler");
                System.out.println("----------------");
                for (DemoResultTree rt : resultTrees) {
                    ListenableUndirectedGraph<ResultNode, RelationshipInfra> anOutputTree = rt.tree.anOutputTree;
                    for (ResultNode node : anOutputTree.vertexSet()) {
                        System.out.println(node.nodeId + ": " + node.labels);
                    }
                }
                break;
        }
		return resultTrees;
    }

    public HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>> Explore() throws Exception {
    	HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>> sugKeywords = new HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>>();
        switch (config.KWS) {
            case "DR":
                sugKeywords = DRHandler.Explore();
//			// DRHandler.dr.rootKWSExpand.bestKeywordInfo
//			//After we click the new keyword, we want to directly show the results tree
//			DRHandler.dr.incEval = new IncEval(DRHandler.dr.graph, DRHandler.dr.rootKWSExpand.bestKeywordInfo,
//					DRHandler.dr.rootKWSExpand.topNAnswers, this.config.getExplorationRange(),
//					DummyProperties.KWSSetting.DISTINCTROOT);
//			DRHandler.dr.incEval.incEval();
//			this.resultTrees = DRHandler.updateResultTrees(DRHandler.dr.graph, this.resultTrees,
//					DRHandler.dr.rootKWSExpand.bestKeywordInfo, DRHandler.dr.incEval);
                break;
            case "ST":
                sugKeywords = STHnadler.Explore();
                break;
            case "SG":
                sugKeywords = SGHnadler.Explore();
                break;
        }
		return sugKeywords;
    }

//    public LinkedList<DemoResultTree> getResultTrees() {
//        return resultTrees;
//    }

//	public HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>> getSugKeywords() {
//		return sugKeywords;
//	}

    public MutableNetwork<NodeInfra, RelationshipInfra> createTree(DemoResultTree resultTree) throws Exception {

        ListenableUndirectedGraph<ResultNode, RelationshipInfra> anOutputTree = resultTree.tree.anOutputTree;
        MutableNetwork<NodeInfra, RelationshipInfra> graph = NetworkBuilder.undirected().allowsParallelEdges(true)
                .build();

        Set<ResultNode> vertices = anOutputTree.vertexSet();

        HashMap<Integer, ResultNode> nodeIdNode = new HashMap<Integer, ResultNode>();
        for (ResultNode node : vertices) {
            nodeIdNode.put(node.nodeId, node);
        }

        for (RelationshipInfra relInfra : anOutputTree.edgeSet()) {
            ResultNode srcNode = nodeIdNode.get(relInfra.sourceId);
            ResultNode targetNode = nodeIdNode.get(relInfra.destId);
            // graph.addEdge(srcNode.nodeId + ": " + srcNode.labels,
            // targetNode.nodeId + ": " + targetNode.labels,
            // relInfra);
            graph.addEdge(srcNode.node, targetNode.node, relInfra);

        }

        // LinkedList<ResultNode> queue = new LinkedList<ResultNode>();
        // queue.add(resultTree.rootNode);
        //
        // HashMap<ResultNode, String> visitedOldNodes = new HashMap<ResultNode,
        // String>();
        //
        // while (!queue.isEmpty()) {
        // ResultNode currentOldNode = queue.poll();
        // for (RelationshipInfra relInfra :
        // anOutputTree.edgesOf(currentOldNode)) {
        // ResultNode srcNode = anOutputTree.getEdgeSource(relInfra);
        // ResultNode targetNode = anOutputTree.getEdgeTarget(relInfra);
        // ResultNode otherNode = (srcNode==currentOldNode) ? targetNode :
        // srcNode;
        // if (visitedOldNodes.containsKey(otherNode)) {
        //// tree.addEdge(visitedOldNodes.get(currentOldNode),
        // visitedOldNodes.get(otherNode), relInfra.relId);
        // graph.addEdge(visitedOldNodes.get(currentOldNode),
        // visitedOldNodes.get(otherNode), relInfra.relId);
        // } else {
        // queue.add(otherNode);
        // }
        // visitedOldNodes.put(currentOldNode, currentOldNode.nodeId + "; " +
        // currentOldNode.labels);
        // }
        // }

        // // for debug
        // System.out.println("Debug in CreateTree in Dr.java");
        // for (ResultNode node : anOutputTree.vertexSet()) {
        // System.out.println(node.nodeId + ": " + node.labels);
        // }
        // for (RelationshipInfra edge : anOutputTree.edgeSet()) {
        // int src = anOutputTree.getEdgeSource(edge).nodeId;
        // int des = anOutputTree.getEdgeTarget(edge).nodeId;
        // System.out.println("edge" + edge.relId + ": " + src + " -> " + des);
        // }
        // System.out.println("Done!");
        //
        // Set<ResultNode> vertices = anOutputTree.vertexSet();
        //
        // // Set<RelationshipInfra> edges = resultTree.anOutputTree.edgeSet();
        //
        // MutableCTreeNetwork<String, Integer> tree =
        // TreeNetworkBuilder.builder().expectedNodeCount(vertices.size())
        // .build();
        //
        // int rootId = resultTree.rootNode.nodeId;
        // String rootLabel = "";
        // int j = 1;
        // for (int i : dr.graph.nodeOfNodeId.get(rootId).tokens) {
        // rootLabel += StringPoolUtility.getStringOfId(i);
        // if (j < dr.graph.nodeOfNodeId.get(rootId).tokens.size()) {
        // rootLabel += ";";
        // }
        // j++;
        // }
        // tree.addNode(rootLabel);
        // // tree.addNode(String.valueOf(rootId));
        //
        // HashMap<Integer, LinkedHashSet<String>> edgeMap = new
        // HashMap<Integer, LinkedHashSet<String>>();
        // for (RelationshipInfra e : anOutputTree.edgeSet()) {
        // int src = anOutputTree.getEdgeSource(e).nodeId;
        // int des = anOutputTree.getEdgeTarget(e).nodeId;
        //
        // System.out.println("edge" + e.relId + ": " + src + " -> " + des);
        //
        // if (!edgeMap.containsKey(des)) {
        // LinkedHashSet<String> srcS = new LinkedHashSet<String>();
        // srcS.add(String.valueOf(src) + "," + String.valueOf(e.relId));
        // edgeMap.put(des, srcS);
        // } else {
        // edgeMap.get(des).add(String.valueOf(src) + "," +
        // String.valueOf(e.relId));
        // }
        // }
        //
        // PriorityQueue<Integer> queue = new PriorityQueue<Integer>();
        // queue.add(rootId);
        //
        // while (!queue.isEmpty()) {
        // int curId = queue.poll();
        // HashSet<String> srcS = edgeMap.get(curId);
        // if (srcS != null) {
        // for (String str : srcS) {
        // String[] strTem = str.split(",");
        // String curULabel = "";
        // int cntU = 1;
        // for (int i : dr.graph.nodeOfNodeId.get(curId).tokens) {
        // curULabel += StringPoolUtility.getStringOfId(i);
        // if (cntU < dr.graph.nodeOfNodeId.get(curId).tokens.size()) {
        // curULabel += ";";
        // }
        // cntU++;
        // }
        //
        // String curVLabel = "";
        // int cntV = 1;
        // for (int i :
        // dr.graph.nodeOfNodeId.get(Integer.parseInt(strTem[0])).tokens) {
        // curVLabel += StringPoolUtility.getStringOfId(i);
        // if (cntV <
        // dr.graph.nodeOfNodeId.get(Integer.parseInt(strTem[0])).tokens.size())
        // {
        // curVLabel += ";";
        // }
        // cntV++;
        // }
        //
        // tree.addEdge(curULabel, curVLabel, Integer.parseInt(strTem[1]));
        // queue.add(Integer.parseInt(strTem[0]));
        // }
        // }
        // }
        //
//        System.out.println("graph edges: " + graph.edges().size());
//        System.out.println("graph nodes: " + graph.nodes().size());

        return graph;
    }
}
