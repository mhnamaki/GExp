package demo;

/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */

import edu.uci.ics.jung.graph.CTreeNetwork;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import edu.uci.ics.jung.layout.algorithms.RadialTreeLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.TreeLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.model.PolarPoint;
import edu.uci.ics.jung.samples.util.ControlHelpers;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithmTransition;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonsrates TreeLayout and RadialTreeLayout.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class TreeLayoutDemoCustom extends JPanel {

	CTreeNetwork<MyNode, Integer> graph;

	/** the visual component and renderer for the graph */
	VisualizationViewer<MyNode, Integer> vv;

	TreeLayoutAlgorithm<MyNode> treeLayoutAlgorithm;

	public TreeLayoutDemoCustom() {

		setLayout(new BorderLayout());
		// create a simple graph for the demo
		graph = createTree();
		treeLayoutAlgorithm = new TreeLayoutAlgorithm<>();
		vv = new VisualizationViewer<>(graph, treeLayoutAlgorithm, new Dimension(300, 300));

		vv.getRenderContext().setNodeLabelFunction(MyNode::toString);
		vv.setNodeToolTipFunction(MyNode::toString);

		final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
		add(panel);

	}

	private CTreeNetwork<MyNode, Integer> createTree() {
		MutableCTreeNetwork<MyNode, Integer> tree = TreeNetworkBuilder.builder().expectedNodeCount(27).build();

		MyNode root = new MyNode("root", 0);
		MyNode v1 = new MyNode("V1", 1);
		int edgeId = 0;
		tree.addEdge(root, v1, edgeId++);
		return tree;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Container content = frame.getContentPane();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		content.add(new TreeLayoutDemoCustom());
		frame.pack();
		frame.setVisible(true);
	}
}

class MyNode {
	String label;
	Integer id;

	public MyNode(String label, Integer id) {
		this.label = label;
		this.id = id;
	}

	@Override
	public String toString() {
		return id + ";" + label;
	}
}

