package incrementalEvaluation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import aqpeq.utilities.Dummy.DummyProperties;
import aqpeq.utilities.Dummy.DummyProperties.KWSSetting;
import dataset.BerkeleyDB.BerkleleyDB;
import graphInfra.GraphInfraReaderArray;
import queryExpansion.AnswerAsInput;

import queryExpansion.CostAndNodesOfAnswersPair;
import tryingToTranslate.PrunedLandmarkLabeling;

public class IncEval {
	// augmented data graph
	GraphInfraReaderArray graph;

	public int b;

	public int visitedNodes = 0;

	public double incEvalStartTime = 0d;
	public double incEvalDuration = 0d;

	public ArrayList<AnswerAsInput> topNAnswers;

	public KWSSetting kwsSetting;

	public CostAndNodesOfAnswersPair bestKeywordInfo;

	public IncBFSTriple[] lastTripleOfKeywordMatchToTarget;

	public IncEval(GraphInfraReaderArray graph, CostAndNodesOfAnswersPair bestKeywordInfo,
			ArrayList<AnswerAsInput> topNAnswers, int b, KWSSetting kwsSetting) {

		this.topNAnswers = topNAnswers;
		this.bestKeywordInfo = bestKeywordInfo;
		this.kwsSetting = kwsSetting;
		this.graph = graph;

		this.b = b;
	}

	public void incEval(PrunedLandmarkLabeling prunedLandmarkLabeling) throws Exception {

		int n = topNAnswers.size();

		incEvalStartTime = System.nanoTime();

		lastTripleOfKeywordMatchToTarget = new IncBFSTriple[n];

		if (kwsSetting == KWSSetting.SUBGRAPH) {
			for (int i = 0; i < n; i++) {

				lastTripleOfKeywordMatchToTarget[i] = new IncBFSTriple(-1, null, 0, 0d);
				for (int contentNodeId : topNAnswers.get(i).getContentNodes()) {
					lastTripleOfKeywordMatchToTarget[i].setCost(lastTripleOfKeywordMatchToTarget[i].getCost()
							+ prunedLandmarkLabeling.queryDistance(bestKeywordInfo.nodeId[i], contentNodeId));
				}
			}
		}
		incEvalDuration = ((System.nanoTime() - incEvalStartTime) / 1e6);
	}

	public void incEval() throws Exception {

		int n = topNAnswers.size();

		incEvalStartTime = System.nanoTime();

		// if (kwsSetting == KWSSetting.DISTINCTROOT || kwsSetting ==
		// KWSSetting.STEINER)
		lastTripleOfKeywordMatchToTarget = new IncBFSTriple[n];

		// for i := 1 to n do
		for (int i = 0; i < n; i++) {

			HashSet<Integer> targetSet = new HashSet<Integer>();

			switch (kwsSetting) {
			case DISTINCTROOT:
				targetSet.add(topNAnswers.get(i).getRootNodeId());
				break;
			case SUBGRAPH:
				// visit all
				targetSet.addAll(topNAnswers.get(i).getContentNodes());
				break;
			case STEINER:
				// visit only the closest one
				targetSet.addAll(topNAnswers.get(i).getAllNodes());
				break;
			default:
				throw new Exception("unknown setting");
			}

			// queue L := ∅, set visited nodes S := ∅;
			PriorityQueue<IncBFSTriple> frontier = new PriorityQueue<IncBFSTriple>(new Comparator<IncBFSTriple>() {
				@Override
				public int compare(IncBFSTriple o1, IncBFSTriple o2) {
					return Double.compare(o1.getCost(), o2.getCost());
				}
			});

			HashSet<Integer> visitedNodesSet = new HashSet<Integer>();

			// L := L ∪ ⟨ri , 0, 0⟩; /* information node ri in answer ai */
			frontier.add(new IncBFSTriple(bestKeywordInfo.nodeId[i], null, 0, 0d));

			// while (L , ∅) do
			while (!frontier.isEmpty()) {
				/* Picking the next node in a FIFO fashion */
				// ⟨v, d, c⟩ := L.poll ();
				IncBFSTriple currentIncBFSTriple = frontier.poll();
				int v = currentIncBFSTriple.getNodeId();
				int d = currentIncBFSTriple.getDistance();
				double c = currentIncBFSTriple.getCost();

				// S = S ∪ {v };
				visitedNodesSet.add(v);

				if (kwsSetting == DummyProperties.KWSSetting.DISTINCTROOT
						|| kwsSetting == DummyProperties.KWSSetting.STEINER) {

					if (targetSet.contains(v)) {
						lastTripleOfKeywordMatchToTarget[i] = currentIncBFSTriple;
						break;
					}
				} else if (kwsSetting == DummyProperties.KWSSetting.SUBGRAPH) {
					// we should meet all the content nodes before terminating
					if (targetSet.contains(v)) {
						lastTripleOfKeywordMatchToTarget[i] = currentIncBFSTriple;
						if (targetSet.size() == 1) {
							break;
						} else {
							targetSet.remove(v);
						}
					}
				}

				// if d ≥ b then continue
				if (d >= b)
					continue;

				// for each each edge e = (u, v) in G′ do
				for (int nextNodeId : graph.nodeOfNodeId.get(v).getOutgoingRelIdOfSourceNodeId().keySet()) {

					// if u ∈ S then continue
					if (visitedNodesSet.contains(nextNodeId))
						continue;

					// c′ := c + w_e
					double c_ = c + graph.relationOfRelId
							.get(graph.nodeOfNodeId.get(v).getOutgoingRelIdOfSourceNodeId().get(nextNodeId)).weight;

					// L := L ∪ ⟨u, d + 1, c′⟩
					frontier.add(new IncBFSTriple(nextNodeId, currentIncBFSTriple, d + 1, c_));
				}

			}

			visitedNodes += visitedNodesSet.size();

		}

		incEvalDuration = ((System.nanoTime() - incEvalStartTime) / 1e6);

	}

}
