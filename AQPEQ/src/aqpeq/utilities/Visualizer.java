package aqpeq.utilities;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.ListenableUndirectedGraph;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxFastOrganicLayout;

import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;

import aqpeq.utilities.Dummy.DummyFunctions;

import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;
import pairwiseBasedKWS.PairwiseAnswer;

import queryExpansion.CostAndNodesOfAnswersPair;

public class Visualizer {

	public static void visualizePatternWithDuplicateMatches() {

	}

	private static void createAndShowGui(LinkedList<ResultTree> fullOutputsQueue, GraphInfraReaderArray graph,
			HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap,
			Collection<String> keywords) throws Exception {

		JFrame frame;
		if (estimatedWeightOfSuggestedKeywordMap == null) {
			frame = new JFrame("initial query");
		} else {
			frame = new JFrame("expanded query");
		}

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		int cols = fullOutputsQueue.size();
		while (!fullOutputsQueue.isEmpty()) {
			ResultTree theWholeResultTree = fullOutputsQueue.poll();
			ListenableUndirectedGraph<ResultNode, RelationshipInfra> resultTree = theWholeResultTree.anOutputTree;

			HashSet<Integer> nodeIdsInResultTree = new HashSet<Integer>();
			String nodeInfos = "";
			for (ResultNode resultNode : resultTree.vertexSet()) {
				nodeIdsInResultTree.add(resultNode.nodeId);

				NodeInfra nodeInfra = null;
				try {
					nodeInfra = graph.nodeOfNodeId.get(resultNode.nodeId);
				} catch (Exception exc) {
					System.err.println(resultNode.nodeId);
					System.err.println(exc.getMessage());
				}

				String propsStr = "";

				if (nodeInfra != null) {
					resultNode.node = nodeInfra;
					for (int tokenId : nodeInfra.tokens) {
						resultNode.labels += StringPoolUtility.getStringOfId(tokenId) + ", ";
					}

					if (nodeInfra.getProperties() != null) {
						for (int propId : nodeInfra.getProperties()) {
							propsStr += StringPoolUtility.getStringOfId(propId) + ", ";
						}
					}
				}

				nodeInfos += resultNode.nodeId + " " + resultNode.labels + " props:" + propsStr + "\n";

			}

			JGraphXAdapter<ResultNode, RelationshipInfra> graphAdapter1 = new JGraphXAdapter<ResultNode, RelationshipInfra>(
					resultTree);

			mxIGraphLayout layout1 = new mxHierarchicalLayout(graphAdapter1);
			layout1.execute(graphAdapter1.getDefaultParent());

			JPanel prefixNodePanel = new JPanel();
			JPanel NodePanel = new JPanel();
			prefixNodePanel.setLayout(new BoxLayout(prefixNodePanel, BoxLayout.Y_AXIS));
			NodePanel.setLayout(new GridLayout(2, cols));
			JPanel titlePanel = new JPanel();
			JLabel titleLabel = new JLabel("Result Tree");
			titlePanel.add(titleLabel);

			JPanel gPanel = new JPanel();
			gPanel.setLayout(new GridLayout(1, cols));
			gPanel.add(new mxGraphComponent(graphAdapter1));

			gPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

			JTextArea textArea = new JTextArea(10, 20);
			JScrollPane scrollInfoPane = new JScrollPane(textArea);
			textArea.setEditable(false);

			String text = DummyFunctions.getStringOutOfCollection(keywords, ";") + "\n";
			text += "nodes: " + resultTree.vertexSet().size() + "\n";
			text += "edges: " + resultTree.edgeSet().size() + "\n";
			text += "cost: " + theWholeResultTree.cost + "\n";

			// getting frequency and neighborhood information:

			text += nodeInfos + "\n";

			// HashMap<Integer, NodeInfo> nodeInfosOfNode = new HashMap<Integer,
			// NodeInfo>();
			// for each node find in-deg, out-deg, neighbors token types.
			// for (ResultNode resultNode : resultTree.vertexSet()) {

			// int inDegree =
			// graph.nodeOfNodeId.get(resultNode.nodeId).inDegree;
			// int outDegree =
			// graph.nodeOfNodeId.get(resultNode.nodeId).outDegree;

			// HashMap<String, Integer> freqOfNeighborTokenType = new
			// HashMap<String, Integer>();

			// for (int otherNodeId :
			// graph.nodeOfNodeId.get(resultNode.nodeId).outgoingRelIdOfSourceNodeId.keySet())
			// {
			//
			// NodeInfra otherNode = graph.nodeOfNodeId.get(otherNodeId);
			//
			// if (nodeIdsInResultTree.contains(otherNode.nodeId))
			// continue;
			//
			// for (String lbl : otherNode.getLabels()) {
			// String cleanLbl = DummyFunctions.getCleanedString(lbl);
			// for (String token : DummyFunctions.getTokens(cleanLbl)) {
			// freqOfNeighborTokenType.putIfAbsent(token, 0);
			// freqOfNeighborTokenType.put(token,
			// freqOfNeighborTokenType.get(token) + 1);
			// }
			// }
			//
			// }

			// nodeInfosOfNode.put(resultNode.nodeId, new NodeInfo(inDegree,
			// outDegree, freqOfNeighborTokenType));

			// }

			// for (int nodeId : nodeInfosOfNode.keySet()) {
			// text += nodeId + " => " + nodeInfosOfNode.get(nodeId).toStr();
			// }

			// get the total of neighbors type frequency
			// HashMap<String, Integer> totalFreqOfNeighborTokenTypeOfResultTree
			// = new HashMap<String, Integer>();
			// for (int nodeId : nodeInfosOfNode.keySet()) {
			// for (String token :
			// nodeInfosOfNode.get(nodeId).freqOfNeighborTokenType.keySet()) {
			// totalFreqOfNeighborTokenTypeOfResultTree.putIfAbsent(token, 0);
			// totalFreqOfNeighborTokenTypeOfResultTree.put(token,
			// totalFreqOfNeighborTokenTypeOfResultTree.get(token)
			// +
			// nodeInfosOfNode.get(nodeId).freqOfNeighborTokenType.get(token));
			// }
			// }

			// text += "\n total info: \n";
			// Map<String, Integer> map =
			// MapUtil.sortByValueDesc(totalFreqOfNeighborTokenTypeOfResultTree);
			// for (Map.Entry<String, Integer> entry : map.entrySet()) {
			// text += entry.getKey() + ":" + entry.getValue() + "\n";
			// }

			if (estimatedWeightOfSuggestedKeywordMap != null) {
				text += "\n SUGGESTED KEYWORDS (Overall expanded cost): \n";
				for (int newKeyword : estimatedWeightOfSuggestedKeywordMap.keySet()) {
					text += newKeyword + " cost: " + estimatedWeightOfSuggestedKeywordMap.get(newKeyword).cost + "\n";
				}
			}

			textArea.setText(text);

			prefixNodePanel.add(titlePanel);
			NodePanel.add(gPanel);
			NodePanel.add(scrollInfoPane);
			prefixNodePanel.add(NodePanel);

			mainPanel.add(prefixNodePanel);

		}

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.add(mainPanel);
		scrollPane.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, 500);

