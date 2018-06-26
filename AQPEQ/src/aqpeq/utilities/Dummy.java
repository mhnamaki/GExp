package aqpeq.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;

public class Dummy {
    public static class DummyProperties {
        public static boolean softwareMode = false;
        public static boolean visualize = false;
        public static boolean debugMode = false;
        public static boolean incMode = false;
        public static boolean addBackward = true;

        public static String DELIMETERS = "!*^/ ,;:_-[]()&$#@=+'`~.<>{}|\"";
        public static int NUMBER_OF_SNAPSHOTS = 1;
        public static int NUMBER_OF_ALL_FOCUS_NODES = 0;
        public static String SEPARATOR_LABEL_AND_RELTYPE = "#";
        public static boolean bigDataTestMode = false;

        public static double supportThreshold = 0.0d;
        public static double confidenceThreshold = 0.0d;
        public static boolean considerCoOcc = true;

        public static boolean hasOptimization = true;

        public static boolean withProperties = true;

        public static boolean readProperties = true;
        public static boolean readRelType = false;

        // WHEN THIS IS TRUE, other detailed time are not correct, only the
        // final time is correct
        // public static boolean qualityVsTime = false;
        public static int qualitySaveIntervalInMilliSeconds = 1000;

        public static enum Direction {
            INCOMING, OUTGOING
        }

        public static enum SteinerTreeOperation {
            KEYWRODMATCH, GROWTH, MERGE
        }

        public static enum KWSSetting {
            DISTINCTROOT, SUBGRAPH, STEINER
        }

        public static HashSet<Integer> stopwordsSet;

        public static int MaxNumberOfVisitedNodes = 500000;
        public static int MaxFrequencyBoundForKeywordSelection = 50;
        public static int MAXIndexConsultant = 1000;

    }

    public static class DummyFunctions {

