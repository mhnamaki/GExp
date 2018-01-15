package baselines;

public class DFSQBENode {
	private DFSQBENode parentDFSNode;
	private int nodeId;
	private int distance;
	private int relId;

	public DFSQBENode(int nodeId, DFSQBENode parentDFSNode, int distance) {
		this.setNodeId(nodeId);
		this.setParentDFSNode(parentDFSNode);
		this.setDistance(distance);
		
	}
	
	public DFSQBENode(int nodeId,int relId, DFSQBENode parentDFSNode, int distance) {
		this.setNodeId(nodeId);
		this.setRelId(relId);
		this.setParentDFSNode(parentDFSNode);
		this.setDistance(distance);
		
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public DFSQBENode getParentDFSNode() {
		return parentDFSNode;
	}

	public void setParentDFSNode(DFSQBENode parentDFSNode) {
		this.parentDFSNode = parentDFSNode;
	}

	public int getRelId() {
		return relId;
	}

	public void setRelId(int relId) {
		this.relId = relId;
	}
}
