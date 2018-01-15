package graphInfra;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import aqpeq.utilities.StringPoolUtility;

public class GraphInfraReaderArray {

	public String verticesFileName = "vertices.in";
	public String relationshipFileName = "relationships.in";
	// public String graphInfraPath =
	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/amazon/amazonGraphInfra/";
	// public String graphInfraPath = "/Users/zhangxin/Desktop/Summer/newDBP/";
	private String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/Synthetics/n100/";

	public ArrayList<NodeInfra> nodeOfNodeId;
	public ArrayList<RelationshipInfra> relationOfRelId;

	public HashMap<Integer, HashSet<Integer>> nodeIdsOfToken;
	public HashMap<Integer, HashSet<Integer>> relIdsOfType;

	public int maxNodeId = 0;
	public int maxRelId = 0;
	private boolean readLabels = true;
	public boolean addBackward = false;
	HashSet<Integer> stopWords;

	public static void main(String[] args) throws Exception {
		// String graphInfraPath =
		// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/amazon/amazonGraphInfra/";
		// String graphInfraPath = "/Users/zhangxin/Desktop/Summer/newDBP/";
		String graphInfraPath = "/Users/mnamaki/AQPEQ/GraphExamples/highInDegree/graph";
		GraphInfraReaderArray gir = new GraphInfraReaderArray(graphInfraPath, true);
		gir.read();
		gir.indexInvertedListOfTokens(gir);

		// for (RelationshipInfra rel : gir.relationOfRelId) {
		// System.out.println(rel.sourceId + "-->" + rel.destId);
		// System.out.println(
		// gir.nodeOfNodeId.get(rel.sourceId) + "--" + rel.weight + "-->" +
		// gir.nodeOfNodeId.get(rel.destId));
		// }

	}

	public GraphInfraReaderArray(String graphInfraPath, boolean addBackward) {
		if (!graphInfraPath.endsWith("/")) {
			graphInfraPath += "/";
		}
		this.graphInfraPath = graphInfraPath;
		this.addBackward = addBackward;

		stopWords = DummyFunctions.getStopwordsSet();

		if (DummyProperties.debugMode)
			System.out.println("stop words size: " + stopWords.size());
	}

	public void read() throws Exception {

		double startTime = System.nanoTime();
		readVertices();

		if (DummyProperties.debugMode)
			System.out.println("finish read vertices");

		readRelationships();

		if (DummyProperties.debugMode)
			System.out.println("finish read rel");

		if (addBackward) {
			addBackwardEdges();
		}

		if (DummyProperties.debugMode) {
			System.out.println("reading graph duration: " + ((System.nanoTime() - startTime) / 1e6));
			System.out.println("number of nodes: " + nodeOfNodeId.size());
			System.out.println("number of edges: " + relationOfRelId.size());
			System.out.println();
		}
	}

