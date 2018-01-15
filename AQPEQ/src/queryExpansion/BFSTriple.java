package queryExpansion;

public class BFSTriple {
	private int nodeId;
	private int distance;
	private double cost;

	public BFSTriple(int nodeId, int distance, double cost) {
		this.setNodeId(nodeId);
		this.setDistance(distance);
		this.setCost(cost);
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
}
