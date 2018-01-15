package dataset.graphmaker;

import java.util.ArrayList;
import java.util.HashSet;

public class AmazonProduct {
	public String id;
	public String asin;
	public String title;
	public String group;
	public String salesrank;
	public String similar;
	public ArrayList<String> similarPro;
	public AmazonCategory category;
	public AmazonReview review;
	public HashSet<String> cList;

	public AmazonProduct(String id, String asin, String title, String group, String salesrank, String similar,
			ArrayList<String> similarPro, AmazonCategory category, AmazonReview review, HashSet<String> cList) {

		this.id = id;
		this.asin = asin;
		this.title = title;
		this.group = group;
		this.salesrank = salesrank;
		this.similar = similar;
		this.similarPro = similarPro;
		this.category = category;
		this.review = review;
		this.cList = cList;
	}

}
