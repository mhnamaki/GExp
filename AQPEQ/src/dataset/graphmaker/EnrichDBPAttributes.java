package dataset.graphmaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import aqpeq.utilities.Dummy.DummyFunctions;

public class EnrichDBPAttributes {

	public static void main(String[] args) throws Exception {

		EnrichDBPAttributes runner = new EnrichDBPAttributes();
		runner.run(args);
	}

	private String dataGraphPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/dbp";
	private String csvDir = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/dbpediaAsTable/csv/";

	private void run(String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dataGraph")) {
				dataGraphPath = args[++i];
			} else if (args[i].equals("-csvDir")) {
				csvDir = args[++i];
			}
		}
		File storeDir = new File(dataGraphPath);
		GraphDatabaseService dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "8g")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();

		System.out.println("after init data graph");

		Transaction tx1 = dataGraph.beginTx();

		HashMap<String, Long> nodeIdOfURI = new HashMap<String, Long>();

		// fill the map by uri's
		for (Node node : dataGraph.getAllNodes()) {
			for (Label lbl : node.getLabels()) {
				if (lbl.name().startsWith("uri_")) {
					nodeIdOfURI.put(lbl.name().replace("uri_", "").toLowerCase().trim(), node.getId());
				}
			}
		}

		System.out.println("nodeIdOfURI is filled size:" + nodeIdOfURI.size());

		// read from all csv's one by one.
		for (File file : DummyFunctions.getFilesInTheDirfinder(csvDir, "csv")) {
			FileInputStream fis = new FileInputStream(file.getAbsolutePath());

			// Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			// read first line as attribute names
			String line = br.readLine();

			String sep = "\",\"";

			String[] splittedAttributes = line.split(sep);

			// read values of a line
			int propUpdateCnt = 0;
			int notFoundNode = 0;
			int notSameLength = 0;
			while ((line = br.readLine()) != null) {
				if (!line.contains("http://dbpedia.org/resource/")) {
					continue;
				}
				String[] splittedAttributeValues = line.split(sep);
				String lowerCasedURI = splittedAttributeValues[0].replaceAll("\"", "").toLowerCase().trim();

				lowerCasedURI = getCleanURI(lowerCasedURI);
				// System.out.println("lowerCasedURI:" + lowerCasedURI);

				// find the node with this uri
				Long nodeId = nodeIdOfURI.get(lowerCasedURI);

				if (nodeId != null && splittedAttributeValues.length == splittedAttributes.length) {
					// enrich it
					Node uriNode = dataGraph.getNodeById(nodeId);
					for (int s = 1; s < splittedAttributeValues.length; s++) {
						if (!uriNode.hasProperty(splittedAttributes[s]) && !splittedAttributes[s].contains("rdf-")) {

							if (!splittedAttributeValues[s].contains("NULL")) {
								propUpdateCnt++;
								uriNode.setProperty(splittedAttributes[s].replaceAll("\"", ""),
										getCleanURI(splittedAttributeValues[s].replaceAll("\"", "")));

								if (propUpdateCnt % 5000 == 0) {
									System.out.println(file.getName() + " => " + lowerCasedURI + " -> "
											+ splittedAttributes[s].replaceAll("\"", "") + ":"
											+ getCleanURI(splittedAttributeValues[s].replaceAll("\"", "")));
								}

								if (propUpdateCnt % 10000 == 0) {
									System.out.println("propUpdateCnt: " + propUpdateCnt);
									tx1.success();
									tx1.close();
									tx1 = dataGraph.beginTx();
								}
							}
						}
					}
				} else {
					if (nodeId == null) {
						notFoundNode++;
						if (notFoundNode % 10000 == 0) {
							System.err.println("node not found for " + lowerCasedURI);
						}
					} else if (splittedAttributeValues.length != splittedAttributes.length) {
						notSameLength++;
						if (notSameLength % 10000 == 0) {
							System.err.println("The attributes and values length are not equal ");
							System.err.println(Arrays.toString(splittedAttributes));
							System.err.println(Arrays.toString(splittedAttributeValues));
						}

					}
				}
			}
			br.close();

		}

		tx1.success();
		tx1.close();
	}

	private String getCleanURI(String lowerCasedURI) throws Exception {
		String cleaned = lowerCasedURI;
		try {
			URI uri = new URI(cleaned);
			String[] segments = uri.getPath().split("/");
			cleaned = segments[segments.length - 1];
		} catch (Exception exc) {

		}
		cleaned = cleaned.replaceAll("http://dbpedia.org/resource/", "");
		cleaned = cleaned.replaceAll("\\{", "").replaceAll("\\}", "").trim();
		return cleaned;
	}

}
