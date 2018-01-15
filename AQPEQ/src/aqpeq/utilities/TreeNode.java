package aqpeq.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.ListIterator;
import java.util.LinkedList;

import org.neo4j.graphdb.Node;

public class TreeNode<T> {
	private TreeNode<T> parent = null;
	public T data = null;
	
	public List<TreeNode<T>> children = new ArrayList<TreeNode<T>>();

	public TreeNode() {

	}

	public TreeNode(T data) {
		this.data = data;
	}

	public TreeNode(T data, TreeNode<T> parent) {
		this.data = data;
		this.parent = parent;
	}

	public List<TreeNode<T>> getChildren() {
		return children;
	}

	public TreeNode<T> getParent() {
		return parent;
	}

	public void setParent(TreeNode<T> parent) {
		this.parent = parent;
	}

	public void addChildren(T childData) {
		this.children.add(new TreeNode<T>(childData));
	}

	public void addChildren(TreeNode<T> childNode) {
		this.children.add(childNode);
	}

	public T getData() {
		return this.data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public void setParentData(T data) {
		this.parent.data = data;
	}

	public boolean isRoot() {
		return (this.parent == null);
	}

	public boolean isLeaf() {

		if (children.isEmpty())
			return true;

		return false;
	}

	public void removeParent() {
		this.parent = null;
	}
}
