package dataset.graphmaker;

import java.util.ArrayList;
import java.util.HashSet;

public class AmazonCustomer {
	public String cID;
	public HashSet<String> product;
	
	public AmazonCustomer(String cID, HashSet<String> product) {
		this.cID = cID;
		this.product = product;
	}

}
