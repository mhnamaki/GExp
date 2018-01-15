package steiner.keywordSearch;

import aqpeq.utilities.Dummy.DummyProperties.SteinerTreeOperation;

public class MergedSteinerTree extends SteinerTree {
	SteinerTree t1;
	SteinerTree t2;

	public MergedSteinerTree(int rootNodeId, SteinerTree t1, SteinerTree t2, double cost) {
		this.rootNodeId = rootNodeId;
		this.t1 = t1;
		this.t2 = t2;

		this.p.addAll(t1.p);
		this.p.addAll(t2.p);

		this.cost = cost;

		this.generationOperation = SteinerTreeOperation.MERGE;

	}
}
