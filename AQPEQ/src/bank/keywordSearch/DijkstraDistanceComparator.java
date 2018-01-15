package bank.keywordSearch;

import java.util.Comparator;

public class DijkstraDistanceComparator implements Comparator<DijkstraRunner> {

	@Override
	public int compare(DijkstraRunner o1, DijkstraRunner o2) {

		return Double.compare(o1.frontier.peek().costFromOriginalOriginId,
				o2.frontier.peek().costFromOriginalOriginId);
	}

}
