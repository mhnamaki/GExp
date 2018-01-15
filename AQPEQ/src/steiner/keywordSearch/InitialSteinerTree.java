package steiner.keywordSearch;

import aqpeq.utilities.Dummy.DummyProperties.SteinerTreeOperation;

public class InitialSteinerTree extends SteinerTree {

	public InitialSteinerTree(int rootNodeId, String p) {
		this.rootNodeId = rootNodeId;
		this.addRelatedKeyword(p);
		this.generationOperation = SteinerTreeOperation.KEYWRODMATCH;
	}
}
