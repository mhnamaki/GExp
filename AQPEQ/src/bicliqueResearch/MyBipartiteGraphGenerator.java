package bicliqueResearch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import graphInfra.BipartiteGraph;
import graphInfra.NodeInfra;
import graphInfra.BipartiteGraph.BipartiteSide;

public class MyBipartiteGraphGenerator {

	int numberOfU;
	int numberOfV;
	int numberOfEdges;

	public MyBipartiteGraphGenerator(int numberOfU, int numberOfV, int numberOfEdges) {
		this.numberOfU = numberOfU;
		this.numberOfV = numberOfV;
		this.numberOfEdges = numberOfEdges;
	}

	public BipartiteGraph getBipartiteGraph() {

		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);

		VertexFactory<Integer> vertexFactory = new VertexFactory<Integer>() {
			int n = 0;

			@Override
			public Integer createVertex() {
				return n++;
			}
		};

		ArrayList<Integer> nodesU = new ArrayList<Integer>();
		ArrayList<Integer> nodesV = new ArrayList<Integer>();

		generateGraphNoIsolatedVertices(graph, numberOfU, numberOfV, numberOfEdges, vertexFactory, nodesU, nodesV);

		// convert to our version
		return covertJGraphToBipartiteGraph(graph, nodesU, nodesV);
	}

	private BipartiteGraph covertJGraphToBipartiteGraph(UndirectedGraph<Integer, DefaultEdge> graph,
			ArrayList<Integer> nodesU, ArrayList<Integer> nodesV) {
		BipartiteGraph bipartiteGraph = new BipartiteGraph();

		HashMap<Integer, Integer> ourIdOfJgraphId = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> jgraphIdOfOurId = new HashMap<Integer, Integer>();

		// converting U side
		for (int nodeU : nodesU) {
			NodeInfra newNode = bipartiteGraph.getAndAddANewBipartiteNodeInfra(0, 1f, BipartiteSide.USide);
			ourIdOfJgraphId.put(nodeU, newNode.nodeId);
			jgraphIdOfOurId.put(newNode.nodeId, nodeU);
		}

		// converting V side
		for (int nodeV : nodesV) {
			NodeInfra newNode = bipartiteGraph.getAndAddANewBipartiteNodeInfra(0, 1f, BipartiteSide.VSide);
			ourIdOfJgraphId.put(nodeV, newNode.nodeId);
			jgraphIdOfOurId.put(newNode.nodeId, nodeV);
		}

		// converting edges
		for (int vertex : graph.vertexSet()) {
			for (DefaultEdge e : graph.edgesOf(vertex)) {
				int v1 = graph.getEdgeSource(e);
				int v2 = graph.getEdgeTarget(e);
				int otherV = (v1 == vertex) ? v2 : v1;

				if (vertex <= otherV)
					bipartiteGraph.addBipartiteRelationship(ourIdOfJgraphId.get(vertex), ourIdOfJgraphId.get(otherV),
							1f);

			}
		}

		return bipartiteGraph;
	}

	private <V, E> void generateGraphNoIsolatedVertices(Graph<V, E> graph, int numVertices0, int numVertices1,
			int numEdges, final VertexFactory<V> vertexFactory, List<V> vertices0, List<V> vertices1) {

		int minNumEdges = Math.max(numVertices0, numVertices1);
		if (numEdges < minNumEdges) {
			System.out.println("At least " + minNumEdges + " are required to " + "connect each of the " + numVertices0
					+ " vertices " + "to any of the " + numVertices1 + " vertices");
			numEdges = minNumEdges;
		}

		for (int i = 0; i < numVertices0; i++) {
			V v = vertexFactory.createVertex();
			graph.addVertex(v);
			vertices0.add(v);
		}
		for (int i = 0; i < numVertices1; i++) {
			V v = vertexFactory.createVertex();
			graph.addVertex(v);
			vertices1.add(v);
		}

		// Connect each vertex of the larger set with
		// a random vertex of the smaller set
		Random random = new Random(0);
		List<V> larger = null;
		List<V> smaller = null;

		if (numVertices0 > numVertices1) {
			larger = new ArrayList<V>(vertices0);
			smaller = new ArrayList<V>(vertices1);
		} else {
			larger = new ArrayList<V>(vertices1);
			smaller = new ArrayList<V>(vertices0);
		}
		List<V> unmatched = new ArrayList<V>(smaller);
		for (V vL : larger) {
			int i = random.nextInt(unmatched.size());
			V vS = unmatched.get(i);
			unmatched.remove(i);
			if (unmatched.size() == 0) {
				unmatched = new ArrayList<V>(smaller);
			}
			graph.addEdge(vL, vS);
		}

		// Create the remaining edges between random vertices
		while (graph.edgeSet().size() < numEdges) {
			int i0 = random.nextInt(vertices0.size());
			V v0 = vertices0.get(i0);
			int i1 = random.nextInt(vertices1.size());
			V v1 = vertices1.get(i1);
			graph.addEdge(v0, v1);
		}

	}

	public static void main(String[] args) {
		// int numberOfU, int numberOfV, int numberOfEdges
		MyBipartiteGraphGenerator myBipartiteGraphGenerator = new MyBipartiteGraphGenerator(10, 4, 12);
		BipartiteGraph bipartiteGraph = myBipartiteGraphGenerator.getBipartiteGraph();
		
		// we can print the bipartite graph in our format by calling the
		// following method.
		bipartiteGraph.printTheGraph();
	}

}
