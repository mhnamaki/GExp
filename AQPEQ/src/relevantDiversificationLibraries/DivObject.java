package relevantDiversificationLibraries;

import java.util.ArrayList;

public class DivObject {

//	public double tfIdfScore;
//	public double importanceScore;
//	public double diffScore;
	public ArrayList<Integer> newKeywords = new ArrayList<Integer>();
	public ArrayList<Double> scores = new ArrayList<Double>();

//	public DivObject(double tfIdfScore, double importanceScore, double diffScore, ArrayList<Integer> newKeywords) {
//		this.tfIdfScore = tfIdfScore;
//		this.importanceScore = importanceScore;
//		this.diffScore = diffScore;
	public DivObject(ArrayList<Double> scores, ArrayList<Integer> newKeywords) {
		this.newKeywords = newKeywords;
		this.scores = scores;
		
	}

}
