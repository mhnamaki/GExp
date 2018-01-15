package relevantDiversificationLibraries;

import java.util.ArrayList;

public class DivObject {

	public double tfIdfScore;
	public double importanceScore;
	public double diffScore;
	public ArrayList<Integer> newKeywords;

	public DivObject(double tfIdfScore, double importanceScore, double diffScore, ArrayList<Integer> newKeywords) {
		this.tfIdfScore = tfIdfScore;
		this.importanceScore = importanceScore;
		this.diffScore = diffScore;
		this.newKeywords = newKeywords;
	}

	public double getTfIdfScore() {
		return tfIdfScore;
	}

	public void setTfIdfScore(double tfIdfScore) {
		this.tfIdfScore = tfIdfScore;
	}

	public double getImportanceScore() {
		return importanceScore;
	}

	public void setImportanceScore(double importanceScore) {
		this.importanceScore = importanceScore;
	}

	public double getDiffScore() {
		return diffScore;
	}

	public void setDiffScore(double diffScore) {
		this.diffScore = diffScore;
	}

	public ArrayList<Integer> getNewKeywords() {
		return newKeywords;
	}

	public void setNewKeywords(ArrayList<Integer> newKeywords) {
		this.newKeywords = newKeywords;
	}

}
