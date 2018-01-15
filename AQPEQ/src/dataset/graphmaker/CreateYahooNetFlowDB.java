package dataset.graphmaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import aqpeq.utilities.Dummy.DummyFunctions;

public class CreateYahooNetFlowDB {

	int StartIndex = 0;
	int EndIndex = 1;
	int SifIndex = 2;
	int SrcIPaddressIndex = 3;
	int SrcPIndex = 4;
	int DIfIndex = 5;
	int DstIPaddressIndex = 6;
	int DstPIndex = 7;
	int PIndex = 8;
	int FlIndex = 9;
	int Pkts = 10;
	int Octets = 11;

	public static void main(String[] args) throws Exception {
		CreateYahooNetFlowDB yahooNetFlow = new CreateYahooNetFlowDB();
		yahooNetFlow.run(args);
	}

	private String dataGraphPath = "yahooNetFlow.db";
	private String netFlowFilesDir = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/yahooNetFlowFiles/";
	int numberOfCreatedNodes = 0;
	int numberOfCreatedRels = 0;

	private void run(String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dataGraph")) {
				dataGraphPath = args[++i];
			} else if (args[i].equals("-netFlowFilesDir")) {
				netFlowFilesDir = args[++i];
			}
		}

		File storeDir = new File(dataGraphPath);
		GraphDatabaseService dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "8g")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();

		Transaction tx1 = dataGraph.beginTx();

		HashMap<String, IPNodeInfo> nodeInfoOfAnIP = new HashMap<String, IPNodeInfo>();

		HashMap<Integer, String> protocolOfIndex = new HashMap<Integer, String>();
		protocolOfIndex.put(1, "ICMP");
		protocolOfIndex.put(6, "TCP");
		protocolOfIndex.put(17, "UDP");

		for (File file : DummyFunctions.getFilesInTheDirfinder(netFlowFilesDir, "permuted")) {
			System.out.println("file: " + file.getName());

			FileInputStream fis = new FileInputStream(file.getAbsolutePath());

			// Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			// read first line as attribute names
			String line = br.readLine();

			String[] splittedAttributes = line.split(" +");

			// TODO: 0429.07:59:03.068 MMdd.HH:mm:ss.SSS

			// read values of a line
			int cnt = 0;
			while ((line = br.readLine()) != null) {
				cnt++;
				String[] splittedValues = line.split("\t");

				String srcIPAddress = splittedValues[SrcIPaddressIndex];
				Integer srcPortNumber = Integer.parseInt(splittedValues[SrcPIndex]);

				createIPAndPort(dataGraph, nodeInfoOfAnIP, srcIPAddress, srcPortNumber);
				// Node srcIPNode =
				// dataGraph.getNodeById(nodeInfoOfAnIP.get(srcIPAddress).ipNodeId);
				Node srcPortNode = dataGraph
						.getNodeById(nodeInfoOfAnIP.get(srcIPAddress).portNodeIdOfPort.get(srcPortNumber));

				String destIPAddress = splittedValues[DstIPaddressIndex];
				Integer destPortNumber = Integer.parseInt(splittedValues[DstPIndex]);
				createIPAndPort(dataGraph, nodeInfoOfAnIP, destIPAddress, destPortNumber);
				// Node destIPNode =
				// dataGraph.getNodeById(nodeInfoOfAnIP.get(destIPAddress).ipNodeId);
				Node destPortNode = dataGraph
						.getNodeById(nodeInfoOfAnIP.get(destIPAddress).portNodeIdOfPort.get(destPortNumber));

				Relationship communicationRel = srcPortNode.createRelationshipTo(destPortNode,
						RelationshipType.withName("comminicate_with"));
				numberOfCreatedRels++;

				for (int s = 0; s < splittedAttributes.length; s++) {
					if (s == 8) {
						String protocol = protocolOfIndex.get(Integer.parseInt(splittedValues[s]));
						if (protocol == null) {
							communicationRel.setProperty(splittedAttributes[s], splittedValues[s]);
						} else {
							communicationRel.setProperty(splittedAttributes[s], protocol);
						}
					} else {
						communicationRel.setProperty(splittedAttributes[s], splittedValues[s]);
					}
				}

				if (cnt % 10000 == 0) {
					tx1.success();
					tx1.close();
					tx1 = dataGraph.beginTx();
					System.out.println("cnt: " + cnt);
					System.out.println("numberOfCreatedNodes: " + numberOfCreatedNodes);
					System.out.println("numberOfCreatedRels: " + numberOfCreatedRels);
					System.out.println();
				}
			}
			br.close();
		}
		tx1.success();
		tx1.close();

		dataGraph.shutdown();
	}

	private void createIPAndPort(GraphDatabaseService dataGraph, HashMap<String, IPNodeInfo> nodeInfoOfAnIP,
			String ipAddress, Integer portNumber) {

		Node ipNode = null;
		Node portNode = null;
		if (nodeInfoOfAnIP.containsKey(ipAddress)) {
			ipNode = dataGraph.getNodeById(nodeInfoOfAnIP.get(ipAddress).ipNodeId);

		} else {
			// create an ip node
			ipNode = dataGraph.createNode();
			numberOfCreatedNodes++;
			ipNode.addLabel(Label.label("IP"));
			ipNode.addLabel(Label.label(ipAddress));
			String[] ipSections = ipAddress.split("\\.");
			for (int i = 1; i < 4; i++) {
				String newLabel = "";
				for (int j = 1; j <= 4; j++) {
					if (j <= i) {
						newLabel += ipSections[j - 1];
						if (j < 4) {
							newLabel += ".";
						}
					} else {
						newLabel += "*";
						if (j < 4) {
							newLabel += ".";
						}
					}
				}
				ipNode.addLabel(Label.label(newLabel));
			}

			nodeInfoOfAnIP.put(ipAddress, new IPNodeInfo(ipNode.getId()));

		}

		if (nodeInfoOfAnIP.get(ipAddress).portNodeIdOfPort.containsKey(portNumber)) {
			portNode = dataGraph.getNodeById(nodeInfoOfAnIP.get(ipAddress).portNodeIdOfPort.get(portNumber));
		} else {

			// create a port node for ip
			portNode = dataGraph.createNode();
			numberOfCreatedNodes++;
			portNode.addLabel(Label.label("Port"));
			portNode.setProperty("number", portNumber);

			// create a rel between ip and port
			ipNode.createRelationshipTo(portNode, RelationshipType.withName("port_of"));
			numberOfCreatedRels++;

			// store the information of ip and port in map
			nodeInfoOfAnIP.get(ipAddress).portNodeIdOfPort.put(portNumber, portNode.getId());
		}

	}

}

class IPNodeInfo {
	Long ipNodeId;
	HashMap<Integer, Long> portNodeIdOfPort;

	public IPNodeInfo(Long ipNodeId) {
		this.ipNodeId = ipNodeId;
		portNodeIdOfPort = new HashMap<Integer, Long>();
	}
}