		frame.getContentPane().add(scrollPane);

		frame.pack();

		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}

	public static void visualizeOutput(LinkedList<ResultTree> fullOutputsQueue, GraphInfraReaderArray graph,
			HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap,
			Collection<String> keywords) throws Exception {
		createAndShowGui(fullOutputsQueue, graph, estimatedWeightOfSuggestedKeywordMap, keywords);
	}

	public static void visualizePairwise(LinkedList<PairwiseAnswer> fullOutputsQueue, GraphInfraReaderArray graph,
			HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap,
			ArrayList<String> keywords) throws Exception {
		// when visualizeOutput, we add estimatedWeightOfSuggestedKeywordMap
		createAndShowPairwise(fullOutputsQueue, graph, estimatedWeightOfSuggestedKeywordMap, keywords);

	}

	private static void createAndShowPairwise(LinkedList<PairwiseAnswer> fullOutputsQueue, GraphInfraReaderArray graph,
			HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap,
			ArrayList<String> keywords) {
		// TODO: showing pairwise results including the query suggestion part
	}

}

class NodeInfo {
	public int inDegree;
	public int outDegree;
	public HashMap<String, Integer> freqOfNeighborTokenType;

	public NodeInfo(int inDegree, int outDegree, HashMap<String, Integer> freqOfNeighborTokenType) {
		this.inDegree = inDegree;
		this.outDegree = outDegree;
		this.freqOfNeighborTokenType = freqOfNeighborTokenType;
	}

	public String toStr() {
		String res = "in-deg: " + inDegree + ", out-deg: " + outDegree + "\n";

		Map<String, Integer> map = MapUtil.sortByValueDesc(freqOfNeighborTokenType);
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			res += entry.getKey() + ":" + entry.getValue() + "\n";
		}

		return res;
	}
}
