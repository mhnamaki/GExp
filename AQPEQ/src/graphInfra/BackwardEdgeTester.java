package graphInfra;

import java.util.HashSet;

public class BackwardEdgeTester {

	public static void main(String[] args) throws Exception {
		String dataGraphPath = "/Users/mnamaki/AQPEQ/GraphExamples/k4Infra/";
		GraphInfraReaderArray graph = new GraphInfraReaderArray(dataGraphPath, true);
		graph.read();

		System.out.println("11:");
		System.out.println(graph.nodeOfNodeId.get(11).getIncomingRelIdOfSourceNodeId(graph));

//		System.out.println("2:");
//		System.out.println(graph.nodeOfNodeId.get(2).getIncomingRelIdOfSourceNodeId(graph));
	}

}
