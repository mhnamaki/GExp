package neo4jBasedKWS;

import java.util.Comparator;

public class ExtendedNodeComparator implements Comparator<ExtendedNode> {

	@Override
	public int compare(ExtendedNode o1, ExtendedNode o2) {
		return 0;
		// return Double.compare(o1cost, o2.cost); //by ascending based on the
		// cost of each node
	}

}
