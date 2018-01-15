package baselines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import aqpeq.utilities.KWSUtilities;
import aqpeq.utilities.MapUtil;
import aqpeq.utilities.StringPoolUtility;
import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import dataset.BerkeleyDB.BerkleleyDB;
import graphInfra.GraphInfraReaderArray;
import queryExpansion.AnswerAsInput;

public class DataCloudDistinguishability {

	// number of keywords to be returned
	int k;

	// augmented data graph
	GraphInfraReaderArray graph;

	// top selected keywords
	public ArrayList<String> topFrequentKeywords;

	private HashSet<String> initialKeywords;

	private HashMap<Integer, HashSet<Integer>> nodeIdsOfToken;

	public DataCloudDistinguishability(GraphInfraReaderArray graph, HashMap<Integer, HashSet<Integer>> nodeIdsOfToken,
			ArrayList<AnswerAsInput> topNAnswers, int k, Collection<String> initialKeywords) {

		this.k = k; // we want top-1 then just k=1

		this.initialKeywords = new HashSet<String>(initialKeywords);
		this.graph = graph;
		this.nodeIdsOfToken = nodeIdsOfToken;
	}

	public ArrayList<String> expand() throws Exception {

		HashMap<String, HashSet<Integer>> candidatesOfAKeyword = new HashMap<String, HashSet<Integer>>();
		HashSet<Integer> candidatesSet = new HashSet<Integer>();

		KWSUtilities.findCandidatesOfKeywordsUsingInvertedList(initialKeywords, nodeIdsOfToken, candidatesOfAKeyword,
				candidatesSet);

		HashMap<Integer, Scores> scoresOfKeyword = new HashMap<Integer, Scores>();

		// for each keyword in query
		for (String keyword : candidatesOfAKeyword.keySet()) {

			// for each candidate node id
			for (int nodeId : candidatesOfAKeyword.get(keyword)) {

				// for each neighbor node id
				for (int otherNodeId : graph.nodeOfNodeId.get(nodeId).getOutgoingRelIdOfSourceNodeId().keySet()) {

					// find all tokens for that neighbor
					HashSet<Integer> allTokensOfNode = new HashSet<Integer>();
					allTokensOfNode.addAll(graph.nodeOfNodeId.get(otherNodeId).tokens);

					// also from properties
					if (graph.nodeOfNodeId.get(otherNodeId).getProperties() != null)
						allTokensOfNode.addAll(graph.nodeOfNodeId.get(otherNodeId).getProperties());

					// count the frequency of that token
					for (int tokenId : allTokensOfNode) {
						scoresOfKeyword.putIfAbsent(tokenId, new Scores());
						scoresOfKeyword.get(tokenId).setFrequency(scoresOfKeyword.get(tokenId).getFrequency() + 1);
					}
				}
			}
		}

		int totalTokensInCandidatesAndTheirNeighbors = scoresOfKeyword.keySet().size();
		int totalTokensInGraph = nodeIdsOfToken.size();
		for (int tokenId : scoresOfKeyword.keySet()) {

			Scores scores = scoresOfKeyword.get(tokenId);

			scores.setTf(
					1 + Math.log((double) scores.getFrequency() / (double) totalTokensInCandidatesAndTheirNeighbors));

			scores.setIdf(1 + Math.log((double) totalTokensInGraph / (double) nodeIdsOfToken.get(tokenId).size()));

			scores.setTfIdf(scores.getTf() * scores.getIdf());
		}

		List<Map.Entry<Integer, Scores>> list = new LinkedList<Map.Entry<Integer, Scores>>(scoresOfKeyword.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Scores>>() {
			public int compare(Map.Entry<Integer, Scores> o1, Map.Entry<Integer, Scores> o2) {
				return Double.compare(o1.getValue().getTfIdf(), o1.getValue().getTfIdf());
			}
		});

		Map<Integer, Scores> result = new LinkedHashMap<Integer, Scores>();
		for (Map.Entry<Integer, Scores> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		int i = 0;

		int minFreq = Integer.MAX_VALUE;
		String minFreqKeyword = StringPoolUtility.getStringOfId(result.entrySet().iterator().next().getKey());

		topFrequentKeywords = new ArrayList<String>();

		for (Map.Entry<Integer, Scores> entry : result.entrySet()) {

			if (initialKeywords.contains(StringPoolUtility.getStringOfId(entry.getKey())))
				continue;

			if (nodeIdsOfToken.get(entry.getKey()).size() > DummyProperties.MaxFrequencyBoundForKeywordSelection) {
				if (nodeIdsOfToken.get(entry.getKey()).size() < minFreq) {
					minFreq = nodeIdsOfToken.get(entry.getKey()).size();
					minFreqKeyword = StringPoolUtility.getStringOfId(entry.getKey());
				}
				continue;
			}

			topFrequentKeywords.add(StringPoolUtility.getStringOfId(entry.getKey()));

			i++;

			if (i == k)
				break;
		}

		if (topFrequentKeywords.isEmpty()) {
			System.out.println("min freq was " + minFreq + " and max acceptable freq was set "
					+ DummyProperties.MaxFrequencyBoundForKeywordSelection);

			topFrequentKeywords.add(minFreqKeyword);

			System.out.println("dataCloud selected " + minFreqKeyword + " instead ");

		}

		return topFrequentKeywords;

	}
}