        public static void fillStopWord() {
            Dummy.DummyProperties.stopwordsSet = new HashSet<Integer>();
            FileInputStream file;
            try {
                file = new FileInputStream("stopwords.in");

                BufferedReader br = new BufferedReader(new InputStreamReader(file));
                String line = "";
                while ((line = br.readLine()) != null) {
                    Dummy.DummyProperties.stopwordsSet.add(StringPoolUtility.insertIntoStringPool(line.trim()));
                }
                br.close();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public static HashSet<Integer> getStopwordsSet() {
            if (Dummy.DummyProperties.stopwordsSet == null) {
                fillStopWord();
            }
            return DummyProperties.stopwordsSet;
        }

        public static float log(int x, int base) {
            return (float) ((double) Math.log(x) / (double) Math.log(base));
        }

        public static ArrayList<Integer> getSortedUniqueRandomNumbers(int maximumNumber, int size) {
            ArrayList<Integer> wholeList = new ArrayList<Integer>();
            ArrayList<Integer> output = new ArrayList<Integer>();
            for (int i = 0; i < maximumNumber; i++) {
                wholeList.add(new Integer(i));
            }

            Collections.shuffle(wholeList);
            for (int i = 0; i < size; i++) {
                output.add(wholeList.get(i));
            }

            Collections.sort(output);
            return output;
        }

        public static void sleepAndWakeUp(int milisecondsOfSleep) throws Exception {
            System.out.println("sleeping..." + new Date());
            System.gc();
            System.runFinalization();
            Thread.sleep(milisecondsOfSleep);
            System.gc();
            System.runFinalization();
            Thread.sleep(milisecondsOfSleep);
            System.out.println("waking up..." + new Date());
        }

        public static boolean deleteCompletely(Path rootPath) {
            try {
                Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        // System.out.println("delete file: " +
                        // file.toString());
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        // System.out.println("delete dir: " + dir.toString());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        public static int getNumberOfAllNodes(GraphDatabaseService dataGraph) {
            int numberOfAllNodes = 0;
            for (Node node : dataGraph.getAllNodes()) {
                numberOfAllNodes++;
            }
            return numberOfAllNodes;
        }

        public static int getNumberOfAllRels(GraphDatabaseService dataGraph) {
            int numberOfAllRelationships = 0;
            for (Relationship rel : dataGraph.getAllRelationships()) {
                numberOfAllRelationships++;
            }
            return numberOfAllRelationships;
        }

        public static HashSet<String> getDifferentLabels(GraphDatabaseService dataGraph) {
            HashSet<String> differentLabels = new HashSet<String>();
            for (Label label : dataGraph.getAllLabels()) {
                differentLabels.add(label.name());
            }
            return differentLabels;
        }

        public static double getAvgOutDegreesOfASet(GraphInfraReaderArray dataGraph, Collection<Integer> nodeIds) {
            double allDegrees = 0.0d;
            double avgDegrees = 0.0d;
            for (Integer nodeId : nodeIds) {
                NodeInfra node = dataGraph.nodeOfNodeId.get(nodeId);
                allDegrees += node.getOutDegree();
            }
            avgDegrees = (double) allDegrees / (double) nodeIds.size();
            return avgDegrees;
        }

        public static double getAvgInDegrees(GraphDatabaseService dataGraph) {
            double allDegrees = 0.0d;
            double avgDegrees = 0.0d;
            int numberOfAllNodes = 0;
            for (Node node : dataGraph.getAllNodes()) {

                // if(numberOfAllNodes==7){
                // System.out.println(numberOfAllNodes);
                allDegrees += node.getDegree(Direction.INCOMING);
                numberOfAllNodes++;
            }
            avgDegrees = allDegrees / numberOfAllNodes;
            return avgDegrees;
        }

        public static double getAvgDegrees(GraphDatabaseService dataGraph) {
            double allDegrees = 0.0d;
            double avgDegrees = 0.0d;
            int numberOfAllNodes = 0;
            for (Node node : dataGraph.getAllNodes()) {

                // if(numberOfAllNodes==7){
                // System.out.println(numberOfAllNodes);
                allDegrees += node.getDegree(Direction.BOTH);
                numberOfAllNodes++;
            }
            avgDegrees = allDegrees / numberOfAllNodes;
            return avgDegrees;
        }

        public static double getAvgDegreeOfFocusNodes(GraphDatabaseService dataGraph, HashSet<Integer> allFocusNodes,
                                                      int numberOfAllFocusNodes) throws Exception {
            double avgOutDegreeOfFocusNodes = 0;
            for (Integer nodeId : allFocusNodes) {
                avgOutDegreeOfFocusNodes += dataGraph.getNodeById(nodeId).getDegree();
            }

            if (avgOutDegreeOfFocusNodes == 0) {
                System.err.println("avgDegreeOfFocusNodes is zero!");
                return 0;
            }

            return avgOutDegreeOfFocusNodes / numberOfAllFocusNodes;
        }

        public static double getAverageOfDoubleArray(ArrayList<Double> arrayOfDoubles) {
            if (arrayOfDoubles.size() == 0) {
                return 0;
            }

            double sum = 0;
            for (double val : arrayOfDoubles) {
                sum += val;
            }
            return sum / (arrayOfDoubles.size());
        }

        public static double getTotalSumOfDoubleArray(ArrayList<Double> arrayOfDoubles) {
            double sum = 0;
            for (double val : arrayOfDoubles) {
                sum += val;
            }
            return sum;
        }

        public static void registerShutdownHook(final GraphDatabaseService dataGraph) {
            // Registers a shutdown hook for the Neo4j instance so that it
            // shuts down nicely when the VM exits (even if you "Ctrl-C" the
            // running application).
            // Runtime.getRuntime().addShutdownHook(new Thread() {
            // @Override
            // public void run() {
            // dataGraph.shutdown();
            // }
            // });

        }

        public static HashSet<String> getDifferentRelType(GraphDatabaseService dataGraph) {

            HashSet<String> relTypes = new HashSet<String>();
            for (RelationshipType relType : dataGraph.getAllRelationshipTypes()) {
                relTypes.add(relType.name());
            }
            return relTypes;
        }

        public static File[] getFilesInTheDirfinder(String dirName, String ext) {
            File dir = new File(dirName);

            File[] files = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(ext);
                }
            });

            if (files != null && files.length > 1)
                Arrays.sort(files);

            for (int i = 0; i < files.length; i++) {
                System.out.println("catched file " + i + "; " + files[i].getName());
            }
            return files;

        }

