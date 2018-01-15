package tryingToTranslate;

public class Index_t {

	public byte[] bpspt_d;
	public long[][] bpspt_s;
	public int[] spt_v = null;
	public byte[] spt_d = null;

	public Index_t(int kNumBitParallelRoots) {
		bpspt_d = new byte[kNumBitParallelRoots];
		bpspt_s = new long[kNumBitParallelRoots][2]; // [0]:
		// S^{-1},
		// [1]:
		// S^{0}
	}
}


