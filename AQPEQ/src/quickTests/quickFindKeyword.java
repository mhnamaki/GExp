package quickTests;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import aqpeq.utilities.Dummy.DummyFunctions;

public class quickFindKeyword {

	public static void main(String[] args) {
		String dataGraphPath = "/Users/zhangxin/Desktop/neo4jImdb";
		File storeDir = new File(dataGraphPath);
		GraphDatabaseService dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "4g")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();

		Transaction tx1 = dataGraph.beginTx();
		
		String actress = "AishaAlfa";
		for (Node node : dataGraph.getAllNodes()){
			for (Label lbl : node.getLabels()) {
//				if (lbl.toString().equals(actress.toLowerCase())){
//					System.out.println(actress);
//				}
				System.out.println(node.getId() + " -> " + lbl.toString());
			}
		}
		
		tx1.success();
		tx1.close();

	}

}
