package tryingToTranslateXin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import graphInfra.GraphInfraReaderArray;

public class ReadGraph {

	HashMap<String, HashSet<Integer>> termNodeToNode = new HashMap<String, HashSet<Integer>>();
	HashMap<String, Integer> termNodeMap = new HashMap<String, Integer>();
	NodeComparator comparator = new NodeComparator();
	public PriorityQueue<BitNode> queue = new PriorityQueue<BitNode>(comparator);

	public ReadGraph(String graphPath) {
	}

	public ArrayList<BitNode> readNode(String graphPath) {
		ArrayList<BitNode> nodeList = new ArrayList<BitNode>();
		File file = new File(graphPath + "vertices.in");
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file));

			BufferedReader br = new BufferedReader(reader);
			String line = "";
			// HashSet<String> termSet = new HashSet<String>();
			termNodeToNode = new HashMap<String, HashSet<Integer>>();
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					String[] splittedVerticeLine = line.split("#");
					int nodeId = Integer.parseInt(splittedVerticeLine[0]);
					String term = splittedVerticeLine[3];
					HashSet<String> currentNodeTermSet = new HashSet<String>();
					currentNodeTermSet.add(term);

					if (termNodeToNode.containsKey(term)) {
						termNodeToNode.get(term).add(nodeId);
					} else {
						HashSet<Integer> nodeIds = new HashSet<Integer>();
						nodeIds.add(nodeId);
						termNodeToNode.put(term, nodeIds);
					}

					BitNode node = new BitNode(nodeId);
					node.termSet = currentNodeTermSet;
					nodeList.add(node);
					queue.add(node);
				}
			}
			br.close();
			
			int size = nodeList.size();

			// add term nodes into nodeList
			int cnt = 1;
			for (String term : termNodeToNode.keySet()) {
				BitNode termNode = new BitNode(size+cnt++);
				HashSet<String> currentNodeTermSet = new HashSet<String>();
				currentNodeTermSet.add(term);
				termNode.isTermNode = true;
				termNode.termSet = currentNodeTermSet;
				nodeList.add(termNode);
				termNodeMap.put(term, termNode.nodeId);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nodeList;
	}

	public void readEdge(String graphPath, ArrayList<BitNode> nodeList) {
		File file = new File(graphPath + "relationships.in");
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file));

			BufferedReader br = new BufferedReader(reader);
			String line = "";
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					String[] splittedRelLine = line.split("#");
					int nodeU = Integer.parseInt(splittedRelLine[1]);
					int nodeV = Integer.parseInt(splittedRelLine[2]);
					nodeList.get(nodeU - 1).addNodeIntoNGV(nodeV);
					nodeList.get(nodeV - 1).addNodeIntoNGV(nodeU);
				}
			}
			br.close();

			// add term node relation
			for (BitNode node : nodeList) {
//				int nodeId = node.nodeId;
				if (!node.isTermNode) {//normal node
					for (String term : node.termSet) {
						node.NGV.add(termNodeMap.get(term));
					}
				} else {//term node
					for (String term : node.termSet) {
						node.NGV = termNodeToNode.get(term);
					}
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String graphPath = "/Users/zhangxin/AQPEQ/GraphExamples/distanceExmaple/";
		ReadGraph readGraph = new ReadGraph(graphPath);
		ArrayList<BitNode> nodeList = readGraph.readNode(graphPath);
		readGraph.readEdge(graphPath, nodeList);
		for (BitNode node : nodeList) {
			System.out.println("node: " + node.nodeId + "; neighbor = " + node.NGV);
		}
	}

}
