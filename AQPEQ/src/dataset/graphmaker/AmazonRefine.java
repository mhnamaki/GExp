package dataset.graphmaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class AmazonRefine {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static void customerR() throws IOException{
		File filename = new File("/Users/zhangxin/Desktop/Summer/test/Customer.csv");
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(filename));
			BufferedReader br = new BufferedReader(reader);
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] splittedValues = line.split(":");
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
