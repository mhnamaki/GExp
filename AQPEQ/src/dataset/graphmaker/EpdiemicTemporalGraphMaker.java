package dataset.graphmaker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import aqpeq.utilities.Dummy.DummyFunctions;

public class EpdiemicTemporalGraphMaker {

	public static void main(String[] args) {
		EpdiemicTemporalGraphMaker etgm = new EpdiemicTemporalGraphMaker();
		etgm.createAGraph();

	}

	private void createAGraph() {
		String newDataGraphPath = "/Users/mnamaki/Documents/Education/PhD/Spring2017/GTAR/DATA/HealthEpidemic/p10";
		File storeDir = new File(newDataGraphPath);
		GraphDatabaseService dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig("cache_type", "none").setConfig(GraphDatabaseSettings.pagecache_memory, "245760")
				.newGraphDatabase();

		DummyFunctions.registerShutdownHook(dataGraph);

		Random rnd = new Random();
		Transaction tx1 = dataGraph.beginTx();

		int people = 10;
		int disease = 3;
		int T = 20;
		int maxDiseaseRecurrence = 3;
		int maxNumberOfDiseaseForEachPerson = Math.min(2, disease - 1);

		double maleRand = 0.45;
		Node maleNode = dataGraph.createNode();
		maleNode.addLabel(Label.label("Male"));
		Node femaleNode = dataGraph.createNode();
		femaleNode.addLabel(Label.label("Female"));

		double persian = 0.2;
		Node persianNode = dataGraph.createNode();
		persianNode.addLabel(Label.label("Persian"));
		double american = 0.5;
		Node americanNode = dataGraph.createNode();
		americanNode.addLabel(Label.label("American"));
		double chinese = 1;
		Node chineseNode = dataGraph.createNode();
		chineseNode.addLabel(Label.label("Chinese"));

		ArrayList<Long> peopleArr = new ArrayList<Long>();
		// create people with different attributes
		for (int p = 0; p < people; p++) {
			Node pNode = dataGraph.createNode();
			pNode.addLabel(Label.label("Person"));
			peopleArr.add(pNode.getId());
			if (rnd.nextDouble() > maleRand) {
				pNode.setProperty("Gender", "Female");
				pNode.createRelationshipTo(femaleNode, RelationshipType.withName("Gender"));
			} else {
				pNode.setProperty("Gender", "Male");
				pNode.createRelationshipTo(maleNode, RelationshipType.withName("Gender"));
			}

			double rndNationatlity = rnd.nextDouble();
			if (rndNationatlity <= persian) {
				pNode.setProperty("Nationality", "Persian");
				pNode.createRelationshipTo(persianNode, RelationshipType.withName("Nationality"));
			} else if (rndNationatlity <= american) {
				pNode.setProperty("Nationality", "American");
				pNode.createRelationshipTo(americanNode, RelationshipType.withName("Nationality"));
			} else if (rndNationatlity <= chinese) {
				pNode.setProperty("Nationality", "Chinese");
				pNode.createRelationshipTo(chineseNode, RelationshipType.withName("Nationality"));
			}
		}

		ArrayList<Long> diseaseArr = new ArrayList<Long>();
		// create different disease
		for (int d = 0; d < disease; d++) {
			Node dNode = dataGraph.createNode();
			dNode.addLabel(Label.label("Disease_" + d));
			diseaseArr.add(dNode.getId());
		}

		ArrayList<Integer> allDisease = new ArrayList<Integer>();
		for (int d = 0; d < disease; d++) {
			allDisease.add(d);
		}
		for (Node node : dataGraph.getAllNodes()) {
			if (node.getLabels().iterator().next().name().equals("Person")) {
				int numberOfDiseaseForThisPerson = rnd.nextInt(maxNumberOfDiseaseForEachPerson) + 1;
				Collections.shuffle(allDisease);

				for (int j = 0; j < numberOfDiseaseForThisPerson; j++) {
					Relationship rel = node.createRelationshipTo(
							dataGraph.getNodeById(diseaseArr.get(allDisease.get(j))),
							RelationshipType.withName("hasDisease"));

					int lastDiseaseTime = 0;
					int diseaseRecurrence = 0;

					List<Integer> validTimestamps = new ArrayList<Integer>();
					while (lastDiseaseTime < T && diseaseRecurrence < maxDiseaseRecurrence) {
						int init = Math.min(T, rnd.nextInt(T - lastDiseaseTime) + lastDiseaseTime);
						int finish = Math.min(T, rnd.nextInt(T - lastDiseaseTime) + (lastDiseaseTime + init));
						lastDiseaseTime = finish;
						validTimestamps.add(init);
						validTimestamps.add(finish);
						diseaseRecurrence++;
					}

					rel.setProperty("timepoints", validTimestamps.toArray(new Integer[validTimestamps.size()]));

				}
			}
		}

		for (Relationship rel : dataGraph.getAllRelationships()) {
			if (!rel.hasProperty("timepoints")) {
				rel.setProperty("timepoints", new int[] { 0, T });
			}
		}

		tx1.success();
		tx1.close();

		dataGraph.shutdown();
		System.out.println("done!");

	}

}