        public static boolean isContain(String source, String subItem) {
            // String pattern = "\\b" + subItem + "\\b";
            // Pattern p = Pattern.compile(pattern);
            // Matcher m = p.matcher(source);
            // return m.find();
            StringTokenizer st = new StringTokenizer(source);
            while (st.hasMoreTokens()) {
                if (st.nextToken().equals(subItem)) {
                    return true;
                }
            }
            return false;
        }

        public static String getCleanedString(String initStr) {
            return initStr.toLowerCase().trim();
        }

        public static ArrayList<String> getTokens(String expression) {
            ArrayList<String> tokens = new ArrayList<String>();
            StringTokenizer stringTokenizer = new StringTokenizer(expression);
            while (stringTokenizer.hasMoreTokens()) {
                tokens.add(stringTokenizer.nextToken());
            }
            return tokens;
        }

        public static ArrayList<String> getTokens(Label lbl, String delimiters) {
            String lblStr = lbl.name().toString().toLowerCase();
            ArrayList<String> tokens = new ArrayList<String>();
            StringTokenizer stringTokenizer = new StringTokenizer(lblStr, delimiters);
            while (stringTokenizer.hasMoreTokens()) {
                tokens.add(stringTokenizer.nextToken());
            }
            return tokens;
        }

        public static double normalizedDistance(String a, String b) {
            return 1 - (float) distance(a, b) / (Math.max(a.length(), b.length()));
        }

        public static int distance(String a, String b) {
            a = a.toLowerCase();
            b = b.toLowerCase();
            // i == 0
            int[] costs = new int[b.length() + 1];
            for (int j = 0; j < costs.length; j++)
                costs[j] = j;
            for (int i = 1; i <= a.length(); i++) {
                // j == 0; nw = lev(i - 1, j)
                costs[0] = i;
                int nw = i - 1;
                for (int j = 1; j <= b.length(); j++) {
                    int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                            a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                    nw = costs[j];
                    costs[j] = cj;
                }
            }
            return costs[b.length()];
        }

        public static RelationshipInfra getRelationshipOfPairNodes(GraphInfraReaderArray graph, int srcNodeId,
                                                                   int destNodeId) {

            // checking if src->dest
            HashMap<Integer, Integer> outgoingRelIdsOfSourceNodeId = graph.nodeOfNodeId.get(srcNodeId)
                    .getOutgoingRelIdOfSourceNodeId();

            if (outgoingRelIdsOfSourceNodeId.containsKey(destNodeId)) {

                // TODO: return multi-relationships
                int relId = outgoingRelIdsOfSourceNodeId.get(destNodeId);
                return graph.relationOfRelId.get(relId);

            }

            // checking if dest->src
            // HashMap<Integer, Integer> incomingRelIdsOfTargetNodeId =
            // graph.nodeOfNodeId.get(srcNodeId)
            // .getIncomingRelIdOfTargetNodeId();
            // if (incomingRelIdsOfTargetNodeId.containsKey(destNodeId)) {
            //
            // // TODO: return multi-relationships
            // int relId = incomingRelIdsOfTargetNodeId.get(destNodeId);
            // return graph.relationOfRelId.get(relId);
            // }

            return null;
        }

