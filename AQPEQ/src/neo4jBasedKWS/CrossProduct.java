package neo4jBasedKWS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CrossProduct {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		HashMap<String, HashSet<Integer>> matchesOfKeywords = new HashMap<String, HashSet<Integer>>();
		matchesOfKeywords.put("a", new HashSet<Integer>());
		matchesOfKeywords.put("b", new HashSet<Integer>());
		matchesOfKeywords.put("c", new HashSet<Integer>());

		matchesOfKeywords.get("a").add(4);
		matchesOfKeywords.get("b").add(5);
		matchesOfKeywords.get("b").add(6);
		matchesOfKeywords.get("c").add(7);
		matchesOfKeywords.get("c").add(8);

		ArrayList<HashMap<String, HashSet<Integer>>> vListArr = crossProduct(matchesOfKeywords);
		for (HashMap<String, HashSet<Integer>> vList : vListArr) {
			for (Entry<String, HashSet<Integer>> entry : vList.entrySet()) {
				String key = entry.getKey();
				HashSet<Integer> value = entry.getValue();
				System.out.print("key " + key + " :");
				for (int aLong : value) {
					System.out.print(aLong + ",");
				}
				System.out.println();
			}
		}

	}

	/**
	 * 
	 * @param originsOfKeywords
	 * @return
	 */
	public static ArrayList<HashMap<String, HashSet<Integer>>> crossProduct(
			HashMap<String, HashSet<Integer>> originsOfKeywords) {

		List<Map.Entry<String, HashSet<Integer>>> mapEntryList = new ArrayList<Map.Entry<String, HashSet<Integer>>>(
				originsOfKeywords.entrySet());
		List<List<Integer>> combinationsList = new ArrayList<List<Integer>>();
		List<Integer> combinations = new ArrayList<Integer>();

		generateCombinations(mapEntryList, combinations, combinationsList);

		ArrayList<HashMap<String, HashSet<Integer>>> vListArray = new ArrayList<HashMap<String, HashSet<Integer>>>();

		for (int i = 0; i < combinationsList.size(); i++) {
			List<Integer> combination = combinationsList.get(i);
			int j = 0;
			HashMap<String, HashSet<Integer>> vList = new HashMap<String, HashSet<Integer>>();
			for (String keyword : originsOfKeywords.keySet()) {
				vList.put(keyword, new HashSet<>());
				vList.get(keyword).add(combination.get(j));
				j++;
			}
			vListArray.add(vList);
		}
		return vListArray;
	}

	/**
	 * 
	 * @param mapEntryList
	 * @param combinations
	 * @param combinationsList
	 */
	private static void generateCombinations(List<Map.Entry<String, HashSet<Integer>>> mapEntryList,
			List<Integer> combinations, List<List<Integer>> combinationsList) {

		if (mapEntryList.isEmpty()) {
			combinationsList.add(new ArrayList<Integer>(combinations));
			return;
		}

		Map.Entry<String, HashSet<Integer>> entry = mapEntryList.remove(0);

		List<Integer> entryValue = new ArrayList<Integer>(entry.getValue());

		while (!entryValue.isEmpty()) {

			int rr = entryValue.remove(0);

			combinations.add(rr);

			generateCombinations(mapEntryList, combinations, combinationsList);

			combinations.remove(combinations.size() - 1);
		}

		mapEntryList.add(0, entry);

	}

}
