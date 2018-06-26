package graphFormatConverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;

public class InfraToPrunedLandmarkConverter {

	public String graphInfraPath = "/Users/mnamaki/AQPEQ/GraphExamples/demo/IMDB1/";
	public String newGraphForPrunedLandmarkTest = "/Users/mnamaki/AQPEQ/GraphExamples/demo/IMDB1/IMDB1.tsv";

	public static void main(String[] args) throws Exception {
		InfraToPrunedLandmarkConverter converter = new InfraToPrunedLandmarkConverter();
		converter.run();

	}

	private void run() throws Exception {

		File fout = new File(newGraphForPrunedLandmarkTest);
		FileOutputStream fos = new FileOutputStream(fout);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		GraphInfraReaderArray graph = new GraphInfraReaderArray(graphInfraPath,true);
		graph.readWithNoLabels();

		for (NodeInfra node : graph.nodeOfNodeId) {
			for (Integer targetNodeId : node.outgoingRelIdOfSourceNodeId.keySet()) {
				bw.write(node.nodeId + "\t" + targetNodeId + "\n");
			}
		}

		bw.close();

	}

}
