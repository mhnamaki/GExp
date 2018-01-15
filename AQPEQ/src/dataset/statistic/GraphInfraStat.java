package dataset.statistic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.joda.time.DateTime;

import aqpeq.utilities.StringPoolUtility;
import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class GraphInfraStat {

	private static GraphInfraReaderArray graph;
	private static String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/dbpGraphInfra/graph/dbp/";
	private static boolean addBackward = true;
	private static HashMap<Integer, HashSet<Integer>> nodeIdsOfToken;

	public static void main(String[] args) throws Exception {

		DummyProperties.withProperties = false;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dataGraph")) {
				graphInfraPath = args[++i];
			} else if (args[i].equals("-withProperties")) {
				DummyProperties.withProperties = Boolean.parseBoolean(args[++i]);
			}
		}

		// GraphInfraStat.readRelationships();

		graph = new GraphInfraReaderArray(graphInfraPath, addBackward);

		System.out.println("before read: " + new DateTime());
		graph.read();

		NodeInfra node = graph.nodeOfNodeId.get(1284808);

		for (int targetId : node.getOutgoingRelIdOfSourceNodeId().keySet()) {
			System.out.println(
					"targetId: " + targetId + ": relId:" + node.getOutgoingRelIdOfSourceNodeId().get(targetId));
			for (int tokenId : graph.nodeOfNodeId.get(targetId).getTokens()) {
				System.out.print(StringPoolUtility.getStringOfId(tokenId) + ", ");
			}
			System.out.println();
		}

		// System.out.println("after read: " + new DateTime());
		//
		// System.out.println("before nodeIdsOfToken: " + new DateTime());
		// nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);
		// System.out.println("after nodeIdsOfToken: " + new DateTime());
		//
		// System.out.println("nodes: " + graph.nodeOfNodeId.size());
		// System.out.println("edges: " + graph.relationOfRelId.size());
		// System.out.println("nodeIdsOfToken size: " + nodeIdsOfToken.size());
		// System.out.println("number of stored tokens: " +
		// StringPoolUtility.getCurrentAutoIncrement());
		//
		// nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);
		//
		// System.out.println("starting sleep");
		// Thread.sleep(60000);
		// System.out.println("program is finsihed!");

	}

	private static void readRelationships() throws Exception {

		FileInputStream fis = new FileInputStream(graphInfraPath + "relationships.in");
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		HashSet<String> edgeType = new HashSet<String>();

		int relCnt = 0;
		String line = null;
		while ((line = br.readLine()) != null) {
			relCnt++;
			if (relCnt % 1000000 == 0)
				System.out.println("relCnt: " + relCnt);

			String[] splittedRelLine = line.split("#");

			if (splittedRelLine.length > 3) {
				if (splittedRelLine[3].trim().length() > 0)
					edgeType.add(splittedRelLine[3].trim().toLowerCase());
			}
		}
		br.close();

		System.out.println("edgetypes: " + edgeType.size());
	}
}
