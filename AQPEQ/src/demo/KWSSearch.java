package demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import neo4jBasedKWS.ResultTree;
import queryExpansion.CostAndNodesOfAnswersPair;

public interface KWSSearch {
	
	LinkedList<DemoResultTree> Search(boolean incSearch) throws Exception;
	
	HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>> Explore() throws Exception;

}
