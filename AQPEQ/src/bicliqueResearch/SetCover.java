package bicliqueResearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import graphInfra.BipartiteGraph;

interface Filter<T> {
	boolean matches(T t);
}

class SetInfoHolder {
	int startIndex;
	HashSet<Integer> setOfSize;

	public SetInfoHolder(int startIndex, HashSet<Integer> setOfSize) {
		this.startIndex = startIndex;
		this.setOfSize = setOfSize;
	}
}

public class SetCover {

	public static Set<Set<Integer>> getMinSetCover(BipartiteGraph bipartiteGraph, List<Set<Integer>> listOfNodeIdSets,
			Set<Integer> universalNodeIdSet) {

		Set<Set<Integer>> firstSolution = new HashSet<Set<Integer>>();

		// sort bicliques based on size desc.
		Collections.sort(listOfNodeIdSets, new Comparator<Set<Integer>>() {
			@Override
			public int compare(Set<Integer> o1, Set<Integer> o2) {
				return Integer.compare(o2.size(), o1.size());
			}
		});

		int maxCombination = listOfNodeIdSets.size(); // we can improve to max
														// (U,V)

		float maxWeightSoFar = Float.MIN_VALUE;
		HashSet<Integer> bestSetIndexSoFar = new HashSet<Integer>();

		HashMap<Integer, ArrayList<SetInfoHolder>> setsIndexOfSize = new HashMap<Integer, ArrayList<SetInfoHolder>>();

		// emptyset
		setsIndexOfSize.put(0, new ArrayList<>());
		setsIndexOfSize.get(0).add(new SetInfoHolder(0, new HashSet<>()));

		boolean foundCover = false;
		for (int i = 0; i < maxCombination; i++) {

			// check for cover

			for (SetInfoHolder setInfoOfSizeI : setsIndexOfSize.get(i)) {
				HashSet<Integer> union = new HashSet<Integer>();
				for (int setIndex : setInfoOfSizeI.setOfSize) {
					union.addAll(listOfNodeIdSets.get(setIndex));
				}

				if (union.containsAll(universalNodeIdSet)) { // it's a cover
					foundCover = true;
					float currentWeight = computeWeight(bipartiteGraph, setInfoOfSizeI.setOfSize, listOfNodeIdSets);
					if (currentWeight > maxWeightSoFar) {
						maxWeightSoFar = currentWeight;
						bestSetIndexSoFar = setInfoOfSizeI.setOfSize;
					}
				}
			}

			if (foundCover)
				break;

			setsIndexOfSize.putIfAbsent(i + 1, new ArrayList<SetInfoHolder>());

			// generate sets with a larger size.
			for (SetInfoHolder setInfoOfSizeI : setsIndexOfSize.get(i)) {

				// add all possible new element to each set
				for (int j = setInfoOfSizeI.startIndex; j < maxCombination; j++) {

					HashSet<Integer> setOfNewSize = new HashSet<Integer>(setInfoOfSizeI.setOfSize);
					setOfNewSize.add(j);
					setsIndexOfSize.get(i + 1).add(new SetInfoHolder(j + 1, setOfNewSize));
				}
			}

		}

		for (int setIndex : bestSetIndexSoFar) {
			firstSolution.add(listOfNodeIdSets.get(setIndex));
		}

		return firstSolution;
	}

	// public static Set<Set<Integer>> getMinSetCover(List<Set<Integer>>
	// listOfSets, Set<Integer> solutionSet) {
	//
	// Filter<Set<Set<Integer>>> filter = new Filter<Set<Set<Integer>>>() {
	// public boolean matches(Set<Set<Integer>> integers) {
	// Set<Integer> union = new LinkedHashSet<Integer>();
	// for (Set<Integer> ints : integers)
	// union.addAll(ints);
	// return union.equals(solutionSet);
	// }
	// };
	//
	// Set<Set<Integer>> firstSolution = shortestCombination(filter,
	// listOfSets);
	// return firstSolution;
	// }
	//
	// private static <T> Set<T> shortestCombination(Filter<Set<T>> filter,
	// List<T> listOfSets) {
	// final int size = listOfSets.size();
	//// if (size > 20)
	//// throw new IllegalArgumentException("Too many combinations");
	// int combinations = 1 << size;
	// List<Set<T>> possibleSolutions = new ArrayList<Set<T>>();
	// for (int l = 0; l < combinations; l++) {
	// Set<T> combination = new LinkedHashSet<T>();
	// for (int j = 0; j < size; j++) {
	// if (((l >> j) & 1) != 0)
	// combination.add(listOfSets.get(j));
	// }
	// possibleSolutions.add(combination);
	// }
	// // the possible solutions in order of size.
	// Collections.sort(possibleSolutions, new Comparator<Set<T>>() {
	// public int compare(Set<T> o1, Set<T> o2) {
	// return o1.size() - o2.size();
	// }
	// });
	// for (Set<T> possibleSolution : possibleSolutions) {
	// if (filter.matches(possibleSolution))
	// return possibleSolution;
	// }
	// return null;
	// }

	private static float computeWeight(BipartiteGraph bipartiteGraph, HashSet<Integer> setIndexOfSize,
			List<Set<Integer>> listOfNodeIdSets) {
		float weight = 0;
		for (int setIndex : setIndexOfSize) {
			for (int nodeId : listOfNodeIdSets.get(setIndex)) {
				weight += bipartiteGraph.getNodeById(nodeId).weight;
			}
		}

		return weight;
	}

	public static void print(Set<Set<Integer>> setCover) {
		System.out.print("{");
		Iterator<Set<Integer>> iter1 = setCover.iterator();
		while (iter1.hasNext()) {
			Set<Integer> set = iter1.next();

			Iterator<Integer> iter2 = set.iterator();
			System.out.print("{");
			while (iter2.hasNext()) {
				int nodeId = iter2.next();
				System.out.print(nodeId + ",");
			}
			System.out.print("},");
		}
		System.out.print("}\n");
	}
}