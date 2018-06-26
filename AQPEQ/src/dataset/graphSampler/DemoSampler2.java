package dataset.graphSampler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import aqpeq.utilities.StringPoolUtility;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class DemoSampler2 {

	// private static String graphInfraPath =
	// "/Users/zhangxin/Desktop/DBP/untitled folder/";
	private static String graphInfraPath = "/Users/zhangxin/AQPEQ/GraphExamples/demo/sample";
	private static String dataset = "dep";
	final String verticesFileName = "Vertices.in";
	final String relationshipsFileName = "Relationships.in";
	// private static String keywords =
	// "Jessica;Chastain,Taylor;Swift,AnneHathaway";
	private static String keywords = "wisconsin;floyd";

	GraphInfraReaderArray graph;
	ArrayList<NodeInfra> nodeOfNodeId = new ArrayList<NodeInfra>();
	ArrayList<RelationshipInfra> relationOfRelId = new ArrayList<RelationshipInfra>();
	HashMap<Integer, HashSet<Integer>> nodeIdsOfToken = new HashMap<Integer, HashSet<Integer>>();
	HashSet<RelationshipInfra> edgeSet = new HashSet<RelationshipInfra>();
	HashSet<NodeInfra> nodeSet = new HashSet<NodeInfra>();
	HashMap<Integer, Integer> oldIdToNewId = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> oldIdToNewIdEdge = new HashMap<Integer, Integer>();
	static int numberOfNode = 40;

	// HashMap<Integer, String> nodeLine = new HashMap<Integer, String>();
	// HashMap<Integer, String> edgeLine = new HashMap<Integer, String>();

	public DemoSampler2() {

	}

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dataGraph")) {
				graphInfraPath = args[++i];
			} else if (args[i].equals("-dataset")) {
				dataset = args[++i];
			} else if (args[i].equals("-numberOfNode")) {
				numberOfNode = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-keywords")) {
				keywords = args[++i];
			}
		}

		DummyProperties.withProperties = true;
		DummyProperties.readRelType = true;
		DummyProperties.debugMode = true;
		DemoSampler2 sampler = new DemoSampler2();
		sampler.loadGraph();

	}

	public void loadGraph() throws Exception {
		boolean addBackward = false;
		graph = new GraphInfraReaderArray(graphInfraPath, addBackward);
		graph.read();
		nodeOfNodeId = graph.nodeOfNodeId;
		relationOfRelId = graph.relationOfRelId;
		nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);

		String nodePath = graphInfraPath + "/vertices.in";
		ArrayList<String> nodeLine = readNode(nodePath);

		String edgePath = graphInfraPath + "/relationships.in";
		ArrayList<String> edgeLine = readEdge(edgePath);

		sample();
		readAndWriteVertices(nodeLine);
		readAndWriteRelationships(edgeLine);

	}

	public void sample() throws Exception {

		System.out.println("Begin sample");

		String[] keywordSet = keywords.split(",");
		for (String keyword : keywordSet) {
			HashSet<Integer> nodeIds1 = new HashSet<Integer>();
			if (keyword.contains(";")) {
				String[] kwTem = keyword.split(";");
				String kw1 = kwTem[0].toLowerCase().trim();
				int kwID1 = StringPoolUtility.getIdOfStringFromPool(kw1);
				nodeIds1 = nodeIdsOfToken.get(kwID1);
				String kw2 = kwTem[1].toLowerCase().trim();
				int kwID2 = StringPoolUtility.getIdOfStringFromPool(kw2);
				HashSet<Integer> nodeIds2 = nodeIdsOfToken.get(kwID2);
				nodeIds1.retainAll(nodeIds2);
			} else {
				nodeIds1 = nodeIdsOfToken.get(StringPoolUtility.getIdOfStringFromPool(keyword));
			}
			for (int id : nodeIds1) {
				System.out.println(keyword + " id = " + id);
				NodeInfra node = nodeOfNodeId.get(id);
				nodeSet.add(node);

				HashSet<Integer> relIds = new HashSet<>(node.outgoingRelIdOfSourceNodeId.values());

				for (int relId : relIds) {
					RelationshipInfra rel = relationOfRelId.get(relId);
					edgeSet.add(rel);
					NodeInfra otherNode = nodeOfNodeId.get(rel.destId);
					nodeSet.add(otherNode);

					HashSet<Integer> relIdsOther = new HashSet<>(otherNode.outgoingRelIdOfSourceNodeId.values());

					for (int relIdOther : relIdsOther) {
						RelationshipInfra relOther = relationOfRelId.get(relIdOther);
						edgeSet.add(relOther);
						NodeInfra otherNode1 = nodeOfNodeId.get(relOther.destId);
						nodeSet.add(otherNode1);

//						HashSet<Integer> relIdsOther1 = new HashSet<>(otherNode1.outgoingRelIdOfSourceNodeId.values());
//
//						for (int relIdOther1 : relIdsOther1) {
//							RelationshipInfra relOther1 = relationOfRelId.get(relIdOther1);
//							edgeSet.add(relOther1);
//							NodeInfra otherNode2 = nodeOfNodeId.get(relOther1.destId);
//							nodeSet.add(otherNode2);
//
//							 HashSet<Integer> relIdsOther2 = new HashSet<>(
//							 otherNode2.outgoingRelIdOfSourceNodeId.values());
//							
//							 for (int relIdOther2 : relIdsOther2) {
//							 RelationshipInfra relOther2 =
//							 relationOfRelId.get(relIdOther2);
//							 edgeSet.add(relOther2);
//							 NodeInfra otherNode3 =
//							 nodeOfNodeId.get(relOther2.destId);
//							 nodeSet.add(otherNode3);
//							 }
//						}
					}

				}

				HashSet<Integer> relIdsIncoming = new HashSet<>(node.getIncomingRelIdOfSourceNodeId(graph).values());

				for (int relId : relIdsIncoming) {
					RelationshipInfra rel = relationOfRelId.get(relId);
					edgeSet.add(rel);
					NodeInfra otherNode = nodeOfNodeId.get(rel.sourceId);
					nodeSet.add(otherNode);

					HashSet<Integer> relIdsIncomingOther = new HashSet<>(
							otherNode.getIncomingRelIdOfSourceNodeId(graph).values());

					for (int relIdOther : relIdsIncomingOther) {
						RelationshipInfra relOther = relationOfRelId.get(relIdOther);
						edgeSet.add(relOther);
						NodeInfra otherNode1 = nodeOfNodeId.get(relOther.sourceId);
						nodeSet.add(otherNode1);

//						HashSet<Integer> relIdsIncomingOther1 = new HashSet<>(
//								otherNode1.getIncomingRelIdOfSourceNodeId(graph).values());
//
//						for (int relIdOther1 : relIdsIncomingOther1) {
//							RelationshipInfra relOther1 = relationOfRelId.get(relIdOther1);
//							edgeSet.add(relOther1);
//							NodeInfra otherNode2 = nodeOfNodeId.get(relOther1.sourceId);
//							nodeSet.add(otherNode2);
//
//							HashSet<Integer> relIdsIncomingOther2 = new HashSet<>(
//									otherNode2.getIncomingRelIdOfSourceNodeId(graph).values());
//
//							for (int relIdOther2 : relIdsIncomingOther2) {
//								RelationshipInfra relOther2 = relationOfRelId.get(relIdOther2);
//								edgeSet.add(relOther2);
//								NodeInfra otherNode3 = nodeOfNodeId.get(relOther2.sourceId);
//								nodeSet.add(otherNode3);
//							}
//						}

					}
				}
			}

		}

		System.out.println("node size = " + nodeSet.size());
		System.out.println("edge size = " + edgeSet.size());

		// System.out.println("total node size = " + nodeLine.size());
		// System.out.println("total edge size = " + edgeLine.size());

	}

	private void readAndWriteRelationships(ArrayList<String> edgeLine) throws Exception {

		System.out.println("Begin write edge");

		Writer bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(dataset + relationshipsFileName), "UTF-8"));

		int relId = 0;
		for (RelationshipInfra rel : edgeSet) {
			bw.write(relId + "#" + oldIdToNewId.get(rel.sourceId) + "#" + oldIdToNewId.get(rel.destId) + "#"
					+ edgeLine.get(rel.relId) + "\n");

			relId++;
		}

		bw.close();

	}

	private void readAndWriteVertices(ArrayList<String> nodeLine) throws Exception {

		System.out.println("Begin write node");

		Writer bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(dataset + verticesFileName), "UTF-8"));

		int nodeId = 0;

		for (NodeInfra node : nodeSet) {
			bw.write(nodeId + "#1#1#" + nodeLine.get(node.nodeId) + "\n");

			oldIdToNewId.put(node.nodeId, nodeId);

			nodeId++;
		}

		bw.close();

	}

	public ArrayList<String> readNode(String filePath) {

		System.out.println("Begin generate node map");

		ArrayList<String> list = new ArrayList<String>();

		File file = new File(filePath);
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file));

			BufferedReader br = new BufferedReader(reader);
			String line = "";
			while ((line = br.readLine()) != null) {

				int index = line.indexOf("#");
				int nodeId = Integer.parseInt(line.substring(0, index));
				line = line.substring(index + 1);
				int indexI = line.indexOf("#");
				line = line.substring(indexI + 1);
				int indexO = line.indexOf("#");
				line = line.substring(indexO + 1);
				list.add(line);
			}

			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;

	}

	public ArrayList<String> readEdge(String filePath) {

		System.out.println("Begin generate edge map");

		ArrayList<String> list = new ArrayList<String>();

		File file = new File(filePath);
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file));

			BufferedReader br = new BufferedReader(reader);
			String line = "";
			while ((line = br.readLine()) != null) {

				int index = line.indexOf("#");
				int nodeId = Integer.parseInt(line.substring(0, index));
				line = line.substring(index + 1);
				int indexSr = line.indexOf("#");
				line = line.substring(indexSr + 1);
				int indexDes = line.indexOf("#");
				line = line.substring(indexDes + 1);

				list.add(line);
			}

			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;

	}

}
