package oldcodes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.roaringbitmap.RoaringBitmap;
import com.google.common.collect.HashBiMap;

import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;

public class BitMapIndexingTest {

	private static GraphDatabaseService dataGraph;
	private static String dataGraphPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp_3_2_1";
	public RoaringBitmap[] rBitmapsOfNodeIds;
	public RoaringBitmap[] rBitmapsOfKeywords;

	public static void main(String[] args) {
		BitMapIndexingTest bmit = new BitMapIndexingTest();

		File storeDir = new File(dataGraphPath);
		dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir).setConfig("cache_type", "none")
				.setConfig(GraphDatabaseSettings.pagecache_memory, "245760").newGraphDatabase();

		System.out.println("after dataset is initialized");

		Transaction tx1 = dataGraph.beginTx();

		bmit.test();

		tx1.success();
		tx1.close();

	}

	private void test() {

		int nodeCnt = 0;

		int tokenCnt = 0;
		HashBiMap<String, Integer> tokenSetBi = HashBiMap.create(1000000);
		// HashMap<String, Integer> tokenSet = new HashMap<String, Integer>();

		for (Node node : dataGraph.getAllNodes()) {
			nodeCnt++;

			for (Label lbl : node.getLabels()) {
				ArrayList<String> tokens = DummyFunctions.getTokens(lbl, DummyProperties.DELIMETERS);
				for (String token : tokens) {
					if (!tokenSetBi.containsKey(token)) {
						tokenSetBi.put(token, tokenCnt++);
					}
				}

			}
		}

		System.out.println("nodeCnt: " + nodeCnt);

		int notHitted = 0;
		rBitmapsOfNodeIds = new RoaringBitmap[nodeCnt];
		for (int i = 0; i < rBitmapsOfNodeIds.length; i++) {
			rBitmapsOfNodeIds[i] = new RoaringBitmap();
			if (dataGraph.getNodeById(i) != null) {
				for (Label lbl : dataGraph.getNodeById(i).getLabels()) {
					ArrayList<String> tokens = DummyFunctions.getTokens(lbl, DummyProperties.DELIMETERS);
					for (String token : tokens) {
						rBitmapsOfNodeIds[i].add(tokenSetBi.get(token));
					}
					rBitmapsOfNodeIds[i].runOptimize();
				}
			} else {
				notHitted++;
			}

		}
		System.out.println("after v->k");

		rBitmapsOfKeywords = new RoaringBitmap[tokenCnt];
		for (int i = 0; i < rBitmapsOfKeywords.length; i++) {
			rBitmapsOfKeywords[i] = new RoaringBitmap();
		}
		System.out.println("after init of k->v");

		int counter =0;
		for (Node node : dataGraph.getAllNodes()) {
			counter++;
			for (Label lbl : node.getLabels()) {
				ArrayList<String> tokens = DummyFunctions.getTokens(lbl, DummyProperties.DELIMETERS);
				for (String token : tokens) {
					if(!rBitmapsOfKeywords[tokenSetBi.get(token)].contains((int) node.getId())){
						rBitmapsOfKeywords[tokenSetBi.get(token)].add((int) node.getId());
						//rBitmapsOfKeywords[tokenSetBi.get(token)].runOptimize();	
					}
				}
			}
		}

		System.out.println("notHitted: " + notHitted);

		//tokenSetBi.inverse().get(1);
	}

}
