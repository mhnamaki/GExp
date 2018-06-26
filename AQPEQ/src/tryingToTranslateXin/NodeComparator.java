package tryingToTranslateXin;

import java.util.Comparator;

public class NodeComparator implements Comparator<BitNode> {

	@Override
	public int compare(BitNode o1, BitNode o2) {

		// by ascending based on the
		// cost of each node
		if (o1.NGV.size() != o2.NGV.size())
			// the higher, the better
			return Double.compare(o2.NGV.size(), o1.NGV.size());
		else
			return Integer.compare(o1.nodeId, o2.nodeId);
	}

}
