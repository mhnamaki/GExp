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

public class CreateDBPDatasetWithImport {

	private static String dataGraphPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/dbp";
	private static String edgesInfo = "/Users/mnamaki/Documents/workspace/wsu.eecs.mlkd.KGQuery/oldEdgesInformations.txt";

	public static void main(String[] args) throws Exception {

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dataGraph")) {
				dataGraphPath = args[++i];
			} else if (args[i].equals("-edgesInfo")) {
				edgesInfo = args[++i];
			}
		}

		File storeDir = new File(dataGraphPath);

		FileInputStream fisEdges = new FileInputStream(edgesInfo);

		GraphDatabaseService dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "8g")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();

		Transaction tx1 = dataGraph.beginTx();

		HashMap<Long, Long> newNodeIdOfOldNodeId = new HashMap<Long, Long>();
		// HashMap<Long, Long> oldNodeIdOfNewNodeId = new HashMap<Long, Long>();

		int cnt = 0;

		for (Node node : dataGraph.getAllNodes()) {
			long oldNodeId = Long.parseLong(node.getProperty("oldNodeId").toString());
			newNodeIdOfOldNodeId.put(oldNodeId, node.getId());
		}

		tx1.success();
		tx1.close();

		BufferedReader brEdges = new BufferedReader(new InputStreamReader(fisEdges));
		String lineEdges = null;
		
		tx1 = dataGraph.beginTx();
		
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
			if (cnt % 100000 == 0) {
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
