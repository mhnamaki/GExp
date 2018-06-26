package graphInfra;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import aqpeq.utilities.Dummy;
import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import aqpeq.utilities.StringPoolUtility;

public class GraphInfraReaderArray {

	public String verticesFileName = "vertices.in";
	public String relationshipFileName = "relationships.in";
	public int tempMaxRelIdForDemo;
	// public String graphInfraPath =
	// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/amazon/amazonGraphInfra/";
	// public String graphInfraPath = "/Users/zhangxin/Desktop/Summer/newDBP/";
	private String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/Synthetics/n100/";

	public ArrayList<NodeInfra> nodeOfNodeId;
	public ArrayList<RelationshipInfra> relationOfRelId;

	public HashMap<Integer, HashSet<Integer>> nodeIdsOfToken;
	public HashMap<Integer, HashSet<Integer>> relIdsOfType;
	
	public HashMap<Integer, HashSet<Integer>> urlIdsOfLabel;
	public HashMap<Integer, HashSet<Integer>> labelIdsIdsOfLabel;
	
	//public HashMap<Integer, Integer> freuencyOfTokens = new HashMap<Integer, Integer>();

	public int maxNodeId = 0;
	public int maxRelId = 0;
	private boolean readLabels = true;
	public boolean addBackward = false;
	HashSet<Integer> stopWords;

	public static void main(String[] args) throws Exception {
		// String graphInfraPath =
		// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/amazon/amazonGraphInfra/";
		// String graphInfraPath = "/Users/zhangxin/Desktop/Summer/newDBP/";
		String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp/dbpGraphInfra/graph/dbp";
		DummyProperties.withProperties = false;
		GraphInfraReaderArray gir = new GraphInfraReaderArray(graphInfraPath, false);
		gir.read();
		gir.indexInvertedListOfTokens(gir);

		// double sum = 0d;
		//

		HashSet<Integer> nodeSet = new HashSet<Integer>();
		for (NodeInfra node : gir.nodeOfNodeId) {
			if (node.tokens != null) {
				int candidate = 0;
				for (int tokenId : node.tokens) {

					String str = StringPoolUtility.getStringOfId(tokenId);
					if (str.toLowerCase().contains("restaurant")) {
						nodeSet.add(node.nodeId);
					}

				}
				// if(candidate >=2){
				// System.out.print(node.nodeId +",");
				// //System.out.println(node.getOutgoingRelIdOfSourceNodeId());
				// }
			}
		}

		//
		// System.out.println("avg:" + (sum / (double)
		// gir.nodeOfNodeId.size()));

		// TreeMap<Integer, Integer> freqOfTokenId = new TreeMap<Integer,
		// Integer>();
		// for (int tokenId : gir.nodeIdsOfToken.keySet()) {
		// freqOfTokenId.putIfAbsent(gir.nodeIdsOfToken.get(tokenId).size(), 0);
		// freqOfTokenId.put(gir.nodeIdsOfToken.get(tokenId).size(),
		// freqOfTokenId.get(gir.nodeIdsOfToken.get(tokenId).size()) + 1);
		// }
		//
		// for (int key : freqOfTokenId.navigableKeySet())
		// System.out.println(key + "," + freqOfTokenId.get(key));

	}

	public GraphInfraReaderArray(String graphInfraPath, boolean addBackward) {
		StringPoolUtility.reInit();
		Dummy.DummyProperties.stopwordsSet = null;

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
		
		updateDegree();

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
		
		urlIdsOfLabel = new HashMap<Integer, HashSet<Integer>>();
		labelIdsIdsOfLabel = new HashMap<Integer, HashSet<Integer>>();

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

//			if(nodeId==497){
//				System.out.println();
//			}

			NodeInfra node = new NodeInfra(nodeId);
			node.inDegree = inDegree;
			node.outDegree = outDegree;

			nodeOfNodeId.set(nodeId, node);

			nodeOfNodeId.get(nodeId).outgoingRelIdOfSourceNodeId = new HashMap<>(nodeOfNodeId.get(nodeId).outDegree);
			// nodeOfNodeId.get(nodeId).incomingRelIdOfTargetNodeId = new
			// HashMap<>(nodeOfNodeId.get(nodeId).inDegree);

			if (readLabels) {

//				if(node.nodeId==51067){
//					System.out.println();
//				}

				HashSet<Integer> labelIds = new HashSet<Integer>();
				int urlId = 0;
				if (splittedVerticeLine.length > 3) {
					String[] labels = splittedVerticeLine[3].split(";");
					for (String lbl : labels) {
						if (lbl.trim().length() > 0) {
							for (Integer tokenId : DummyFunctions.getTokensOfALabel(lbl)) {
								node.addToken(tokenId);
//								if (freuencyOfTokens.containsKey(tokenId)) {
//									int frequency = freuencyOfTokens.get(tokenId) + 1;
//									freuencyOfTokens.replace(tokenId, frequency);
//								} else {
//									freuencyOfTokens.put(tokenId, 1);
//								}
							}
						}
						// TODO: if lbl starts with uri_ => insrt to pool and
						// add to int uri.
						// TODO: else insrt to pool get the id and add it to the
						// set of labelIds.

						if (lbl.contains("uri_")) {
							String newToken = "";
							String[] tem = lbl.trim().split("_");
							for (int i = 1; i < tem.length; i++) {
								newToken = newToken.trim() + " " + tem[i];
							}
							urlId = StringPoolUtility.insertIntoStringPool(newToken);
							node.urlId = urlId;
						} else {
							int labelId = StringPoolUtility.insertIntoStringPool(lbl.trim().toLowerCase());
							labelIds.add(labelId);
							HashSet<Integer> labelTokens = DummyFunctions.getTokensOfALabel(lbl);
							labelIdsIdsOfLabel.put(labelId, labelTokens);
						}
					}
					node.labels = labelIds;
					for (int labelId : labelIds) {
						for (int labelToken : labelIdsIdsOfLabel.get(labelId)) {
							if (urlIdsOfLabel.containsKey(labelToken)) {
								urlIdsOfLabel.get(labelToken).add(urlId);
							} else {
								HashSet<Integer> urlSet = new HashSet<Integer>();
								urlSet.add(urlId);
								urlIdsOfLabel.put(labelToken, urlSet);
							}
						}
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
							if (node.urlId == -1) {
								int urlId = StringPoolUtility.insertIntoStringPool(propsKeyVal[1]);
								node.urlId = urlId;
							}
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
					if (splittedRelLine[3].trim().length() > 0) {
						//edgeType:
						String edgeType = splittedRelLine[3];
						if (edgeType.startsWith("http")) {
							edgeType = edgeType.replace("http //","http://");
							edgeType = edgeType.replaceAll(" ", "");
							URI uri = new URI(edgeType);
							String path = uri.getPath();
							edgeType = path.substring(path.lastIndexOf('/') + 1);
						}
						for (Integer typeId : DummyFunctions.getTokensOfALabel(edgeType)) {
							rel.addType(typeId);
						}
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
	
	public void updateDegree() {
		for (NodeInfra sourceNode : nodeOfNodeId) {
			sourceNode.outDegree = sourceNode.getOutgoingRelIdOfSourceNodeId().size();
			for (Integer targetNodeId : sourceNode.getOutgoingRelIdOfSourceNodeId().keySet()) {
				nodeOfNodeId.get(targetNodeId).inDegree++;
			}
		}
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