        public static String copyDataSet(String dataGraphPath, int s) throws Exception {

            String destDirStr = "";
            if (s == 0) {
                // File srcDir = new File(dataGraphPath + graphName);
                File srcDir = new File(dataGraphPath);
                destDirStr = dataGraphPath + s;
                File destDir = new File(destDirStr);

                FileUtils.copyDirectory(srcDir, destDir);

            } else {
                int l = s - 1;
                // File srcDir = new File(dataGraphPath + graphName + l);
                File srcDir = new File(dataGraphPath);
                destDirStr = dataGraphPath + s;
                File destDir = new File(destDirStr);

                FileUtils.copyDirectory(srcDir, destDir);
            }

            return destDirStr;

        }

        public static <T> boolean areTwoSetsEqual(HashSet<T> set1, HashSet<T> set2) {
            if (set1.size() != set2.size())
                return false;
            if (!set1.containsAll(set2)) {
                return false;
            }
            return true;

        }

        public static String getKeyForIDAndHashSet(int nodeId, HashSet<String> p) {
            String result = nodeId + " ";
            String[] sortedRelatedKeywords = new String[p.size()];
            Iterator<String> itr = p.iterator();
            for (int i = 0; i < p.size(); i++) {
                sortedRelatedKeywords[i] = itr.next();
            }
            Arrays.sort(sortedRelatedKeywords);
            result += String.join(",", sortedRelatedKeywords);

            return result;
        }

        public static HashSet<Integer> getTokensOfALabel(String lbl) throws Exception {
            HashSet<Integer> results = new HashSet<Integer>();

            lbl = DummyFunctions.getCleanedString(lbl).toLowerCase();

            String newToken = "";
            if (lbl.contains("uri_")) {
                String[] tem = lbl.trim().split("_");
                for (int i = 1; i < tem.length; i++) {
                    newToken = newToken.trim() + " " + tem[i];
                }
            } else {
                newToken = lbl;
            }

            String[] splittedTokens = newToken.split(" ");
            for (String s : splittedTokens) {
                if (s.trim().length() > 0) {
                    // if it's not in stop words;
                    if (StringPoolUtility.tokenExistsInPool(s)
                            && DummyFunctions.getStopwordsSet().contains(StringPoolUtility.getIdOfStringFromPool(s))) {
                        continue;
                    }
                    results.add(StringPoolUtility.insertIntoStringPool(s));
                }

            }

            return results;

        }

        public static HashSet<Integer> getKeywords(GraphInfraReaderArray graph, int nodeId) throws Exception {
            // ArrayList<Integer> keywordsOfNodeId = new ArrayList<Integer>();

            NodeInfra node = graph.nodeOfNodeId.get(nodeId);
            if (!DummyProperties.withProperties || node.getProperties() == null) {
                return node.tokens;
            } else {
                int cap = (node.tokens != null ? node.tokens.size() : 0)+ (node.getProperties() != null ? node.getProperties().size() : 0);
                HashSet<Integer> results = new HashSet<>(cap);
                if (node.tokens != null) {
                    results.addAll(node.tokens);
                }
                if (node.getProperties() != null) {
                    results.addAll(node.getProperties());
                }
                return results;
            }
        }

        public static <T> String getStringOutOfCollection(Collection<T> collection, String delimiter) {
            String result = "";
            for (Object obj : collection) {
                result += obj.toString() + delimiter + " ";
            }
            return result;
        }

        public static double[] getArrOutOfCSV(double[] doubleArr, String string) {
            String[] strArray = string.split(",");
            doubleArr = new double[strArray.length];
            for (int i = 0; i < strArray.length; i++) {
                doubleArr[i] = Double.parseDouble(strArray[i]);
            }
            return doubleArr;
        }

        public static int[] getArrOutOfCSV(int[] intArr, String string) {
            String[] strArray = string.split(",");
            intArr = new int[strArray.length];
            for (int i = 0; i < strArray.length; i++) {
                intArr[i] = Integer.parseInt(strArray[i]);
            }
            return intArr;
        }

    }

}
