package bidirectional.keywordSearch;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {

	@Override
	public int compare(Node o1, Node o2) {

		// by ascending based on the
		// cost of each node
		if (o1.activation != o2.activation)
			// the higher, the better
			return Double.compare(o2.activation, o1.activation);
		else if (o1.degree != o2.degree)
			return Integer.compare(o1.degree, o2.degree);
		else
			return Integer.compare(o1.nodeId, o2.nodeId);

	}

}
