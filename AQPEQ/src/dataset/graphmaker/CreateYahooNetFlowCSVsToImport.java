package dataset.graphmaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import aqpeq.utilities.Dummy.DummyFunctions;

public class CreateYahooNetFlowCSVsToImport {

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
		CreateYahooNetFlowCSVsToImport yahooNetFlow = new CreateYahooNetFlowCSVsToImport();
		yahooNetFlow.removoeDuplicateLabels();
		// yahooNetFlow.run(args);
	}

	private void removoeDuplicateLabels() {
		File storeDir = new File(
				"/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/yahooNetFlow/yahooNetFlowDB3_2_1");
		GraphDatabaseService dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "8g")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();

		Transaction tx1 = dataGraph.beginTx();

		// HashSet<String> labels = new HashSet<String>();
		int cnt1 = 0;
		int cnt2 = 0;
		for (Node node : dataGraph.getAllNodes()) {
			if (node.hasLabel(Label.label("Port "))) {
				node.removeLabel(Label.label("Port "));
				cnt1++;

			}
			if (node.hasLabel(Label.label(""))) {
				node.removeLabel(Label.label(""));
				cnt2++;
			}

			if (cnt1 % 10000 == 0 || cnt2 % 10000 == 0) {
				tx1.success();
				tx1.close();
				tx1 = dataGraph.beginTx();
			}
		}

		System.out.println("cnt1: " + cnt1);
		System.out.println("cnt2: " + cnt2);

		tx1.success();
		tx1.close();

		dataGraph.shutdown();
	}

	int numberOfCreatedNodes = 0;
	int numberOfCreatedRels = 0;
	private String netFlowFilesDir = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/yahooNetFlow/yahooNetFlowRawFiles/";
	private String baseDirectory = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/yahooNetFlow/yahooNetFlowCSVs/";
	private String neo4jImportDir = "/Users/mnamaki/Documents/Education/PhD/Summer2017/neo4j-community-3.2.1/bin/";
	private String newDatasetPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/yahooNetFlow/yahooNetFlowDB_3_2_1";

	private void run(String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-netFlowFilesDir")) {
				netFlowFilesDir = args[++i];
			} else if (args[i].equals("-neo4jImportDir")) {
				neo4jImportDir = args[++i];
			} else if (args[i].equals("-newDatasetPath")) {
				newDatasetPath = args[++i];
			}
		}

		File foutIP = new File(baseDirectory + "IP.csv");
		FileOutputStream fosIP = new FileOutputStream(foutIP);
		BufferedWriter bwIP = new BufferedWriter(new OutputStreamWriter(fosIP));
		bwIP.write(":ID,:Label\n");

		File foutPort = new File(baseDirectory + "Port.csv");
		FileOutputStream fosPort = new FileOutputStream(foutPort);
		BufferedWriter bwPort = new BufferedWriter(new OutputStreamWriter(fosPort));
		bwPort.write(":ID,number,:Label\n");

		File foutIPPort = new File(baseDirectory + "IP_Port.csv");
		FileOutputStream fosIPPort = new FileOutputStream(foutIPPort);
		BufferedWriter bwIPPort = new BufferedWriter(new OutputStreamWriter(fosIPPort));
		bwIPPort.write(":START_ID,:END_ID\n");

		File foutPortPort = new File(baseDirectory + "Port_Port.csv");
		FileOutputStream fosPortPort = new FileOutputStream(foutPortPort);
		BufferedWriter bwPortPort = new BufferedWriter(new OutputStreamWriter(fosPortPort));
		// Start End Sif SrcIPaddress SrcP DIf DstIPaddress DstP P Fl Pkts
		// Octets
		bwPortPort.write(":START_ID,Start,End,Sif,SrcIPaddress,SrcP,DIf,DstIPaddress,DstP,P,Fl,Pkts,Octets,:END_ID\n");

		HashSet<String> seenIPs = new HashSet<String>();
		HashSet<String> seenPorts = new HashSet<String>();

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

				String ip_portOfSrc = handleIPAndCorrespondingPortNodes(seenIPs, seenPorts, bwIP, bwPort, bwIPPort,
						srcIPAddress, srcPortNumber);

				String destIPAddress = splittedValues[DstIPaddressIndex];
				Integer destPortNumber = Integer.parseInt(splittedValues[DstPIndex]);

				String ip_portOfDest = handleIPAndCorrespondingPortNodes(seenIPs, seenPorts, bwIP, bwPort, bwIPPort,
						destIPAddress, destPortNumber);

				if (splittedValues.length == splittedAttributes.length) {

					String rel = ip_portOfSrc + ",";
					// ":START_ID,Start,End,Sif,SrcIPaddress,SrcP,DIf,DstIPaddress,DstP,P,Fl,Pkts,Octets,:END_ID\n"
					for (int s = 0; s < splittedAttributes.length; s++) {
						if (s == 8) {
							String protocol = protocolOfIndex.get(Integer.parseInt(splittedValues[s]));
							if (protocol == null) {
								rel += splittedValues[s] + ",";
							} else {
								rel += protocol + ",";
							}
						} else {
							rel += splittedValues[s] + ",";
						}
					}
					rel += ip_portOfDest;
					bwPortPort.write(rel + "\n");
					numberOfCreatedRels++;
				}

				if (cnt % 1000000 == 0) {

					System.out.println("cnt: " + cnt);
					System.out.println("numberOfCreatedNodes: " + numberOfCreatedNodes);
					System.out.println("numberOfCreatedRels: " + numberOfCreatedRels);
					System.out.println();
				}
			}
			br.close();
		}

		bwIP.close();
		bwPort.close();
		bwIPPort.close();
		bwPortPort.close();

		// Neo4j Import
		System.out.println("CSV files read; Building the Neo4j database");
		String command = neo4jImportDir + "neo4j-import --id-type string --into " + newDatasetPath + " --nodes:IP "
				+ baseDirectory + "IP.csv" + " --nodes:Port " + baseDirectory + "Port.csv" + " --relationships:PortOf "
				+ baseDirectory + "IP_Port.csv" + " --relationships:CommunicateOf " + baseDirectory + "Port_Port.csv";

		System.out.println(command);

		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
		int exitVal = p.exitValue();

		System.out.println("proc.exitValue(): " + exitVal);

		if (exitVal == 0)
			System.out.println("program is finished properly!");
		else {
			String line;
			System.out.println("ERROR: Neo4j messed up");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			in.close();
		}

		// dataGraph.shutdown();
		
	
		
	}

	private String handleIPAndCorrespondingPortNodes(HashSet<String> seenIPs, HashSet<String> seenPorts,
			BufferedWriter bwIP, BufferedWriter bwPort, BufferedWriter bwIPPort, String ipAddress, Integer portNumber)
			throws Exception {

		if (!seenIPs.contains(ipAddress)) {
			String labels = "";

			// create an ip node
			numberOfCreatedNodes++;
			labels += "IP;";
			labels += ipAddress + ";";
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
				labels += newLabel + ";";
			}
			bwIP.write(ipAddress + "," + labels + "\n");
			seenIPs.add(ipAddress);
		}

		String ipAndItsPort = ipAddress + "_" + portNumber;
		if (!seenPorts.contains(ipAndItsPort)) {
			// create a port node for ip
			numberOfCreatedNodes++;
			// ":ID,number,:Label\n"
			bwPort.write(ipAndItsPort + "," + portNumber + ",Port \n");
			seenPorts.add(ipAndItsPort);

			// create a rel between ip and port
			// ":START_ID,:END_ID
			bwIPPort.write(ipAddress + "," + ipAndItsPort + "\n");
			numberOfCreatedRels++;
		}
		return ipAndItsPort;
	}

}
