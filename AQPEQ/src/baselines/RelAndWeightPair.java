package baselines;

public class RelAndWeightPair {
	private int relId;
	private double weight;

	public RelAndWeightPair(int relId, double weight) {
		this.setRelId(relId);
		this.setWeight(weight);
	}

	public int getRelId() {
		return relId;
	}

	public void setRelId(int relId) {
		this.relId = relId;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
