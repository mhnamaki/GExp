package quickTests;

import java.util.HashMap;

public class wrongKeyboard {

	public static void main(String[] args) {
		String persianKey = "اثممخ";

		HashMap<String, String> persianOfEnglishKey = new HashMap<String, String>();
		persianOfEnglishKey.put("a", "ش");
		persianOfEnglishKey.put("b", "ذ");
		persianOfEnglishKey.put("c", "ز");
		persianOfEnglishKey.put("d", "ی");
		persianOfEnglishKey.put("e", "ث");
		persianOfEnglishKey.put("f", "ب");
		persianOfEnglishKey.put("g", "ل");
		persianOfEnglishKey.put("h", "ا");
		persianOfEnglishKey.put("i", "ه");
		persianOfEnglishKey.put("j", "ت");
		persianOfEnglishKey.put("k", "ن");
		persianOfEnglishKey.put("l", "م");
		persianOfEnglishKey.put("m", "پ");
		persianOfEnglishKey.put("n", "د");
		persianOfEnglishKey.put("o", "خ");
		persianOfEnglishKey.put("p", "ح");
		persianOfEnglishKey.put("q", "ض");
		persianOfEnglishKey.put("r", "ق");
		persianOfEnglishKey.put("s", "س");
		persianOfEnglishKey.put("t", "ف");
		persianOfEnglishKey.put("u", "ع");
		persianOfEnglishKey.put("v", "ر");
		persianOfEnglishKey.put("w", "ص");
		persianOfEnglishKey.put("x", "ض");
		persianOfEnglishKey.put("y", "غ");
		persianOfEnglishKey.put("z", "ظ");
		persianOfEnglishKey.put(",", "و");
		persianOfEnglishKey.put(" ", " ");
		persianOfEnglishKey.put("!", "!");
		

		HashMap<String, String> englishOfPersianKey = new HashMap<String, String>();

		for (String engKey : persianOfEnglishKey.keySet()) {
			englishOfPersianKey.put(persianOfEnglishKey.get(engKey), engKey);
		}

		persianToEng(persianKey, englishOfPersianKey);

	}

	private static void persianToEng(String persianKey, HashMap<String, String> englishOfPersianKey) {

		char[] persianKeys = persianKey.toCharArray();

		String output = "";

		for (int i = 0; i < persianKeys.length; i++) {
			String k = String.valueOf(persianKeys[i]);
			if (englishOfPersianKey.containsKey(k)) {
				output += englishOfPersianKey.get(k);
			} else {
				System.err.println("not exist in map:" + k);
			}
		}

		System.out.println(output);
	}

}
