package quickTests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;

import com.google.common.base.Strings;

import graphInfra.NodeInfra;

public class OneMilionTest {

	public static void main(String[] args) throws Exception {

		String t = "????";
		System.out.println(t.contains("?"));
		
		System.out.println("     ".trim().length() > 0);
		

		ArrayList<NodeInfra> memoryTest = new ArrayList<NodeInfra>();
		for (int i = 0; i < 5000000; i++) {
			memoryTest.add(new NodeInfra(i));

		}

		System.out.println();

	}

}
