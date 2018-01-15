package graphFormatConverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;

public class InfraToPrunedLandmarkConverter {

	public String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/amazon/amazonGraphInfra/";
	public String newGraphForPrunedLandmarkTest = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/amazon/amazonGraphInfra/amazonForLandmarkTest.tsv";

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
