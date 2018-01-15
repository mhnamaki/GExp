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
import dataset.BerkeleyDB.BerkleleyDB;
import graphInfra.GraphInfraReaderArray;

public class GraphKWSExpand {

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

	public double w;
	private int b;
	private BerkleleyDB berkeleyDB;

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

	// public int gundamId = -1;

	public GraphKWSExpand(GraphInfraReaderArray graph, ArrayList<AnswerAsInput> topNAnswers, double delta, int b,
			BerkleleyDB berkeleyDB, HashSet<Integer> initialKeywords) throws Exception {
		this.topNAnswers = topNAnswers;
		this.delta = delta;
		this.graph = graph;
		this.initialKeywords = initialKeywords;
		this.berkeleyDB = berkeleyDB;

		for (int m = 0; m < topNAnswers.size(); m++) {
			this.w += topNAnswers.get(m).getCost();
		}

		initialOveralWeight = this.w;

		this.b = b;

		// gundamId = StringPoolUtility.getIdOfStringFromPool("gundam");
	}

	public HashMap<Integer, CostAndNodesOfAnswersPair> expand() throws Exception {

		estimatedWeightOfSuggestedKeywordMap = new HashMap<Integer, CostAndNodesOfAnswersPair>();
		costOfAKeywordToAnAnswer = new HashMap<Integer, CostNodePair[]>();

		int n = topNAnswers.size();
		int l = topNAnswers.get(0).getContentNodes().size();

		querySuggestionKWSStartTime = System.nanoTime();

		// for i := 1 to n do
		for (int i = 0; i < n; i++) {

			// vertex-distance map vDist := ∅;
			HashMap<Integer, CostNodePair[]> costOfAContentNodeToANode = new HashMap<Integer, CostNodePair[]>();

			// for j := 1 to l do
			for (int j = 0; j < l; j++) {

				// queue L := ∅, set visited nodes S := ∅;
				LinkedList<BFSTriple> queue = new LinkedList<BFSTriple>();
				HashSet<Integer> visitedNodesSet = new HashSet<Integer>();

				// L := L ∪ ⟨cij , 0, 0⟩; /* content node ci j relevant to
				// keyword kj in answer ai */
				queue.add(new BFSTriple(topNAnswers.get(i).getContentNodes().get(j), 0, 0d));

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

					// TEST:
					// if (graph.nodeOfNodeId.get(v).tokens.contains(gundamId))
					// {
					// System.out.println();
					// }

					// if j = 1 or v ∈ vDist then
					if (j == 0 || costOfAContentNodeToANode.containsKey(v)) {

						// vDist [v][kj] = min(vDist [v][kj ], d);

						if (!costOfAContentNodeToANode.containsKey(v)) {
							costOfAContentNodeToANode.put(v, new CostNodePair[l]);
							CostNodePair[] arr = costOfAContentNodeToANode.get(v);
							for (int o = 0; o < arr.length; o++) {
								arr[o] = new CostNodePair(-1, new InfiniteDouble());
							}
						}

						if (costOfAContentNodeToANode.get(v)[j].cost.getValue() > c) {
							costOfAContentNodeToANode.get(v)[j].cost.setValue(c);
							costOfAContentNodeToANode.get(v)[j].nodeId = v;
						}

						/* Last BFS for an answer */
						// if j = l then
						if (j == l - 1) {

							// σ := Σlm=1vDist [v][m];
							double sigma = QueryExpandUtility.getSumOfCosts(costOfAContentNodeToANode.get(v));

							// if(sigma < Double.MAX_VALUE){
							// System.out.println();
							// }

							// if σ ≤ δ ∗ w then
							if (sigma <= delta * w) {

								// for each keyword k′ in v.getKeywords () do

								double startTime = System.nanoTime();
								Collection<Integer> keywordsOfV = DummyFunctions.getKeywords(graph, v);
								getKeywordsDuration += (System.nanoTime() - startTime) / 1e6;

								if (keywordsOfV == null) {
									keywordsOfV = new HashSet<Integer>();
								}

								for (int k_ : keywordsOfV) {

									// if i > 1 and k′ < kCost then continue
									if (i > 0 && !costOfAKeywordToAnAnswer.containsKey(k_))
										continue;

									if (initialKeywords.contains(k_))
										continue;

									// kCost [k′][ai ] := min(kCost [k′][ai ], c
									// );

									if (!costOfAKeywordToAnAnswer.containsKey(k_)) {
										costOfAKeywordToAnAnswer.put(k_, new CostNodePair[n]);
										CostNodePair[] arr = costOfAKeywordToAnAnswer.get(k_);
										for (int o = 0; o < arr.length; o++) {
											arr[o] = new CostNodePair(-1, new InfiniteDouble());
										}
									}

									if (costOfAKeywordToAnAnswer.get(k_)[i].cost.getValue() > sigma) {
										costOfAKeywordToAnAnswer.get(k_)[i].cost.setValue(sigma);
										costOfAKeywordToAnAnswer.get(k_)[i].nodeId = v;
									}

									/* Last BFS */
									// if i = n then
									if (i == n - 1 && j == l - 1) {

										// w′ :=\sum i=1 to n kCost [k′][ai];
										double w_ = QueryExpandUtility.getSumOfCosts(costOfAKeywordToAnAnswer.get(k_));

										// if w′ ≤ δ ∗ w then
										if (w_ <= delta * w) {

											// K := K ∪ (k′, w + w′);
											int[] nodeIds = QueryExpandUtility
													.getNodeIdArr(costOfAKeywordToAnAnswer.get(k_));

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
							}
						}
					}

					// if d ≥ b then continue
					if (d >= b)
						continue;

					// to take care of high-degree
					if (queue.size() > DummyProperties.MaxNumberOfVisitedNodes)
						continue;

					// for each each edge e = (u, v) in G′ do
					for (int targetNodeId : graph.nodeOfNodeId.get(v).getOutgoingRelIdOfSourceNodeId().keySet()) {

						// if u ∈ S then continue
						if (visitedNodesSet.contains(targetNodeId))
							continue;

						// c′ := c + w_e
						double c_ = c + 1;

						// if w′ > δ ∗ w then continue
						if (c_ > delta * w)
							continue;

						// L := L ∪ ⟨u, d + 1, c′⟩
						queue.add(new BFSTriple(targetNodeId, d + 1, c_));
					}

				}
				visitedNodes += visitedNodesSet.size();

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

}
