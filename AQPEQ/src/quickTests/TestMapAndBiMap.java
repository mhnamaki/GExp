package quickTests;

import java.util.HashMap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class TestMapAndBiMap {

	public static void main(String[] args) {
		 BiMap<String, Integer> biMap = HashBiMap.create();
		
		 for (int i = 1; i < 10000000; i++) {
		 biMap.put(("k" + i), i);
		 }

//		HashMap<String, Integer> biMap1 = new HashMap();
//		HashMap<Integer, String> biMap2 = new HashMap();
//
//		for (int i = 1; i < 10000000; i++) {
//			biMap1.put("k" + i, i);
//			biMap2.put(i, "k" + i);
//		}

		System.out.println();
	}

}
