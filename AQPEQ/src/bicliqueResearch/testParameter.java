package bicliqueResearch;

import java.util.ArrayList;
import java.util.HashSet;

import graphInfra.BipartiteGraph;

public class testParameter {

	public static void main(String[] args) {
		double pu = 0.3;
		double pw = 0.3;
		double epsilon = 0.8;
		double theta = 0.6;
		int k = 4;
		
		int mHat = (int) (1/Math.pow(epsilon, 2));
		double tem = Math.log(k/(pw*epsilon));
		mHat = (int) (mHat * tem);
		System.out.println("mHat = " + mHat);
		
		int m = (int) ((2*Math.log(k))/pu);
		m = m*mHat;
		System.out.println("m = " + m);
		
		int t = (int) (Math.log(1/epsilon)/(pu*pw*Math.pow(epsilon, 3)));
		t= t * m;
		System.out.println("t = " + t);
		
		HashSet<Integer> test = new HashSet<Integer>();
		ArrayList<Integer> poll = new ArrayList<Integer>();
		poll.add(3);
		poll.add(7);
		test.addAll(poll);
		System.out.println(test);
	}

}
