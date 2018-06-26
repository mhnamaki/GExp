//TODO: Fbest and lowerbound computation

package queryExpansion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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
import kotlin.jvm.internal.Lambda;
import tryingToTranslate.PrunedLandmarkLabeling;

public class IndexGenTemKWSExpand {

	// top-n answer trees A discovered by KWS algorithm A,
	ArrayList<AnswerAsInput> topNAnswers;

	// augmented data graph
	GraphInfraReaderArray graph;

	// DummyProperties.MaxNumberOfVisitedNodes;
	private int r;

	public int visitedNodes = 0;
	public int visitedKeywords = 0;

	public double querySuggestionKWSStartTime = 0d;
	public double querySuggestionKWSDuration = 0d;

	private HashSet<Integer> keywordsSet;

	private double upperboundlowerboundcomputationStart;
	public double upperboundlowerboundcomputationDuration;

	private double diviersificationStart;
	public double diviersificationDuration;

	private double indexComputationStart;
	public double indexComputationDuration;

	public int numberOfDistaneQuerying;
	private double distanceQueryingStart;
	private double distanceQueryingDuration;
	public double avgDistanceQueryingDuration;

	public HashMap<Integer, TermInfo> infosOfTokenId = new HashMap<Integer, TermInfo>();
	// HashMap, key: tokenId, value: distance
	public HashMap<Integer, Double> termDistance = new HashMap<Integer, Double>();
	HashSet<Integer> visitedTermBySSSPs = new HashSet<Integer>();
	StreamDivQ streamDivQ;
	HashSet<Integer> qT;
	public double qT_F = 0d;

	boolean logAnyTime = false;
	int callNumber = 0;

	HashMap<Integer, SSSPIterator> ssspOfNodeId;
	HashMap<Integer, HashSet<Integer>> nodeIdsOfCluster = new HashMap<Integer, HashSet<Integer>>();
	HashMap<Integer, Integer> clusterOfNodeId = new HashMap<Integer, Integer>();

	// index
	String indexPath = "/Users/zhangxin/AQPEQ/GraphExamples/GenTemTest/graph2/index_from_java";
	public PrunedLandmarkLabeling pl;
	public HashMap<Integer, HashSet<Integer>> nodeIdsOfToken;
	public HashMap<Integer, HashSet<Integer>> termIdsOfNode;

	double F_s = 0;
	double F_t = 0;
	double UB_F_s = Double.MAX_VALUE;
	double UB_rev_s = Double.MAX_VALUE;
	double UB_div_s = Double.MAX_VALUE;
	ObjectiveHandler obj;

	private int maxTokenOfNode;
	BufferedWriter bw;

	public IndexGenTemKWSExpand(GraphInfraReaderArray graph, HashSet<Integer> keywordsSet,
			ArrayList<AnswerAsInput> topNAnswers, int k, int r, double epsilon, double lambda,
			PrunedLandmarkLabeling pl, int numberOfQueries, HashMap<Integer, HashSet<Integer>> nodeIdsOfToken,
			int mTokenOfNode) throws Exception {

		this.topNAnswers = topNAnswers;
		this.graph = graph;

		this.pl = pl;

		this.keywordsSet = keywordsSet;
		this.nodeIdsOfToken = nodeIdsOfToken;
		// for (String keyword : keywordList){
		// this.keywords.add(StringPoolUtility.getIdOfStringFromPool(keyword));
		// }

		this.r = r;
		this.maxTokenOfNode = mTokenOfNode;

		streamDivQ = new StreamDivQ(numberOfQueries, epsilon, lambda);
		obj = new ObjectiveHandler(numberOfQueries, epsilon, lambda);

		File fout = new File("log.txt");
		FileOutputStream fos = new FileOutputStream(fout, true);

		bw = new BufferedWriter(new OutputStreamWriter(fos));
		bw.write(epsilon + ", ");
	}

	public HashSet<Integer> expand() throws Exception {

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
		}

		// System.out.println("contentNodes length = " + contentNodes.size());
		// System.out.println("top-n answers = " + topNAnswers.size());
		// System.out.println();

		querySuggestionKWSStartTime = System.nanoTime();

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

			if (currentNode == null || currentNode.node == null) {
				continue;
			}

			visitedNodesSet.add(currentNode.node.nodeId);

			if (visitedNodesSet.size() > DummyProperties.MaxNumberOfVisitedNodes)
				break;

			// for each term t \in L(u) do
			if (currentNode.node.tokens != null) {
				for (Integer tokenId : currentNode.node.tokens) {
					
					tokenCnt++;
					
					if (tokenCnt > maxTokenOfNode)
						break;

					handleTokenId(tokenId, currentNode.costFromOriginId, i, orderedContentNodeIds);

					if (qT != null) {
						// don't continue the alg
						break;
					}
				}
			}

			if (DummyProperties.withProperties == true && currentNode.node.getProperties() != null) {
				for (Integer tokenId : currentNode.node.getProperties()) {

					tokenCnt++;
					if (tokenCnt > maxTokenOfNode)
						break;

					handleTokenId(tokenId, currentNode.costFromOriginId, i, orderedContentNodeIds);

					if (qT != null) {
						// don't continue the alg
						break;
					}
				}
			}

