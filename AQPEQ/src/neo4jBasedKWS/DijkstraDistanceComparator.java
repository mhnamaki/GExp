package neo4jBasedKWS;

import java.util.Comparator;

public class DijkstraDistanceComparator implements Comparator<DijkstraRunner> {

	@Override
	public int compare(DijkstraRunner o1, DijkstraRunner o2) {

		return Integer.compare(o1.frontier.peek().distanceFromOriginId.get(o1.originNodeId),
				o2.frontier.peek().distanceFromOriginId.get(o2.originNodeId));
	}

}
