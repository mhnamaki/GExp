package baselines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import aqpeq.utilities.MapUtil;
import aqpeq.utilities.StringPoolUtility;
import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import graphInfra.GraphInfraReaderArray;
import queryExpansion.AnswerAsInput;

public class CoOccurrence {

	// top-n answer trees A discovered by KWS algorithm A,
	ArrayList<AnswerAsInput> topNAnswers;

	// number of keywords to be returned
	int k;

	// augmented data graph
	GraphInfraReaderArray graph;

	// count the frequency of terms in the answers
	HashMap<Integer, Integer> freqOfKeywordsOnTheAnswers = new HashMap<Integer, Integer>();

	// top selected keywords
	public ArrayList<String> topFrequentKeywords;
	public ArrayList<Integer> topFrequentKeywordsInt;

	private HashSet<String> initialKeywords;

	private HashMap<Integer, HashSet<Integer>> nodeIdsOfToken;


	public CoOccurrence(GraphInfraReaderArray graph, HashMap<Integer, HashSet<Integer>> nodeIdsOfToken,
			ArrayList<AnswerAsInput> topNAnswers, int k, Collection<String> initialKeywords) {

		this.k = k; // we want top-1 then just k=1
		this.topNAnswers = topNAnswers;
		this.initialKeywords = new HashSet<String>(initialKeywords);
		this.graph = graph;
		this.nodeIdsOfToken = nodeIdsOfToken;
	}

	public ArrayList<String> expand() throws Exception {

		for (AnswerAsInput answer : topNAnswers) {
			for (Integer nodeId : answer.getAllNodes()) {
				for (Integer keyword : DummyFunctions.getKeywords(graph, nodeId)) {
					freqOfKeywordsOnTheAnswers.putIfAbsent(keyword, 0);
					freqOfKeywordsOnTheAnswers.put(keyword, freqOfKeywordsOnTheAnswers.get(keyword) + 1);
				}
			}
		}

		Map<Integer, Integer> map = MapUtil.sortByValueDesc(freqOfKeywordsOnTheAnswers);

		int i = 0;

		int minFreq = Integer.MAX_VALUE;
		String minFreqKeyword = StringPoolUtility.getStringOfId(map.entrySet().iterator().next().getKey());

		topFrequentKeywords = new ArrayList<String>();
		topFrequentKeywordsInt = new ArrayList<Integer>();

		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {

			if (initialKeywords.contains(StringPoolUtility.getStringOfId(entry.getKey())))
				continue;

			if (nodeIdsOfToken.get(entry.getKey()).size() > DummyProperties.MaxFrequencyBoundForKeywordSelection) {
				if (nodeIdsOfToken.get(entry.getKey()).size() < minFreq) {
					minFreq = nodeIdsOfToken.get(entry.getKey()).size();
					minFreqKeyword = StringPoolUtility.getStringOfId(entry.getKey());
				}
				continue;
			}

			boolean validToken = true;

			for (AnswerAsInput answer : topNAnswers) {
				if (answer.getRootNodeId() > -1
						&& graph.nodeOfNodeId.get(answer.getRootNodeId()).getTokens().contains(entry.getKey())) {
					validToken = false;
					break;
				}
			}

			if (!validToken)
				continue;

			topFrequentKeywords.add(StringPoolUtility.getStringOfId(entry.getKey()));
			topFrequentKeywordsInt.add(entry.getKey());

			i++;

			if (i == k)
				break;
		}

		if (topFrequentKeywords.isEmpty()) {
			System.out.println("min freq was " + minFreq + " and max acceptable freq was set "
					+ DummyProperties.MaxFrequencyBoundForKeywordSelection);

			topFrequentKeywords.add(minFreqKeyword);

			System.out.println("coOcc selected " + minFreqKeyword + " instead ");

		}

		return topFrequentKeywords;

	}
}
