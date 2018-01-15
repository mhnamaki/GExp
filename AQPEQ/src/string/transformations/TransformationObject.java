package string.transformations;

import java.util.HashMap;

public class TransformationObject {
	public HashMap<String, Double> synonym = new HashMap<String, Double>();
	public HashMap<String, Double> abbreviation = new HashMap<String, Double>();
	public HashMap<String, Double> otherTransormation = new HashMap<String, Double>();

	public TransformationObject(HashMap<String, Double> synonym, HashMap<String, Double> abbreviation,
			HashMap<String, Double> otherTransormation) {
		this.synonym = synonym;
		this.abbreviation = abbreviation;
		this.otherTransormation = otherTransormation;
	}
	
	public HashMap<String, Double> getSynonym (TransformationObject transformation) {
		return transformation.synonym;
	}
	
	public HashMap<String, Double> getAbbreviation (TransformationObject transformation) {
		return transformation.abbreviation;
	}
	
	public HashMap<String, Double> getOtherTransformation (TransformationObject transformation) {
		return transformation.otherTransormation;
	}
}
