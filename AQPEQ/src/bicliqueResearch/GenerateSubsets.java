package bicliqueResearch;

import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.math3.util.Combinations;
import graphInfra.NodeInfra;

/**
 * 
 * @author zhangxin
 * 
 *         Input: mHat, list of NodeInfra X Output: all subsets of X, each
 *         subset S that has size mHat
 */

public class GenerateSubsets {

	public static ArrayList<int[]> getSubsetsOfGivenSize(int mHat, ArrayList<Integer> nodeList) {
		ArrayList<int[]> subsets = new ArrayList<int[]>();
		for (Iterator<int[]> iter = new Combinations(nodeList.size(), mHat).iterator(); iter.hasNext();) {
			// subsets.add(iter.next());
			int[] subset = new int[mHat];
			int i = 0;
			for (int index : iter.next()) {
				subset[i] = nodeList.get(index);
				i++;
			}
			subsets.add(subset);
		}
		return subsets;
	}

	public static void main(String[] args) {
		ArrayList<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		// list.add("e");
		ArrayList<String[]> subsets = new ArrayList<String[]>();
		for (Iterator<int[]> iter = new Combinations(list.size(), 3).iterator(); iter.hasNext();) {
			// subsets.add(iter.next());
			String[] subset = new String[3];
			int i = 0;
			for (int index : iter.next()) {
				subset[i] = list.get(index);
				i++;
			}
			subsets.add(subset);
		}
		for (int i = 0; i < subsets.size(); i++) {
			String[] s = subsets.get(i);
			for (int j = 0; j < 3; j++) {
				System.out.print(s[j]);
			}
			System.out.println();
		}

	}

}
