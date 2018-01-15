package neo4jBasedKWS;

import graphInfra.NodeInfra;

public class ResultNode {
	public String labels;
	public int nodeId;
	public NodeInfra node;

	public ResultNode(String labels, int nodeId, NodeInfra node) {
		this.labels = labels;
		this.nodeId = nodeId;
		this.node = node;
	}

	@Override
	public String toString() {
		return this.nodeId + ":" + this.labels;
	}

	@Override
	public int hashCode() {
		return nodeId;
	}
}
