package dataset.graphmaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

public class CreateDBPDatasetFromFiles {

	private static String dataGraphPath = "dbpedia_3_2_1.db";
	private static String nodesInfo = "/Users/mnamaki/Documents/workspace/wsu.eecs.mlkd.KGQuery/nodesInformations.txt";
	private static String edgesInfo = "/Users/mnamaki/Documents/workspace/wsu.eecs.mlkd.KGQuery/edgesInformations.txt";

	public static void main(String[] args) throws Exception {

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dataGraph")) {
				dataGraphPath = args[++i];
			} else if (args[i].equals("-nodesInfo")) {
				nodesInfo = args[++i];
			} else if (args[i].equals("-edgesInfo")) {
				edgesInfo = args[++i];
			}
		}

		File storeDir = new File(dataGraphPath);
		FileInputStream fisNodes = new FileInputStream(nodesInfo);
		FileInputStream fisEdges = new FileInputStream(edgesInfo);

		GraphDatabaseService dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "8g")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();

		Transaction tx1 = dataGraph.beginTx();

		BufferedReader brNodes = new BufferedReader(new InputStreamReader(fisNodes));
		BufferedReader brEdges = new BufferedReader(new InputStreamReader(fisEdges));

		HashMap<Long, Long> newNodeIdOfOldNodeId = new HashMap<Long, Long>();
		// HashMap<Long, Long> oldNodeIdOfNewNodeId = new HashMap<Long, Long>();

		double s;
		double splitDuration = 0d;
		double createNodeDuration = 0d;
		double addLabelsDuration = 0d;
		double setPropertiesDuration = 0d;
		double commitDuration = 0d;

		int cnt = 0;
		String lineNode = null;
		// 1;[SpatialThing,Country,france,];[__URI__:http://dbpedia.org/resource/France,]
		while ((lineNode = brNodes.readLine()) != null) {
			cnt++;
			s = System.nanoTime();
			String[] splittedLine = lineNode.split(";");
			splitDuration += ((System.nanoTime() - s) / 1e6);

			s = System.nanoTime();
			Node node = dataGraph.createNode();
			createNodeDuration += ((System.nanoTime() - s) / 1e6);

			// splittedLine[0] id
			newNodeIdOfOldNodeId.put(Long.parseLong(splittedLine[0]), node.getId());
			// oldNodeIdOfNewNodeId.put(node.getId(),
			// Long.parseLong(splittedLine[0]));

			// splittedLine[1] labels
			// adding labels
			s = System.nanoTime();
			String[] labels = splittedLine[1].split(",");
			for (String lbl : labels) {
				if (lbl.length() > 2) {
					node.addLabel(Label.label(lbl.replace("[", "").replace("]", "")));
				}
			}
			addLabelsDuration += ((System.nanoTime() - s) / 1e6);

			// splittedLine[2] keyValuePairs (props) (attrs)
			s = System.nanoTime();
			String[] props = splittedLine[2].split(",");
			for (String prop : props) {
				if (prop.length() > 2) {
					String[] keyValue = prop.replace("[", "").replace("]", "").split("\\#\\:\\$");
					node.setProperty(keyValue[0], keyValue[1]);
				}
			}
			setPropertiesDuration += ((System.nanoTime() - s) / 1e6);

			if (cnt % 50000 == 0) {
				s = System.nanoTime();
				tx1.success();
				tx1.close();
				tx1 = dataGraph.beginTx();
				commitDuration += ((System.nanoTime() - s) / 1e6);
				System.out.println(cnt);

				// if (cnt > 30000)
				// break;
			}

		}
		brNodes.close();

		System.out.println("splitDuration: " + splitDuration);
		// System.out.println("replaceDuration: " + replaceDuration);
		System.out.println("createNodeDuration: " + createNodeDuration);
		System.out.println("addLabelsDuration: " + addLabelsDuration);
		System.out.println("setPropertiesDuration: " + setPropertiesDuration);
		System.out.println("commitDuration: " + commitDuration);

		System.out.println("nodemap.txt");
		// writing the map:
		File fout = new File("nodemap.txt");
		FileOutputStream fos = new FileOutputStream(fout);

		BufferedWriter bwNodeMap = new BufferedWriter(new OutputStreamWriter(fos));

		for (Long oldNodeId : newNodeIdOfOldNodeId.keySet()) {
			bwNodeMap.write(oldNodeId + ";" + newNodeIdOfOldNodeId.get(oldNodeId) + "\n");
		}
		bwNodeMap.flush();
		bwNodeMap.close();

		// 0;0;[http://www.w3.org/2000/01/rdf-schema#seeAlso];[__URI__:http://www.w3.org/2000/01/rdf-schema#seeAlso]
		// splittedLine[0]: old startNodeId
		// splittedLine[1]: old endNodeId
		// splittedLine[2]: relationType
		// splittedLine[3]: props
		String lineEdges = null;
		while ((lineEdges = brEdges.readLine()) != null) {

			String[] splittedLine = lineEdges.split(";");
			Long oldStartNodeId = Long.parseLong(splittedLine[0]);
			Long newStartNodeId = newNodeIdOfOldNodeId.get(oldStartNodeId);

			Long oldEndNodeId = Long.parseLong(splittedLine[1]);
			Long newEndNodeId = newNodeIdOfOldNodeId.get(oldEndNodeId);

			if (newStartNodeId == null || newEndNodeId == null) {
				if (newStartNodeId == null) {
					System.err.println(newStartNodeId + " is not there!");
				}
				if (newEndNodeId == null) {
					System.err.println(newEndNodeId + " is not there!");
				}

				continue;
			}

			// relation type
			String relationType = splittedLine[2].replace("[", "").replace("]", "");

			Node startNode = dataGraph.getNodeById(newStartNodeId);
			Node endNode = dataGraph.getNodeById(newEndNodeId);
			Relationship relationship = startNode.createRelationshipTo(endNode,
					DynamicRelationshipType.withName(relationType));

			// props
			String[] props = splittedLine[3].split(",");
			for (String prop : props) {
				if (prop.length() > 2) {
					String[] keyValue = prop.replace("[", "").replace("]", "").split("\\#\\:\\$");
					relationship.setProperty(keyValue[0], keyValue[1]);
				}
			}
			cnt++;
			if (cnt % 10000 == 0) {
				tx1.success();
				tx1.close();
				tx1 = dataGraph.beginTx();
				System.out.println(cnt);
			}
		}
		brEdges.close();

		tx1.success();
		tx1.close();

		dataGraph.shutdown();
		System.out.println("program is finished properly!");
	}
}