			if (i == ssspOfNodeId.size() - 1) {
				upperboundlowerboundcomputationStart = System.nanoTime();

				double distLB = 0;
				HashMap<Integer, Double> covUB = new HashMap<Integer, Double>();
				for (int cNodeId : orderedContentNodeIds) {
					distLB += ssspOfNodeId.get(cNodeId).peekDist();

					covUB.putIfAbsent(clusterOfNodeId.get(cNodeId), 0d);
					if (distLB <= r) {
						covUB.put(clusterOfNodeId.get(cNodeId), clusterOfNodeId.get(cNodeId) + 1d);
					}
				}
				UB_rev_s = 1d / (1d + distLB);
				UB_F_s = obj.computeFUpperbound(UB_rev_s, covUB, nodeIdsOfCluster, infosOfTokenId);

				upperboundlowerboundcomputationDuration += ((System.nanoTime() - upperboundlowerboundcomputationStart)
						/ 1e6);
			}

			if (qT != null) {
				break;
			}

		}

		if (qT == null || qT.isEmpty()) {
			qT = streamDivQ.getTheBestCurrentQT();
		}

		querySuggestionKWSDuration = ((System.nanoTime() - querySuggestionKWSStartTime) / 1e6);

		visitedKeywords = infosOfTokenId.size();

		visitedNodes = visitedNodesSet.size();

		qT_F = obj.computeF(qT, infosOfTokenId, nodeIdsOfCluster, r, null);

		bw.write("" + qT_F);
		bw.newLine();
		bw.close();

		// debug
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
		// System.out.println(qT);

		for (int tokenId : qT) {
			termDistance.put(tokenId, infosOfTokenId.get(tokenId).getTotalDistOfTokenId());
		}

		avgDistanceQueryingDuration = (distanceQueryingDuration / ((double) numberOfDistaneQuerying));

		return qT;
	}

	private void handleTokenId(int tokenId, double costFromOriginId, int ssspi,
			ArrayList<Integer> orderedContentNodeIds) throws Exception {
		// if t \in dist then continue ; /*better d already captured*/
		if (visitedTermBySSSPs.contains(tokenId) || keywordsSet.contains(tokenId))
			return;

		visitedTermBySSSPs.add(tokenId);

		TermInfo termInfo = new TermInfo(tokenId, ssspOfNodeId.size());
		infosOfTokenId.put(tokenId, termInfo);

		// computing distance from content node to this term

		for (int j = 0; j < orderedContentNodeIds.size(); j++) {

			if (j != ssspi) {
				indexComputationStart = System.nanoTime();
				double d = operateIndex(tokenId, orderedContentNodeIds.get(j),
						ssspOfNodeId.get(orderedContentNodeIds.get(j)));
				indexComputationDuration += ((System.nanoTime() - indexComputationStart) / 1e6);

				infosOfTokenId.get(tokenId).setDistance(j, d, clusterOfNodeId.get(orderedContentNodeIds.get(j)),
						orderedContentNodeIds.get(j));
			} else if (j == ssspi) {
				infosOfTokenId.get(tokenId).setDistance(j, costFromOriginId,
						clusterOfNodeId.get(orderedContentNodeIds.get(j)), orderedContentNodeIds.get(j));
			}

		}

		// c :=\Sigma dist[t][i];
		double c = infosOfTokenId.get(tokenId).getTotalDistOfTokenId();

		diviersificationStart = System.nanoTime();

		F_t = obj.singletonElemOfF(tokenId, infosOfTokenId, nodeIdsOfCluster, r);

		F_s = Math.max(F_s, F_t);

		qT = streamDivQ.checkDivAndAdd(tokenId, UB_F_s, F_s, F_t, infosOfTokenId, nodeIdsOfCluster, r);

		if (logAnyTime == true) {
			callNumber++;

			if (callNumber < 500) {
				bw.write(streamDivQ.getTheBestCurrentQTValue() + ",");
			}

		}

		diviersificationDuration += ((System.nanoTime() - diviersificationStart) / 1e6);

	}

	public boolean hasNextNode(HashMap<Integer, SSSPIterator> sssp) {
		for (Integer nodeId : sssp.keySet()) {
			if (sssp.get(nodeId).peekDist() <= r) {
				return true;
			}
		}
		return false;
	}

	// HashMap<Integer, HashSet<Integer>> nodeIdsOfToken
	// key: tokenId, value: HashSet of nodeIds
	public int operateIndex(int termId, int nodeId, SSSPIterator ssspIterator) {
		// pl.printDist(srcId, DesId);
		// printDist needs nodeId
		HashSet<Integer> nodeIds = nodeIdsOfToken.get(termId);
		double peekDist = ssspIterator.peekDist();
		// TODO: here we want a distance set or the shortest distance from this
		// term to the node
		int distance = Integer.MAX_VALUE;
		int consultantCnt = 0;
		for (int nodeIdOfTerm : nodeIds) {

			distanceQueryingStart = System.nanoTime();
			int temDistance = pl.queryDistance(nodeIdOfTerm, nodeId);
			distanceQueryingDuration += ((System.nanoTime() - distanceQueryingStart) / 1e6);

			numberOfDistaneQuerying++;
			distance = Math.min(distance, temDistance);
			if (distance <= peekDist) {
				return distance;
			}
			if (consultantCnt++ > DummyProperties.MAXIndexConsultant) {
				return distance;
			}
		}
		return distance;
	}

	// public static void main(String[] args) {
	// int termId = 547;
	// int nodeId = 5;
	// }

}
