package queryExpansion;

import java.util.HashMap;
import java.util.HashSet;

public class TermInfo {
	private int tokenId;
	private double[] distFromCotentNode;
	//key: cluster id
	//value: HashMap -> key: node_Id, value: distance
	public HashMap<Integer, HashMap<Integer, Double>> distFromContentnodeMap = new HashMap<Integer, HashMap<Integer, Double>>();
	private int cnt = 0;
	private double totalDistanceToVC = Double.MAX_VALUE;
	public double tempMGValue=0; // don't trust this, it's created for divQ

	public TermInfo(int tokenId, int numberOfContentNodes) {
		this.tokenId = tokenId;
		distFromCotentNode = new double[numberOfContentNodes];

		for (int i = 0; i < numberOfContentNodes; i++) {
			distFromCotentNode[i] = Double.MAX_VALUE;
		}
	}

	public void setDistance(int indexOfContentNode, double w, int clusterId, int nodeId) {
		// if (distFromCotentNode[indexOfContentNode] == Double.MAX_VALUE) {
		// cnt++;
		// }
		distFromCotentNode[indexOfContentNode] = w;
		
		if(!distFromContentnodeMap.containsKey(clusterId)){
			HashMap<Integer, Double> distanceFromContentNode = new HashMap<Integer, Double>();
			distanceFromContentNode.put(nodeId, w);
			distFromContentnodeMap.put(clusterId, distanceFromContentNode);
		} else {
			if(distFromContentnodeMap.get(clusterId).get(nodeId) == null ||distFromContentnodeMap.get(clusterId).get(nodeId) > w){
				distFromContentnodeMap.get(clusterId).put(nodeId, w);
			}
		}
	}

	public void incrementCnt() {
		cnt++;
	}

//	public double getDistance(int indexOfContentNode) {
//		return distFromCotentNode[indexOfContentNode];
//	}

	public int getCnt() {
		return cnt;
	}

	public int getTokenId() {
		return tokenId;
	}

	public double getTotalDistOfTokenId() {

		if (totalDistanceToVC == Double.MAX_VALUE) {
			totalDistanceToVC = 0;
			for (int i = 0; i < distFromCotentNode.length; i++) {
				if (distFromCotentNode[i] == Double.MAX_VALUE) {
					totalDistanceToVC = Double.MAX_VALUE;
					return Double.MAX_VALUE;
				}
				totalDistanceToVC += distFromCotentNode[i];
			}

			return totalDistanceToVC;

		} else {

			return totalDistanceToVC;

		}
	}

}
