// Generating BDB for inverted list 

package dataset.BerkeleyDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.sleepycat.je.Transaction;

import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import dataset.BerkeleyDB.BerkleleyDB.EdgeInfoObject;

import dataset.BerkeleyDB.BerkleleyDB.LabelNeighborObject;
import dataset.BerkeleyDB.BerkleleyDB.MyBDBObject;
import dataset.BerkeleyDB.BerkleleyDB.NodeInfoObject;
import dataset.BerkeleyDB.BerkleleyDB.NodeInfoWithProObject;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class GenerateBDB {

	// xin
	// private static String graphInfraPath =
	// "/Users/zhangxin/Desktop/AQPEQ/GraphExamples/k1Infra/graph/";
	//// "/Users/zhangxin/Desktop/IMDB/sample/";
	// private static GraphInfraReaderArray graph;
	// // private static double threshold = 0.3;
	// private static int cnt;
	// private static BerkleleyDB berkeleyDB;
	// private static String database = "database";
	// private static String catDatabase = "catDatabase";
	// private static String envFilePath =
	// "/Users/zhangxin/Desktop/AQPEQ/GraphExamples/k1Infra/dbEnv";
	// "/Users/zhangxin/Desktop/IMDB/sample/dbEnv";

	// mhn
	private static String graphInfraPath = "/Users/mnamaki/AQPEQ/GraphExamples/subgraphTest/graph/";
	private static GraphInfraReaderArray graph;
	// private static double threshold = 0.3;
	private static int cnt;
	private static BerkleleyDB berkeleyDB;
	private static String database = "database";
	private static String catDatabase = "catDatabase";
	private static String envFilePath = "/Users/mnamaki/AQPEQ/GraphExamples/subgraphTest/graph/dbEnvWithProp";

	private static boolean withProperties = true;
	private static boolean readProperties = true;

	private static HashSet<String> stopWord = DummyFunctions.getStopwordsSet();

	public static void main(String[] args) throws Exception {

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dataGraph")) {
				graphInfraPath = args[++i];
			} else if (args[i].equals("-database")) {
				database = args[++i];
			} else if (args[i].equals("-catDatabase")) {
				catDatabase = args[++i];
			} else if (args[i].equals("-envFilePath")) {
				envFilePath = args[++i];
			} else if (args[i].equals("-withProperties")) {
				withProperties = Boolean.parseBoolean(args[++i]);
			} else if (args[i].equals("-readProperties")) {
				readProperties = Boolean.parseBoolean(args[++i]);
			}
		}

		DummyProperties.withProperties = withProperties;
		DummyProperties.readProperties = readProperties;

		GenerateBDB gBDB = new GenerateBDB();
		if (withProperties) {// generate BDB with node and edge properties
			gBDB.initializeWithPro(database, catDatabase, envFilePath, gBDB);
		} else { // generate BDB without node and edge properties
			gBDB.initializeNoPro(database, catDatabase, envFilePath, gBDB);
		}

	}

	public void initializeNoPro(String database, String catDatabase, String envFilePath, GenerateBDB gBDB)
			throws Exception {

		graph = new GraphInfraReaderArray(graphInfraPath, true);
		graph.read();
		HashMap<String, HashSet<Integer>> nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);
		System.out.println("all keyword size -> " + nodeIdsOfToken.size());

		berkeleyDB = new BerkleleyDB(database, catDatabase, envFilePath);
		gBDB.generateTokenInfo(nodeIdsOfToken);// key: label + (property)
												// value:node id
		gBDB.generateNodeInfo();// key: node id value: label + (property) value

		System.out.println("after generateNodeInfoDBP");
		gBDB.generateEdgeInfo();// key: rel id value: type + (property)

		System.out.println("after generateEdgeInfoDBP");
		gBDB.generateLabelNeighborTable(nodeIdsOfToken);// key: token value:
														// neighbor token
		System.out.println("after generateLabelNeighborTable");
		berkeleyDB.CloseDatabase();
	}

	public void initializeWithPro(String database, String catDatabase, String envFilePath, GenerateBDB gBDB)
			throws Exception {
		graph = new GraphInfraReaderArray(graphInfraPath, true);

		graph.read();

		System.out.println("after reading the graph " + new java.util.Date());

		HashMap<String, HashSet<Integer>> nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);
		System.out.println("after nodeIdsOfToken " + new java.util.Date());

		System.out.println("all keyword size -> " + nodeIdsOfToken.size());

		berkeleyDB = new BerkleleyDB(database, catDatabase, envFilePath);
		System.out.println("generating inverted list for nodes " + new java.util.Date());
		gBDB.generateTokenInfo(nodeIdsOfToken);// key: label + (property)
												// value:node id

		System.out.println("generating NodeInfo " + new java.util.Date());
		gBDB.generateNodeInfoWithPro();// key: node id value: label + (property)
										// value

		System.out.println("generating EdgeInfo " + new java.util.Date());
		gBDB.generateEdgeInfo();// key: rel id value: type

		System.out.println("generating LabelNeighborTable " + new java.util.Date());
		gBDB.generateLabelNeighborTable(nodeIdsOfToken);// key: token value:
														// neighbor token

		berkeleyDB.CloseDatabase();
	}

	// TODO: after test, add synonym back
	public void generateTokenInfo(HashMap<String, HashSet<Integer>> nodeIdsOfToken) throws Exception {
		// nodeIdsOfToken
		// key: token value: nodeId
		cnt = 0;
		Transaction txn = BerkleleyDB.environment.beginTransaction(null, null);
		for (String token : nodeIdsOfToken.keySet()) {

			ArrayList<String> synonym = new ArrayList<String>();
			if (!stopWord.contains(token)) {
				MyBDBObject object = new MyBDBObject(synonym, nodeIdsOfToken.get(token));
				berkeleyDB.InsertTransformation(txn, token, object);
			}
			cnt++;
			if (cnt % 500000 == 0) {
				System.out.println(cnt);
				txn.commit();
				txn = BerkleleyDB.environment.beginTransaction(null, null);
			}
		}
		txn.commit();
	}

	public void generateNodeInfo() throws Exception {
		ArrayList<NodeInfra> nodeIdList = graph.nodeOfNodeId;
		cnt = 0;
		Transaction txn = BerkleleyDB.environment.beginTransaction(null, null);
		for (NodeInfra node : nodeIdList) {
			NodeInfoObject object = new NodeInfoObject(node.labels);
			berkeleyDB.InsertNodeInfo(txn, node.nodeId, object);
			cnt++;
			if (cnt % 500000 == 0) {
				System.out.println(cnt);
				txn.commit();
				txn = BerkleleyDB.environment.beginTransaction(null, null);

			}
		}
		txn.commit();
	}

	public void generateEdgeInfo() throws Exception {
		ArrayList<RelationshipInfra> edgeIdList = graph.relationOfRelId;
		cnt = 0;
		Transaction txn = BerkleleyDB.environment.beginTransaction(null, null);
		for (RelationshipInfra edge : edgeIdList) {
			if (edge != null) {
				EdgeInfoObject object = new EdgeInfoObject(edge.types);
				berkeleyDB.InsertEdgeInfo(txn, edge.relId, object);
			}
			cnt++;
			if (cnt % 500000 == 0) {
				System.out.println(cnt);
				txn.commit();
				txn = BerkleleyDB.environment.beginTransaction(null, null);
			}
		}
		txn.commit();
	}

	public void generateNodeInfoWithPro() throws Exception {
		ArrayList<NodeInfra> nodeIdList = graph.nodeOfNodeId;
		cnt = 0;
		Transaction txn = BerkleleyDB.environment.beginTransaction(null, null);
		for (NodeInfra node : nodeIdList) {
			NodeInfoWithProObject object = new NodeInfoWithProObject(node.labels, node.getProperties());
			berkeleyDB.InsertNodeInfoWithPro(txn, node.nodeId, object);
			cnt++;
			if (cnt % 500000 == 0) {
				System.out.println(cnt);
				txn.commit();
				txn = BerkleleyDB.environment.beginTransaction(null, null);
			}
		}
		txn.commit();
	}

	public void generateLabelNeighborTable(HashMap<String, HashSet<Integer>> nodeIdsOfToken) throws Exception {
		// nodeIdsOfToken
		// key: token value: nodeId
		cnt = 0;
		Transaction txn = BerkleleyDB.environment.beginTransaction(null, null);
		for (String token : nodeIdsOfToken.keySet()) {

			// if it's not a stop word
			if (!stopWord.contains(token)) {

				HashSet<String> neighborTermsSet = new HashSet<String>();

				// get node ids contain that keyword
				HashSet<Integer> nodeIds = nodeIdsOfToken.get(token);

				// for each node that contains the keyword
				for (int sourceNodeId : nodeIds) {

					NodeInfra sourceNode = graph.nodeOfNodeId.get(sourceNodeId);

					// get next node id
					for (int targetNodeId : sourceNode.getOutgoingRelIdOfSourceNodeId().keySet()) {

						// add the labels of next node id
						for (String label : graph.nodeOfNodeId.get(targetNodeId).getLabels()) {
							for (String t : DummyFunctions.getTokensOfALabel(label)) {

								if (stopWord.contains(t))
									continue;

								neighborTermsSet.add(t);
							}

						}

						// add the prop value of next node id
						if (graph.nodeOfNodeId.get(targetNodeId).getProperties() != null) {
							for (String value : graph.nodeOfNodeId.get(targetNodeId).getProperties().values()) {
								for (String t : DummyFunctions.getTokensOfALabel(value)) {

									if (stopWord.contains(t))
										continue;

									neighborTermsSet.add(t);
								}
							}
						}
					}
				}

				LabelNeighborObject object = new LabelNeighborObject(neighborTermsSet);
				berkeleyDB.InsertLabelNeighbor(txn, token, object);
			}
			cnt++;
			if (cnt % 500000 == 0) {
				System.out.println(cnt);

				txn.commit();
				txn = BerkleleyDB.environment.beginTransaction(null, null);
			}
		}
		txn.commit();
	}

}

