package dataset.graphSampler;

import java.awt.Dimension;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.ListenableUndirectedGraph;

import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;

import aqpeq.utilities.StringPoolUtility;
import aqpeq.utilities.Dummy.DummyProperties;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class GenerateGraphPortionFromCandidates {

	int[] candidatesNodeId = new int[] { 1609145, 1729086, 303106, 2470561, 4046907 };
	public static String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/dbpGraphInfra/graph/dbp/";

	private GraphInfraReaderArray graph;

	public static void main(String[] args) throws Exception {

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dataGraph")) {
				graphInfraPath = args[++i];
			}
		}

		DummyProperties.withProperties = false;
		GenerateGraphPortionFromCandidates g = new GenerateGraphPortionFromCandidates();
		g.run(args);
		//g.findIntersectionInNeighbors(args);
	}

	private void run(String[] args) throws Exception {

		HashSet<Integer> candidatesNodeIdSet = new HashSet<Integer>();
		for (int i : candidatesNodeId) {
			candidatesNodeIdSet.add(i);
		}

		graph = new GraphInfraReaderArray(graphInfraPath, false);

		graph.read();

		HashMap<Integer, SampleNode> sampleNodeOfNodeId = new HashMap<Integer, SampleNode>();
		HashSet<Integer> addedRelIds = new HashSet<Integer>();
		ListenableDirectedGraph<SampleNode, String> portionOfGraph = new ListenableDirectedGraph<SampleNode, String>(
				String.class);

		for (int srcNodeId : candidatesNodeId) {

			NodeInfra src = null;

			src = graph.nodeOfNodeId.get(srcNodeId);

			SampleNode sourceSampleNode = null;
			if (!sampleNodeOfNodeId.containsKey(srcNodeId)) {

				HashSet<String> lbls = new HashSet<String>();
				for (int tokenId : src.tokens) {
					lbls.add(StringPoolUtility.getStringOfId(tokenId));
				}

				HashMap<String, String> props = new HashMap<String, String>();
				int m = 0;
				if (src.getProperties() != null) {
					for (int tokenId : src.getProperties()) {
						props.put(m++ + "", StringPoolUtility.getStringOfId(tokenId));
					}
				}

				sourceSampleNode = new SampleNode(srcNodeId, lbls, props);
				portionOfGraph.addVertex(sourceSampleNode);
				sampleNodeOfNodeId.put(srcNodeId, sourceSampleNode);
				System.out.println("srcNodeId: " + srcNodeId + " lbl:" + sourceSampleNode.label);

			} else {
				sourceSampleNode = sampleNodeOfNodeId.get(srcNodeId);
			}

			for (int targetNodeId : graph.nodeOfNodeId.get(srcNodeId).getOutgoingRelIdOfSourceNodeId().keySet()) {

				// TO just get graph from node ids
				if (!candidatesNodeIdSet.contains(targetNodeId)) {
					continue;
				}

				NodeInfra target = null;

				target = graph.nodeOfNodeId.get(targetNodeId);

				SampleNode targetSampleNode = null;
				if (!sampleNodeOfNodeId.containsKey(targetNodeId)) {

					HashSet<String> lbls = new HashSet<String>();
					for (int tokenId : target.tokens) {
						lbls.add(StringPoolUtility.getStringOfId(tokenId));
					}

					HashMap<String, String> props = new HashMap<String, String>();
					int m = 0;
					if (src.getProperties() != null) {
						for (int tokenId : target.getProperties()) {
							props.put(m++ + "", StringPoolUtility.getStringOfId(tokenId));
						}
					}

					targetSampleNode = new SampleNode(targetNodeId, lbls, props);
					portionOfGraph.addVertex(targetSampleNode);
					sampleNodeOfNodeId.put(targetNodeId, targetSampleNode);
					System.out.println("targetNodeId: " + targetNodeId + " lbl:" + targetSampleNode.label);
				} else {
					targetSampleNode = sampleNodeOfNodeId.get(targetNodeId);
				}

				int relId = graph.nodeOfNodeId.get(srcNodeId).getOutgoingRelIdOfSourceNodeId().get(targetNodeId);
				RelationshipInfra rel = graph.relationOfRelId.get(relId);

				if (!addedRelIds.contains(relId)) {
					// HashSet<String> edgeTypes =
					// berkeleyDB.SearchEdgeTypeByRelId(relId);

					boolean res = portionOfGraph.addEdge(sourceSampleNode, targetSampleNode,
							relId + "" /* + ";" + edgeTypes.toString() */);
					if (!res) {
						System.err.println("rel id: " + relId + " cannot be added!");
					}
					System.out.println(sourceSampleNode.nodeId + "-->" + targetSampleNode.nodeId);
					addedRelIds.add(relId);
				}

			}
		}

		visualize(portionOfGraph);

	}

	private void findIntersectionInNeighbors(String[] args) throws Exception {

		HashSet<Integer> candidatesNodeIdSet = new HashSet<Integer>();
		for (int i : candidatesNodeId) {
			candidatesNodeIdSet.add(i);
		}

		graph = new GraphInfraReaderArray(graphInfraPath, false);

		graph.read();

		HashMap<Integer, HashSet<Integer>> neighborsOfNode = new HashMap<Integer, HashSet<Integer>>();

		for (RelationshipInfra relId : graph.relationOfRelId) {

			if (candidatesNodeIdSet.contains(relId.sourceId)) {
				neighborsOfNode.putIfAbsent(relId.sourceId, new HashSet<Integer>());
				neighborsOfNode.get(relId.sourceId).add(relId.destId);
			}

			if (candidatesNodeIdSet.contains(relId.destId)) {
				neighborsOfNode.putIfAbsent(relId.destId, new HashSet<Integer>());
				neighborsOfNode.get(relId.destId).add(relId.sourceId);
			}
		}

		HashSet<Integer> intersections = new HashSet<Integer>(neighborsOfNode.values().iterator().next());

		for (int neighborId : neighborsOfNode.keySet()) {
			intersections.retainAll(neighborsOfNode.get(neighborId));
		}

		System.out.println(intersections.size());

		System.out.println("intersections");
		for (int nodeId : intersections) {
			System.out.print(nodeId + ",");
		}

	}

	private void visualize(ListenableDirectedGraph<SampleNode, String> portionOfGraph) {
		JFrame frame;
		frame = new JFrame("graph portion");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

		// String nodeInfos = "";
		// for (SampleNode resultNode : portionOfGraph.vertexSet()) {
		// nodeInfos += resultNode.nodeId + " " + resultNode.label + " props:" +
		// resultNode.props + "\n";
		// }

		JGraphXAdapter<SampleNode, String> graphAdapter1 = new JGraphXAdapter<SampleNode, String>(portionOfGraph);

		mxIGraphLayout layout1 = new mxHierarchicalLayout(graphAdapter1);
		layout1.execute(graphAdapter1.getDefaultParent());

		JPanel prefixNodePanel = new JPanel();
		JPanel NodePanel = new JPanel();
		prefixNodePanel.setLayout(new BoxLayout(prefixNodePanel, BoxLayout.Y_AXIS));

		// JPanel titlePanel = new JPanel();
		// JLabel titleLabel = new JLabel("Result Tree");
		// titlePanel.add(titleLabel);

		JPanel gPanel = new JPanel();
		gPanel.setSize(new Dimension(800, 800));

		gPanel.add(new mxGraphComponent(graphAdapter1));

		gPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

		// prefixNodePanel.add(titlePanel);
		// NodePanel.add(gPanel);

		// prefixNodePanel.add(NodePanel);

		mainPanel.add(gPanel);

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.add(mainPanel);
		scrollPane.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, 500);

		frame.getContentPane().add(scrollPane);

		frame.pack();

		frame.setLocationByPlatform(true);
		frame.setVisible(true);

	}

}

class SampleNode {
	int nodeId;
	String label;
	String props;

	public SampleNode(int nodeId, HashSet<String> labels, HashMap<String, String> hashMap) {
		this.nodeId = nodeId;
		this.label = labels != null ? labels.toString() : "";
		this.props = hashMap != null ? hashMap.toString() : "";
	}

	@Override
	public String toString() {
		return this.nodeId + ", " + this.label + ", " + this.props;
	}
}
