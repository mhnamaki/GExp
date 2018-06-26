package queryExpansion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import aqpeq.utilities.StringPoolUtility;
import tryingToTranslate.PrunedLandmarkLabeling;

public class ObjectiveHandler {
	int k;
	double epsilon;
	double f_max = 0;
	double lambda;

	public ObjectiveHandler(int k, double epsilon, double lambda) {
		this.k = k;
		this.epsilon = epsilon;
		this.lambda = lambda;

	}

	public double marginalGain(HashSet<Integer> qT, int newTokenId, HashMap<Integer, TermInfo> infosOfTokenId,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfCluster, double distanceBound, double currentFValue)
			throws Exception {

		return computeF(qT, infosOfTokenId, nodeIdsOfCluster, distanceBound, newTokenId) - currentFValue;
	}

	public double marginalGainSieve(SieveInfo sieveInfo, int newTokenId, HashMap<Integer, TermInfo> infosOfTokenId,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfCluster, double distanceBound) throws Exception {

		return computeF(sieveInfo.QtOfV, infosOfTokenId, nodeIdsOfCluster, distanceBound, newTokenId)
				- sieveInfo.fValue;

	}

	public double singletonElemOfF(int newTokenId, HashMap<Integer, TermInfo> infosOfTokenId,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfCluster, double distanceBound) throws Exception {

		return rev(null, infosOfTokenId, newTokenId)
				+ lambda * div(null, infosOfTokenId, nodeIdsOfCluster, distanceBound, newTokenId);

	}

	public double computeF(HashSet<Integer> qT, HashMap<Integer, TermInfo> infosOfTokenId,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfCluster, double distanceBound, Integer newTokenId)
			throws Exception {

		return rev(qT, infosOfTokenId, newTokenId)
				+ lambda * div(qT, infosOfTokenId, nodeIdsOfCluster, distanceBound, newTokenId);

	}

	public double rev(HashSet<Integer> qT, HashMap<Integer, TermInfo> infosOfTokenId, Integer newTokenId) {
		double rev = 0d;

		if (qT != null) {
			for (int tokenId : qT) {
				double far = infosOfTokenId.get(tokenId).getTotalDistOfTokenId();
				double close = 1d / (1d + far);
				rev += close;
			}
		}

		if (newTokenId != null) {
			double far = infosOfTokenId.get(newTokenId).getTotalDistOfTokenId();
			double close = 1d / (1d + far);
			rev += close;
		}

		return rev;
	}

	public double div(HashSet<Integer> qT, HashMap<Integer, TermInfo> infosOfTokenId,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfCluster, double distanceBound, Integer newTokenId)
			throws Exception {
		double div = 0.0;
		// key: cluster id
		// value: HashMap -> key: node_Id, value: distance
		// public HashMap<Integer, HashMap<Integer, Double>>
		// distFromContentnodeMap
		for (int clusterId : nodeIdsOfCluster.keySet()) {
			double divOfOneTerm = 0;

			if (qT != null) {
				for (int termId : qT) {
					int numberOfCov = 0;
					HashMap<Integer, Double> distanceFromContentNode = infosOfTokenId.get(termId).distFromContentnodeMap
							.get(clusterId);
					for (double distance : distanceFromContentNode.values()) {
						if (distance <= distanceBound) {
							numberOfCov++;
						}
					}
					divOfOneTerm += (double) numberOfCov / (double) nodeIdsOfCluster.get(clusterId).size();
					// System.out.println("term = " +
					// StringPoolUtility.getStringOfId(termId) + ", numberOfCov
					// = "
					// + numberOfCov + ", div = " + divOfOneTerm);
				}
			}

			if (newTokenId != null) {
				int numberOfCov = 0;
				HashMap<Integer, Double> distanceFromContentNode = infosOfTokenId.get(newTokenId).distFromContentnodeMap
						.get(clusterId);

				if (distanceFromContentNode != null) {
					for (double distance : distanceFromContentNode.values()) {
						if (distance <= distanceBound) {
							numberOfCov++;
						}
					}

					divOfOneTerm += (double) numberOfCov / (double) nodeIdsOfCluster.get(clusterId).size();
				}

			}

			div += Math.sqrt(divOfOneTerm);

		}
		// 1/n * (sigma sqrt sigma)
		div = (1d / nodeIdsOfCluster.size()) * div;

		return div;
	}

