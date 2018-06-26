package bicliqueResearch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;

import graphInfra.BipartiteGraph;
import graphInfra.BipartiteGraph.BipartiteSide;
import graphInfra.NodeInfra;

/**
 * Input: parameter: int_m, int_pu, double_epsilon; NodeList_U Output: Z and
 * W(Z), Z belongs to U and W(Z) belongs to V(W)
 */

public class ApxMaximumBiclique {
	
	double pu = 0.0;
	double pw = 0.0;
	double epsilon = 0.0;
	BipartiteGraph biPartiteGraph;
	int mHat = 0;
	int m = 0;
	int t = 0;
	ArrayList<Integer> U = new ArrayList<Integer>();

	public ApxMaximumBiclique(double pu, double pw, double epsilon, BipartiteGraph biPartiteGraph) {
		
		this.pu = pu;
		this.pw = pw;
		this.epsilon = epsilon;
		this.biPartiteGraph = biPartiteGraph;
		
		// get U
		U = biPartiteGraph.getNodesOfSide(BipartiteSide.USide);

		// Compute mHat, m, t
		mHat = (int) ((16/Math.pow(epsilon, 2))*Math.log((40/(pw*epsilon))));
		m = (int) ((2 / pu) * mHat);
		t = (int) (m * 96 / (pu * Math.pow(epsilon, 2)));
		
	}
	
	public Tuple run(){
		// Draw a sample X of m vertices uniformly and independently from U.
		// Draw another sample T of t vertices uniformly and independently from U.
		ArrayList<Integer> X = getSample(m);
		ArrayList<Integer> T = getSample(t);

		ArrayList<int[]> subsetsOfX = GenerateSubsets.getSubsetsOfGivenSize(mHat, X);
		
		TupleComparator comparator = new TupleComparator();
		PriorityQueue<Tuple> queueOfZ = new PriorityQueue<Tuple>(comparator);
		
		// For each subset S of X that has size mˆ do:
		for (int[] s : subsetsOfX) {
			// Wˆ ( S ) ← Γ ( S )
			HashSet<Integer> wHat = new HashSet<Integer>();
			for (int nodeId : s) {
				wHat.addAll(biPartiteGraph.getNextNodes(nodeId));
			}
			// Tˆ(S) ← vertices in T that neighbor most of Wˆ , i.e., T ∩ Γε(Wˆ (S)).
			HashSet<Integer> tHat = new HashSet<Integer>(getENeighbor(wHat, T));
			
			// Tˆ(S) ≥ (3ρU/4)t 
			int threshold = (int) ((3*pu/4)*t);
			if (tHat.size() < threshold){
				continue;
			} else {
				Tuple z = new Tuple(tHat, wHat);
				//put Z into a PriorityQueue
				queueOfZ.add(z);
			}
			
		}
		return queueOfZ.peek();
	}

	public ArrayList<Integer> getSample(int size) {
		ArrayList<Integer> sampleList = new ArrayList<>();
		HashSet<Integer> temSet = new HashSet<Integer>();
		while (temSet.size() < size) {
			Random random = new Random();
			int index = random.nextInt(U.size());
			temSet.add(U.get(index));
		}
		sampleList.addAll(temSet);
		return sampleList;

	}

	public HashSet<Integer> getENeighbor(HashSet<Integer> wHat, ArrayList<Integer> T) {
		HashSet<Integer> tHat = new HashSet<Integer>();
		
		double threshold = (1 - epsilon) * wHat.size();
		
		for (int nodeId : T) {
			HashSet<Integer> neighborOfNodeT = new HashSet<Integer>();
			neighborOfNodeT.addAll(biPartiteGraph.getNextNodes(nodeId));
			neighborOfNodeT.retainAll(wHat);
			if(neighborOfNodeT.size() < threshold){
				continue;
			}else {
				tHat.add(nodeId);
			}
		}

		return tHat;

	}

}
