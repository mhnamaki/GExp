package baselines;

public class Scores {
	private double tf;
	private double idf;
	private int frequency;
	private double tfIdf;

	public Scores() {

	}

	public double getTf() {
		return tf;
	}

	public void setTf(double tf) {
		this.tf = tf;
	}

	public double getIdf() {
		return idf;
	}

	public void setIdf(double idf) {
		this.idf = idf;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public void setTfIdf(double tfIdf) {
		this.tfIdf = tfIdf;

	}

	public double getTfIdf() {
		return tfIdf;
	}
}
