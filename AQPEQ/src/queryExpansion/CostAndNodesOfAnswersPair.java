package queryExpansion;

public class CostAndNodesOfAnswersPair {
	public int[] nodeId;
	public double cost;

	private double importance;
	
	private double tfIdf;
	
	public CostAndNodesOfAnswersPair(int[] nodeId, double cost) {
		this.nodeId = nodeId;
		this.cost = cost;
	}
	public double getImportance() {
		return importance;
	}
	public void setImportance(double importance) {
		this.importance = importance;
	}
	public double getTfIdf() {
		return tfIdf;
	}
	public void setTfIdf(double tfIdf) {
		this.tfIdf = tfIdf;
	}
	
}
