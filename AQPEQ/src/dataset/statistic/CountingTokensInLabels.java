package dataset.statistic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import com.sleepycat.je.Database;
import com.sleepycat.je.Environment;

import aqpeq.utilities.MapUtil;
import aqpeq.utilities.Dummy.DummyFunctions;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;

public class CountingTokensInLabels {

	public HashSet<String> stopWords = new HashSet<String>();
	public String dataGraphPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/citation.graphdb";

	GraphDatabaseService dataGraph;

	public CountingTokensInLabels(String[] args) {
		if (args.length > 0)
			dataGraphPath = args[0];
	}

	public static void main(String[] args) throws Exception {

		CountingTokensInLabels c = new CountingTokensInLabels(args);
		 c.countTokenFrequency();
		//c.countLabelFrequency();	
		 
		 
		 
		 
		 

	}

	private void countLabelFrequency() throws Exception {

		// stopWords

		FileInputStream fis = new FileInputStream("stopwords.in");

		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String line = null;
		while ((line = br.readLine()) != null) {
			stopWords.add(line.trim());
		}

		br.close();

		File fout = new File("lblFrequency.txt");
		FileOutputStream fos = new FileOutputStream(fout);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		File storeDir = new File(dataGraphPath);
		dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "8g")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();

		System.out.println("after dataset is initialized");

		Transaction tx1 = dataGraph.beginTx();

		// HashSet<String> distinctTokens = new HashSet<String>();
		HashMap<String, Integer> freqOfTokens = new HashMap<String, Integer>();

		int cnt = 0;
		for (Node node : dataGraph.getAllNodes()) {
			for (Label lbl : node.getLabels()) {
				if (lbl.name().toLowerCase().equals("uri_ncbi.nlm.nih.gov")) {
					System.out.println(node.getId() + " " + node.getLabels());
					Map<String, Object> props = node.getAllProperties();
					for (String key : props.keySet()) {
						System.out.print(key + ":" + props.get(key));
					}
					System.out.println();
				}
				cnt++;
				freqOfTokens.putIfAbsent(lbl.name().toLowerCase(), 0);
				freqOfTokens.put(lbl.name().toLowerCase(), freqOfTokens.get(lbl.name().toLowerCase()) + 1);
				// if (cnt % 100000 == 0) {
				// System.out.println("cnt: " + cnt);
				// }
			}
		}

		Map<String, Integer> map = MapUtil.sortByValueDesc(freqOfTokens);
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			bw.write(entry.getKey() + ":" + entry.getValue());
			bw.newLine();
		}

		bw.close();

		tx1.success();
		tx1.close();

		// dataGraph.shutdown();

	}

	private void countTokenFrequency() throws Exception {

		// stopWords

		FileInputStream fis = new FileInputStream("stopwords.in");

		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String line = null;
		while ((line = br.readLine()) != null) {
			stopWords.add(line.trim());
		}

		br.close();

		File fout = new File("tokenFrequency.txt");
		FileOutputStream fos = new FileOutputStream(fout);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		File storeDir = new File(dataGraphPath);
		dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "8g")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();

		System.out.println("after dataset is initialized");

		Transaction tx1 = dataGraph.beginTx();

		// HashSet<String> distinctTokens = new HashSet<String>();
		HashMap<String, Integer> freqOfTokens = new HashMap<String, Integer>();

		int cnt = 0;
		for (Node node : dataGraph.getAllNodes()) {
			for (Label lbl : node.getLabels()) {

				// if (!lbl.name().toString().toLowerCase().startsWith("uri_"))
				// {
				// distinctTokens.add(lbl.name().toString().toLowerCase());
				// }

				for (String token : DummyFunctions.getTokens(lbl, "!*^/,;:_-[]()&$#@=+'`~.<>{}|\"")) {
					if (stopWords.contains(token) || token.length() < 3) {
						continue;
					}
					token = token.intern();
					cnt++;

					freqOfTokens.putIfAbsent(token, 0);
					freqOfTokens.put(token, freqOfTokens.get(token) + 1);

					if (cnt % 100000 == 0) {
						System.out.println("cnt: " + cnt);
					}
					// distinctTokens.add(token);
				}
			}
		}

		Map<String, Integer> map = MapUtil.sortByValueDesc(freqOfTokens);
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			bw.write(entry.getKey() + ":" + entry.getValue());
			bw.newLine();
		}

		bw.close();

		tx1.success();
		tx1.close();

		// dataGraph.shutdown();

	}

}

class NodeWithInfo {
	Node node;
	int stepsFromRoot;
	ArrayList<Node> pathFromRootToHere = new ArrayList<Node>();

	public NodeWithInfo(Node node, int stepsFromRoot, NodeWithInfo parentNodeWithInfo) {
		this.node = node;
		this.stepsFromRoot = stepsFromRoot;

		if (parentNodeWithInfo != null && parentNodeWithInfo.pathFromRootToHere != null)
			this.pathFromRootToHere.addAll(parentNodeWithInfo.pathFromRootToHere);

		this.pathFromRootToHere.add(node);
	}
}
