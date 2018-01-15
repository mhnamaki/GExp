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
		prunedLandmarkLabeling.LoadIndex("/Users/mnamaki/AQPEQ/GraphExamples/k1Infra/distGraph/k1_8bits.jin");
//		
		System.out.println(prunedLandmarkLabeling.queryDistance(5, 6));
		System.out.println(prunedLandmarkLabeling.queryDistance(5, 8));
		System.out.println(prunedLandmarkLabeling.queryDistance(8, 7));
		System.out.println(prunedLandmarkLabeling.queryDistance(7, 5));
		
		

	}

}
