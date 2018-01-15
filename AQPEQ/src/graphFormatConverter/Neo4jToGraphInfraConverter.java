package graphFormatConverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

public class Neo4jToGraphInfraConverter {
	static GraphDatabaseService dataGraph;
	// public String dataGraphPath =
	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbpSampledBuffalo/dbpSampledBuffalo";
	// public String newDataGraphPath =
	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbpSampledBuffalo/graph/";
	final String verticesFileName = "vertices.in";
	final String relationshipsFileName = "relationships.in";

	// xin
	public static String dataGraphPath = "/Users/mnamaki/AQPEQ/GraphExamples/simpler/simpler/";
	public static String newDataGraphPath = "/Users/mnamaki/AQPEQ/GraphExamples/simpler/graph/";

	int currentNodeId = 0;
	int currentEdgeId = 0;
	HashMap<Integer, Integer> newNodeIdOfNeo4jNodeId = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> newEdgeIdOfNeo4jNodeId = new HashMap<Integer, Integer>();

	public static void main(String[] args) throws Exception {

		if (args.length == 2) {
			dataGraphPath = args[0];
			newDataGraphPath = args[1];
		}

		Neo4jToGraphInfraConverter converter = new Neo4jToGraphInfraConverter();
		converter.initGraph();

		Transaction tx1 = dataGraph.beginTx();
		converter.convert();

		System.out.println("program is finished");

		tx1.success();
		tx1.close();
		tx1 = null;
		dataGraph.shutdown();
		dataGraph = null;
	}

	private void initGraph() {
		File storeDir = new File(dataGraphPath);
		dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "1M")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").setConfig("cache_type", "none")
				.newGraphDatabase();

		System.out.println("data graph inited!");

	}

	private void convert() throws Exception {

		for (Node node : dataGraph.getAllNodes()) {

			if (node.hasLabel(Label.label("hasKeyword")))
				continue;

			newNodeIdOfNeo4jNodeId.put((int) node.getId(), currentNodeId);
			currentNodeId++;
		}

		System.out.println("after initializing the node id map!");

		for (Relationship rel : dataGraph.getAllRelationships()) {

			if (rel.isType(RelationshipType.withName("hasKeyword")))
				continue;

			newEdgeIdOfNeo4jNodeId.put((int) rel.getId(), currentEdgeId);
			currentEdgeId++;
		}
		System.out.println("after initializing the edge id map!");

		readAndWriteVertices();

		readAndWriteRelationships();

	}

	private void readAndWriteRelationships() throws Exception {

		Writer bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(newDataGraphPath + relationshipsFileName), "UTF-8"));

		for (Relationship rel : dataGraph.getAllRelationships()) {

			if (rel.isType(RelationshipType.withName("hasKeyword")))
				continue;

			Map<String, Object> props = rel.getAllProperties();
			String propStr = "";
			for (String key : props.keySet()) {

				String value = props.get(key).toString();
				value = getCleanString(value);
				key = getCleanString(key);

				if (key.equals("") || value.equals(""))
					continue;

				propStr += key + ":" + getCleanString(value) + ";";
			}
			if (propStr.length() > 0)
				propStr = propStr.substring(0, propStr.length() - 1);

			bw.write(newEdgeIdOfNeo4jNodeId.get((int) rel.getId()) + "#"
					+ newNodeIdOfNeo4jNodeId.get((int) rel.getStartNodeId()) + "#"
					+ newNodeIdOfNeo4jNodeId.get((int) rel.getEndNodeId()) + "#" + getCleanString(rel.getType().name())
					+ "#" + propStr);

			bw.write("\n");
		}

		bw.close();

	}

	private void readAndWriteVertices() throws Exception {

		// File fout = new File();
		// FileOutputStream fos = new FileOutputStream(fout);
		// BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		Writer bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(newDataGraphPath + verticesFileName), "UTF-8"));

		for (Node node : dataGraph.getAllNodes()) {

			if (node.hasLabel(Label.label("hasKeyword")))
				continue;

			bw.write(newNodeIdOfNeo4jNodeId.get((int) node.getId()) + "#");
			bw.write(node.getDegree(Direction.INCOMING) + "#");
			bw.write(node.getDegree(Direction.OUTGOING) + "#");
			String labels = "";
			for (Label lbl : node.getLabels()) {
				labels += lbl.name().replaceAll(";", " ").replaceAll("#", " ") + ";";
			}
			if (labels.length() > 0)
				labels = labels.substring(0, labels.length() - 1);

			bw.write(labels + "#");

			Map<String, Object> props = node.getAllProperties();
			String propStr = "";
			for (String key : props.keySet()) {

				if (key.equals("nodeId") || key.equals("oldNodeId") || key.equals("uri")
						|| key.toLowerCase().equals("index"))
					continue;

				String value = props.get(key).toString();
				value = value.replaceAll(":", " ").replaceAll("#", " ").replaceAll(";", " ");

				key = key.replaceAll(":", " ").replaceAll("#", " ").replaceAll(";", " ");

				if (key.equals("") || value.equals(""))
					continue;

				propStr += key + ":" + value + ";";
			}

			if (propStr.length() > 0)
				propStr = propStr.substring(0, propStr.length() - 1);

			bw.write(propStr);

			bw.write("\n");
		}

		bw.close();
	}

	private String getCleanString(String str) {
		return str.replaceAll(":", " ").replaceAll("#", " ").replaceAll(";", " ");
	}

}
