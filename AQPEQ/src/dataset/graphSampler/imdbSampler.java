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

public class imdbSampler {

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
	static int numberOfEdge = 40;
	
	HashMap<Integer, String> nodeLine = new HashMap<Integer, String>();
	HashMap<Integer, String> edgeLine = new HashMap<Integer, String>();

	public imdbSampler() {

	}

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dataGraph")) {
				graphInfraPath = args[++i];
			} else if (args[i].equals("-dataset")) {
				dataset = args[++i];
			} else if (args[i].equals("-numberOfEdge")) {
				numberOfEdge = Integer.parseInt(args[++i]);
			}
		}

		DummyProperties.withProperties = true;
		DummyProperties.readRelType = true;
		DummyProperties.debugMode = true;
		imdbSampler sampler = new imdbSampler();
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
		

		String nodePath = graphInfraPath + "/vertices.in";
		readNode(nodePath);
		
		String edgePath = graphInfraPath + "/relationships.in";
		readEdge(edgePath);
	}

	public void sample() throws Exception {
		
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

		System.out.println("node size = " + nodeSet.size());
		System.out.println("edge size = " + edgeSet.size());

	}

	private void readAndWriteRelationships() throws Exception {

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

	private void readAndWriteVertices() throws Exception {

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
	
	public void readNode(String filePath) {

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
				nodeLine.put(nodeId, line);
			}

			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	
	public void readEdge(String filePath) {

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
				
				edgeLine.put(nodeId, line);
			}

			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

}
