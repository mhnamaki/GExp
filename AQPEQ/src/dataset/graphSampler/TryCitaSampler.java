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

public class TryCitaSampler {

	private static String graphInfraPath = "/Users/zhangxin/AQPEQ/GraphExamples/GenTemTest/graph2/";
	private static String newPath = "/Users/zhangxin/Desktop/apweb/";
	final String verticesFileName = "sampleVertices.in";
	final String relationshipsFileName = "smapleRelationships.in";

	GraphInfraReaderArray graph;
	ArrayList<NodeInfra> nodeOfNodeId;
	ArrayList<RelationshipInfra> relationOfRelId;
	HashSet<RelationshipInfra> edgeSet = new HashSet<RelationshipInfra>();
	HashSet<NodeInfra> nodeSet = new HashSet<NodeInfra>();
	HashMap<Integer, Integer> oldIdToNewId = new HashMap<Integer, Integer>();
	static int numberOfEdge = 5;

	public TryCitaSampler() {

	}

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dataGraph")) {
				graphInfraPath = args[++i];
			} else if (args[i].equals("-newPath")) {
				newPath = args[++i];
			} else if (args[i].equals("-numberOfEdge")) {
				numberOfEdge = Integer.parseInt(args[++i]);
			}
		}

		DummyProperties.withProperties = true;
		DummyProperties.debugMode = false;
		TryCitaSampler sampler = new TryCitaSampler();
		sampler.loadGraph();
		sampler.sample(numberOfEdge);
		sampler.readAndWriteVertices();
		sampler.readAndWriteRelationships();

	}

	public void loadGraph() throws Exception {
		boolean addBackward = false;
		graph = new GraphInfraReaderArray(graphInfraPath, addBackward);
		// if (!usingBDB) {
		graph.read();
		nodeOfNodeId = graph.nodeOfNodeId;
		relationOfRelId = graph.relationOfRelId;
	}

	public void sample(int numberOfEdge) {
		int startNodeId = 1;
		NodeInfra startNode = nodeOfNodeId.get(startNodeId);

		while (edgeSet.size() < numberOfEdge) {

			HashSet<Integer> relIds = new HashSet<>(startNode.outgoingRelIdOfSourceNodeId.values());

			for (int relId : relIds) {
				RelationshipInfra rel = relationOfRelId.get(relId);
				edgeSet.add(rel);
				nodeSet.add(nodeOfNodeId.get(rel.sourceId));
				nodeSet.add(nodeOfNodeId.get(rel.destId));
			}

			startNodeId++;
			startNode = nodeOfNodeId.get(startNodeId);

		}

	}

	private void readAndWriteRelationships() throws Exception {

		Writer bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(newPath + relationshipsFileName), "UTF-8"));

		int relId = 0;
		for (RelationshipInfra rel : edgeSet) {
			bw.write(relId + "#" + oldIdToNewId.get(rel.sourceId) + "#" + oldIdToNewId.get(rel.destId) + "#" + "" + "#" + "");
			relId++;
			bw.write("\n");
		}

		bw.close();

	}

	private void readAndWriteVertices() throws Exception {

		Writer bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(newPath + verticesFileName), "UTF-8"));

		int nodeId = 0;
		for (NodeInfra node : nodeSet) {

			bw.write(nodeId + "#");
			bw.write(node.inDegree + "#");
			bw.write(node.outDegree + "#");
			String labels = "";
			for (int tokenId : node.tokens) {
				labels += StringPoolUtility.getStringOfId(tokenId);
			}

			bw.write(labels + "#");

			String propStr = "";
			boolean key = true;
			if (node.getProperties() != null){
				for (int proId : node.getProperties()) {
					if(key){
						propStr += StringPoolUtility.getStringOfId(proId) + ":";
						key = false;
					}else {
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