	private void addBackwardEdges() {

		for (NodeInfra sourceNode : nodeOfNodeId) {

			int currentAutoRelId = relationOfRelId.size();

			ArrayList<TargetIdAndBackwardEdge> tempBcwdRels = new ArrayList<TargetIdAndBackwardEdge>();

			int sourceOutDeg = this.nodeOfNodeId.get(sourceNode.nodeId).getOutDegree();

			for (Integer targetNodeId : sourceNode.getOutgoingRelIdOfSourceNodeId().keySet()) {

				if (!this.nodeOfNodeId.get(targetNodeId).getOutgoingRelIdOfSourceNodeId()
						.containsKey(sourceNode.nodeId)) {

					int targetIndegree = this.nodeOfNodeId.get(targetNodeId).getInDegree();
					float weight = DummyFunctions.log(1 + targetIndegree, 2);

					int forwardRelId = sourceNode.getOutgoingRelIdOfSourceNodeId().get(targetNodeId);
					// // creating a ghost rel
					// if(currentAutoRelId == 6069514){
					// System.out.println();
					// }
					tempBcwdRels.add(new TargetIdAndBackwardEdge(targetNodeId, new BackwardRelInfra(currentAutoRelId++,
							forwardRelId, weight, targetNodeId, sourceNode.nodeId)));
				}
			}

			//
			int currentTargetId = sourceNode.nodeId;
			for (TargetIdAndBackwardEdge targetIdAndBackwardEdge : tempBcwdRels) {

				int currentSourceId = targetIdAndBackwardEdge.targetId;

				relationOfRelId.add(targetIdAndBackwardEdge.bwdEdge);

				if (DummyProperties.debugMode) {
					if ((relationOfRelId.size() - 1) != targetIdAndBackwardEdge.bwdEdge.relId) {
						System.out.println("size is not ok with the relId " + " size:" + relationOfRelId.size()
								+ " relId:" + targetIdAndBackwardEdge.bwdEdge.relId);
					}
				}

				int relId = targetIdAndBackwardEdge.bwdEdge.relId;
				//
				// // neighborhood indexing
				// //
				// nodeOfNodeId.get(currentSourceId).outgoingRelIdsOfSourceNodeId.putIfAbsent(currentTargetId,
				// // new ArrayList<>(1));
				// //
				// nodeOfNodeId.get(currentSourceId).outgoingRelIdsOfSourceNodeId.get(currentTargetId).add(relId);
				//
				nodeOfNodeId.get(currentSourceId).outgoingRelIdOfSourceNodeId.putIfAbsent(currentTargetId, relId);
				//
				// //
				// nodeOfNodeId.get(currentTargetId).incomingRelIdsOfTargetNodeId.putIfAbsent(currentSourceId,
				// // new ArrayList<>(1));
				// //
				// nodeOfNodeId.get(currentTargetId).incomingRelIdsOfTargetNodeId.get(currentSourceId).add(relId);
				//
				// //
				// nodeOfNodeId.get(currentTargetId).incomingRelIdOfTargetNodeId.putIfAbsent(currentSourceId,
				// // relId);
			}

		}

		for (NodeInfra node : nodeOfNodeId) {
			node.inDegree = 0;
			node.outDegree = 0;
		}

		for (NodeInfra sourceNode : nodeOfNodeId) {
			sourceNode.outDegree = sourceNode.getOutgoingRelIdOfSourceNodeId().size();
			for (Integer targetNodeId : sourceNode.getOutgoingRelIdOfSourceNodeId().keySet()) {
				nodeOfNodeId.get(targetNodeId).inDegree++;
			}
		}
	}

	public void readWithNoLabels() throws Exception {
		readLabels = false;
		read();
		readLabels = true;
	}

