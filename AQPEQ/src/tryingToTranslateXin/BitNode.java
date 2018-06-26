package tryingToTranslateXin;

import java.util.HashSet;

public class BitNode {
	public int nodeId; //v_id
	public int P;
	public boolean isTermNode = false;
	public HashSet<String> termSet = new HashSet<String>();
	public HashSet<Integer> NGV = new HashSet<Integer>(); //neighbor of v
	public HashSet<Integer> SRN = new HashSet<Integer>(); // S_r^-1
	public HashSet<Integer> SRZ = new HashSet<Integer>(); // S_r^0
	
	BitNode(int nodeId){
		this.nodeId = nodeId;
	}
	
	public void addNodeIntoNGV(int neighborId){
		this.NGV.add(neighborId);
	}

}
