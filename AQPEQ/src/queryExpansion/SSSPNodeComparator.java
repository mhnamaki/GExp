package queryExpansion;

import java.util.Comparator;

public class SSSPNodeComparator implements Comparator<SSSPNode> {

	@Override
	public int compare(SSSPNode o1, SSSPNode o2) {

		// by ascending based on the
		// cost of each node
		if (o1.costFromOriginId != o2.costFromOriginId)
			return Double.compare(o1.costFromOriginId, o2.costFromOriginId);
		else
			return Double.compare(o1.distanceFromOriginId, o2.distanceFromOriginId);

	}

}
