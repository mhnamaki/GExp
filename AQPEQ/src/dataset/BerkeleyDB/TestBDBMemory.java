package dataset.BerkeleyDB;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import com.sleepycat.je.DatabaseException;

import aqpeq.utilities.Dummy;
import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;

public class TestBDBMemory {

	// BDB
	private static String database = "database";
	private static String catDatabase = "catDatabase";
	private static String envFilePath = "/Users/zhangxin/Desktop/citation/dbEnvWithProp";
	private static BerkleleyDB berkeleyDB;
	private static String graphInfraPath = "/Users/zhangxin/Desktop/citation/";
	// private static boolean withProperties = false;
	static String keyword = "Paul,D.,Clough";

	public TestBDBMemory() {

	}

	public static void main(String[] args) throws Exception {

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-keyword")) {
				keyword = args[++i];
			} else if (args[i].equals("-dataGraph")) {
				graphInfraPath = args[++i];
			} else if (args[i].equals("-envFilePath")) {
				envFilePath = args[++i];
			}
		}

		TestBDBMemory test = new TestBDBMemory();
		DummyProperties.withProperties = true;
		berkeleyDB = new BerkleleyDB(database, catDatabase, envFilePath);
		ArrayList<String> keywords = new ArrayList<String>();
		// String actor = "Jim Carrey";
		// keywords.add("Jim");
		// keywords.add("Carrey");
		// keywords.add("stand-up");
		// keywords.add("comedy");
		// LeBron James
		// keywords.add("LeBron");
		// keywords.add("James");
		// cleveland cavaliers
		// keywords.add("cleveland");
		// keywords.add("cavaliers");
		// buffalo wild wings
		// keywords.add("buffalo");
		// keywords.add("wild");
		// keywords.add("wings");
		// Wenfei Fan
		// keywords.add("Wenfei");
		// keywords.add("Fan");
		// Paul D Clough
		// keywords.add("Paul");
		// keywords.add("Clough");
		//Journal Control Science  Engineering
		String[] keywordList = keyword.split(",");
		for (int i = 0; i < keywordList.length; i++) {
			keywords.add(keywordList[i]);
		}

		test.findInformation(keywords);
		berkeleyDB.CloseDatabase();
	}

	public void findInformation(ArrayList<String> keywords) throws Exception {
		System.out.println(keywords);
		HashSet<Integer> candidatesSet = berkeleyDB.SearchNodeIdsByKeyword(keywords);
		// System.out.println(candidatesSet);
		for (int nodeId : candidatesSet) {
			NodeInfra node = new NodeInfra(nodeId);
			node = berkeleyDB.SearchNodeInfoWithPro(nodeId);
			System.out.println(nodeId + " --- " + node.labels + " --- " + node.getProperties().values());
			// src -> dest -> relIds (multi-relationships)
		}
		int[] neighbor = {1966174, 2829865, 3037531, 2661793, 3290347, 2743061, 1966115, 3128034};
		for (int id : neighbor) {
			NodeInfra node = new NodeInfra(id);
			node = berkeleyDB.SearchNodeInfoWithPro(id);
			System.out.print(node.labels);
			if (node.getProperties() != null) {
				System.out.print( " --- " + node.getProperties().values());
			}
			System.out.println();
		}
//		GraphInfraReaderArray graph = new GraphInfraReaderArray(graphInfraPath, true);
//		graph.readWithNoLabels();
//		for (int candidate : candidatesSet) {
//			NodeInfra nodeA = graph.nodeOfNodeId.get(candidate);
//			HashMap<Integer, Integer> neighborA = nodeA.outgoingRelIdOfSourceNodeId;
//			System.out.println(candidate + " -> " + neighborA.keySet());
//			for (int nodeId : neighborA.keySet()) {
//				NodeInfra node = new NodeInfra(nodeId);
//				node = berkeleyDB.SearchNodeInfoWithPro(nodeId);
//				System.out.print(node.labels);
//				if (nodeA.getProperties() != null) {
//					System.out.print( " --- " + nodeA.getProperties().values());
//				}
//				System.out.println();
//			}
//		}
	}

}
