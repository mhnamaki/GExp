package tryingToTranslate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;

import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class GraphInfraToUndirectedRelsWriter {

	public static void main(String[] args) throws Exception {

		//String graphInfraPath = "/Users/mnamaki/AQPEQ/GraphExamples/subgraphTest/graph/";
		String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/citation/graph/";
		if (args.length > 0) {
			graphInfraPath = args[0];
		}

		GraphInfraReaderArray graph = new GraphInfraReaderArray(graphInfraPath, false);
		graph.readWithNoLabels();

		ArrayList<Pair<Integer, Integer>> es = new ArrayList<>();
		HashSet<String> addedRels = new HashSet<String>();
		int cnt = 0;
		for (RelationshipInfra rel : graph.relationOfRelId) {
			cnt++;
			if (cnt % 250000 == 0) {
				System.out.println("cnt rels processed: " + cnt + " size of es: " + es.size());
			}

			if (rel != null) {
				if (rel.sourceId < rel.destId) {
					String s = rel.sourceId + "_" + rel.destId;
					if (!addedRels.contains(s)) {
						es.add(new Pair<Integer, Integer>(rel.sourceId, rel.destId));
						addedRels.add(s);
					}
				} else {
					String s = rel.destId + "_" + rel.sourceId;
					if (!addedRels.contains(s)) {
						es.add(new Pair<Integer, Integer>(rel.destId, rel.sourceId));
						addedRels.add(s);
					}
				}
			}

			else {
				System.out.println("rel null at cnt " + cnt);
			}
		}

		System.out.println("done with reading pairs");

		File fout = new File(graphInfraPath + "undirectedRels.tsv");
		FileOutputStream fos = new FileOutputStream(fout);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		for (Pair<Integer, Integer> e : es) {
			bw.write(e.first + "\t" + e.second);
			bw.newLine();
		}

		bw.close();

		FileInputStream fin = new FileInputStream(fout);

		BufferedReader br = new BufferedReader(new InputStreamReader(fin));

		HashSet<Integer> nodeIdSet = new HashSet<Integer>();
		String line = "";
		while ((line = br.readLine()) != null) {

			String[] splittedNodeIds = line.split("\t");

			nodeIdSet.add(Integer.parseInt(splittedNodeIds[0]));
			nodeIdSet.add(Integer.parseInt(splittedNodeIds[1]));

		}

		System.out.println("node ids size: " + nodeIdSet.size());

		br.close();

	}

}
