package bicliqueResearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;

import graphInfra.BipartiteGraph;
import graphInfra.NodeInfra;
import graphInfra.BipartiteGraph.BipartiteSide;

public class BBicliqueCover {

	BipartiteGraph bipartiteGraph;
	int boundOnU = 3;

	public BBicliqueCover(BipartiteGraph bipartiteGraph, int boundOnU) {
		this.bipartiteGraph = bipartiteGraph;
		this.boundOnU = boundOnU;
	}

	public void computeBBC() {

		Set<Integer> allNodes = new HashSet<Integer>(bipartiteGraph.getAllNodesInSet());

		HashMap<Integer, ArrayList<int[]>> subsetsOfUGivenSize = new HashMap<Integer, ArrayList<int[]>>();
		// HashMap<Integer, ArrayList<HashSet<Integer>>> listOfSetsOfGivenSize =
		// new HashMap<Integer, ArrayList<HashSet<Integer>>>();
		List<Set<Integer>> listOfSets = new ArrayList<Set<Integer>>();

		// if E = U*V => k=1
		// we just need to find b-bicliques with highest weight

		for (int b = boundOnU; b >= 1; b--) {
			// get all possible subsets of size b;
			ArrayList<int[]> subsetsOfU = GenerateSubsets.getSubsetsOfGivenSize(b,
					bipartiteGraph.getNodesOfSide(BipartiteSide.USide));

			subsetsOfUGivenSize.put(b, subsetsOfU);

			// now, for each subset, we induce the biclique they create and also
			// the
			// set of nodes they cover.
			for (int i = 0; i < subsetsOfU.size(); i++) {

				int[] subsetOfU = subsetsOfU.get(i);

				// find neighbors of subsetOfU
				ArrayList<Integer> neighborsOfSubsetOfU = new ArrayList<>(
						bipartiteGraph.getNodesOfSide(BipartiteSide.VSide));
				for (int u : subsetOfU) {
					neighborsOfSubsetOfU.retainAll(bipartiteGraph.getNextNodes(u));
				}
				if (neighborsOfSubsetOfU.size() == 0)
					continue;
				else {
					System.out.println("subsetOfU: " + Arrays.toString(subsetOfU) + ", neighborsOfSubsetOfU: "
							+ neighborsOfSubsetOfU);
				}

				// subsetOfU and neighborsOfSubsetOfU are a biclique
				LinkedHashSet<Integer> currentSet = new LinkedHashSet<Integer>(
						subsetsOfU.size() + neighborsOfSubsetOfU.size());
				fillCurrentSet(currentSet, subsetOfU, neighborsOfSubsetOfU);

				// listOfSetsOfGivenSize.putIfAbsent(b, new
				// ArrayList<HashSet<Integer>>());
				// listOfSetsOfGivenSize.get(b).add(currentSet);
				listOfSets.add(currentSet);

			}
		}

		// let's find a min cover

		Set<Set<Integer>> setCover = SetCover.getMinSetCover(bipartiteGraph, listOfSets, allNodes);
		float weight = computeTotalWeight(bipartiteGraph, setCover);

		SetCover.print(setCover);
		System.out.println("weight: " + weight);

	}

	private float computeTotalWeight(BipartiteGraph bipartiteGraph, Set<Set<Integer>> setCover) {
		float weight = 0;
		Iterator<Set<Integer>> iter1 = setCover.iterator();
		while (iter1.hasNext()) {
			Set<Integer> set = iter1.next();
			Iterator<Integer> iter2 = set.iterator();
			while (iter2.hasNext()) {
				int nodeId = iter2.next();
				weight += bipartiteGraph.getNodeById(nodeId).weight;
			}
		}

		return weight;

	}

	private boolean unionOfAllCoverAll(List<Set<Integer>> listOfSets, int sizeOfBipartiteGraph) {
		Set<Integer> union = new HashSet<Integer>();
		for (Set<Integer> set : listOfSets) {
			union.addAll(set);
		}

		if (union.size() == sizeOfBipartiteGraph) {
			return true;
		}
		return false;
	}

	private void fillCurrentSet(LinkedHashSet<Integer> currentSet, int[] subsetOfU,
			ArrayList<Integer> neighborsOfSubsetOfU) {

		for (int i = 0; i < subsetOfU.length; i++) {
			currentSet.add(subsetOfU[i]);
		}

		currentSet.addAll(neighborsOfSubsetOfU);

	}

	public static void main(String[] args) {

		MyBipartiteGraphGenerator gen = new MyBipartiteGraphGenerator(10, 10, 20);
		BipartiteGraph bipartiteGraph = gen.getBipartiteGraph();
		BBicliqueCover bBicliqueCover = new BBicliqueCover(bipartiteGraph, 3);
		bBicliqueCover.computeBBC();

	}

}
