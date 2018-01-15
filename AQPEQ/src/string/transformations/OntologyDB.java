package string.transformations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

/** 
 * @author Casey Fleck
 * @project Transformation function
 * @email cjfleck@coastal.edu
 * @date 6.20.17
 */

/**
 * OntologyDB.java - uses Jena to extract and RDF "model"(graph) using an
 * ontology graph from dbpedia database.
 */
public class OntologyDB {

	private OntModel model;
	private String ns;
	public static InputStream in;
	// datatype creates an ontology model with its own specific methods

	/**
	 * OntologyDB - constructor for this class. Sets up useful variables to
	 * retrieve information from Ontology dataset
	 * 
	 * @throws Exception
	 *             - thrown if dataset file containing rdf's could not be
	 *             located
	 */
	public OntologyDB() throws Exception {
		this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		this.ns = "http://dbpedia.org/ontology/";
		in = new FileInputStream(
				"dbpedia_2016-04.owl");
	}

	/**
	 * Ontology - retrieves list of superClasses corresponding to input from
	 * parameters
	 * 
	 * @param input
	 *            - specific category of interest being searched for
	 * @param map
	 *            - copy of the map to add values to
	 * @return map containing the list of related super classes
	 * @throws Exception
	 *             - file not found exception, or if no such class is found
	 */
	public void Ontology(String input, Map<String, List<String>> map)
			throws Exception {

		String aClass = null;
		HashMap<String, OntClass> ontClassOfALabel = new HashMap<String, OntClass>();
		ArrayList<String> classList = new ArrayList<String>();

		model.read(in, null);
		in.close();
		Iterator<OntClass> it = model.listClasses();
		// creating an iterator to go over the model that was read in

		// NOTE TO SELF: try to find if label just contains a snippet of the
		// input so that there are more matches to input
		while (it.hasNext()) {
			OntClass ontclass = it.next();
			ontClassOfALabel.put(ontclass.getLabel("en"), ontclass);
			// retrieving the English label of each class, creating a map to its
			// Ontology class

			if (ontclass.getLabel("en").equalsIgnoreCase(input)) {
				aClass = ontclass.getLocalName();
			}
			// retrieving local name of URI from model if label of class matches
			// input
		}

		OntClass oc = ontClassOfALabel.get(input);
		// searching if map contains the String input as a label

		// section below retrieves the label of input's superclass if it exists
		if (oc != null) {

			OntClass tempClass = model.getOntClass(ns + aClass);
			// retrieving the class name of the input label

			while (tempClass != null) {
				classList.add(tempClass.getLabel("en"));
				tempClass = tempClass.getSuperClass();
			}
			// retrieves each superClass of the given input's class
			// adds classes to arraylist

			classList.remove(classList.size() - 1);
			// removes extra null statement from arraylist

			map.put(input, classList);
			// map the input label to the arraylist

			// return map;
		}
		// return map;

	}
}
