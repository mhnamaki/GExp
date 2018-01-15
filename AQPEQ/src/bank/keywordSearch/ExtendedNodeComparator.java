package bank.keywordSearch;

import java.util.Comparator;

public class ExtendedNodeComparator implements Comparator<ExtendedNode> {

	@Override
	public int compare(ExtendedNode o1, ExtendedNode o2) {

		// by ascending based on the
		// cost of each node
		if (o1.distanceFromOriginalOriginId != o2.distanceFromOriginalOriginId)
			return Double.compare(o1.distanceFromOriginalOriginId, o2.distanceFromOriginalOriginId);
		else
			return Double.compare(o1.costFromOriginalOriginId, o2.costFromOriginalOriginId);

	}

}
