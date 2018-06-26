//TODO: add current keywords to make sure we do not get them again!

package queryExpansion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import aqpeq.utilities.KWSUtilities;
import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import graphInfra.GraphInfraReaderArray;

public class SteinerKWSExpansion {

	// top-n answer trees A discovered by KWS algorithm A,
	ArrayList<AnswerAsInput> topNAnswers;

	// quality bound
	double delta;

	// quality preservable keywords K ={(k′, w′), . . . }.
	public HashMap<Integer, CostAndNodesOfAnswersPair> estimatedWeightOfSuggestedKeywordMap;

	// augmented data graph
	GraphInfraReaderArray graph;

	// kCost
	HashMap<Integer, CostNodePair[]> costOfAKeywordToAnAnswer;

	private double w;
	private int b;

	public int visitedNodes = 0;
	public int visitedKeywords = 0;
	public double avgQualityOfSuggestedKeyword = 0d;
	public double initialOveralWeight = 0d;
	public double querySuggestionKWSStartTime = 0d;
	public double querySuggestionKWSDuration = 0d;

	public double totalWeightOfSuggestedKeywords = 0d;
	public double lowestWeightOfSuggestedKeyword;
	public int lowestWeightSuggestedKeywordId;
	public CostAndNodesOfAnswersPair bestKeywordInfo;

	private HashSet<Integer> initialKeywords;


	public int highFrequentKeywordsRemovedNum = 0;
	public double getKeywordsDuration = 0d;

	// FOR TEST
	// public boolean testSizeOfGrVsExploredArea = true;
	// public int totalNumberOfNodesVisitedInGr = 0;
	// public int totalNumberOfEdgesVisitedInGr = 0;
	// public int totalNumberOfEdgesVisitedInExplore = 0;
	// public int totalNumberOfNodesVisitedInExplore = 0;
	// public HashSet<Integer> Q_G_DELTA_Q_G_Nodes = new HashSet<Integer>();
	// public HashSet<Integer> Q_G_DELTA_Q_G_Edges = new HashSet<Integer>();
	// public HashSet<Integer> G_r_Nodes = new HashSet<Integer>();
	// public HashSet<Integer> G_r_Edges = new HashSet<Integer>();

	public SteinerKWSExpansion(GraphInfraReaderArray graph, ArrayList<AnswerAsInput> topNAnswers, double delta, int b, HashSet<Integer> initialKeywords) {
		this.topNAnswers = topNAnswers;
		this.delta = delta;
		this.graph = graph;
		this.initialKeywords = initialKeywords;


		for (int m = 0; m < topNAnswers.size(); m++) {
			this.w += topNAnswers.get(m).getCost();
		}

		if (this.w == 0) {
			this.w = 1;
			topNAnswers.get(0).setCost(1);
		}

		initialOveralWeight = this.w;

		this.b = b;
	}

