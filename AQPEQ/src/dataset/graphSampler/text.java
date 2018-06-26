package dataset.graphSampler;

import java.util.ArrayList;

import aqpeq.utilities.Dummy.DummyProperties;
import aqpeq.utilities.StringPoolUtility;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class text {

	public static void main(String[] args) throws Exception {
		String graphInfraPath = "/Users/zhangxin/Desktop/DBP/untitled folder/";
		DummyProperties.withProperties = false;
		DummyProperties.readRelType = true;
		DummyProperties.debugMode = true;
		
		boolean addBackward = false;
		GraphInfraReaderArray graph;
		graph = new GraphInfraReaderArray(graphInfraPath, addBackward);
		graph.read();
		ArrayList<NodeInfra> nodeOfNodeId = graph.nodeOfNodeId;
		ArrayList<RelationshipInfra> relationOfRelId = graph.relationOfRelId;
		
//		for (NodeInfra node : nodeOfNodeId) {
//			String labels = "";
//			for (int labelId : node.tokens) {
//				labels += StringPoolUtility.getStringOfId(labelId) + "---";
//			}
//			System.out.println("Node " + node.nodeId + ", labels = " + labels);
//		}
		
		
		for (RelationshipInfra rel: relationOfRelId) {
			String types = "";
			for (int typeId : rel.types) {
				types += StringPoolUtility.getStringOfId(typeId) + "---";
			}
			
			System.out.println("Edge " + rel.relId + ", types = " + types);
			
		}

	}

}
