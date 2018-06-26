package demo;

import java.util.HashSet;

import org.jgrapht.graph.ListenableUndirectedGraph;

import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;

public class DemoResultTree implements Cloneable{

    public HashSet<NodeInfra> newNodes = new HashSet<NodeInfra>();
    public HashSet<RelationshipInfra> newEdges = new HashSet<RelationshipInfra>();
    ResultTree tree;
    public int treeId;

    public DemoResultTree(int treeId, ResultTree tree) {
        this.tree = tree;
        this.treeId = treeId;
    }

    @Override
    public int hashCode() {
        return this.treeId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        DemoResultTree other = (DemoResultTree) obj;
        if (this.treeId != other.treeId)
            return false;

        return true;
    }

}
