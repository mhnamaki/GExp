//TODO: add current keywords to make sure we do not get them again!

package queryExpansion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import aqpeq.utilities.KWSUtilities;
import aqpeq.utilities.StringPoolUtility;
import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import graphInfra.GraphInfraReaderArray;
import graphInfra.RelationshipInfra;
import tryingToTranslate.PrunedLandmarkLabeling;

public class DivQKWSExpand {

	// top-n answer trees A discovered by KWS algorithm A,
	ArrayList<AnswerAsInput> topNAnswers;

	// augmented data graph
	GraphInfraReaderArray graph;

	private int r;

	public int visitedNodes = 0;
	public int visitedKeywords = 0;
	public double avgQualityOfSuggestedKeyword = 0d;
	public double initialOveralWeight = 0d;
	public double querySuggestionKWSStartTime = 0d;
	public double querySuggestionKWSDuration = 0d;

	// after pruning high-frequent keywordsF
	public double totalWeightOfSuggestedKeywords = 0d;
	public double lowestWeightOfSuggestedKeyword;
	public Integer lowestWeightSuggestedKeywordId;

	public int highFrequentKeywordsRemovedNum = 0;

	public CostAndNodesOfAnswersPair bestKeywordInfo;

	private HashSet<Integer> keywordsSet;

	public double getKeywordsDuration = 0d;

	private double diviersificationStart;
	public double diviersificationDuration;

	public HashMap<Integer, TermInfo> infosOfTokenId = new HashMap<Integer, TermInfo>();
	// HashMap, key: tokenId, value: distance
	public HashMap<Integer, Double> termDistance = new HashMap<Integer, Double>();
	ArrayList<HashSet<Integer>> visitedTermBySSSPi = new ArrayList<>();
	HashSet<Integer> singleQ_T = new HashSet<Integer>();
	public double qT_F = 0d;
	HashMap<Integer, SSSPIterator> ssspOfNodeId;
	HashMap<Integer, HashSet<Integer>> nodeIdsOfCluster = new HashMap<Integer, HashSet<Integer>>();
	HashMap<Integer, Integer> clusterOfNodeId = new HashMap<Integer, Integer>();

	// index
	String indexPath = "/Users/zhangxin/AQPEQ/GraphExamples/GenTemTest/graph2/index_from_java";
	public PrunedLandmarkLabeling pl;
	public HashMap<Integer, HashSet<Integer>> nodeIdsOfToken;
	public HashMap<Integer, HashSet<Integer>> termIdsOfNode;
	ObjectiveHandler objHandler;

	private int numberOfQueries = 1;

	private int maxTokenOfNode;

	public DivQKWSExpand(GraphInfraReaderArray graph, HashSet<Integer> keywordsSet,
			ArrayList<AnswerAsInput> topNAnswers, int r, double epsilon, double lambda, int numberOfQueries,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfToken, int mTokenOfNode) throws Exception {
		this.topNAnswers = topNAnswers;
		this.graph = graph;

		// pl = new PrunedLandmarkLabeling(8);
		// pl.LoadIndex(indexPath);

		this.keywordsSet = keywordsSet;
		this.nodeIdsOfToken = nodeIdsOfToken;

		this.r = r;
		this.numberOfQueries = numberOfQueries;

		// for (RelationshipInfra rel : graph.relationOfRelId) {
		// rel.weight = 1.0f;
		// }
		this.maxTokenOfNode = mTokenOfNode;

		objHandler = new ObjectiveHandler(numberOfQueries, epsilon, lambda);

	}

