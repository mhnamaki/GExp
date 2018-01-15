package dataset.statistic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

public class DatasetStatisticGrabber {

	// getting a dataset path and printing the ....
	public static void main(String[] args) throws IOException {
		String dataGraphPath = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/dbp_3_2_1";
		// String dataGraphPath = "/Users/zhangxin/Desktop/Summer/dbp_3_2_1";
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dataGraph")) {
				dataGraphPath = args[++i];
			}
		}

		File storeDir = new File(dataGraphPath);
		GraphDatabaseService dataGraph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir)
				.setConfig(GraphDatabaseSettings.pagecache_memory, "8g")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();

		System.out.println("dataset: " + dataGraphPath);

		File readme = new File(dataGraphPath + "/README.txt");
		FileWriter readmeWriter = new FileWriter(readme);

		int allNodesCnt = 0;
		// number of nodes
		try (Transaction tx1 = dataGraph.beginTx()) {
			allNodesCnt = 0;
			for (Node node : dataGraph.getAllNodes()) {
				allNodesCnt++;
			}

			readmeWriter.write("number of nodes: " + allNodesCnt + "\n");

			tx1.success();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// number of relationships
		try (Transaction tx1 = dataGraph.beginTx()) {
			int allEdgesCnt = 0;
			for (Relationship edge : dataGraph.getAllRelationships()) {
				allEdgesCnt++;
			}

			readmeWriter.write("number of relationships: " + allEdgesCnt + "\n");
			readmeWriter.flush();
			tx1.success();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// try (Transaction tx1 = dataGraph.beginTx()) {
		//
		// long maxNodeId = 0;
		// for (Node node : dataGraph.getAllNodes()) {
		// maxNodeId = Math.max(maxNodeId, node.getId());
		// }
		// System.out.println("maxNodeId: " + maxNodeId);
		//
		// tx1.success();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// number of RelatinshipType
		try (Transaction tx1 = dataGraph.beginTx()) {

			HashMap<String, Integer> relTypeFreq = new HashMap<>();

			for (Relationship rel : dataGraph.getAllRelationships()) {

				if (relTypeFreq.containsKey(rel.getType().name())) {
					relTypeFreq.put(rel.getType().name(), relTypeFreq.get(rel.getType().name()) + 1);
				} else {
					relTypeFreq.put(rel.getType().name(), 1);
				}
			}

			readmeWriter.write("Distinct relationship types: " + relTypeFreq.keySet().size() + "\n");

			for (String s : relTypeFreq.keySet()) {
				readmeWriter.write(s + " : " + relTypeFreq.get(s) + "\n");
			}

			tx1.success();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// max degree
		try (Transaction tx1 = dataGraph.beginTx()) {
			int maxDegree = 0;
			for (Node node : dataGraph.getAllNodes()) {

				if (node.getDegree() > maxDegree) {
					maxDegree = node.getDegree();
				}
			}

			readmeWriter.write("max degree: " + maxDegree + "\n");
			readmeWriter.flush();
			tx1.success();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// avg degree
		try (Transaction tx1 = dataGraph.beginTx()) {
			int sumDegree = 0;
			allNodesCnt = 0;
			for (Node node : dataGraph.getAllNodes()) {
				sumDegree += node.getDegree();
				allNodesCnt++;
			}

			readmeWriter.write("avg degree: " + (sumDegree / allNodesCnt) + "\n");
			readmeWriter.flush();
			tx1.success();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try (Transaction tx1 = dataGraph.beginTx()) {
			HashMap<String, HashSet<String>> propertyValuesOfAType = new HashMap<String, HashSet<String>>();
			for (Node node : dataGraph.getAllNodes()) {
				String lbl = node.getLabels().iterator().next().name().toString();
				propertyValuesOfAType.putIfAbsent(lbl, new HashSet<String>());

				for (String key : node.getPropertyKeys()) {
					propertyValuesOfAType.get(lbl).add(key);
				}
			}

			for (String lbl : propertyValuesOfAType.keySet()) {
				System.out.print(lbl + "");
				for (String key : propertyValuesOfAType.get(lbl)) {
					System.out.print(key + ",");
				}
				System.out.println();
			}
			tx1.success();

		} catch (Exception e) {

		}

		// all distinct labels into a file alphabetically ordered and their
		// frequency
		try (Transaction tx1 = dataGraph.beginTx()) {
			TreeMap<String, Integer> distinctLabelsMap = new TreeMap<String, Integer>();
			for (Node node : dataGraph.getAllNodes()) {
				for (Label label : node.getLabels()) {
					if (distinctLabelsMap.containsKey(label.toString())) {
						distinctLabelsMap.put(label.toString(), distinctLabelsMap.get(label.toString()) + 1);
					} else {
						distinctLabelsMap.put(label.toString(), 1);
					}
				}
			}

			readmeWriter.write("the number of distinct labels: " + distinctLabelsMap.size() + "\n");
			for (String label : distinctLabelsMap.navigableKeySet()) {
				readmeWriter.write(label + " : " + distinctLabelsMap.get(label) + "\n");
			}
			readmeWriter.flush();
			tx1.success();
		} catch (Exception e) {
			// TODO: handle exception
		}

		// all distinct NodeType->RelType->NodeType
		try (Transaction tx1 = dataGraph.beginTx()) {
			HashSet<String> distinctEdgeTypeLabelType = new HashSet<String>();
			for (Relationship rel : dataGraph.getAllRelationships()) {
				distinctEdgeTypeLabelType.add(rel.getStartNode().getLabels().iterator().next().name() + "-"
						+ rel.getType() + "->" + rel.getEndNode().getLabels().iterator().next().name());
			}
			readmeWriter.write("the number of distinct triple type: " + distinctEdgeTypeLabelType.size() + "\n");
			for (String t : distinctEdgeTypeLabelType) {
				readmeWriter.write(t + "\n");
			}
			readmeWriter.flush();
			tx1.success();
		} catch (Exception e) {
			// TODO: handle exception
		}

		// all distinct property keys and their frequency in alphabetical order
		try (Transaction tx1 = dataGraph.beginTx()) {
			TreeMap<String, Integer> distinctPropertiesKeysMap = new TreeMap<String, Integer>();
			for (Node node : dataGraph.getAllNodes()) {
				Map<String, Object> propertyMap = node.getAllProperties();
				for (String key : propertyMap.keySet()) {
					if (distinctPropertiesKeysMap.containsKey(key)) {
						distinctPropertiesKeysMap.put(key, distinctPropertiesKeysMap.get(key) + 1);
					} else {
						distinctPropertiesKeysMap.put(key, 1);
					}
				}
			}

			readmeWriter.write("the number of distinct property keys: " + distinctPropertiesKeysMap.size() + "\n");
			for (String key : distinctPropertiesKeysMap.navigableKeySet()) {
				readmeWriter.write(key + " : " + distinctPropertiesKeysMap.get(key) + "\n");
			}
			readmeWriter.flush();
			tx1.success();
		} catch (Exception e) {
			// TODO: handle exception
		}

		// node's labels frequency
		try (Transaction tx1 = dataGraph.beginTx()) {
			TreeMap<Integer, Integer> nodeNumberOfLabelsMap = new TreeMap<Integer, Integer>();
			for (Node node : dataGraph.getAllNodes()) {
				HashSet<String> distinctLabels = new HashSet<String>();
				for (Label label : node.getLabels()) {
					distinctLabels.add(label.toString());
				}
				if (nodeNumberOfLabelsMap.containsKey(distinctLabels.size())) {
					nodeNumberOfLabelsMap.put(distinctLabels.size(),
							nodeNumberOfLabelsMap.get(distinctLabels.size()) + 1);
				} else {
					nodeNumberOfLabelsMap.put(distinctLabels.size(), 1);
				}

			}

			for (Integer key : nodeNumberOfLabelsMap.navigableKeySet()) {
				readmeWriter.write(key + " : " + nodeNumberOfLabelsMap.get(key) + "\n");
			}
			readmeWriter.flush();
			tx1.success();
		} catch (Exception e) {
			// TODO: handle exception
		}

		// degree histogram
		try (Transaction tx1 = dataGraph.beginTx()) {
			TreeMap<Integer, Integer> degreeNodeCountMap = new TreeMap<Integer, Integer>();

			for (Node node : dataGraph.getAllNodes()) {
				if (degreeNodeCountMap.containsKey(node.getDegree())) {
					degreeNodeCountMap.put(node.getDegree(), degreeNodeCountMap.get(node.getDegree()) + 1);
				} else {
					degreeNodeCountMap.put(node.getDegree(), 1);
				}
			}

			readmeWriter.write("degree histogram" + "\n");
			for (Integer degree : degreeNodeCountMap.navigableKeySet()) {
				readmeWriter.write(degree + " : " + degreeNodeCountMap.get(degree) + "\n");
			}

			tx1.success();
		} catch (Exception e) {
			// TODO: handle exception
		}

		// System.out.println("grouped label started");
		// try (Transaction tx1 = dataGraph.beginTx()) {
		//
		// HashSet<String> groupedLabels = new HashSet<String>();
		// for (Node node : dataGraph.getAllNodes()) {
		// ArrayList<String> groupedLblsArr = new ArrayList<String>();
		// for (Label lbl : node.getLabels()) {
		// groupedLblsArr.add(lbl.name().toString());
		// }
		// Collections.sort(groupedLblsArr);
		// groupedLabels.add(String.join(",", groupedLblsArr));
		// }
		//
		// readmeWriter.write("grouped labels" + "\n");
		// for (String str : groupedLabels) {
		// readmeWriter.write(str + "\n");
		// }
		//
		// tx1.success();
		// } catch (Exception e) {
		// // TODO: handle exception
		// }
		try (Transaction tx1 = dataGraph.beginTx()) {

			HashSet<String> distinctPropKeys = new HashSet<String>();
			for (Relationship rel : dataGraph.getAllRelationships()) {
				Map<String, Object> props = rel.getAllProperties();
				for (String key : props.keySet()) {
					distinctPropKeys.add(key);
				}
			}

			readmeWriter.write("Distinct Relationships Prop Keys" + "\n");
			for (String s : distinctPropKeys) {
				readmeWriter.write(s + "\n");
			}
			tx1.success();
		} catch (Exception e) {
			// TODO: handle exception
		}
		readmeWriter.write("\n");

		try (Transaction tx1 = dataGraph.beginTx()) {
			HashMap<String, Integer> map = new HashMap<String, Integer>();

			for (Relationship rel : dataGraph.getAllRelationships()) {
				String key = rel.getStartNode().getId() + "_" + rel.getEndNode().getId();
				if (map.containsKey(key)) {
					map.put(key, map.get(key) + 1);
				} else {
					map.put(key, 1);
				}

			}

			HashSet<Integer> freq = new HashSet<Integer>();
			for (String label : map.keySet()) {
				freq.add(map.get(label));
			}
			ArrayList<Integer> freqArr = new ArrayList<Integer>();
			freqArr.addAll(freq);
			Collections.sort(freqArr);

			readmeWriter.write("Top 10 number of multiple relationships" + "\n");
			for (int i = (freqArr.size() - 1); i >= Math.max(0, freqArr.size() - 10); i--) {
				readmeWriter.write(freqArr.get(i) + "\n");
			}

			readmeWriter.flush();

			tx1.success();
		} catch (Exception e) {
			// TODO: handle exception
		}

		// distinct property values for all of the nodes
		// readmeWriter.write();
		// try (Transaction tx1 = dataGraph.beginTx()) {
		// HashMap<String, HashSet<String>> propKeyMap = new HashMap<String,
		// HashSet<String>>();
		// for (Node node : dataGraph.getAllNodes()) {
		// Map<String, Object> props = node.getAllProperties();
		// for (String key : props.keySet()) {
		// HashSet<String> values;
		// if (propKeyMap.containsKey(key)) {
		// values = propKeyMap.get(key);
		// } else {
		// values = new HashSet<String>();
		// }
		// values.add(node.getProperty(key).toString());
		// propKeyMap.put(key, values);
		// }
		//
		// }
		//
		// for (String key : propKeyMap.keySet()) {
		// readmeWriter.write("property key:" + key);
		// readmeWriter.write("size:" + propKeyMap.get(key).size());
		// for (String value : propKeyMap.get(key)) {
		// readmeWriter.write(value + ", ");
		// }
		// readmeWriter.write();
		// readmeWriter.write();
		// }
		//
		// tx1.success();
		// } catch (Exception e) {
		// // TODO: handle exception
		// }

		// Transaction tx2 = dataGraph.beginTx();
		// HashMap<Long, HashMap<Long, Integer>> freq = new HashMap<Long,
		// HashMap<Long, Integer>>();
		//
		// for (Relationship rel : dataGraph.getAllRelationships()) {
		// freq.putIfAbsent(rel.getStartNode().getId(), new HashMap<Long,
		// Integer>());
		// freq.get(rel.getStartNode().getId()).putIfAbsent(rel.getEndNode().getId(),
		// 0);
		// freq.get(rel.getStartNode().getId()).put(rel.getEndNode().getId(),
		// freq.get(rel.getStartNode().getId()).get(rel.getEndNode().getId()) +
		// 1);
		// }
		//
		// for (Relationship rel : dataGraph.getAllRelationships()) {
		// if
		// (freq.get(rel.getStartNode().getId()).get(rel.getEndNode().getId()) >
		// 1) {
		// readmeWriter.write(rel.getStartNode().getId() + "; " +
		// rel.getEndNode().getId() + "\n");
		// }
		// }
		//
		// tx2.success();
		// tx2.close();
		readmeWriter.flush();
		readmeWriter.close();

	}

}
