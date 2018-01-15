package aqpeq.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class TimeLogger {

	public static void LogTime(String outputPath, boolean append, ArrayList<InfoHolder> timeInfos) throws Exception {

		Collections.sort(timeInfos, new Comparator<InfoHolder>() {
			@Override
			public int compare(InfoHolder o1, InfoHolder o2) {
				return o1.index.compareTo(o2.index);
			}

		});

		File fout = new File(outputPath);
		boolean isFreshFile = true;
		if (fout.exists()) {
			isFreshFile = false;
		}

		FileOutputStream fos = new FileOutputStream(fout, append);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		if (isFreshFile) {
			for (InfoHolder infoHolder : timeInfos) {
				bw.write(infoHolder.key + ",");
			}

			bw.newLine();
		}

		for (InfoHolder infoHolder : timeInfos) {
			bw.write(infoHolder.value.toString() + ",");
		}

		bw.newLine();

		bw.close();
	}
}
