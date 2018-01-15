package queryGeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ExtractKeywordsOutOfRandomWalkFiles {

	private static int lFrom = 2;
	private static int bFrom = 2;
	private static int bTo = 2;
	private static String dataset = "queryVaryRel";
	private static int maxL = 2;
	//mhn
//	private static String rndWalkFilesPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/rndFiles/";
//	private static String outputPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/imdb/queries/";

	//xin
	private static String rndWalkFilesPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/dbpGraphInfra/graph/dbp/graph1/query/";
	private static String outputPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/dbpGraphInfra/graph/dbp/graph1/query/";

	
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-bFrom")) {
				bFrom = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-bTo")) {
				bTo = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-maxL")) {
				maxL = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-rndWalkFilesPath")) {
				rndWalkFilesPath = args[++i];
				if (!rndWalkFilesPath.endsWith("/"))
					rndWalkFilesPath += "/";
			} else if (args[i].equals("-dataset")) {
				dataset = args[++i];
			} else if (args[i].equals("-outputPath")) {
				outputPath = args[++i];
				if (!outputPath.endsWith("/"))
					outputPath += "/";
			}
		}

		if (lFrom > maxL)
			throw new Exception("lFrom > maxL");

		File fout = new File(outputPath + dataset + "_query.txt");
		FileOutputStream fos = new FileOutputStream(fout);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		// for each b from bFrom to bTo
		for (int b = bFrom; b <= bTo; b++) {
			// read from its corresponding file and maxL

			FileInputStream fis = new FileInputStream(
					rndWalkFilesPath + dataset + "_query_l_" + maxL + "_b_" + b + ".txt");

			// Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			// generate from l=2 to maxL query files by
			// considering frequency bound
			int num = maxL - lFrom + 1;
			// File[] fouts = new File[num];
			// FileOutputStream[] foss = new FileOutputStream[num];
			// BufferedWriter[] bws = new BufferedWriter[num];
			// for (int i = 0; i < num; i++) {
			// fouts[i] = new File(outputPath + dataset + "_query_l_" + (i +
			// lFrom) + "_b_" + b + ".txt");
			// foss[i] = new FileOutputStream(fouts[i]);
			// bws[i] = new BufferedWriter(new OutputStreamWriter(foss[i]));
			// }

			String rndWalkFileLine = null;
			while ((rndWalkFileLine = br.readLine()) != null) {

				if (rndWalkFileLine.contains("?")) {
					continue;
				}

				String[] tokens = rndWalkFileLine.split(",");

				for (int i = 0; i < num; i++) {
					bw.write("b:" + b + ",l:" + (i + lFrom) + ";");
					for (int j = 0; j < i + lFrom; j++) {
						bw.write(tokens[j].split(":")[0]);
						if (j < i + 1) {
							bw.write(",");
						}
					}
					bw.write("\n");
				}
			}

			br.close();
		}

		bw.close();

	}
}