// public void generateTokenInfo(HashMap<String, HashSet<Integer>>
// nodeIdsOfToken) throws Exception {
//// TODO: change this part
// File wordNet = new File("WordNet-3.0/dict");
// System.setProperty("wordnet.database.dir", wordNet.toString());
// WordNetDatabase databaseSynonym = WordNetDatabase.getFileInstance();
//// nodeIdsOfToken
//// key: token value: nodeId
// cnt = 0;
// for (String token : nodeIdsOfToken.keySet()) {
//
// if (token.length() < 3)
// continue;
//
// ArrayList<String> synonym = new ArrayList<String>();
// if (!stopWord.contains(token)) {
// if (databaseSynonym.getSynsets(token).length > 0) {
// Synset[] synsets = databaseSynonym.getSynsets(token);
// if (synsets.length > 0) {
// for (int i = 0; i < synsets.length; i++) {
// String[] wordForms = synsets[i].getWordForms();
// for (int j = 0; j < wordForms.length; j++) {
// synonym.add(wordForms[j]);
// }
// }
// }
// }
// MyBDBObject object = new MyBDBObject(synonym, nodeIdsOfToken.get(token));
// berkeleyDB.InsertTransformation(token, object);
// }
// cnt++;
// if (cnt % 500000 == 0) {
// System.out.println(cnt);
// }
// }
// }
