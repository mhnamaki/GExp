package dataset.graphSampler;

import aqpeq.utilities.Dummy.DummyProperties;
import aqpeq.utilities.StringPoolUtility;
import com.google.common.collect.Sets;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

import java.io.*;
import java.util.*;

public class DemoSamplerFromFileMHN {

    // private static String graphInfraPath =
    // "/Users/zhangxin/Desktop/DBP/untitled folder/";
    private static String graphInfraPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbpSampled/";
    private static String dataset = "dbpSampled";
    final String verticesFileName = "Vertices.in";
    final String relationshipsFileName = "Relationships.in";
    // private static String keywords =
    // "Jessica;Chastain,Taylor;Swift,AnneHathaway";
//	ArrayList<String> keywords = new ArrayList<String>();
    private static String keywordPath = "entertainmentKeywords.txt";

    GraphInfraReaderArray graph;
    ArrayList<NodeInfra> nodeOfNodeId = new ArrayList<NodeInfra>();
    ArrayList<RelationshipInfra> relationOfRelId = new ArrayList<RelationshipInfra>();
    HashMap<Integer, HashSet<Integer>> nodeIdsOfToken = new HashMap<Integer, HashSet<Integer>>();
    HashSet<Integer> edgeSet = new HashSet<Integer>();
    HashSet<Integer> nodeSet = new HashSet<Integer>();
    HashMap<Integer, Integer> oldIdToNewId = new HashMap<Integer, Integer>();
    HashMap<Integer, Integer> oldIdToNewIdEdge = new HashMap<Integer, Integer>();
    static int numberOfNode = 40;

    private static boolean addNodesToTheMax = false;
    private static int maxNodeCapacity = 1000000;
    private static int maxEdgeCapacity = 5000000;

    // HashMap<Integer, String> nodeLine = new HashMap<Integer, String>();
    // HashMap<Integer, String> edgeLine = new HashMap<Integer, String>();

