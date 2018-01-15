package quickTests;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashSet;

public class countIMDBMovies {
	public static void main(String[] args) throws Exception {
		FileInputStream fis = new FileInputStream("/Users/mnamaki/Documents/Education/PhD/Fall2017/KWS/movies.list");

		// Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		HashSet<String> movieSet = new HashSet<String>();

		int cnt = 0;
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] lineSplitted = line.split("\t");
			String movieComplete = lineSplitted[0].trim().toLowerCase();
			String movieTitle = movieComplete.substring(0, movieComplete.indexOf("(")).replaceAll("\"", "");
			movieSet.add(movieTitle);

			cnt++;
			if (cnt % 250000 == 0)
				System.out.println(movieTitle);
		}

		System.out.println("movieSet: " + movieSet.size());

		br.close();
	}
}