	private void readVertices() throws Exception {
		FileInputStream fis = new FileInputStream(graphInfraPath + "" + verticesFileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String line = null;
		while ((line = br.readLine()) != null) {
			String[] splittedVerticeLine = line.split("#");
			int nodeId = Integer.parseInt(splittedVerticeLine[0]);
			maxNodeId = Math.max(nodeId, maxNodeId);
		}
		br.close();
		// System.out.println("maxNodeId: " + maxNodeId);
		nodeOfNodeId = new ArrayList<>(maxNodeId + 1);

		for (int i = 0; i < (maxNodeId + 1); i++) {
			nodeOfNodeId.add(new NodeInfra(i));
		}

		fis = new FileInputStream(graphInfraPath + "" + verticesFileName);
		br = new BufferedReader(new InputStreamReader(fis));
		line = null;
		while ((line = br.readLine()) != null) {
			String[] splittedVerticeLine = line.split("#");
			int nodeId = Integer.parseInt(splittedVerticeLine[0]);
			int inDegree = Integer.parseInt(splittedVerticeLine[1]);
			int outDegree = Integer.parseInt(splittedVerticeLine[2]);

			NodeInfra node = new NodeInfra(nodeId);
			node.inDegree = inDegree;
			node.outDegree = outDegree;

			nodeOfNodeId.set(nodeId, node);

			nodeOfNodeId.get(nodeId).outgoingRelIdOfSourceNodeId = new HashMap<>(nodeOfNodeId.get(nodeId).outDegree);
			// nodeOfNodeId.get(nodeId).incomingRelIdOfTargetNodeId = new
			// HashMap<>(nodeOfNodeId.get(nodeId).inDegree);

			if (readLabels) {
				if (splittedVerticeLine.length > 3) {
					String[] labels = splittedVerticeLine[3].split(";");
					for (String lbl : labels)
						if (lbl.trim().length() > 0) {
							for (Integer tokenId : DummyFunctions.getTokensOfALabel(lbl))
								node.addToken(tokenId);
						}
				}
			}

			if (DummyProperties.withProperties) {
				if (splittedVerticeLine.length > 4) {
					String[] props = splittedVerticeLine[4].split(";");
					for (String prop : props) {
						String[] propsKeyVal = prop.split(":");
						if (propsKeyVal == null || propsKeyVal.length < 2) {
							continue;
						}
						if (propsKeyVal[0].trim().length() > 0 && propsKeyVal[1].trim().length() > 0) {
							String key = propsKeyVal[0];

							HashSet<Integer> tokenValues = new HashSet<Integer>();
							for (Integer tokenId : DummyFunctions.getTokensOfALabel(propsKeyVal[1])) {
								tokenValues.add(tokenId);
							}
							node.addProperties(/*
												 * StringPoolUtility.
												 * insertIntoStringPool(key),
												 */ tokenValues);

						}
					}
				}
			}

		}
		br.close();
	}

	private void readRelationships() throws Exception {

		FileInputStream fis = new FileInputStream(graphInfraPath + "" + relationshipFileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String line = null;
		while ((line = br.readLine()) != null) {
			String[] splittedRelLine = line.split("#");
			int relId = Integer.parseInt(splittedRelLine[0]);
			maxRelId = Math.max(maxRelId, relId);
		}
		br.close();

		// System.out.println("maxRelId: " + maxRelId);

		if (!addBackward)
			relationOfRelId = new ArrayList<>(maxRelId + 1);
		else
			relationOfRelId = new ArrayList<>(2 * maxRelId + 1);

		for (int i = 0; i < (maxRelId + 1); i++) {
			relationOfRelId.add(null);
		}

		fis = new FileInputStream(graphInfraPath + "" + relationshipFileName);
		br = new BufferedReader(new InputStreamReader(fis));

		int relCnt = 0;
		line = null;
		while ((line = br.readLine()) != null) {
			relCnt++;
			if (relCnt % 1000000 == 0)
				System.out.println("relCnt: " + relCnt);

			String[] splittedRelLine = line.split("#");
			int relId = Integer.parseInt(splittedRelLine[0]);
			int sourceId = Integer.parseInt(splittedRelLine[1]);
			int targetId = Integer.parseInt(splittedRelLine[2]);
			RelationshipInfra rel = new RelationshipInfra(relId, sourceId, targetId);
			relationOfRelId.set(relId, rel);

			nodeOfNodeId.get(sourceId).outgoingRelIdOfSourceNodeId.putIfAbsent(targetId, relId);
			// nodeOfNodeId.get(sourceId).outgoingRelIdOfSourceNodeId.get(targetId).add(relId);

			// nodeOfNodeId.get(targetId).incomingRelIdOfTargetNodeId.putIfAbsent(sourceId,
			// relId);
			// nodeOfNodeId.get(targetId).incomingRelIdOfTargetNodeId.get(sourceId).add(relId);

			if (DummyProperties.readRelType && readLabels) {
				if (splittedRelLine.length > 3) {
					if (splittedRelLine[3].trim().length() > 0)
						for (Integer typeId : DummyFunctions.getTokensOfALabel(splittedRelLine[3])) {
							rel.addType(typeId);
						}
				}
			}

			if (DummyProperties.readRelType && DummyProperties.withProperties) {
				if (splittedRelLine.length > 4) {
					String[] props = splittedRelLine[4].split(";");
					for (String prop : props) {
						String[] propsKeyVal = prop.split(":");
						if (propsKeyVal == null || propsKeyVal.length < 2) {
							continue;
						}
						if (propsKeyVal[0].trim().length() > 0 && propsKeyVal[1].trim().length() > 0) {
							String key = propsKeyVal[0];

							HashSet<Integer> tokenValues = new HashSet<Integer>();
							for (Integer tokenId : DummyFunctions.getTokensOfALabel(propsKeyVal[1])) {
								tokenValues.add(tokenId);
							}
							rel.addProperties(StringPoolUtility.insertIntoStringPool(key), tokenValues);

						}
					}
				}
			}

			if (splittedRelLine.length > 5) {
				rel.weight = Float.parseFloat(splittedRelLine[5]);
			}

		}
		br.close();
	}

	public HashMap<Integer, HashSet<Integer>> indexInvertedListOfTokens(GraphInfraReaderArray graph) {

		nodeIdsOfToken = new HashMap<Integer, HashSet<Integer>>();

		long startIndexTime = System.currentTimeMillis();
		for (NodeInfra node : graph.nodeOfNodeId) {
			// index labels
			for (Integer tokenId : node.getTokens()) {
				nodeIdsOfToken.putIfAbsent(tokenId, new HashSet<Integer>(1));
				nodeIdsOfToken.get(tokenId).add(node.nodeId);
			}

			// if with-prop
			if (DummyProperties.withProperties) {
				HashSet<Integer> prop = node.getProperties();

				if (prop == null)
					continue;

				// for (Integer key : prop.keySet()) {
				for (Integer valueTokenId : prop) {
					nodeIdsOfToken.putIfAbsent(valueTokenId, new HashSet<Integer>(1));
					nodeIdsOfToken.get(valueTokenId).add(node.nodeId);
				}
				// }
			}
		}

		long endIndexTime = System.currentTimeMillis();
		long indexTime = (endIndexTime - startIndexTime);

		if (DummyProperties.debugMode) {
			System.out.println("index inverted list tokens: " + indexTime + " ms.");
		}
		return nodeIdsOfToken;

	}

	public HashMap<Integer, HashSet<Integer>> indexEdgeInvertedListOfType(GraphInfraReaderArray graph) {
		relIdsOfType = new HashMap<Integer, HashSet<Integer>>();

		long startIndexTime = System.currentTimeMillis();
		int cnt = 0;
		for (RelationshipInfra rel : graph.relationOfRelId) {

			// index labels
			if (rel != null && rel.types != null) {
				for (Integer type : rel.types) {
					relIdsOfType.putIfAbsent(type, new HashSet<Integer>(1));
					relIdsOfType.get(type).add(rel.relId);
				}
			}
			// else if (rel == null) {
			// System.out.println("rel null cnt at " + cnt);
			// }
			cnt++;

			// if with-prop
			// if (DummyProperties.withProperties) {
			// if (rel != null && rel.properties != null) {
			// for (String key : rel.properties.keySet()) {
			// String p = rel.properties.get(key).toLowerCase().trim();
			// relIdsOfType.putIfAbsent(p, new HashSet<Integer>());
			// relIdsOfType.get(p).add(rel.relId);
			// }
			// }
			// }
		}

		long endIndexTime = System.currentTimeMillis();
		long indexTime = (endIndexTime - startIndexTime);

		if (DummyProperties.debugMode) {
			System.out.println("index inverted list tokens: " + indexTime + " ms.");
		}
		return relIdsOfType;

	}
}

class TargetIdAndBackwardEdge {
	public int targetId;
	public BackwardRelInfra bwdEdge;

	public TargetIdAndBackwardEdge(int targetId, BackwardRelInfra bwdEdge) {

		this.targetId = targetId;
		this.bwdEdge = bwdEdge;
	}
}