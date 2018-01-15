package neo4jBasedKWS;

//import java.util.ArrayList;
//import java.util.HashMap;
//
public class ExtendedNodePath {
//
//	// private ArrayList<Long> originsOfKeywords = new ArrayList<Long>();
//	private ArrayList<ExtendedNodeProperty> exNodes = new ArrayList<ExtendedNodeProperty>();
//	private HashMap<Long, ArrayList<ExtendedNodeProperty>> pathOfRootToTheOrigin = new HashMap<Long, ArrayList<ExtendedNodeProperty>>();
//
//	public ExtendedNodePath() {
//
//	}
//
//	/**
//	 * 
//	 * @param exNodes
//	 * @param pathOfRootToTheOrigin
//	 */
//	public ExtendedNodePath(ArrayList<ExtendedNodeProperty> exNodes,
//			HashMap<Long, ArrayList<ExtendedNodeProperty>> pathOfRootToTheOrigin) {
//		this.exNodes = exNodes;
//		this.pathOfRootToTheOrigin = pathOfRootToTheOrigin;
//	}
//
//	public ArrayList<ExtendedNodeProperty> getExNodes() {
//		return exNodes;
//	}
//
//	public void setExNodes(ArrayList<ExtendedNodeProperty> exNodes) {
//		this.exNodes = exNodes;
//	}
//
//	public void addExNodes(ArrayList<ExtendedNodeProperty> exNodes) {
//		this.exNodes = exNodes;
//	}
//
//	public void createAndAddExNodesProperty(int level, long exNodeId) {
//		ExtendedNodeProperty prop = new ExtendedNodeProperty();
//		prop.setId(exNodeId);
//		exNodes.add(prop);
//		setLevel(prop);
//	}
//
//	public int getLevel(long id) {
//		for (ExtendedNodeProperty p : this.exNodes) {
//			if (p.getId() == id) {
//				return p.getLevel();
//			}
//		}
//		return -1;
//	}
//
//	// 0 = leaf -> top/root = last index
//	public void setLevel(ExtendedNodeProperty prop) {
//		int i = 0;
//
//		exNodes.get(exNodes.indexOf(prop)).setLevel(exNodes.indexOf(prop));
//	}
//
//	public HashMap<Long, ArrayList<ExtendedNodeProperty>> getPathOfRootToTheOrigin() {
//		return pathOfRootToTheOrigin;
//	}
//
//	public void setPathOfRootToTheOrigin(
//			HashMap<Long, ArrayList<ExtendedNodeProperty>> pathOfRootToTheOrigin) {
//		this.pathOfRootToTheOrigin = pathOfRootToTheOrigin;
//	}
}
