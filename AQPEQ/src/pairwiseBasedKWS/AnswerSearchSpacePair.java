package pairwiseBasedKWS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class AnswerSearchSpacePair {
	PairwiseAnswer answer;
	LinkedHashSet<Integer>[] searchSpace;

	public AnswerSearchSpacePair(PairwiseAnswer answer, LinkedHashSet<Integer>[] searchSpace) {
		this.answer = answer;
		this.searchSpace = searchSpace;
	}

	@Override
	public String toString() {

		if (answer == null || searchSpace == null)
			return super.toString();

		return answer.toString() + ", searchSpace:" + Arrays.toString(searchSpace);
	}

}