    public DemoSamplerFromFileMHN() {

    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-dataGraph")) {
                graphInfraPath = args[++i];
            } else if (args[i].equals("-dataset")) {
                dataset = args[++i];
            } else if (args[i].equals("-numberOfNode")) {
                numberOfNode = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-keywordPath")) {
                keywordPath = args[++i];
            } else if (args[i].equals("-addNodesToTheMax")) {
                addNodesToTheMax = Boolean.parseBoolean(args[++i]);
            } else if (args[i].equals("-maxNodeCapacity")) {
                maxNodeCapacity = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-maxEdgeCapacity")) {
                maxEdgeCapacity = Integer.parseInt(args[++i]);
            }

        }

        DummyProperties.withProperties = false;
        DummyProperties.readRelType = false;
        DummyProperties.debugMode = true;
        DemoSamplerFromFileMHN sampler = new DemoSamplerFromFileMHN();
        sampler.loadGraph();

    }

    public void loadGraph() throws Exception {
        boolean addBackward = false;
        graph = new GraphInfraReaderArray(graphInfraPath, addBackward);
        graph.read();
        nodeOfNodeId = graph.nodeOfNodeId;
        relationOfRelId = graph.relationOfRelId;
        nodeIdsOfToken = graph.indexInvertedListOfTokens(graph);

        String nodePath = graphInfraPath + "/vertices.in";
        ArrayList<String> nodeLine = readNode(nodePath);

        String edgePath = graphInfraPath + "/relationships.in";
        ArrayList<String> edgeLine = readEdge(edgePath);

        ArrayList<String> keywords = readKeywords(keywordPath);
        System.out.println("keyword size = " + keywords.size());

        sample(keywords);
        readAndWriteVertices(nodeLine);
        readAndWriteRelationships(edgeLine);

    }

    public void sample(ArrayList<String> keywords) throws Exception {

        System.out.println("sample");

        int cnt = 0;
        // first find all seed node ids to avoid repeated exploration for taylor and swift!
        HashSet<Integer> seedNodeIds = new HashSet<Integer>();
        for (String keyword : keywords) {
            cnt++;
            System.out.println("#" + cnt + ": " + keyword);
            if (keyword.contains(";")) {
                HashSet<Integer> nodeIds1 = new HashSet<Integer>();
                String[] kwTem = keyword.split(";");
                String kw1 = kwTem[0].toLowerCase().trim();
                String kw2 = kwTem[1].toLowerCase().trim();
                int kwID1 = -1;
                int kwID2 = -1;
                try {
                    kwID1 = StringPoolUtility.getIdOfStringFromPool(kw1);
                    kwID2 = StringPoolUtility.getIdOfStringFromPool(kw2);
                } catch (Exception exc) {
                    System.err.println(exc.getMessage());
                }

                if (kwID1 > -1 && kwID2 > -1) {
                    nodeIds1 = nodeIdsOfToken.get(kwID1);
                    HashSet<Integer> nodeIds2 = nodeIdsOfToken.get(kwID2);
                    nodeIds1.retainAll(nodeIds2);
                }

                if (nodeIds1 != null) {
                    seedNodeIds.addAll(nodeIds1);
                }
            } else {
                int tokenId = -1;
                try
                {
                    tokenId = StringPoolUtility.getIdOfStringFromPool(keyword);
                }
                catch (Exception exc){
                    System.err.println(exc.getMessage());
                }

                if(tokenId>-1) {
                    seedNodeIds.addAll(nodeIdsOfToken.get(tokenId));
                }
            }
        }

        nodeSet.addAll(seedNodeIds);

        System.out.println("seed nodes size:" + seedNodeIds.size());


        //for outgoing
        for (int seedNodeId : seedNodeIds) {
            //get outgoing nodes and rels for seed nodes
            for (int targetNodeId : nodeOfNodeId.get(seedNodeId).getOutgoingRelIdOfSourceNodeId().keySet()) {
                nodeSet.add(targetNodeId);
                edgeSet.add(relationOfRelId.get(nodeOfNodeId.get(seedNodeId).getOutgoingRelIdOfSourceNodeId().get(targetNodeId)).relId);
            }
        }

        System.out.println("node size after outgoing = " + nodeSet.size());
        System.out.println("edge size after outgoing = " + edgeSet.size());


        // for incoming:
        //check if for any node in the graph, it's next node is a seed node
        int progress = 0;
        for (NodeInfra potentialNode : nodeOfNodeId) {
            progress++;
            for (int potentialSeedId : nodeOfNodeId.get(potentialNode.nodeId).getOutgoingRelIdOfSourceNodeId().keySet()) {
                if (seedNodeIds.contains(potentialSeedId)) {
                    nodeSet.add(potentialNode.nodeId);
                    edgeSet.add(relationOfRelId.get(nodeOfNodeId.get(potentialNode.nodeId).getOutgoingRelIdOfSourceNodeId().get(potentialSeedId)).relId);
                }
            }
            if (progress % 100000 == 0) {
                System.out.println("progress for incoming: " + progress);
            }
        }

        System.out.println("node size after both out/in = " + nodeSet.size());
        System.out.println("edge size after both out/in = " + edgeSet.size());

        ///// while still budget (select a set of non-expanded nodes randomly and expand)


        if (addNodesToTheMax) {
            //seed nodes already expanded
            HashSet<Integer> expandedNodes = new HashSet<>();
            expandedNodes.addAll(seedNodeIds);

            //check to see once we consumed the budget
            boolean done = false;

            while (nodeSet.size() < maxNodeCapacity && edgeSet.size() < maxEdgeCapacity) {

                // select a random subset of not-expanded nodes
                Sets.SetView<Integer> notExpandedNodes = Sets.difference(nodeSet, expandedNodes);
                List<Integer> notExpandedItemsList = new ArrayList<>();
                notExpandedItemsList.addAll(notExpandedNodes);
                Collections.shuffle(notExpandedItemsList);
                int sampleSize = maxNodeCapacity - nodeSet.size();
                notExpandedItemsList = notExpandedItemsList.subList(0, Math.min(sampleSize, notExpandedItemsList.size()));
                expandedNodes.addAll(notExpandedItemsList);

                for (int newNodeId : notExpandedItemsList) {
                    for (int targetNodeId : nodeOfNodeId.get(newNodeId).getOutgoingRelIdOfSourceNodeId().keySet()) {
                        nodeSet.add(targetNodeId);
                        edgeSet.add(relationOfRelId.get(nodeOfNodeId.get(newNodeId).getOutgoingRelIdOfSourceNodeId().get(targetNodeId)).relId);

                        if (nodeSet.size() > maxNodeCapacity || edgeSet.size() > maxEdgeCapacity) {
                            done = true;
                            break;
                        }
                    }


                }

                if (done) {
                    break;
                }
                progress = 0;
                for (NodeInfra potentialNode : nodeOfNodeId) {
                    progress++;
                    for (int potentialSeedId : nodeOfNodeId.get(potentialNode.nodeId).getOutgoingRelIdOfSourceNodeId().keySet()) {
                        if (expandedNodes.contains(potentialSeedId)) {
                            nodeSet.add(potentialNode.nodeId);
                            edgeSet.add(relationOfRelId.get(nodeOfNodeId.get(potentialNode.nodeId).getOutgoingRelIdOfSourceNodeId().get(potentialSeedId)).relId);

                            if (nodeSet.size() > maxNodeCapacity || edgeSet.size() > maxEdgeCapacity) {
                                done = true;
                                break;
                            }
                        }
                    }
                    if (done) {
                        break;
                    }
                    if (progress % 100000 == 0) {
                        System.out.println("progress for incoming: " + progress);
                    }
                }
                if (done) {
                    break;
                }
            }
        }


    }

    private void readAndWriteRelationships(ArrayList<String> edgeLine) throws Exception {

        System.out.println("write edge");

        Writer bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(dataset + relationshipsFileName), "UTF-8"));

        int relIdCounter = 0;
        for (int relId : edgeSet) {
            bw.write(relIdCounter + "#" + oldIdToNewId.get(relationOfRelId.get(relId).sourceId) + "#" + oldIdToNewId.get(relationOfRelId.get(relId).destId) + "#"
                    + edgeLine.get(relId) + "\n");

            relIdCounter++;
        }

        bw.close();

    }

    private void readAndWriteVertices(ArrayList<String> nodeLine) throws Exception {
        System.out.println("write node");
        Writer bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(dataset + verticesFileName), "UTF-8"));
        int nodeIdcounter = 0;
        for (int nodeId : nodeSet) {
            bw.write(nodeIdcounter + "#1#1#" + nodeLine.get(nodeId) + "\n");
            oldIdToNewId.put(nodeId, nodeIdcounter);
            nodeIdcounter++;
        }
        bw.close();
    }

    public ArrayList<String> readNode(String filePath) {

        System.out.println("generate node map");

        ArrayList<String> list = new ArrayList<String>();

        File file = new File(filePath);
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(new FileInputStream(file));

            BufferedReader br = new BufferedReader(reader);
            String line = "";
            while ((line = br.readLine()) != null) {

                int index = line.indexOf("#");
                int nodeId = Integer.parseInt(line.substring(0, index));
                line = line.substring(index + 1);
                int indexI = line.indexOf("#");
                line = line.substring(indexI + 1);
                int indexO = line.indexOf("#");
                line = line.substring(indexO + 1);
                int indexP = line.indexOf("#");
                line = line.substring(0, indexP);
                list.add(line);
            }

            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;

    }

    public ArrayList<String> readEdge(String filePath) {

        System.out.println("generate edge map");

        ArrayList<String> list = new ArrayList<String>();

        File file = new File(filePath);
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(new FileInputStream(file));

            BufferedReader br = new BufferedReader(reader);
            String line = "";
            while ((line = br.readLine()) != null) {

                int index = line.indexOf("#");
                int nodeId = Integer.parseInt(line.substring(0, index));
                line = line.substring(index + 1);
                int indexSr = line.indexOf("#");
                line = line.substring(indexSr + 1);
                int indexDes = line.indexOf("#");
                line = line.substring(indexDes + 1);

                list.add(line);
            }

            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;

    }

    public ArrayList<String> readKeywords(String filePath) {

        System.out.println("Read keywords.");

        ArrayList<String> list = new ArrayList<String>();

        File file = new File(filePath);
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(new FileInputStream(file));

            BufferedReader br = new BufferedReader(reader);
            String line = "";
            while ((line = br.readLine()) != null) {
                list.add(line.toLowerCase().trim());
            }

            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;

    }

}
