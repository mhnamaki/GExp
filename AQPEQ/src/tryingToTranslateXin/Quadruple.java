package tryingToTranslateXin;

import java.util.HashSet;

public class Quadruple {
	public int rId;
	public int distanceRS;
	public HashSet<Integer> SRN = new HashSet<Integer>(); // S_r^-1
	public HashSet<Integer> SRZ = new HashSet<Integer>(); // S_r^0
//	public int vId;
	
	public Quadruple(int rId, int distanceRS, HashSet<Integer> SRN, HashSet<Integer> SRZ){
		this.rId = rId;
		this.distanceRS = distanceRS;
		this.SRN = SRN;
		this.SRZ = SRZ;
//		this.vId = vId;
	}
}
