package tryingToTranslateXin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class BitParallelBFS {

	static public ArrayList<BitNode> nodeList = new ArrayList<BitNode>();

	public BitParallelBFS() {

	}

	public ArrayList<BitNode> run(ArrayList<BitNode> nodeListRun, int rId, HashSet<Integer> Sr) {

		// Initialization
		NodeComparator comparator = new NodeComparator();
		PriorityQueue<BitNode> q0 = new PriorityQueue<BitNode>(comparator);
		PriorityQueue<BitNode> q1 = new PriorityQueue<BitNode>(comparator);

		for (BitNode node : nodeListRun) {
			int nodeId = node.nodeId;
			if (nodeId == rId) {
				node.P = 0;
				q0.add(node);
			} else if (Sr.contains(nodeId)) {
				if (node.isTermNode) {
					node.P = 0;
					node.SRN.add(nodeId);
				} else {
					node.P = 1;
					node.SRN.add(nodeId);
					q1.add(node);
				}
			} else {
				// change Integer.MAX_VALUE to -1
				node.P = Integer.MAX_VALUE;
			}
		}

		ArrayList<Map> e0 = new ArrayList<Map>();
		ArrayList<Map> e1 = new ArrayList<Map>();

		while (!q0.isEmpty()) {
			e0 = new ArrayList<Map>();
			e1 = new ArrayList<Map>();
			while (!q0.isEmpty()) {
				BitNode v = q0.poll();
				for (int uId : v.NGV) {
					BitNode u = nodeListRun.get(uId - 1);
					if ((u.P == Integer.MAX_VALUE) || (u.P == v.P + 1)) {
						Map map = new Map(v, u);
						e1.add(map);
						if (u.P == Integer.MAX_VALUE) {
							if (!u.isTermNode) {
								u.P = v.P + 1;
								q1.add(u);
							} else {// term node does not go into Q1,
								// P[term node] = P[v]
								u.P = v.P;
							}
						}
					} else if (u.P == v.P) {
						Map map = new Map(v, u);
						e0.add(map);
					}
				}
			}
			if (e0.size() > 0) {
				for (Map map : e0) {
					// S^0(u) = S^0(u) + S^-1(v)
					map.u.SRZ.addAll(map.v.SRN);
					// System.out.println("debug");
					// System.out.println("node v is " + + map.v.nodeId);
					// System.out.println("node u is " + + map.u.nodeId);
					// System.out.println(map.v.nodeId + " S^0: " + map.v.SRZ);
					// System.out.println(map.u.nodeId + " S^0: " + map.u.SRZ);
					// System.out.println();
				}
			}
			if (e1.size() > 0) {
				for (Map map : e1) {
					// S^-1(u) = S^-1(u) + S^0(v)
					map.u.SRN.addAll(map.v.SRN);
					// S^0(u) = S^0(u) + S^0(v)
					map.u.SRZ.addAll(map.v.SRZ);
					// System.out.println("debug");
					// System.out.println("node v is " + map.v.nodeId);
					// System.out.println("node u is " + map.u.nodeId );
					// System.out.println(map.v.nodeId + " S^-1: " + map.v.SRZ);
					// System.out.println(map.u.nodeId + " S^-1: " + map.u.SRZ);
					// System.out.println(map.v.nodeId + " S^-0: " + map.v.SRN);
					// System.out.println(map.u.nodeId + " S^-0: " + map.u.SRN);
					// System.out.println();
				}
			}

			q0.addAll(q1);
			q1 = new PriorityQueue<BitNode>(comparator);
		}
		return nodeListRun;

	}

	public int distanceQuerying(HashMap<Integer, ArrayList<Quadruple>> finalResult, int rId, int sId, int tId) {
		int distance = 0;
		return distance;
	}

	public static void main(String[] args) {
		String graphPath = "/Users/zhangxin/AQPEQ/GraphExamples/distanceExmaple/test1/";
		ReadGraph readGraph = new ReadGraph(graphPath);
		nodeList = readGraph.readNode(graphPath);
		readGraph.readEdge(graphPath, nodeList);

		for (BitNode node : nodeList) {
			System.out.println("node: " + node.nodeId + "; neighbor = " + node.NGV);
		}
		System.out.println();
		PriorityQueue<BitNode> queue = readGraph.queue;

		HashMap<Integer, ArrayList<Quadruple>> finalResult = new HashMap<Integer, ArrayList<Quadruple>>();
		while (!queue.isEmpty()) {
			ArrayList<BitNode> nodeListRun = new ArrayList<BitNode>(nodeList);
			BitNode nodeR = queue.poll();
			int rId = nodeR.nodeId;
			HashSet<Integer> Sr = new HashSet<Integer>();
			int b = 3;
			int cnt = 0;
			for (int neighborId : nodeList.get(rId - 1).NGV) {
				if (cnt < b) {
					Sr.add(neighborId);
				}
				cnt++;
			}
			BitParallelBFS BPBFS = new BitParallelBFS();
			ArrayList<BitNode> resultList = BPBFS.run(nodeListRun, rId, Sr);

			// result
			for (BitNode node : resultList) {
				Quadruple quadruple = new Quadruple(rId, node.P, node.SRN, node.SRZ);

				if(finalResult.containsKey(node.nodeId)){
					finalResult.get(node.nodeId).add(quadruple);
				} else {
					ArrayList<Quadruple> quadrupleList = new ArrayList<Quadruple>();
					quadrupleList.add(quadruple);
					finalResult.put(node.nodeId, quadrupleList);
				}
			}
		}

		// for (BitNode node : nodeList) {
		// System.out.println("node: " + node.nodeId + "; neighbor = " +
		// node.NGV);
		// System.out.println("P = " + node.P + "; S^-1 = " + node.SRN + "; S^0
		// = " + node.SRZ);
		// System.out.println();
		// }
		// System.out.println();
		//
		// //result
		// int[] pList = new int[nodeList.size()];
		// ArrayList<HashSet<Integer>> SRN = new ArrayList<HashSet<Integer>>();
		// ArrayList<HashSet<Integer>> SRZ = new ArrayList<HashSet<Integer>>();
		// for(BitNode node : nodeList){
		// pList[node.nodeId-1] = node.P;
		// SRN.add(node.SRN);
		// SRZ.add(node.SRZ);
		// }
		// System.out.println("Print result");
		// for(int i=0; i<nodeList.size(); i++){
		// System.out.println(i+1);
		// System.out.println("P = " + pList[i]);
		// System.out.println("SRN = " + SRN.get(i));
		// System.out.println("SRZ = " + SRZ.get(i));
		// }
	}

}
