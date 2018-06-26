package bicliqueResearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;

import org.apache.commons.math3.util.Combinations;

import graphInfra.BipartiteGraph;
import graphInfra.NodeInfra;
import graphInfra.BipartiteGraph.BipartiteSide;

/**
 * 	Input: parameter: int_m, int_pu, int_k, double_epsilon; NodeList_U
	Output: wTilde
	wTilde is a set of Z and W(Z), Z belongs to U and W(Z) belongs to V(W)
	wTilde contains k groups of Z and W(Z)
 */

public class ApxConjunctiveClusters {
	
	double pu = 0.0;
	double pw = 0.0;
	int k = 0;
	double epsilon = 0.0;
	double theta = 0.0;
	BipartiteGraph biPartiteGraph;
	int mHat = 0;
	int m = 0;
	int t = 0;
	ArrayList<Integer> U = new ArrayList<Integer>();
	ArrayList<Integer> vNodes = new ArrayList<Integer>();
	int w = 0;
	
	
	public ApxConjunctiveClusters(double pu, double pw, double epsilon, double theta, int k, BipartiteGraph biPartiteGraph){
		this.pu = pu;
		this.pw = pw;
		this.epsilon = epsilon;
		this.theta = theta;
		this.k = k;
		this.biPartiteGraph = biPartiteGraph;
		
		// get U
		U = biPartiteGraph.getNodesOfSide(BipartiteSide.USide);
		//get V
		vNodes = biPartiteGraph.getNodesOfSide(BipartiteSide.VSide);
		w = vNodes.size();
		
		//compute mHat, m, t
		mHat = (int) ((1/Math.pow(epsilon, 2))*Math.log((k/(pw*epsilon))));
		m = (int) (2*Math.log(k)/pu * mHat);
		t = (int) (Math.log(1/epsilon)/(pu*pw*Math.pow(epsilon, 3))*m);
		
//		//for test
//		mHat = 4;
//		m = 30;
//		t = 70;
	}
	
	public ArrayList<Tuple> run(){
		ArrayList<Tuple> wTilde = new ArrayList<Tuple>();
		
		//Draw a sample X of m vertices uniformly and independently from U.
		//Draw another sample T of t vertices uniformly and independently from U.
		ArrayList<Integer> X = getSample(m);
		ArrayList<Integer> T = getSample(t);
		
		//Wˆ ← ∅ .
		TupleComparator comparator = new TupleComparator();
		PriorityQueue<Tuple> queueOfZ = new PriorityQueue<Tuple>(comparator);
		
		ArrayList<int[]> subsetsOfX = GenerateSubsets.getSubsetsOfGivenSize(mHat, X);

		//For each subset S of X that has size mˆ do
		for (int[] s : subsetsOfX) {
			//Wˆ ( S ) ← Γ ( S ) .
			HashSet<Integer> wHat = new HashSet<Integer>();
			for (int nodeId : s) {
				wHat.addAll(biPartiteGraph.getNextNodes(nodeId));
			}
			
			//Tˆ(S) ← T ∩ Γε(Wˆ (S)).
			HashSet<Integer> tHat = new HashSet<Integer>(getENeighbor(wHat, T));
			
			//If|Wˆ(S)|≥ρW ·|W|and|Tˆ(S)|≥(ρU/2)·tthenaddWˆ(S)toWˆ.
			int thresholdW = (int) (pw * w);
			int thresholdT = (int) ((pu/2)*t);
			if(wHat.size() < thresholdW || tHat.size() < thresholdT){
				continue;
			}else {
				Tuple Z = new Tuple(tHat, wHat);
				createEdge(Z);
				queueOfZ.add(Z);
			}
		}
		
		double thresholdCover = theta + 2 * epsilon;
		while ((wTilde.size()<k) && !queueOfZ.isEmpty()){
			Tuple Z = queueOfZ.poll();
			boolean flag = true;
			if (wTilde.isEmpty()){
				wTilde.add(Z);
			} else {
				HashSet<Integer> edgesOfNumerator = new HashSet<Integer>(Z.edges);
				HashSet<Integer> edgesOfDenominator = new HashSet<Integer>(Z.edges);
				for(Tuple tem : wTilde){
					HashSet<Integer> edgesOfTem = tem.edges;
					edgesOfNumerator.retainAll(edgesOfTem);
					edgesOfDenominator.addAll(edgesOfTem);
					if ((edgesOfNumerator.size() / edgesOfDenominator.size()) > thresholdCover) {
						flag = false;
					}
				}
				if (flag){
					wTilde.add(Z);
				}
			}
		}
		
		return wTilde;
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
	
	//Γε(S) = {w : |Γ(w)∩S| ≥ (1−ε)|S|}
	public HashSet<Integer> getENeighbor(HashSet<Integer> wHat, ArrayList<Integer> T) {
		HashSet<Integer> tHat = new HashSet<Integer>();
		
		double threshold = (1 - epsilon) * wHat.size();
		
		for (int nodeId : T) {
			HashSet<Integer> neighborOfNode = new HashSet<Integer>(biPartiteGraph.getNextNodes(nodeId));
			neighborOfNode.retainAll(wHat);

			if(neighborOfNode.size() < threshold){
				continue;
			}else {
				tHat.add(nodeId);
			}
		}

		return tHat;

	}
	
	public void createEdge(Tuple Z){
		HashSet<Integer> edges = new HashSet<Integer>();
		HashSet<Integer> tHat = Z.tHat;
		HashSet<Integer> wHat = Z.wHat;
		for (int tId : tHat) {
			HashMap<Integer, Integer> outgoingRelIdOfSourceNodeId = biPartiteGraph.getNodeById(tId).outgoingRelIdOfSourceNodeId;
			for (int wId : wHat) {
				if (outgoingRelIdOfSourceNodeId.containsKey(wId)) {
					edges.add(outgoingRelIdOfSourceNodeId.get(wId));
				}
			}
		}
		Z.edges = edges;
//		System.out.println("this Z contains " + edges.size() + " edges.");
	}
	
	public static void main(String[] args) {
		// int numberOfU, int numberOfV, int numberOfEdges
		MyBipartiteGraphGenerator myBipartiteGraphGenerator = new MyBipartiteGraphGenerator(100, 100, 3000);
		BipartiteGraph bipartiteGraph = myBipartiteGraphGenerator.getBipartiteGraph();
		
		// we can print the bipartite graph in our format by calling the
		// following method.
//		bipartiteGraph.printTheGraph();
		
		//double pu, double pw, double epsilon, double theta, int k, BipartiteGraph biPartiteGraph
		double pu = 0.3;
		double pw = 0.3;
		double epsilon = 0.8;
		double theta = 0.6;
		int k = 4;
		ApxConjunctiveClusters cc = new ApxConjunctiveClusters(pu, pw, epsilon, theta, k, bipartiteGraph);
		
		System.out.println("mHat = " + cc.mHat);
		System.out.println("m = " + cc.m);
		System.out.println("t = " + cc.t);
		
		ArrayList<Tuple> wTilde = cc.run();
		System.out.println("debug");
		System.out.println("wTild size = " + wTilde.size());
		for (Tuple z : wTilde) {
			System.out.println(z.size);
			System.out.println("tHat" + z.tHat);
			System.out.println("wHat" + z.wHat);
		}
	}
}
