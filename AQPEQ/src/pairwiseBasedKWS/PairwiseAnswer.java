package pairwiseBasedKWS;

import java.util.ArrayList;
import java.util.Arrays;

import neo4jBasedKWS.ResultTree;

public class PairwiseAnswer {

	public ArrayList<Integer> nodeMatches;
	public int weight;
	public int pairwiseWeight;

	public ResultTree resultTree;

	public PairwiseAnswer(int numOfKeywords) {
		nodeMatches = new ArrayList<Integer>(numOfKeywords);
		weight = Integer.MAX_VALUE;
	}

	@Override
	public String toString() {
		if (nodeMatches == null)
			return super.toString();

		return "matches:" + Arrays.toString(nodeMatches.toArray()) + ", weight:" + weight + ", pairWeight:"
				+ pairwiseWeight;
	}

}