	public HashSet<Integer> expand() throws Exception {

		querySuggestionKWSStartTime = System.nanoTime();

		HashSet<Integer> visitedNodesSet = new HashSet<Integer>();

		HashSet<Integer> contentNodes = new HashSet<Integer>();
		for (int i = 0; i < topNAnswers.size(); i++) {
			contentNodes.addAll(topNAnswers.get(i).getContentNodes());

			// nodeIdsOfCluster, key: cluster_id, value: HashSet<> ->
			// contentNode id
			for (int j = 0; j < topNAnswers.get(i).getContentNodes().size(); j++) {
				nodeIdsOfCluster.putIfAbsent(j, new HashSet<Integer>());
				int contentNodeId = topNAnswers.get(i).getContentNodes().get(j);
				nodeIdsOfCluster.get(j).add(contentNodeId);
				clusterOfNodeId.put(contentNodeId, j);
			}

		}

		/* initializing |V_C| number of SSSP's */

		ssspOfNodeId = new HashMap<Integer, SSSPIterator>();

		ArrayList<Integer> orderedContentNodeIds = new ArrayList<Integer>();

		// for each v_i \in V_C do
		for (int nodeId : contentNodes) {
			// create iterator SSSPi originated from vi bounded by r;
			ssspOfNodeId.put(nodeId, new SSSPIterator(graph, nodeId, r));
			orderedContentNodeIds.add(nodeId);
			visitedTermBySSSPi.add(new HashSet<>());
		}

		// System.out.println("contentNodes length = " + contentNodes.size());
		// System.out.println("top-n answers = " + topNAnswers.size());
		// System.out.println();

		/* term generation using a TA-style algorithm */
		// while exits v_i \in V_C : SSSPi:peekDist() <= r do
		int i = -1;
		while (hasNextNode(ssspOfNodeId)) {
			// i := pick from [1; jVCj] in a round-robin way;
			i++;
			i = i % ssspOfNodeId.size();

			int currentContentNodeId = orderedContentNodeIds.get(i);

			// <u; d> := SSSPi:next();
			SSSPNode currentNode = ssspOfNodeId.get(currentContentNodeId).getNextSSSPNode();

			int tokenCnt = 0;
			if (currentNode == null) {
				continue;
			}

			visitedNodesSet.add(currentNode.node.nodeId);
			// if (visitedNodes.size() % 2000 == 0) {
			// System.out.println("visitedNodes.size: " + visitedNodes.size());
			// }

			if (visitedNodesSet.size() > DummyProperties.MaxNumberOfVisitedNodes) {
				break;
			}

			// for each term t \in L(u) do
			if (currentNode.node.tokens != null) {
				for (Integer tokenId : currentNode.node.tokens) {

					tokenCnt++;
					if (tokenCnt > maxTokenOfNode)
						break;

					// if t \in dist then continue ; /*better d already
					// captured*/
					if (visitedTermBySSSPi.get(i).contains(tokenId) || keywordsSet.contains(tokenId))
						continue;

					visitedTermBySSSPi.get(i).add(tokenId);

					// dist[t][i] := d;
					if (!infosOfTokenId.containsKey(tokenId)) {
						TermInfo termInfo = new TermInfo(tokenId, ssspOfNodeId.size());
						infosOfTokenId.put(tokenId, termInfo);
					}

					infosOfTokenId.get(tokenId).setDistance(i, currentNode.costFromOriginId,
							clusterOfNodeId.get(currentContentNodeId), currentContentNodeId);

				}
			}

			if (DummyProperties.withProperties == true && currentNode.node.getProperties() != null) {

				for (Integer tokenId : currentNode.node.getProperties()) {

					tokenCnt++;
					if (tokenCnt > maxTokenOfNode)
						break;

					// if t \in dist then continue ; /*better d already
					// captured*/
					if (visitedTermBySSSPi.get(i).contains(tokenId) || keywordsSet.contains(tokenId))
						continue;

					visitedTermBySSSPi.get(i).add(tokenId);

					// dist[t][i] := d;
					if (!infosOfTokenId.containsKey(tokenId)) {
						TermInfo termInfo = new TermInfo(tokenId, ssspOfNodeId.size());
						infosOfTokenId.put(tokenId, termInfo);
					}

					infosOfTokenId.get(tokenId).setDistance(i, currentNode.costFromOriginId,
							clusterOfNodeId.get(currentContentNodeId), currentContentNodeId);

				}
			}

		}

		// now we've explored distance r;
		// we need to find the one with the best singleton element first, do it
		// for k times to find other good terms.

		diviersificationStart = System.nanoTime();

		for (int j = 0; j < numberOfQueries; j++) {
			// compute current F for each visited term
			double maxMGValue = 0d;
			int maxMGTermId = 0;
			for (int tokenId : infosOfTokenId.keySet()) {

				if (!singleQ_T.contains(tokenId)) {
					double tempF = objHandler.computeF(singleQ_T, infosOfTokenId, nodeIdsOfCluster, r, null);
					infosOfTokenId.get(tokenId).tempMGValue = objHandler.marginalGain(singleQ_T, tokenId,
							infosOfTokenId, nodeIdsOfCluster, r, tempF);

					if (infosOfTokenId.get(tokenId).tempMGValue > maxMGValue) {
						maxMGValue = infosOfTokenId.get(tokenId).tempMGValue;
						maxMGTermId = tokenId;
					}
				}
			}

			singleQ_T.add(maxMGTermId);

		}

		diviersificationDuration += ((System.nanoTime() - diviersificationStart) / 1e6);

		querySuggestionKWSDuration = ((System.nanoTime() - querySuggestionKWSStartTime) / 1e6);

		visitedKeywords = infosOfTokenId.size();

		visitedNodes = visitedNodesSet.size();

		qT_F = objHandler.computeF(singleQ_T, infosOfTokenId, nodeIdsOfCluster, r, null);

		// debug
		// System.out.println("DivQ");
		// System.out.println("nodeIdsOfCluster");
		// for (int clusterId : nodeIdsOfCluster.keySet()) {
		// System.out.println("cluster id = " + clusterId + ", node id = " +
		// nodeIdsOfCluster.get(clusterId));
		// }
		// System.out.println("infosOfTokenId");
		// for (int termId : infosOfTokenId.keySet()) {
		// System.out.println("term id = " + termId + ", term is " +
		// StringPoolUtility.getStringOfId(termId));
		// System.out.println("distance map");
		// TermInfo termInfo = infosOfTokenId.get(termId);
		// for (int clusterId : termInfo.distFromContentnodeMap.keySet()) {
		// System.out.println("cluster " + clusterId + " nodeId to distance Map
		// = "
		// + termInfo.distFromContentnodeMap.get(clusterId));
		// }
		// }
		// System.out.println("qT");
		// System.out.println(singleQ_T);

		for (int tokenId : singleQ_T) {
			termDistance.put(tokenId, infosOfTokenId.get(tokenId).getTotalDistOfTokenId());
		}

		return singleQ_T;
	}

	public boolean hasNextNode(HashMap<Integer, SSSPIterator> sssp) {
		for (Integer nodeId : sssp.keySet()) {
			if (sssp.get(nodeId).peekDist() <= r) {
				return true;
			}
		}
		return false;
	}

	// // HashMap<Integer, HashSet<Integer>> nodeIdsOfToken
	// // key: tokenId, value: HashSet of nodeIds
	// public int operateIndex(int termId, int nodeId) {
	// // pl.printDist(srcId, DesId);
	// // printDist needs nodeId
	// HashSet<Integer> nodeIds = nodeIdsOfToken.get(termId);
	// // TODO: here we want a distance set or the shortest distance from this
	// // term to the node
	// int distance = Integer.MAX_VALUE;
	// for (int nodeIdOfTerm : nodeIds) {
	// int temDistance = pl.queryDistance(nodeIdOfTerm, nodeId);
	// distance = Math.min(distance, temDistance);
	// }
	// return distance;
	// }

	// public static void main(String[] args) {
	//
	// }

}
