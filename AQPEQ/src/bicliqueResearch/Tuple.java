package bicliqueResearch;

import java.util.HashSet;

public class Tuple {
	HashSet<Integer> tHat = new HashSet<Integer>();
	HashSet<Integer> wHat = new HashSet<Integer>();
	HashSet<Integer> edges = new HashSet<Integer>();
	int size = 0;
	public Tuple(HashSet<Integer> tHat, HashSet<Integer> wHat){
		this.tHat = tHat;
		this.wHat = wHat;
		size = tHat.size() * wHat.size();
	}
}
