package neo4jBasedKWS;

import java.util.Comparator;

public class ResultTreeRelevanceComparator implements Comparator<ResultTree> {

	@Override
	public int compare(ResultTree r1, ResultTree r2) {
		return Double.compare(r1.cost, r2.cost);
	}

}