	public double computeFUpperbound(double uB_rev_s, HashMap<Integer, Double> covUB,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfCluster, HashMap<Integer, TermInfo> infosOfTokenId) {

		double div = 0.0;
		for (int clusterId : nodeIdsOfCluster.keySet()) {
			double divOfOneTerm = 0;

			divOfOneTerm += (double) covUB.get(clusterId) / (double) nodeIdsOfCluster.get(clusterId).size();

			div += Math.sqrt(divOfOneTerm);

		}
		// 1/n * (sigma sqrt sigma)
		div = (1d / nodeIdsOfCluster.size()) * div;

		return (uB_rev_s + lambda * div);
	}

	public double computeFFromScratch(HashSet<Integer> qTOutsider, List<AnswerAsInput> list,
			PrunedLandmarkLabeling pl, HashMap<Integer, HashSet<Integer>> nodeIdsOfToken, int distanceBound) {

		double F = 0d;

		HashMap<Integer, HashSet<Integer>> nodeIdsOfCluster = new HashMap<Integer, HashSet<Integer>>();
		HashMap<Integer, Integer> clusterOfNodeId = new HashMap<Integer, Integer>();
		

		HashSet<Integer> contentNodes = new HashSet<Integer>();
		for (int i = 0; i < list.size(); i++) {
			contentNodes.addAll(list.get(i).getContentNodes());
			// nodeIdsOfCluster, key: cluster_id, value: HashSet<> ->
			// contentNode id
			for (int j = 0; j < list.get(i).getContentNodes().size(); j++) {
				nodeIdsOfCluster.putIfAbsent(j, new HashSet<Integer>());
				int contentNodeId = list.get(i).getContentNodes().get(j);
				nodeIdsOfCluster.get(j).add(contentNodeId);
				clusterOfNodeId.put(contentNodeId, j);
			}
		}

		double rev = 0d;

		if (qTOutsider != null) {
			for (int tokenId : qTOutsider) {
				double far = 0d;
				for (int c : contentNodes) {
					far += operateIndex(tokenId, c, pl, nodeIdsOfToken);
				}
				double close = 1d / (1d + far);
				rev += close;
			}
		}

		double div = 0.0;
		// key: cluster id
		// value: HashMap -> key: node_Id, value: distance
		// public HashMap<Integer, HashMap<Integer, Double>>
		// distFromContentnodeMap
		for (int clusterId : nodeIdsOfCluster.keySet()) {
			double divOfOneTerm = 0;

			if (qTOutsider != null) {
				for (int termId : qTOutsider) {
					int numberOfCov = 0;
					
					for (int nodeId : contentNodes) {
						if(clusterOfNodeId.get(nodeId)==clusterId){
							if(operateIndex(termId, nodeId, pl, nodeIdsOfToken) <= distanceBound){
								numberOfCov++;
							}
						}
					}

					divOfOneTerm += (double) numberOfCov / (double) nodeIdsOfCluster.get(clusterId).size();
				}
			}

			div += Math.sqrt(divOfOneTerm);

		}
		// 1/n * (sigma sqrt sigma)
		div = (1d / nodeIdsOfCluster.size()) * div;
		
		return  (rev + lambda * div);

	}

	public int operateIndex(int termId, int nodeId, PrunedLandmarkLabeling pl,
			HashMap<Integer, HashSet<Integer>> nodeIdsOfToken) {

		HashSet<Integer> nodeIds = nodeIdsOfToken.get(termId);
		// TODO: here we want a distance set or the shortest distance from this
		// term to the node
		int distance = Integer.MAX_VALUE;
		for (int nodeIdOfTerm : nodeIds) {
			int temDistance = pl.queryDistance(nodeIdOfTerm, nodeId);
			distance = Math.min(temDistance, distance);
		}
		return distance;
	}

}
