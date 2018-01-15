package dataset.graphmaker;

import java.util.ArrayList;

public class AmazonReview {
	public String reviewNum;
	//public int reviewDow;
	public String reviewAvgRating;
	public ArrayList<String> customer;
	public ArrayList<String> reviewVal;

	public AmazonReview(String reviewNum, String reviewAvgRating, ArrayList<String> customer,
			ArrayList<String> reviewVal) {
		this.reviewNum = reviewNum;
		//this.reviewDow = reviewDow;
		this.reviewAvgRating = reviewAvgRating;
		this.customer = customer;
		this.reviewVal = reviewVal;

	}

}
