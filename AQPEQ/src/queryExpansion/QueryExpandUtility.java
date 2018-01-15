package queryExpansion;

import java.util.ArrayList;
import java.util.HashSet;

import dataset.BerkeleyDB.BerkleleyDB;
import graphInfra.GraphInfraReaderArray;

public class QueryExpandUtility {

	public static double getSumOfCosts(CostNodePair[] costNodePairs) {
		double result = 0d;

		for (int m = 0; m < costNodePairs.length; m++) {
			if (costNodePairs[m].cost.getValue() == Double.MAX_VALUE) {
				return Double.MAX_VALUE;
			}
			result += costNodePairs[m].cost.getValue();
		}

		return result;
	}

	public static int[] getNodeIdArr(CostNodePair[] costNodePairs) {
		
		int[] nodeIds = new int[costNodePairs.length];

		for (int i = 0; i < costNodePairs.length; i++) {
			nodeIds[i] = costNodePairs[i].nodeId;
		}

		return nodeIds;
	}

}
