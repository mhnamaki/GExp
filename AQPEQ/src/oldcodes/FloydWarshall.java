package oldcodes;

import java.io.File;
import java.util.HashMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import com.sleepycat.je.DatabaseException;



public class FloydWarshall {
//	final static int INF = 99999;
//	//public String dataGraphPath = "/Users/mnamaki/AQPEQ/GraphExamples/k2";
//	public String dataGraphPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp_3_2_1";
//	int nodeCnt = 0;
//	private static GraphDatabaseService dataGraph;
//	FloydWarshallDA da;
//	BDBEnv floydWarshallEnv;
//
//	public static void main(String[] args) throws Exception {
//		FloydWarshall floydWarshallRunner = new FloydWarshall();
//
//		floydWarshallRunner.init();
//
//		Transaction tx1 = dataGraph.beginTx();
//
//		floydWarshallRunner.floydWarshall(dataGraph);
//		tx1.success();
//		tx1.close();
//
//	}
//
//	private void init() throws Exception {
//
//		File storeDir = new File(dataGraphPath);
//		dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
//				.setConfig(GraphDatabaseSettings.pagecache_memory, "1g")
//				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();
//
//		System.out.println("after dataset is initialized");
//
//		File floydWarshallEnvPath = new File("dbEnv/floydWarshall");
//		// File kDistFile = new File("./kDistDB.txt");
//
//		floydWarshallEnv = new BDBEnv();
//		floydWarshallEnv.setup(floydWarshallEnvPath, false);
//		da = new FloydWarshallDA(floydWarshallEnv.getEntityStore());
//
//	}
//
//	void floydWarshall(GraphDatabaseService dataGraph) throws Exception {
//
//		int relCnt = 0;
//
//		HashMap<Integer, Integer> nodeIdOfIndex = new HashMap<Integer, Integer>();
//		HashMap<Integer, Integer> indexOfNodeId = new HashMap<Integer, Integer>();
//
//		for (Node node : dataGraph.getAllNodes()) {
//			indexOfNodeId.put((int) node.getId(), nodeCnt);
//			nodeIdOfIndex.put(nodeCnt, (int) node.getId());
//			nodeCnt++;
//		}
//
//		// for (Relationship rel : dataGraph.getAllRelationships()) {
//		// relCnt++;
//		// }
//		int i, j, k;
//
//		com.sleepycat.je.Transaction txn = floydWarshallEnv.getEnv().beginTransaction(null, null);
//
//		int cnt =0;
//		for (i = 0; i < nodeCnt; i++) {
//			for (j = 0; j < nodeCnt; j++) {
//				cnt++;
//				FloydWarshallArrayEntity fae = new FloydWarshallArrayEntity();
//				fae.setPK(i, j);
//				fae.setDistance(INF);
//				da.floydWarshallArrayEntityPK.put(fae);
//				// dist[i][j] = INF;
//				if(cnt % 1000000 ==0){
//					System.out.println(cnt);
//				}
//			}
//		}
//		txn.commit();
//
//		// int dist[][] = new int[nodeCnt][nodeCnt];
//		// int next[][] = new int[nodeCnt][nodeCnt];
//
//		/*
//		 * Initialize the solution matrix same as input graph matrix. Or we can
//		 * say the initial values of shortest distances are based on shortest
//		 * paths considering no intermediate vertex.
//		 */
//		// for (i = 0; i < nodeCnt; i++) {
//		// for (j = 0; j < nodeCnt; j++) {
//		// dist[i][j] = INF;
//		// }
//		// }
//
//		txn = floydWarshallEnv.getEnv().beginTransaction(null, null);
//
//		for (Relationship rel : dataGraph.getAllRelationships()) {
//			FloydWarshallArrayEntity fae = new FloydWarshallArrayEntity();
//			fae.setPK(indexOfNodeId.get((int) rel.getStartNodeId()), indexOfNodeId.get((int) rel.getEndNodeId()));
//			fae.setDistance(1);
//			fae.setNextNodeIndex(indexOfNodeId.get((int) rel.getEndNodeId()));
//			da.floydWarshallArrayEntityPK.put(fae);
//		}
//		txn.commit();
//
//		/*
//		 * Add all vertices one by one to the set of intermediate vertices. --->
//		 * Before start of a iteration, we have shortest distances between all
//		 * pairs of vertices such that the shortest distances consider only the
//		 * vertices in set {0, 1, 2, .. k-1} as intermediate vertices. ---->
//		 * After the end of a iteration, vertex no. k is added to the set of
//		 * intermediate vertices and the set becomes {0, 1, 2, .. k}
//		 */
//		txn = floydWarshallEnv.getEnv().beginTransaction(null, null);
//		for (k = 0; k < nodeCnt; k++) {
//			// Pick all vertices as source one by one
//			for (i = 0; i < nodeCnt; i++) {
//				// Pick all vertices as destination for the
//				// above picked source
//				for (j = 0; j < nodeCnt; j++) {
//					// If vertex k is on the shortest path from
//					// i to j, then update the value of dist[i][j]
//					if (da.floydWarshallArrayEntityPK.get(i + "_" + k).getDistance() + da.floydWarshallArrayEntityPK
//							.get(k + "_" + j).getDistance() < da.floydWarshallArrayEntityPK.get(i + "_" + j)
//									.getDistance()) {
//
//						FloydWarshallArrayEntity fae = new FloydWarshallArrayEntity();
//						fae.setPK(i, j);
//						fae.setDistance(da.floydWarshallArrayEntityPK.get(i + "_" + k).getDistance()
//								+ da.floydWarshallArrayEntityPK.get(k + "_" + j).getDistance());
//						fae.setNextNodeIndex(da.floydWarshallArrayEntityPK.get(i + "_" + k).getNextNodeId());
//						da.floydWarshallArrayEntityPK.put(fae);
//
//					}
//				}
//			}
//		}
//		txn.commit();
//
//		txn = floydWarshallEnv.getEnv().beginTransaction(null, null);
//		for (int u = 0; u < nodeCnt; u++) {
//			for (int v = 0; v < nodeCnt; v++) {
//				pathConstruction‍(u, v, da, nodeIdOfIndex, indexOfNodeId);
//			}
//		}
//		txn.commit();
//		// Print the shortest distance matrix
//		// printSolution(dist, nodeCnt);
//	}
//
//	private String pathConstruction‍(int u, int v, FloydWarshallDA da, HashMap<Integer, Integer> nodeIdOfIndex,
//			HashMap<Integer, Integer> indexOfNodeId) throws Exception {
//
//		if (da.floydWarshallArrayEntityPK.get(u + "_" + v) == null
//				|| da.floydWarshallArrayEntityPK.get(u + "_" + v).getNextNodeId() == null)
//			return "";
//
//		String path = nodeIdOfIndex.get(u).toString();
//		while (u != v) {
//			u = da.floydWarshallArrayEntityPK.get(u + "_" + v).getNextNodeId();
//			path += "->" + nodeIdOfIndex.get(u);
//		}
//		System.out.println(path);
//		return path;
//
//	}
//
//	void printSolution(int dist[][], int nodeCnt) {
//		System.out.println("Following matrix shows the shortest " + "distances between every pair of vertices");
//		for (int i = 0; i < nodeCnt; ++i) {
//			for (int j = 0; j < nodeCnt; ++j) {
//				if (dist[i][j] == INF)
//					System.out.print("INF ");
//				else
//					System.out.print(dist[i][j] + "   ");
//			}
//			System.out.println();
//		}
//	}

}
