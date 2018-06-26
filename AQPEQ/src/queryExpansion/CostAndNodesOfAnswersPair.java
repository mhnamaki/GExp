package queryExpansion;

public class CostAndNodesOfAnswersPair {
	public int[] nodeId;
	public double cost;
	
	public int keywordIndex;

	private double importance=0;
	
	private double tfIdf=0;
	
	public double FScore = 0;
	
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
