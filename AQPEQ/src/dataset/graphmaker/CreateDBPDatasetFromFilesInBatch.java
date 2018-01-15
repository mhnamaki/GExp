package dataset.graphmaker;

import java.awt.List;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.neo4j.unsafe.batchinsert.internal.BatchInserterImpl;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParser;
import org.neo4j.tooling.ImportTool;

import scala.Array;

public class CreateDBPDatasetFromFilesInBatch {

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

		FileInputStream fisNodes = new FileInputStream(nodesInfo);
		FileInputStream fisEdges = new FileInputStream(edgesInfo);

		BatchInserter dataGraph = null;

		//FileUtils.deleteRecursively(new File(dataGraphPath));
		File storeDir = new File(dataGraphPath);

		Map<String, String> config = new HashMap<String, String>();
		config.put("neostore.nodestore.db.mapped_memory", "50000M");
		dataGraph = BatchInserters.inserter(storeDir, config);

		// Neo4jBatchHandler handler = new Neo4jBatchHandler(db, 500000000,
		// 600);
		// BatchInserterIndexProvider indexProvider = new
		// LuceneBatchInserterIndexProvider(db);
		// index = indexProvider.nodeIndex("ttlIndex",
		// MapUtil.stringMap("type", "exact"));
		// index.setCacheCapacity(URI_PROPERTY, indexCache + 1);

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

			// splittedLine[1] labels
			// adding labels
			s = System.nanoTime();
			String[] labels = splittedLine[1].split(",");
			ArrayList<Label> labelsList = new ArrayList<Label>();
			for (String lbl : labels) {
				if (lbl.length() > 2) {
					labelsList.add(Label.label(lbl.replace("[", "").replace("]", "")));
				}
			}
			Label[] labelsArr = new Label[labelsList.size()];
			for (int i = 0; i < labelsArr.length; i++) {
				labelsArr[i] = labelsList.get(i);
			}
			addLabelsDuration += ((System.nanoTime() - s) / 1e6);

			// splittedLine[2] keyValuePairs (props) (attrs)
			s = System.nanoTime();
			Map<String, Object> propsMap = new HashMap<String, Object>();
			String[] props = splittedLine[2].split(",");
			for (String prop : props) {
				if (prop.length() > 2) {
					String[] keyValue = prop.replace("[", "").replace("]", "").split("\\#\\:\\$");
					propsMap.put(keyValue[0], keyValue[1]);
				}
			}
			setPropertiesDuration += ((System.nanoTime() - s) / 1e6);

			s = System.nanoTime();
			long newNodeId = dataGraph.createNode(propsMap, labelsArr);
			createNodeDuration += ((System.nanoTime() - s) / 1e6);

			// splittedLine[0] id
			newNodeIdOfOldNodeId.put(Long.parseLong(splittedLine[0]), newNodeId);
			// oldNodeIdOfNewNodeId.put(node.getId(),
			// Long.parseLong(splittedLine[0]));

		}
		brNodes.close();
		System.out.println("after nodes insertion!");

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

			// props
			Map<String, Object> propsMap = new HashMap<String, Object>();
			String[] props = splittedLine[3].split(",");
			for (String prop : props) {
				if (prop.length() > 2) {
					String[] keyValue = prop.replace("[", "").replace("]", "").split("\\#\\:\\$");
					propsMap.put(keyValue[0], keyValue[1]);
				}
			}

			dataGraph.createRelationship(newStartNodeId, newEndNodeId, DynamicRelationshipType.withName(relationType),
					propsMap);
			cnt++;

		}
		brEdges.close();
		System.out.println("after edges insertion!");

		dataGraph.shutdown();
		System.out.println("program is finished properly!");
	}
}
