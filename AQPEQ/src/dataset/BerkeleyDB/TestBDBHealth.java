package dataset.BerkeleyDB;

import java.util.HashMap;
import java.util.HashSet;

import com.sleepycat.je.DatabaseException;

import aqpeq.utilities.Dummy.DummyProperties;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class TestBDBHealth {

	private static String graphPath = "/Users/zhangxin/Desktop/DBP/graph";
	private static String envPath = "/Users/zhangxin/Desktop/DBP/dbEnvNoProp";
	private static GraphInfraReaderArray graph;
	private static BerkleleyDB berkeleyDB;
	private static String database = "database";
	private static String catDatabase = "catDatabase";

	public static void main(String[] args) throws Exception, Exception {
		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("-dataGraph")) {
				graphPath = args[++i];
			} else if (args[i].equals("-envFilePath")) {
				envPath = args[++i];
			} else if (args[i].equals("-withProperties")) {
				DummyProperties.withProperties = Boolean.parseBoolean(args[++i]);
				DummyProperties.readProperties = Boolean.parseBoolean(args[++i]);
			}
		}

		DummyProperties.withProperties = false;
		DummyProperties.readProperties = false;

		graph = new GraphInfraReaderArray(graphPath, true);
		graph.readWithNoLabels();
		berkeleyDB = new BerkleleyDB(database, catDatabase, envPath);

		int cntTotal = 0;
		int cntErr = 0;
		HashMap<String, HashSet<Integer>> nodeExceptionMessages = new HashMap<String, HashSet<Integer>>();
		for (NodeInfra node : graph.nodeOfNodeId) {
			cntTotal++;
			try {
				berkeleyDB.SearchNodeInfoWithPro(node.nodeId);
			} catch (Exception exc) {
				cntErr++;
				nodeExceptionMessages.putIfAbsent(exc.getMessage(), new HashSet<Integer>());
				nodeExceptionMessages.get(exc.getMessage()).add(node.nodeId);
			}
			if (cntTotal % 100000 == 0) {
				System.out.println("cntTotal: " + cntTotal + ", cntErr: " + cntErr);
			}
		}

		for (String msg : nodeExceptionMessages.keySet()) {
			System.out.println(msg);
			for (Integer nodeId : nodeExceptionMessages.get(msg)) {
				System.out.print(nodeId + ", ");
			}
			System.out.println();
		}

		// edge info
		cntTotal = 0;
		cntErr = 0;
		HashMap<String, HashSet<Integer>> edgeExceptionMessages = new HashMap<String, HashSet<Integer>>();
		for (RelationshipInfra rel : graph.relationOfRelId) {
			cntTotal++;
			try {
				berkeleyDB.SearchEdgeTypeByRelId(rel.relId);
			} catch (Exception exc) {
				cntErr++;
				edgeExceptionMessages.putIfAbsent(exc.getMessage(), new HashSet<Integer>());
				edgeExceptionMessages.get(exc.getMessage()).add(rel.relId);
			}
			if (cntTotal % 500000 == 0) {
				System.out.println("cntTotal: " + cntTotal + ", cntErr: " + cntErr);
			}
		}

		for (String msg : edgeExceptionMessages.keySet()) {
			System.out.println(msg);
			for (Integer relID : edgeExceptionMessages.get(msg)) {
				System.out.print(relID + ", ");
			}
			System.out.println();
		}

	}

}
