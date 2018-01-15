package queryExpansion;

import java.util.ArrayList;

public class AnswerAsInput {
	private int rootNodeId;
	private ArrayList<Integer> contentNodes;
	private ArrayList<Integer> allNodes;
	private double cost; // quality

	public AnswerAsInput(int rootNodeId, ArrayList<Integer> contentNodes, ArrayList<Integer> allNodes, double cost) {
		this.setRootNodeId(rootNodeId);
		this.setContentNodes(contentNodes);
		this.setAllNodes(allNodes);
		this.setCost(cost);
	}

	public int getRootNodeId() {
		return rootNodeId;
	}

	public void setRootNodeId(int rootNodeId) {
		this.rootNodeId = rootNodeId;
	}

	public ArrayList<Integer> getContentNodes() {
		return contentNodes;
	}

	public void setContentNodes(ArrayList<Integer> contentNodes) {
		this.contentNodes = contentNodes;
	}

	public ArrayList<Integer> getAllNodes() {
		return allNodes;
	}

	public void setAllNodes(ArrayList<Integer> allNodes) {
		this.allNodes = allNodes;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}
}
