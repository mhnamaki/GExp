package pairwiseBasedKWS;

public class NearestNodeAndDistance {
	public Integer nodeId;
	public int distance = Integer.MAX_VALUE;

	public NearestNodeAndDistance() {
	}

	public NearestNodeAndDistance(Integer nodeId, int distance) {
		this.nodeId = nodeId;
		this.distance = distance;
	}

	@Override
	public String toString() {
		return "id:" + this.nodeId + ", dist:" + this.distance;
	}
}
