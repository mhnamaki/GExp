////TODO: add current keywords to make sure we do not get them again!
//
//package queryExpansion;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//
//import aqpeq.utilities.KWSUtilities;
//import aqpeq.utilities.StringPoolUtility;
//import aqpeq.utilities.Dummy.DummyFunctions;
//import aqpeq.utilities.Dummy.DummyProperties;
//import dataset.BerkeleyDB.BerkleleyDB;
//import graphInfra.GraphInfraReaderArray;
//import graphInfra.RelationshipInfra;
//
//public class GenTemRootKWSExpand {
//
//	// top-n answer trees A discovered by KWS algorithm A,
//	ArrayList<AnswerAsInput> topNAnswers;
//
//	// augmented data graph
//	GraphInfraReaderArray graph;
//
//	private int r;
//	private int k;
//
//	public int visitedNodes = 0;
//	public int visitedKeywords = 0;
//	public double avgQualityOfSuggestedKeyword = 0d;
//	public double initialOveralWeight = 0d;
//	public double querySuggestionKWSStartTime = 0d;
//	public double querySuggestionKWSDuration = 0d;
//
//	// after pruning high-frequent keywordsF
//	public double totalWeightOfSuggestedKeywords = 0d;
//	public double lowestWeightOfSuggestedKeyword;
//	public Integer lowestWeightSuggestedKeywordId;
//
//	public int highFrequentKeywordsRemovedNum = 0;
//
//	public CostAndNodesOfAnswersPair bestKeywordInfo;
//
//	private HashSet<Integer> keywordsSet;
//
//	public double getKeywordsDuration = 0d;
//
//	public HashMap<Integer, TermInfo> infosOfTokenId = new HashMap<Integer, TermInfo>();
//	// HashMap, key: tokenId, value: distance
//	public HashMap<Integer, Double> termDistance = new HashMap<Integer, Double>();
//	ArrayList<HashSet<Integer>> visitedTermBySSSPi = new ArrayList<>();
//	DivTerm divTerm;
//	HashSet<Integer> qT = new HashSet<Integer>();
//	HashMap<Integer, SSSPIterator> ssspOfNodeId;
//	HashMap<Integer, HashSet<Integer>> nodeIdsOfCluster = new HashMap<Integer, HashSet<Integer>>();
//	HashMap<Integer, Integer> clusterOfNodeId = new HashMap<Integer, Integer>();
//
//	public GenTemRootKWSExpand(GraphInfraReaderArray graph, HashSet<Integer> keywordsSet,
//			ArrayList<AnswerAsInput> topNAnswers, int k, int r, double epsilon, double lambda) throws Exception {
//		this.topNAnswers = topNAnswers;
//		this.graph = graph;
//
//		this.keywordsSet = keywordsSet;
//		// for (String keyword : keywordList){
//		// this.keywords.add(StringPoolUtility.getIdOfStringFromPool(keyword));
//		// }
//
//		this.k = k;
//		this.r = r;
//
//		divTerm = new DivTerm(k, epsilon, lambda);
//
//		for (RelationshipInfra rel : graph.relationOfRelId) {
//			rel.weight = 1.0f;
//		}
//
//	}
//
//	public HashSet<Integer> expand() throws Exception {
//
//		// LB := 0;
//		double LB = Double.MAX_VALUE;
//
//		HashSet<Integer> contentNodes = new HashSet<Integer>();
//		for (int i = 0; i < topNAnswers.size(); i++) {
//			contentNodes.addAll(topNAnswers.get(i).getContentNodes());
//
//			// nodeIdsOfCluster, key: cluster_id, value: HashSet<> ->
//			// contentNode id
//			for (int j = 0; j < topNAnswers.get(i).getContentNodes().size(); j++) {
//				nodeIdsOfCluster.putIfAbsent(j, new HashSet<Integer>());
//				int contentNodeId = topNAnswers.get(i).getContentNodes().get(j);
//				nodeIdsOfCluster.get(j).add(contentNodeId);
//				clusterOfNodeId.put(contentNodeId, j);
//			}
//
//		}
//
//		// int[] V_C = new int[topNAnswers.size() * keywordsSet.size()];
//		// for (int i = 0; i < topNAnswers.size(); i++) {
//		// for (int j = 0; j < keywordsSet.size(); j++) {
//		// V_C[keywordsSet.size() * i + j] =
//		// topNAnswers.get(i).getContentNodes().get(j);
//		// }
//		// }
//		// int[] V_C = new int[contentNodes.size()];
//		// int j=0;
//		// for (int contentNodeId : contentNodes){
//		// V_C[j] = contentNodeId;
//		// j++;
//		// }
//
//		/* initializing |V_C| number of SSSP's */
//
//		ssspOfNodeId = new HashMap<Integer, SSSPIterator>();
//
//		ArrayList<Integer> orderedContentNodeIds = new ArrayList<Integer>();
//
//		// for each v_i \in V_C do
//		for (int nodeId : contentNodes) {
//			// create iterator SSSPi originated from vi bounded by r;
//			ssspOfNodeId.put(nodeId, new SSSPIterator(graph, nodeId, r));
//			orderedContentNodeIds.add(nodeId);
//			visitedTermBySSSPi.add(new HashSet<>());
//		}
//
//		System.out.println("contentNodes length = " + contentNodes.size());
//		System.out.println("top-n answers = " + topNAnswers.size());
//		System.out.println();
//
//		/* term generation using a TA-style algorithm */
//		// while exits v_i \in V_C : SSSPi:peekDist() <= r do
//		int i = -1;
//		while (hasNextNode(ssspOfNodeId)) {
//			// i := pick from [1; jVCj] in a round-robin way;
//			i++;
//			i = i % ssspOfNodeId.size();
//
//			int currentContentNodeId = orderedContentNodeIds.get(i);
//
//			// <u; d> := SSSPi:next();
//			SSSPNode currentNode = ssspOfNodeId.get(currentContentNodeId).getNextSSSPNode();
//
//			if (currentNode == null) {
//				continue;
//			}
//
//			// for each term t \in L(u) do
//			for (Integer tokenId : currentNode.node.tokens) {
//				// if t \in dist then continue ; /*better d already captured*/
//				if (visitedTermBySSSPi.get(i).contains(tokenId) || keywordsSet.contains(tokenId))
//					continue;
//
//				// dist[t][i] := d;
//				if (!infosOfTokenId.containsKey(tokenId)) {
//					TermInfo termInfo = new TermInfo(tokenId, ssspOfNodeId.size());
//					infosOfTokenId.put(tokenId, termInfo);
//				}
//				// setDistance(int indexOfContentNode, double w, int clusterId,
//				// int nodeId)
//				infosOfTokenId.get(tokenId).setDistance(i, currentNode.costFromOriginId,
//						clusterOfNodeId.get(currentContentNodeId), currentContentNodeId);
//
//				// cnt[t] := cnt[t] + 1;
//				infosOfTokenId.get(tokenId).incrementCnt();
//
//				// if cnt[t] == jVCj then /*all SSSP's visited the term t*/
//				if (infosOfTokenId.get(tokenId).getCnt() == ssspOfNodeId.size()) {
//					// c :=\Sigma dist[t][i];
//					double c = infosOfTokenId.get(tokenId).getTotalDistOfTokenId();
//					// HashMap, key: tokenId, value: distance
//					termDistance.put(tokenId, c);
//
//					qT.add(tokenId);
//
//					// // for each term t in dist then
//					// for (int tId : infosOfTokenId.keySet()) {
//					// // LBt := compute the potential minimum distance of t
//					// // using SSSP's and dist[t];
//					// if (infosOfTokenId.get(tId).getCnt() != V_C.length) {
//					// double lb_t = computeLowerboundOfTokenId(sssp, tId);
//					//
//					// // LB := min(LBt;LB);
//					// LB = Math.min(lb_t, LB);
//					// }
//					//
//					// }
//					//
//					// // :=PjVCjj=1 SSSPi:peekDist();
//					// double LB_unseen = computeUnseenLowerbound(sssp);
//					//
//					// // LB := min(LBunseen;LB);
//					// LB = Math.min(LB_unseen, LB);
//					//
//					// // QT := DivTerm(t;QT ; c; LB);
//					// qT = divTerm.checkDivAndAdd(tokenId, c, LB,
//					// infosOfTokenId, V_C);
//					// if (qT != null) {
//					// return qT;
//					// }
//				}
//			}
//		}
//
//		// debug
//		System.out.println("nodeIdsOfCluster");
//		for (int clusterId : nodeIdsOfCluster.keySet()) {
//			System.out.println("cluster id = " + clusterId + ", node id = " + nodeIdsOfCluster.get(clusterId));
//		}
//		System.out.println("infosOfTokenId");
//		for (int termId : infosOfTokenId.keySet()){
//			System.out.println("term id = " + termId + ", term is " + StringPoolUtility.getStringOfId(termId));
//			System.out.println("distance map");
//			TermInfo termInfo = infosOfTokenId.get(termId);
//			for (int clusterId : termInfo.distFromContentnodeMap.keySet()){
//				System.out.println("cluster " + clusterId +" nodeId to distance Map = " + termInfo.distFromContentnodeMap.get(clusterId));
//			}
//		}
//		System.out.println("qT");
//		System.out.println(qT);
//
//		// TODO: assign distanceBound for div
//		double testDiv = divTerm.div(qT, infosOfTokenId, nodeIdsOfCluster, r);
//		System.out.println("div = " + testDiv);
//		// TODO: when !hasNextNode(sssp)
//		return qT;
//	}
//
//	private double computeUnseenLowerbound(SSSPIterator[] sssp) {
//		double lb_unseen = 0;
//		for (int i = 0; i < sssp.length; i++) {
//			lb_unseen += sssp[i].peekDist();
//		}
//		return lb_unseen;
//	}
//
//	private double computeLowerboundOfTokenId(SSSPIterator[] sssp, int tokenId) {
//		double lb_t = 0;
//		for (int i = 0; i < sssp.length; i++) {
//			if (infosOfTokenId.get(tokenId).getDistance(i) == Double.MAX_VALUE) {
//				lb_t += sssp[i].peekDist();
//			} else {
//				lb_t += infosOfTokenId.get(tokenId).getDistance(i);
//			}
//		}
//		return lb_t;
//	}
//
//	private boolean hasNextNode(HashMap<Integer, SSSPIterator> sssp) {
//		for (Integer nodeId : sssp.keySet()) {
//			if (sssp.get(nodeId).peekDist() < Double.MAX_VALUE) {
//				return true;
//			}
//		}
//		return false;
//	}
//}
