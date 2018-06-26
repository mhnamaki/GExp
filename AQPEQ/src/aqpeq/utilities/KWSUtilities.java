package aqpeq.utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;

import graphInfra.GraphInfraReaderArray;
import queryExpansion.CostAndNodesOfAnswersPair;
import static java.util.stream.Collectors.toCollection;

public class KWSUtilities {

	public static ArrayList<ArrayList<String>> readKeywords(String keywordsPath, int l) throws Exception {
		ArrayList<ArrayList<String>> keywordsSet = new ArrayList<ArrayList<String>>();
		FileInputStream fis = new FileInputStream(keywordsPath);
		// Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String keywordsLine = "";
		while ((keywordsLine = br.readLine()) != null) {

			String[] splittedQueryLine = keywordsLine.split(";");
			// b
			int currentB = Integer.parseInt(splittedQueryLine[0].split(",")[0].split(":")[1]);

			// TODO: I'm not sure which one is better
			// if (currentB != b)
			// continue;

			// l
			int currentL = Integer.parseInt(splittedQueryLine[0].split(",")[1].split(":")[1]);

			if (currentL != l)
				continue;

			ArrayList<String> keywords = new ArrayList<String>();
			StringTokenizer stringTokenizer = new StringTokenizer(splittedQueryLine[1], ",", false);

			if (DummyProperties.debugMode)
				System.out.println(stringTokenizer.countTokens());

			while (stringTokenizer.hasMoreElements()) {
				String nextToken = DummyFunctions.getCleanedString(stringTokenizer.nextElement().toString());

				if (DummyProperties.debugMode)
					System.out.println("nextToken: " + nextToken);

				keywords.add(nextToken);
			}
			keywordsSet.add(keywords);
		}
		br.close();
		return keywordsSet;
	}

	// public static void findCandidatesOfKeywordsUsingBDB(Collection<String>
	// keywords, BerkleleyDB berkeleyDB,
	// HashMap<String, HashSet<Integer>> candidatesOfAKeyword, HashSet<Integer>
	// candidatesSet) throws Exception {
	// HashSet<String> stopWord = DummyFunctions.getStopwordsSet();
	// for (String keyword : keywords) {
	// ArrayList<String> keywordList = new ArrayList<String>();
	// for (String token : Dummy.DummyFunctions.getTokens(keyword)) {
	// if (!stopWord.contains(token)) {
	// keywordList.add(token);
	// }
	// }
	//
	// HashSet<Integer> candidate =
	// berkeleyDB.SearchNodeIdsByKeyword(keywordList);
	//
	// candidatesOfAKeyword.put(keyword, candidate);
	// candidatesSet.addAll(candidate);
	// }
	//
	// // debug
	// if (DummyProperties.debugMode) {
	// for (String keyword : candidatesOfAKeyword.keySet()) {
	// System.out.println(
	// "keyword: " + keyword + ", size of candidate set:" +
	// candidatesOfAKeyword.get(keyword).size()
	// + ", candidate set: " + candidatesOfAKeyword.get(keyword));
	// }
	// System.out.println();
	// }
	// }

	public static void findCandidatesOfKeywordsUsingInvertedList(Collection<String> keywords,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfToken, HashMap<String, HashSet<Integer>> candidatesOfAKeyword,
			HashSet<Integer> candidatesSet) throws Exception {

		// System.out.println("nodeIdsOfToken: " + nodeIdsOfToken.size());

		for (String keyword : keywords) {
			HashSet<Integer> candidateSet = new HashSet<Integer>();
			for (String token : Dummy.DummyFunctions.getTokens(keyword)) {

				int tokenId = StringPoolUtility.getIdOfStringFromPool(token);
				if (nodeIdsOfToken.containsKey(tokenId)) {
					HashSet<Integer> candidateTem = new HashSet<Integer>();
					candidateTem.addAll(nodeIdsOfToken.get(tokenId));
					if (candidateSet.isEmpty()) {
						candidateSet = candidateTem;
					} else {
						candidateSet.retainAll(candidateTem);
					}
				}

			}

			if (candidateSet.size() > DummyProperties.MaxFrequencyBoundForKeywordSelection) {
				candidateSet = candidateSet.stream().limit(DummyProperties.MaxFrequencyBoundForKeywordSelection)
						.collect(toCollection(LinkedHashSet::new));
			}

			candidatesOfAKeyword.put(keyword, candidateSet);
			candidatesSet.addAll(candidatesOfAKeyword.get(keyword));

		}

		// debug
		if (DummyProperties.debugMode) {
			for (String keyword : candidatesOfAKeyword.keySet()) {
				System.out.println("keyword: " + keyword + ", candidate set: " + candidatesOfAKeyword.get(keyword));
			}
			System.out.println();
		}
	}

	public static void removeHighFrequentKeywordsFromMap(GraphInfraReaderArray graph,
			HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap) throws Exception {

		Iterator<Integer> itr = estimatedWeightOfSuggestedKeywordMap.keySet().iterator();
		while (itr.hasNext()) {
			Integer key = itr.next();
			int size = 0;
			try {
				size = graph.nodeIdsOfToken.get(key).size();
			} catch (Exception exc) {
				System.err.println("key: " + key);
				System.err.println(exc.getMessage());
			}

			if (size > DummyProperties.MaxFrequencyBoundForKeywordSelection) {
				itr.remove();
			}
		}

	}

	public static void printSuggestedKeywords(
			HashMap<String, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap) {
		System.out.println("printSuggestedKeywords info START:");
		for (String key : estimatedWeightOfSuggestedKeywordMap.keySet()) {
			System.out.print(key + ":" + estimatedWeightOfSuggestedKeywordMap.get(key).cost);
		}
		System.out.println("printSuggestedKeywords info END");
	}

}
