package tryingToTranslate;

public class Index_t {
    //LBP(v) a set of quadruples (u, δuv, S−1u (v), S0u(v))
	public int[] nodeId;
	public byte[] bpspt_d;
	public long[][] bpspt_s; // b bits at most 64 as a word
	public int[] spt_v = null;
	public byte[] spt_d = null;

	public Index_t(int kNumBitParallelRoots) {
		bpspt_d = new byte[kNumBitParallelRoots];
		bpspt_s = new long[kNumBitParallelRoots][2]; // [0]:
		// S^{-1},
		// [1]:
		// S^{0}
		
		nodeId = new int[32]; //max of ns
	}
}