	public HashMap<Integer, CostAndNodesOfAnswersPair> expand() throws Exception {

		estimatedWeightOfSuggestedKeywordMap = new HashMap<Integer, CostAndNodesOfAnswersPair>();
		costOfAKeywordToAnAnswer = new HashMap<Integer, CostNodePair[]>();

		int n = topNAnswers.size();

		querySuggestionKWSStartTime = System.nanoTime();

		// for i := 1 to n do
		for (int i = 0; i < n; i++) {

			int vT = topNAnswers.get(i).getAllNodes().size();

			// for j := 1 to l do
			for (int j = 0; j < vT; j++) {

				int tempMaxVisit = DummyProperties.MaxNumberOfVisitedNodes;

				// queue L := ∅, set visited nodes S := ∅;
				LinkedList<BFSTriple> queue = new LinkedList<BFSTriple>();
				HashSet<Integer> visitedNodesSet = new HashSet<Integer>();
				// HashSet<Integer> visitedEdgesSet = new HashSet<Integer>();

				// L := L ∪ ⟨vi , 0, 0⟩; /* a node v_ij in tree a_i */
				queue.add(new BFSTriple(topNAnswers.get(i).getAllNodes().get(j), 0, 0d));

				// while (L , ∅) do
				while (!queue.isEmpty()) {

					/* Picking the next node in a FIFO fashion */
					// ⟨v, d, c⟩ := L.poll ();
					BFSTriple currentBFSTriple = queue.poll();

					int v = currentBFSTriple.getNodeId();
					int d = currentBFSTriple.getDistance();
					double c = currentBFSTriple.getCost();

					// S = S ∪ {v };
					visitedNodesSet.add(v);

					double startTime = System.nanoTime();
					Collection<Integer> keywordsOfV = DummyFunctions.getKeywords(graph, v);
					getKeywordsDuration += (System.nanoTime() - startTime) / 1e6;

					// for each keyword k′ in v.getKeywords () do
					if (keywordsOfV == null) {
						keywordsOfV = new HashSet<Integer>();
					}

					for (Integer k_ : keywordsOfV) {

						// if i > 1 and k′ < kCost then continue
						if (i > 0 && !costOfAKeywordToAnAnswer.containsKey(k_))
							continue;

						if (initialKeywords.contains(k_))
							continue;

						// kCost [k′][ai ] := min(kCost [k′][ai ], c );
						if (!costOfAKeywordToAnAnswer.containsKey(k_)) {
							costOfAKeywordToAnAnswer.put(k_, new CostNodePair[n]);
							CostNodePair[] arr = costOfAKeywordToAnAnswer.get(k_);
							for (int o = 0; o < arr.length; o++) {
								arr[o] = new CostNodePair(-1, new InfiniteDouble());
							}
						}

						if (costOfAKeywordToAnAnswer.get(k_)[i].cost.getValue() > c) {
							costOfAKeywordToAnAnswer.get(k_)[i].cost.setValue(c);
							costOfAKeywordToAnAnswer.get(k_)[i].nodeId = v;
						}

						/* Last BFS */
						// if i = n then
						if (i == n - 1) {
							// w′ :=\sum i=1 to n kCost [k′][ai];
							double w_ = QueryExpandUtility.getSumOfCosts(costOfAKeywordToAnAnswer.get(k_));

							// if w′ ≤ δ ∗ w then
							if (w_ <= delta * w) {

								int[] nodeIds = QueryExpandUtility.getNodeIdArr(costOfAKeywordToAnAnswer.get(k_));

								// K := K ∪ (k′, w + w′);
								if (estimatedWeightOfSuggestedKeywordMap.containsKey(k_)) {
									if (estimatedWeightOfSuggestedKeywordMap.get(k_).cost > w_ + w) {

										estimatedWeightOfSuggestedKeywordMap.put(k_,
												new CostAndNodesOfAnswersPair(nodeIds, w_ + w));
									}
								} else {
									estimatedWeightOfSuggestedKeywordMap.put(k_,
											new CostAndNodesOfAnswersPair(nodeIds, w_ + w));
								}
							}

						}

					}

					// if d ≥ b then continue
					if (d >= b)
						continue;

					// to take care of high-degree
					if (queue.size() > tempMaxVisit) {
						tempMaxVisit--;
						continue;
					}

					// for each each edge e = (u, v) in G′ do
					for (int targetNodeId : graph.nodeOfNodeId.get(v).getOutgoingRelIdOfSourceNodeId().keySet()) {

						// if (testSizeOfGrVsExploredArea)
						// visitedEdgesSet
						// .add(graph.nodeOfNodeId.get(v).getOutgoingRelIdOfSourceNodeId().get(targetNodeId));

						// if u ∈ S then continue
						if (visitedNodesSet.contains(targetNodeId))
							continue;

						// c′ := c + w_e
						double c_ = c + graph.relationOfRelId.get(
								graph.nodeOfNodeId.get(targetNodeId).getOutgoingRelIdOfSourceNodeId().get(v)).weight;

						// if w′ > δ ∗ w then continue
						if (c_ > delta * w)
							continue;

						// L := L ∪ ⟨u, d + 1, c′⟩
						queue.add(new BFSTriple(targetNodeId, d + 1, c_));
					}
				}

				visitedNodes += visitedNodesSet.size();

				// if (testSizeOfGrVsExploredArea) {
				// totalNumberOfEdgesVisitedInExplore += visitedEdgesSet.size();
				// totalNumberOfEdgesVisitedInExplore += visitedNodesSet.size();
				//
				// Q_G_DELTA_Q_G_Nodes.addAll(visitedNodesSet);
				// Q_G_DELTA_Q_G_Edges.addAll(visitedEdgesSet);
				// }
			}
		}

		querySuggestionKWSDuration = ((System.nanoTime() - querySuggestionKWSStartTime) / 1e6);

		visitedKeywords = costOfAKeywordToAnAnswer.size();

		int tempNum = estimatedWeightOfSuggestedKeywordMap.size();

		KWSUtilities.removeHighFrequentKeywordsFromMap(graph, estimatedWeightOfSuggestedKeywordMap);

		highFrequentKeywordsRemovedNum = tempNum - estimatedWeightOfSuggestedKeywordMap.size();

		totalWeightOfSuggestedKeywords = 0d;
		lowestWeightOfSuggestedKeyword = Double.MAX_VALUE;
		lowestWeightSuggestedKeywordId = 0;
		if (estimatedWeightOfSuggestedKeywordMap.size() > 0) {

			for (int k : estimatedWeightOfSuggestedKeywordMap.keySet()) {
				totalWeightOfSuggestedKeywords += estimatedWeightOfSuggestedKeywordMap.get(k).cost;

				if (lowestWeightOfSuggestedKeyword > estimatedWeightOfSuggestedKeywordMap.get(k).cost) {
					lowestWeightOfSuggestedKeyword = estimatedWeightOfSuggestedKeywordMap.get(k).cost;
					lowestWeightSuggestedKeywordId = k;
				}

			}

			avgQualityOfSuggestedKeyword = totalWeightOfSuggestedKeywords
					/ (double) estimatedWeightOfSuggestedKeywordMap.size();

		}

		bestKeywordInfo = estimatedWeightOfSuggestedKeywordMap.get(lowestWeightSuggestedKeywordId);

		return estimatedWeightOfSuggestedKeywordMap;
	}

