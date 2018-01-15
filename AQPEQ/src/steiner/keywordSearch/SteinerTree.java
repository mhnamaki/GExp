package steiner.keywordSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import org.neo4j.kernel.lifecycle.LifecycleAdapter;

import aqpeq.utilities.Dummy.DummyFunctions;
import aqpeq.utilities.Dummy.DummyProperties.SteinerTreeOperation;

abstract class SteinerTree {
	int treeIndex;
	HashSet<String> p = new HashSet<String>();
	int rootNodeId;
	double cost;
	SteinerTreeOperation generationOperation;

	public HashSet<String> getRelatedKeywords() {
		return p;

	}

	public void addRelatedKeyword(String newkeyword) {
		p.add(newkeyword);
	}

	public void addRelatedKeywords(HashSet<String> newkeywords) {
		p.addAll(newkeywords);
	}

	public String getTheHistoryOfTheTree() {

		String history = "";
		Stack<SteinerTree> queue = new Stack<>();
		queue.push(this);

		while (!queue.isEmpty()) {
			SteinerTree current = queue.pop();

			if (current instanceof MergedSteinerTree) {
				history += "( merged " + ((MergedSteinerTree) current).t1.treeIndex + " & "
						+ ((MergedSteinerTree) current).t2.treeIndex + ", becomes " + current + " c:" + current.cost
						+ " ) <- ";
				queue.push(((MergedSteinerTree) current).t1);
				queue.push(((MergedSteinerTree) current).t2);
			} else if (current instanceof GrownSteinerTree) {
				history += "( grown from " + ((GrownSteinerTree) current).t1.treeIndex + ", becomes " + current + " c:"
						+ current.cost + " ) <- ";
				queue.push(((GrownSteinerTree) current).t1);
			} else if (current instanceof InitialSteinerTree) {
				history += "( inited " + current + " c:" + current.cost + " ) ";
			}
		}
		return history;
	}

	@Override
	public int hashCode() {
		final int prime = 7;
		int result = prime * rootNodeId;
		result = prime * result + (p == null ? 0 : p.hashCode());
		return result;
	}

	@Override
	public String toString() {
		// String his = "";
		// try {
		// his = " h:" + getTheHistoryOfTheTree();
		// } catch (Exception exc) {
		// System.out.println(this + "" + exc.getMessage());
		// }
		return "index: " + this.treeIndex + " key:" + DummyFunctions.getKeyForIDAndHashSet(this.rootNodeId, this.p);// +
		// his;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		SteinerTree other = (SteinerTree) obj;
		if (this.rootNodeId != other.rootNodeId)
			return false;

		if (this.p.size() != other.p.size())
			return false;

		if (!this.p.containsAll(other.p)) {
			return false;
		}

		return true;
	}
}
