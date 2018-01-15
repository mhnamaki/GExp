package string.transformations;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class TransformationFunction {
	
	public static HashMap<String, String> TransformationFunction() {
		HashMap<String, String> abbreviation = new HashMap<String, String>(); 
		FileInputStream abbrevFile;
		FileInputStream celebrityAbbrevFile;
		FileInputStream commonAcroFile;
		try {
			abbrevFile = new FileInputStream("abbrev.txt");
			celebrityAbbrevFile = new FileInputStream("celebrityAbbrev.txt");
			commonAcroFile = new FileInputStream("commonAcro.txt");
			
			//check abbrev.txt
			BufferedReader brAbbrev = new BufferedReader(new InputStreamReader(abbrevFile));
			String line = "";
			while ((line = brAbbrev.readLine()) != null) {
				String[] tem = line.trim().split(" ");
				String abbrev = tem[0].trim();
				String keyword = "";
				for (int i=1; i<tem.length; i++) {
					keyword = tem[i] + " ";
				}
				abbreviation.put(abbrev, keyword.trim());
			}
			brAbbrev.close();
			
			//check celebrityAbbrev.txt
			BufferedReader brCelebrityAbbrev = new BufferedReader(new InputStreamReader(celebrityAbbrevFile));
			//String line = "";
			while ((line = brCelebrityAbbrev.readLine()) != null) {
				String[] tem = line.trim().split(" ");
				String abbrev = tem[0].trim();
				String keyword = "";
				for (int i=1; i<tem.length; i++) {
					keyword = tem[i] + " ";
				}
				abbreviation.put(abbrev, keyword.trim());
			}
			brCelebrityAbbrev.close();
			
			//check commonAcro.txt
			BufferedReader brCommonAcro = new BufferedReader(new InputStreamReader(commonAcroFile));
			//String line = "";
			while ((line = brCommonAcro.readLine()) != null) {
				String[] tem = line.trim().split(" ");
				String abbrev = tem[0].trim();
				String keyword = "";
				for (int i=1; i<tem.length; i++) {
					keyword = tem[i] + " ";
				}
				abbreviation.put(abbrev, keyword.trim());
			}
			brCommonAcro.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return abbreviation;
		
	}
}