	// public void updateNumberOfNodesAndEdgesInGr() throws Exception {
	//
	// if (!testSizeOfGrVsExploredArea)
	// return;
	//
	// int n = topNAnswers.size();
	//
	// // for i := 1 to n do
	// for (int i = 0; i < n; i++) {
	//
	// int vT = topNAnswers.get(i).getAllNodes().size();
	//
	// // for j := 1 to l do
	// for (int j = 0; j < vT; j++) {
	// // queue L := ∅, set visited nodes S := ∅;
	// LinkedList<BFSTriple> queue = new LinkedList<BFSTriple>();
	// HashSet<Integer> visitedNodesSet = new HashSet<Integer>();
	// HashSet<Integer> visitedEdgesSet = new HashSet<Integer>();
	//
	// // L := L ∪ ⟨ri , 0, 0⟩; /* information node ri in answer ai */
	// queue.add(new BFSTriple(topNAnswers.get(i).getRootNodeId(), 0, 0d));
	//
	// // while (L , ∅) do
	// while (!queue.isEmpty()) {
	// /* Picking the next node in a FIFO fashion */
	// // ⟨v, d, c⟩ := L.poll ();
	// BFSTriple currentBFSTriple = queue.poll();
	//
	// int v = currentBFSTriple.getNodeId();
	// int d = currentBFSTriple.getDistance();
	// double c = currentBFSTriple.getCost();
	//
	// // S = S ∪ {v };
	// visitedNodesSet.add(v);
	//
	// // if d ≥ b then continue
	// if (d >= b)
	// continue;
	//
	// // to take care of high-degree
	// // if (queue.size() >
	// // DummyProperties.MaxNumberOfVisitedNodes)
	// // continue;
	//
	// // for each each edge e = (u, v) in G′ do
	// for (int targetNodeId :
	// graph.nodeOfNodeId.get(v).getOutgoingRelIdOfSourceNodeId().keySet()) {
	//
	// int relID =
	// graph.nodeOfNodeId.get(v).getOutgoingRelIdOfSourceNodeId().get(targetNodeId);
	//
	// visitedEdgesSet.add(relID);
	//
	// // if u ∈ S then continue
	// if (visitedNodesSet.contains(targetNodeId))
	// continue;
	//
	// // L := L ∪ ⟨u, d + 1, c′⟩
	// queue.add(new BFSTriple(targetNodeId, d + 1, 0));
	// }
	//
	// }
	//
	// totalNumberOfNodesVisitedInGr += visitedNodesSet.size();
	// totalNumberOfEdgesVisitedInGr += visitedEdgesSet.size();
	// G_r_Nodes.addAll(visitedNodesSet);
	// G_r_Edges.addAll(visitedEdgesSet);
	//
	// }
	// }
	//
	// }
}
