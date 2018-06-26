package tryingToTranslate;

import java.math.BigInteger;

public class IndexLoaderAndTester {

	public static void main(String[] args) throws Exception {
		int kNum = 8;
		
		//System.out.println(Long.MAX_VALUE);
		//System.out.println("13834706074121797631");
//		BigInteger b = new BigInteger("13834706074121797631");
		
		// es
		PrunedLandmarkLabeling prunedLandmarkLabeling = new PrunedLandmarkLabeling(kNum);
		
//		prunedLandmarkLabeling.ReadBinaryIndex("/Users/mnamaki/Documents/workspace/HelloWorldCPlusPlus/amazon_index.cout");
//		
		prunedLandmarkLabeling.LoadIndex("/Users/mnamaki/Desktop/index_from_java");
//		
		System.out.println(prunedLandmarkLabeling.queryDistance(12, 8));
		System.out.println(prunedLandmarkLabeling.queryDistance(3, 3));
		System.out.println(prunedLandmarkLabeling.queryDistance(4, 1));
		System.out.println(prunedLandmarkLabeling.queryDistance(3, 5));
		
		

	}

}
