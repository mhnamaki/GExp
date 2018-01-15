package dataset.statistic;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

public class TemplateNeo4jJavaCode {
	public TemplateNeo4jJavaCode(){
		File storeDir = new File("");
		GraphDatabaseService dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "8g")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();
		
		
		Transaction tx1 = dataGraph.beginTx();
		
		// write code
		
		tx1.success();
		tx1.close();

		
	}
}
