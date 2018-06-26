package incrementalEvaluation;

public class IncBFSTriple {
	private int nodeId;
	private int distance;
	private double cost;
	private IncBFSTriple parentBFSTriple;

	public IncBFSTriple(int nodeId, IncBFSTriple parentBFSTriple, int distance, double cost) {
		this.setNodeId(nodeId);
		this.setDistance(distance);
		this.setCost(cost);
		this.setParentBFSTriple(parentBFSTriple);
	}

	@Override
	public String toString() {
		return "id:" + nodeId + ", d:" + distance + ", c:" + cost;
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

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public IncBFSTriple getParentBFSTriple() {
		return parentBFSTriple;
	}

	public void setParentBFSTriple(IncBFSTriple parentBFSTriple) {
		this.parentBFSTriple = parentBFSTriple;
	}
}
