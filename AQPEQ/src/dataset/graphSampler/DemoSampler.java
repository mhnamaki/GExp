package dataset.graphSampler;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
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

public class DemoSampler {

	private static String graphInfraPath = "/Users/zhangxin/Desktop/DBP/untitled folder/";
	private static String dataset = "dep";
	final String verticesFileName = "Vertices.in";
	final String relationshipsFileName = "Relationships.in";
	private static String keywords = "Jessica;Chastain,Taylor;Swift,AnneHathaway";
	//private static String keywords = "western;philosophy";

	GraphInfraReaderArray graph;
	ArrayList<NodeInfra> nodeOfNodeId = new ArrayList<NodeInfra>();
	ArrayList<RelationshipInfra> relationOfRelId = new ArrayList<RelationshipInfra>();
	HashMap<Integer, HashSet<Integer>> nodeIdsOfToken = new HashMap<Integer, HashSet<Integer>>();
	HashSet<RelationshipInfra> edgeSet = new HashSet<RelationshipInfra>();
	HashSet<NodeInfra> nodeSet = new HashSet<NodeInfra>();
	HashMap<Integer, Integer> oldIdToNewId = new HashMap<Integer, Integer>();
	static int numberOfNode = 40;

	public DemoSampler() {

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
		DemoSampler sampler = new DemoSampler();
		sampler.loadGraph();
		sampler.sample();
		sampler.readAndWriteVertices();
		sampler.readAndWriteRelationships();

	}

	public void loadGraph() throws Exception {
		boolean addBackward = false;
		graph = new GraphInfraReaderArray(graphInfraPath, addBackward);
		graph.read();
		nodeOfNodeId = graph.nodeOfNodeId;
		relationOfRelId = graph.relationOfRelId;
		nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);
	}

	public void sample() throws Exception {

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
						
						HashSet<Integer> relIdsOther1 = new HashSet<>(otherNode1.outgoingRelIdOfSourceNodeId.values());

						for (int relIdOther1 : relIdsOther1) {
							RelationshipInfra relOther1 = relationOfRelId.get(relIdOther1);
							edgeSet.add(relOther1);
							NodeInfra otherNode2 = nodeOfNodeId.get(relOther1.destId);
							nodeSet.add(otherNode2);
							
							HashSet<Integer> relIdsOther2 = new HashSet<>(otherNode2.outgoingRelIdOfSourceNodeId.values());

							for (int relIdOther2 : relIdsOther2) {
								RelationshipInfra relOther2 = relationOfRelId.get(relIdOther2);
								edgeSet.add(relOther2);
								NodeInfra otherNode3 = nodeOfNodeId.get(relOther2.destId);
								nodeSet.add(otherNode3);
							}
						}
					}
					
				}

				HashSet<Integer> relIdsIncoming = new HashSet<>(node.getIncomingRelIdOfSourceNodeId(graph).values());

				for (int relId : relIdsIncoming) {
					RelationshipInfra rel = relationOfRelId.get(relId);
					edgeSet.add(rel);
					NodeInfra otherNode = nodeOfNodeId.get(rel.sourceId);
					nodeSet.add(otherNode);
					
					HashSet<Integer> relIdsIncomingOther = new HashSet<>(otherNode.getIncomingRelIdOfSourceNodeId(graph).values());

					for (int relIdOther : relIdsIncomingOther) {
						RelationshipInfra relOther = relationOfRelId.get(relIdOther);
						edgeSet.add(relOther);
						NodeInfra otherNode1 = nodeOfNodeId.get(relOther.sourceId);
						nodeSet.add(otherNode1);
						
						HashSet<Integer> relIdsIncomingOther1 = new HashSet<>(otherNode1.getIncomingRelIdOfSourceNodeId(graph).values());

						for (int relIdOther1 : relIdsIncomingOther1) {
							RelationshipInfra relOther1 = relationOfRelId.get(relIdOther1);
							edgeSet.add(relOther1);
							NodeInfra otherNode2 = nodeOfNodeId.get(relOther1.sourceId);
							nodeSet.add(otherNode2);
							
							HashSet<Integer> relIdsIncomingOther2 = new HashSet<>(otherNode2.getIncomingRelIdOfSourceNodeId(graph).values());

							for (int relIdOther2 : relIdsIncomingOther2) {
								RelationshipInfra relOther2 = relationOfRelId.get(relIdOther2);
								edgeSet.add(relOther2);
								NodeInfra otherNode3 = nodeOfNodeId.get(relOther2.sourceId);
								nodeSet.add(otherNode3);
							}
						}
						
					}
				}
			}

		}

		System.out.println("node size = " + nodeSet.size());
		System.out.println("edge size = " + edgeSet.size());

	}

	private void readAndWriteRelationships() throws Exception {

		Writer bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(dataset + relationshipsFileName), "UTF-8"));

		int relId = 0;
		for (RelationshipInfra rel : edgeSet) {
			bw.write(relId + "#" + oldIdToNewId.get(rel.sourceId) + "#" + oldIdToNewId.get(rel.destId) + "#");

			if (!rel.types.isEmpty()) {
				String typeStr = "";
				int cnt = 0;
				for (int typeId : rel.types) {
					if (cnt < rel.types.size()) {
						typeStr += StringPoolUtility.getStringOfId(typeId) + " ";
					} else {
						typeStr += StringPoolUtility.getStringOfId(typeId);
					}
					cnt++;
				}

				bw.write(typeStr);
			} else {
				bw.write("");
			}

			relId++;
			bw.write("#" + "\n");
		}

		bw.close();

	}

	private void readAndWriteVertices() throws Exception {

		Writer bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(dataset + verticesFileName), "UTF-8"));

		int nodeId = 0;
		for (NodeInfra node : nodeSet) {

			bw.write(nodeId + "#");
			bw.write(node.inDegree + "#");
			bw.write(node.outDegree + "#");
			String labels = "";
			for (int tokenId : node.tokens) {
				labels += StringPoolUtility.getStringOfId(tokenId) + ";";
			}

			bw.write(labels + "#");

			String propStr = "";
			boolean key = true;
			if (node.getProperties() != null) {
				for (int proId : node.getProperties()) {
					if (key) {
						propStr += StringPoolUtility.getStringOfId(proId) + ":";
						key = false;
					} else {

						propStr += StringPoolUtility.getStringOfId(proId) + ";";
						key = true;
					}
				}
			}

			bw.write(propStr);

			bw.write("\n");

			oldIdToNewId.put(node.nodeId, nodeId);

			nodeId++;
		}

		bw.close();

	}

}
