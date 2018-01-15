package graphInfra;

import java.util.HashMap;
import java.util.HashSet;

public class NodeInfra {
	public int nodeId;
	public HashSet<Integer> tokens;
	private HashSet<Integer> properties;
	public int inDegree = 0;
	public int outDegree = 0;

	// src -> dest -> relIds (multi-relationships)
	public HashMap<Integer, Integer> outgoingRelIdOfSourceNodeId;

	// dest -> source -> relIds (multi-relationships)
	// public HashMap<Integer, Integer> incomingRelIdOfTargetNodeId;

	public NodeInfra(Integer nodeId, HashSet<Integer> tokens, HashSet<Integer> properties) {
		this.nodeId = nodeId;
		this.tokens = tokens;
		this.setProperties(properties);
	}

	public NodeInfra(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public void addToken(Integer lbl) {
		if (tokens == null) {
			tokens = new HashSet<>();
		}

		tokens.add(lbl);

	}

	public HashSet<Integer> getTokens() {
		if (tokens == null)
			return new HashSet<Integer>();

		return tokens;
	}

	public void addProperties(HashSet<Integer> values) {
		if (getProperties() == null) {
			setProperties(new HashSet<Integer>());
		}
		getProperties().addAll(values);
	}

	public int getDegree() {
		return inDegree + outDegree;
	}

	public int getInDegree() {
		return inDegree;
	}

	public int getOutDegree() {
		return outDegree;
	}

	// public HashMap<Integer, Integer> getIncomingRelIdOfTargetNodeId() {
	// if (incomingRelIdOfTargetNodeId == null)
	// return new HashMap<Integer, Integer>();
	//
	// return incomingRelIdOfTargetNodeId;
	// }

	public HashMap<Integer, Integer> getIncomingRelIdOfSourceNodeId(GraphInfraReaderArray graph) {
		HashMap<Integer, Integer> incomingRelIdOfTargetNodeId = new HashMap<Integer, Integer>();

		for (int targetNodeId : outgoingRelIdOfSourceNodeId.keySet()) {
			for (int srcNodeId : graph.nodeOfNodeId.get(targetNodeId).getOutgoingRelIdOfSourceNodeId().keySet()) {
				if (srcNodeId == this.nodeId) {
					incomingRelIdOfTargetNodeId.putIfAbsent(targetNodeId,
							graph.nodeOfNodeId.get(targetNodeId).getOutgoingRelIdOfSourceNodeId().get(srcNodeId));
				}
			}
		}

		// TODO: it's not good for large graphs, just for testing when backward
		// edge is off
		if (!graph.addBackward) {
			for (NodeInfra nodeInfra : graph.nodeOfNodeId) {

				if (!nodeInfra.outgoingRelIdOfSourceNodeId.containsKey(this.nodeId))
					continue;

				incomingRelIdOfTargetNodeId.put(nodeInfra.nodeId,
						nodeInfra.outgoingRelIdOfSourceNodeId.get(this.nodeId));
			}
		}

		return incomingRelIdOfTargetNodeId;
	}

	public HashMap<Integer, Integer> getOutgoingRelIdOfSourceNodeId() {
		if (outgoingRelIdOfSourceNodeId == null)
			return new HashMap<Integer, Integer>();

		return outgoingRelIdOfSourceNodeId;
	}

	// public HashMap<Integer, Integer> getRelationshipsOfNodeId() {
	// // TODO: make it much more efficient by returning an iterator.
	// HashMap<Integer, Integer> totalRels = new HashMap<Integer, Integer>();
	//
	// if (outgoingRelIdOfSourceNodeId != null) {
	// for (Integer targetNodeId : outgoingRelIdOfSourceNodeId.keySet()) {
	// totalRels.put(targetNodeId,
	// outgoingRelIdOfSourceNodeId.get(targetNodeId));
	// }
	// }
	//
	// if (incomingRelIdOfTargetNodeId != null) {
	// for (Integer sourceNodeId : incomingRelIdOfTargetNodeId.keySet()) {
	// totalRels.put(sourceNodeId,
	// incomingRelIdOfTargetNodeId.get(sourceNodeId));
	// }
	// }
	//
	// return totalRels;
	//
	// }

	@Override
	public String toString() {
		return "id:" + nodeId + " oD:" + this.getOutDegree() + " iD:" + this.getInDegree() + " l:" + this.getTokens();
	}

	public HashSet<Integer> getProperties() {
		return properties;
	}

	public void setProperties(HashSet<Integer> properties) {
		this.properties = properties;
	}

	@Override
	public int hashCode() {
		return nodeId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		NodeInfra other = (NodeInfra) obj;
		if (this.nodeId != other.nodeId)
			return false;

		return true;
	}
}
