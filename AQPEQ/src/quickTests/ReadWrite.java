package quickTests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ReadWrite {

	public static void main(String[] args) throws IOException {

		int rewrite = 8000000;

		for (int i = 0; i < 1; i++) {

			String relationshipDir = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/citation/graph/";

			FileInputStream fis = new FileInputStream(relationshipDir + "relationships.in");
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			FileOutputStream fos = new FileOutputStream(relationshipDir + "relationships_" + i + ".in");
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			int cnt = 0;
			String line = null;
			while ((line = br.readLine()) != null) {

				if (cnt > rewrite)
					break;

				bw.write(line + "\n");
				cnt++;
			}

			rewrite -= 2000000;
			br.close();
			bw.close();
		}

	}

}
