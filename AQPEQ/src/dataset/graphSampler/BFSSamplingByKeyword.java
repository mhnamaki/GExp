package dataset.graphSampler;

import java.io.File;

import java.util.HashSet;
import java.util.LinkedList;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import aqpeq.utilities.Dummy.DummyFunctions;

public class BFSSamplingByKeyword {

	String dataGraphPath = "/data/wudb/users/mhn/AQEQDatasets/dbpedia321/dbp_3_2_1";
	String keyword = "buffalo";
	int hopBound = 2;

	int maxNumberOfRels = 1000;
	int maxNumberOfNodes = 1000;

	public static void main(String[] args) throws Exception {
		BFSSamplingByKeyword b = new BFSSamplingByKeyword();
		b.run();

	}

	private void run() throws Exception {

		File storeDir = new File(dataGraphPath);
		GraphDatabaseService dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "1g")
				// .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
				.newGraphDatabase();

		System.out.println("dataset:" + dataGraphPath);

		Transaction tx1 = dataGraph.beginTx();

		HashSet<Node> keywordNodesSet = new HashSet<Node>();
		for (Node node : dataGraph.getAllNodes()) {
			for (Label lbl : node.getLabels()) {
				if (lbl.name().toLowerCase().contains(keyword)) {
					keywordNodesSet.add(node);
				}
			}
		}
		tx1.success();
		tx1.close();
		tx1 = dataGraph.beginTx();

		System.out.println("keywordNodesSet size:" + keywordNodesSet.size());

		LinkedList<NodeWithInfo> bfsQueue = new LinkedList<NodeWithInfo>();
		HashSet<Node> visitedNode = new HashSet<Node>();
		HashSet<Relationship> visitedRel = new HashSet<Relationship>();

		for (Node node : keywordNodesSet) {
			bfsQueue.add(new NodeWithInfo(node, 0));
		}
		tx1.success();
		tx1.close();
		tx1 = dataGraph.beginTx();

		while (!bfsQueue.isEmpty()) {
			NodeWithInfo currentNode = bfsQueue.poll();
			visitedNode.add(currentNode.node);

			if (currentNode.stepsFromRoot < hopBound) {
				if (currentNode.node.getDegree() > 1000) {
					System.out.println("degree:" + currentNode.node.getDegree());
					continue;
				}
				for (Relationship rel : currentNode.node.getRelationships()) {
					if (!visitedNode.contains(rel.getOtherNode(currentNode.node))) {
						bfsQueue.add(
								new NodeWithInfo(rel.getOtherNode(currentNode.node), currentNode.stepsFromRoot + 1));
						visitedRel.add(rel);
					}

					if (visitedRel.size() > maxNumberOfRels || visitedNode.size() > maxNumberOfNodes) {
						break;
					}
				}
				tx1.success();
				tx1.close();
				tx1 = dataGraph.beginTx();
			}

			tx1.success();
			tx1.close();
			tx1 = dataGraph.beginTx();
			System.out.println("bfsQueue.size:" + bfsQueue.size());

			if (visitedRel.size() > maxNumberOfRels || visitedNode.size() > maxNumberOfNodes) {
				break;
			}
		}

		System.out.println("after bfs");
		tx1.success();
		System.out.println("after success");
		tx1.close();
		System.out.println("after close");
		dataGraph = null;

		System.out.println("after shut down first G");

		System.out.println("visited nodes: " + visitedNode.size());
		System.out.println("visited rels: " + visitedRel.size());
		System.out.println("after bfs");

		String newPath = DummyFunctions.copyDataSet(dataGraphPath, 1);
		File storeDir2 = new File(newPath);
		GraphDatabaseService newDataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir2)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "8g")
				// .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
				.newGraphDatabase();

		System.out.println("after initing new one");
		Transaction tx2 = newDataGraph.beginTx();

		int cnt = 0;

		for (Relationship rel : newDataGraph.getAllRelationships()) {
			if (!visitedRel.contains(rel)) {
				rel.delete();
				cnt++;

				if ((cnt % 10000) == 0) {
					tx2.success();
					tx2.close();
					tx2 = newDataGraph.beginTx();
					System.out.println("cnt: " + cnt);
				}
			}

		}

		System.out.println("after deleting rels");
		for (Node node : newDataGraph.getAllNodes()) {
			if (!visitedNode.contains(node)) {
				node.delete();
				cnt++;

				if ((cnt % 10000) == 0) {
					tx2.success();
					tx2.close();
					tx2 = newDataGraph.beginTx();
					System.out.println("cnt: " + cnt);
				}
			}
		}
		System.out.println("after deleting nodes");
		for (Node node : newDataGraph.getAllNodes()) {

			if (node.getDegree() == 0) {
				cnt++;
				node.delete();
				if ((cnt % 10000) == 0) {
					tx2.success();
					tx2.close();
					tx2 = newDataGraph.beginTx();
					System.out.println("cnt: " + cnt);
				}
			}
		}
		System.out.println("after deleting nodes with zero degree");

		tx2.success();
		tx2.close();

		System.out.println("program is finished!");
		newDataGraph.shutdown();

	}

}

class NodeWithInfo {
	Node node;
	int stepsFromRoot;

	public NodeWithInfo(Node node, int stepsFromRoot) {
		this.node = node;
		this.stepsFromRoot = stepsFromRoot;
	}
}
