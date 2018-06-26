package demo;

import java.util.Comparator;

import queryExpansion.CostAndNodesOfAnswersPair;

public class DivComparator implements Comparator<CostAndNodesOfAnswersPair>{
	
	@Override
	public int compare(CostAndNodesOfAnswersPair o1, CostAndNodesOfAnswersPair o2) {
		
		return Double.compare(o1.FScore, o2.FScore);

	}

}
