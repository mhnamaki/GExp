package bidirectional.keywordSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import graphInfra.NodeInfra;

public class Node {
	int nodeId;
	// a_u = sum of all a_ui
	double activation;
	private HashMap<String, Double> activationMap = new HashMap<String, Double>();
	int depth = 0;
	// HashSet<Integer> incoming;
	// HashSet<Integer> outgoing;
	// Set of nodes u such that edge (u, v) has been explored
	// initialize is empty
	// key: node id, value: node
	private HashMap<Integer, Node> pU = new HashMap<Integer, Node>();
	// private HashSet<Integer> pU = new HashSet<Integer>();
	// For every keyword term t_i, we maintain the child node sp_u,i
	// Node to follow from u for best path to keyword t_i
	// initialize is Infinity
	private HashMap<String, ArrayList<Integer>> pathToOrigin = new HashMap<String, ArrayList<Integer>>();
	private HashMap<String, Integer> spUI = new HashMap<String, Integer>();
	// Length of best known path from u to a node in S_i
	// map -> key: keyword; value as follows:
	// if u in S_i, value = 0; else value = Infinity
	// dist should be int, but here we need Infinity, so we assign dist to be
	// double here
	private HashMap<String, Double> distUI = new HashMap<String, Double>();
	public int degree;

	public Node(int nodeId, int degree,
			int depth/*
						 * , HashSet<Integer> incoming, HashSet<Integer>
						 * outgoing
						 */) {
		this.nodeId = nodeId;
		this.depth = depth;
		this.degree = degree;
		// this.incoming = incoming;
		// this.outgoing = outgoing;
	}

	// public HashSet<Integer> getpU() {
	// return pU;
	// }
	// public void setpU(HashSet<Integer> pU) {
	// this.pU = pU;
	// }
	public HashMap<String, ArrayList<Integer>> getPathToOrigin() {
		return pathToOrigin;
	}

	public void setPathToOrigin(HashMap<String, ArrayList<Integer>> pathToOrigin) {
		this.pathToOrigin = pathToOrigin;
	}

	public HashMap<String, Double> getDistUI() {
		return distUI;
	}

	public void setDistUI(HashMap<String, Double> distUI) {
		this.distUI = distUI;
	}

	public HashMap<String, Double> getActivationMap() {
		return activationMap;
	}

	public void setActivationMap(HashMap<String, Double> activationMap) {
		this.activationMap = activationMap;
		double activation = 0;
		for (String keyword : activationMap.keySet()) {
			activation += activationMap.get(keyword);
		}
		this.activation = activation;
	}

	public HashMap<Integer, Node> getpU() {
		return pU;
	}

	public void setpU(HashMap<Integer, Node> pU) {
		this.pU = pU;
	}

	public HashMap<String, Integer> getSpUI() {
		return spUI;
	}

	public void setSpUI(HashMap<String, Integer> spUI) {
		this.spUI = spUI;
	}

	@Override
	public String toString() {
		return this.nodeId + " ";
	}

}
