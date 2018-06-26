package bicliqueResearch;

import java.util.Comparator;

public class TupleComparator implements Comparator<Tuple>{
	
	@Override
	public int compare(Tuple o1, Tuple o2) {

		return Integer.compare(o2.size, o1.size);

	}

}
