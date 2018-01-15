package relevantDiversificationLibraries;

import java.util.HashMap;
import java.util.HashSet;

import com.sleepycat.je.DatabaseException;

import aqpeq.utilities.StringPoolUtility;
import dataset.BerkeleyDB.BerkleleyDB;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class TermSimilarFunction {
	static double allKeyword = Math.log((double) 1000000);// 222

	public static void main(String[] args) throws Exception {
		// String strA = "Spanishmovie";
		// String strB = "Victoria";
		// String envFilePath = "/Users/zhangxin/Desktop/IMDB/sample/dbEnv";
		// String strA = "middle";
		// String strB = "c";
		// String envFilePath =
		// "/Users/zhangxin/Desktop/AQPEQ/GraphExamples/k1Infra/dbEnv";
		// BerkleleyDB berkeleyDB = new BerkleleyDB("database", "catDatabase",
		// envFilePath);
		// double gswd = ComputeTermSimilar(strA, strB);
		// System.out.println(gswd);
		// berkeleyDB.CloseDatabase();

	}

	public static double ComputeTermSimilar(GraphInfraReaderArray graph,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfToken, Integer strA, Integer strB) throws Exception {
		double allKeyword = Math.log((double) nodeIdsOfToken.size());
		double gswd = 0;
		double numerator = 0;
		double denominator = 0;
		double sizeA = 0;
		double sizeB = 0;
		double sizeI = 0;
		HashSet<Integer> neighborA = getNeighbors(graph, nodeIdsOfToken, strA);
		HashSet<Integer> neighborB = getNeighbors(graph, nodeIdsOfToken, strB);
		// TODO: // if test = 0; Math.log((double) test) = -Infinity
		// for size = 0; maybe we need to reconsider it
		if (!neighborA.isEmpty() && !neighborB.isEmpty()) {
			sizeA = Math.log((double) neighborA.size());
			sizeB = Math.log((double) neighborB.size());
			neighborA.retainAll(neighborB);
			sizeI = Math.log((double) neighborA.size());
			if (sizeA > sizeB) {// sizeA is in numerator
				numerator = sizeA - sizeI;
				denominator = allKeyword - sizeB;
				gswd = numerator / denominator;
			} else {// sizeB is in numerator
				numerator = sizeB - sizeI;
				denominator = allKeyword - sizeA;
				gswd = numerator / denominator;
			}
		} else {// if test = 0; Math.log((double) test) = -Infinity
			if (neighborB.isEmpty()) {// max = sizeA
				sizeA = Math.log((double) neighborA.size());
				numerator = sizeA;
				denominator = allKeyword;
				gswd = numerator / denominator;
			} else if (neighborA.isEmpty()) { // max = sizeB
				sizeB = Math.log((double) neighborB.size());
				numerator = sizeB;
				denominator = allKeyword;
				gswd = numerator / denominator;
			}
		}

		return gswd;
	}

	private static HashSet<Integer> getNeighbors(GraphInfraReaderArray graph,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfToken, Integer strA) throws Exception {

		HashSet<Integer> neighborTokenIds = new HashSet<Integer>();

		HashSet<Integer> nodeIdsOfStr = nodeIdsOfToken.get(strA);

		for (int nodeId : nodeIdsOfStr) {
			NodeInfra node = graph.nodeOfNodeId.get(nodeId);

			for (int targetNodeId : node.getOutgoingRelIdOfSourceNodeId().keySet()) {

				if (graph.nodeOfNodeId.get(targetNodeId).tokens != null) {
					neighborTokenIds.addAll(graph.nodeOfNodeId.get(targetNodeId).tokens);
				}

				if (graph.nodeOfNodeId.get(targetNodeId).getProperties() != null) {
					neighborTokenIds.addAll(graph.nodeOfNodeId.get(targetNodeId).getProperties());
				}

			}

		}

		return neighborTokenIds;

	}

}
