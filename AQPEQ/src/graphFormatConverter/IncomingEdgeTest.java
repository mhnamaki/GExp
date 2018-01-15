package graphFormatConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class IncomingEdgeTest {
	private static String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/amazon/amazonGraphInfra/";
	private static GraphInfraReaderArray graph;

	public static void main(String[] args) throws Exception {

		graph = new GraphInfraReaderArray(graphInfraPath, false);
		graph.read();
		System.out.println("finish read");

		ArrayList<NodeInfra> nodes = new ArrayList<NodeInfra>();

		for (NodeInfra node : graph.nodeOfNodeId) {
			nodes.add(node);
		}

		Collections.sort(nodes, new Comparator<NodeInfra>() {
			@Override
			public int compare(NodeInfra o1, NodeInfra o2) {
				return Integer.compare(o2.getDegree(), o1.getDegree());
			}
		});

		int cnt = 0;

		for (NodeInfra node : nodes) {

			cnt++;

			System.out.println(node.nodeId + ", inDeg:" + node.inDegree + ", outDeg:" + node.outDegree + ", lbls:"
					+ node.getLabels());

			for (Integer destNode : node.getOutgoingRelIdOfSourceNodeId().keySet()) {

				RelationshipInfra relInfra = graph.relationOfRelId
						.get(node.getOutgoingRelIdOfSourceNodeId().get(destNode));

				String print = "rel type:" + relInfra.types;

				print += " next node: " + graph.nodeOfNodeId.get(destNode).getLabels();
				System.out.println(print);

			}
			System.out.println();

			if (cnt == 10) {

				break;

			}

		}

	}

}
