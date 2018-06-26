package demo;

import java.util.Comparator;

import queryExpansion.CostAndNodesOfAnswersPair;

public class CostComparator implements Comparator<CostAndNodesOfAnswersPair>{
	
	@Override
	public int compare(CostAndNodesOfAnswersPair o1, CostAndNodesOfAnswersPair o2) {
//		if (o1.cost != o2.cost){
//			return Double.compare(o1.cost, o2.cost);
//		} else {
//			return Double.compare(o2.cost, o1.cost);
//		}
		
		return Double.compare(o1.cost, o2.cost);

	}

}
