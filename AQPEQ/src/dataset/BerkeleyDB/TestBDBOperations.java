package dataset.BerkeleyDB;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

import aqpeq.utilities.Dummy;
import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import dataset.BerkeleyDB.BerkleleyDB.MyBDBObject;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class TestBDBOperations {

	static String keywordsPath = "/Users/zhangxin/Desktop/keyword/1.txt";
	static GraphInfraReaderArray graph;

	public TestBDBOperations() {

	}

	public static void main(String[] args) throws Exception {
		TestBDBOperations test = new TestBDBOperations();
		String envFilePath = "/Users/zhangxin/Desktop/IMDB/sample/dbEnv";
		// String envFilePath =
		// "/Users/zhangxin/Desktop/IMDB/withProp/dbEnvWithProp";
		DummyProperties.withProperties = true;
		BerkleleyDB berkeleyDB = new BerkleleyDB("database", "catDatabase", envFilePath);
		HashSet<String> stopWord = DummyFunctions.getStopwordsSet();
		ArrayList<ArrayList<String>> keywordsList = test.readKeywords();
		for (ArrayList<String> keywords : keywordsList) {
			for (String keyword : keywords) {
				ArrayList<String> tokenList = new ArrayList<String>();
				for (String token : Dummy.DummyFunctions.getTokens(keyword)) {
					if (!stopWord.contains(token)) {
						tokenList.add(token);
						HashSet can = berkeleyDB.SearchNodeIdsByToken(token);
						System.out.println(token + " -> " + can.size() + " -> " + can);
					}
				}
				HashSet<Integer> tokenCandidate = berkeleyDB.SearchNodeIdsByKeyword(tokenList);
				System.out.println("token -> " + tokenList + " size -> " + tokenCandidate.size());

			}
		}

		berkeleyDB.CloseDatabase();

	}

	private ArrayList<ArrayList<String>> readKeywords() throws Exception {
		ArrayList<ArrayList<String>> keywordsSet = new ArrayList<ArrayList<String>>();
		FileInputStream fis = new FileInputStream(keywordsPath);
		// Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String keywordsLine = "";
		while ((keywordsLine = br.readLine()) != null) {
			ArrayList<String> keywords = new ArrayList<String>();
			StringTokenizer stringTokenizer = new StringTokenizer(keywordsLine, ",", false);
			System.out.println(stringTokenizer.countTokens());
			while (stringTokenizer.hasMoreElements()) {
				String nextToken = DummyFunctions.getCleanedString(stringTokenizer.nextElement().toString());

				System.out.println("nextToken: " + nextToken);

				keywords.add(nextToken);
			}
			keywordsSet.add(keywords);
		}
		br.close();
		return keywordsSet;
	}

}
