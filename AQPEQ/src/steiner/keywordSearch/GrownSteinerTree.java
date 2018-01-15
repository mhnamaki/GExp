package steiner.keywordSearch;

import aqpeq.utilities.Dummy.DummyProperties.SteinerTreeOperation;
import graphInfra.RelationshipInfra;

public class GrownSteinerTree extends SteinerTree {

	SteinerTree t1;
	RelationshipInfra growingEdge;
	int targetNodeId;
	int sourceNodeId;

	public GrownSteinerTree(int rootNodeId, SteinerTree t1, RelationshipInfra growingEdge, double cost,
			int sourceNode) {
		this.rootNodeId = rootNodeId;
		this.targetNodeId = rootNodeId;
		this.sourceNodeId = sourceNode;
		this.t1 = t1;
		this.growingEdge = growingEdge;
		this.cost = cost;
		this.generationOperation = SteinerTreeOperation.GROWTH;
		this.p.addAll(t1.p);
	}
}
