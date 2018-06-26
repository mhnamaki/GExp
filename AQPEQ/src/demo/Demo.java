/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package demo;

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
import graphInfra.RelationshipInfra;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import javax.swing.*;

import org.jgrapht.graph.ListenableUndirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonsrates TreeLayout and RadialTreeLayout.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class Demo extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(Demo.class);
	CTreeNetwork<String, Integer> graph;

	/** the visual component and renderer for the graph */
	VisualizationViewer<String, Integer> vv;

	VisualizationServer.Paintable rings;

	String root;

	TreeLayoutAlgorithm<String> treeLayoutAlgorithm;

	RadialTreeLayoutAlgorithm<String> radialLayoutAlgorithm;

	public Demo(ResultTree resultTree) {

		setLayout(new BorderLayout());
		// create a simple graph for the demo
		graph = createTree(resultTree);

		treeLayoutAlgorithm = new TreeLayoutAlgorithm<>();
		radialLayoutAlgorithm = new RadialTreeLayoutAlgorithm<>();
		// radialLayout.setSize(new Dimension(600, 600));
		vv = new VisualizationViewer<String, Integer>(graph, treeLayoutAlgorithm, new Dimension(600, 600));
		vv.setBackground(Color.white);
		// vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
		vv.getRenderContext().setNodeLabelFunction(Object::toString);
		// add a listener for ToolTips
		vv.setNodeToolTipFunction(Object::toString);
		vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);

		final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
		add(panel);

		final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

		vv.setGraphMouse(graphMouse);

		JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
		modeBox.addItemListener(graphMouse.getModeListener());
		graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

		JRadioButton animate = new JRadioButton("Animate Transition");
		JToggleButton radial = new JToggleButton("Radial");
		radial.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {

				if (animate.isSelected()) {
					LayoutAlgorithmTransition.animate(vv, radialLayoutAlgorithm);
				} else {
					LayoutAlgorithmTransition.apply(vv, radialLayoutAlgorithm);
				}
				vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
				if (rings == null) {
					rings = new Rings(vv.getModel().getLayoutModel());
				}
				vv.addPreRenderPaintable(rings);
			} else {
				if (animate.isSelected()) {
					LayoutAlgorithmTransition.animate(vv, treeLayoutAlgorithm);
				} else {
					LayoutAlgorithmTransition.apply(vv, treeLayoutAlgorithm);
				}
				vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
				vv.removePreRenderPaintable(rings);
			}
			vv.repaint();
		});

		JPanel layoutPanel = new JPanel(new GridLayout(2, 1));
		layoutPanel.add(radial);
		layoutPanel.add(animate);
		JPanel controls = new JPanel();
		controls.add(layoutPanel);
		controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));
		controls.add(modeBox);

		add(controls, BorderLayout.SOUTH);
	}

	class Rings implements VisualizationServer.Paintable {

		Collection<Double> depths;
		LayoutModel<String> layoutModel;

		public Rings(LayoutModel<String> layoutModel) {
			this.layoutModel = layoutModel;
			depths = getDepths();
		}

		private Collection<Double> getDepths() {
			Set<Double> depths = new HashSet<>();
			Map<String, PolarPoint> polarLocations = radialLayoutAlgorithm.getPolarLocations();
			for (String v : graph.nodes()) {
				PolarPoint pp = polarLocations.get(v);
				depths.add(pp.radius);
			}
			return depths;
		}

		public void paint(Graphics g) {
			g.setColor(Color.lightGray);

			Graphics2D g2d = (Graphics2D) g;
			Point center = radialLayoutAlgorithm.getCenter(layoutModel);

			Ellipse2D ellipse = new Ellipse2D.Double();
			for (double d : depths) {
				ellipse.setFrameFromDiagonal(center.x - d, center.y - d, center.x + d, center.y + d);
				Shape shape = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
						.transform(ellipse);
				g2d.draw(shape);
			}
		}

		public boolean useTransform() {
			return true;
		}
	}

	private CTreeNetwork<String, Integer> createTree(ResultTree resultTree) {
	 
	ListenableUndirectedGraph<ResultNode, RelationshipInfra> anOutputTree = resultTree.anOutputTree;
	
	Set<ResultNode> vertices = anOutputTree.vertexSet();
	
//	Set<RelationshipInfra> edges = resultTree.anOutputTree.edgeSet();
	  
    MutableCTreeNetwork<String, Integer> tree =
        TreeNetworkBuilder.builder().expectedNodeCount(vertices.size()).build();
   
    int rootId = resultTree.rootNode.nodeId;
    tree.addNode(String.valueOf(rootId));
    
    HashMap<Integer, HashSet<String>> edgeMap = new HashMap<Integer, HashSet<String>>();
    for (RelationshipInfra e : anOutputTree.edgeSet()) {
    	int src = anOutputTree.getEdgeSource(e).nodeId;
    	int des = anOutputTree.getEdgeTarget(e).nodeId;
//    	System.out.println("edge" + e.relId + ": " + src + " -> " + des);
    	if (!edgeMap.containsKey(des)) {
    		HashSet<String> srcS = new HashSet<String>();
    		srcS.add(String.valueOf(src) + "," + String.valueOf(e.relId));
    		edgeMap.put(des, srcS);
    	} else {
//    		System.out.println("Before = " + edgeMap.get(des));
    		edgeMap.get(des).add(String.valueOf(src) + "," + String.valueOf(e.relId));
//    		System.out.println("After = " + edgeMap.get(des));
    	}
    }
    
    PriorityQueue<Integer> queue = new PriorityQueue<Integer>();
    queue.add(rootId);
    
    while (!queue.isEmpty()){
    	int curId = queue.poll();
    	HashSet<String> srcS = edgeMap.get(curId);
    	if (srcS != null) {
        	for (String str : srcS){
        		String[] strTem = str.split(",");
        		tree.addEdge(String.valueOf(curId), strTem[0], Integer.parseInt(strTem[1]));
        		queue.add(Integer.parseInt(strTem[0]));
        	}
    	}
    }

    return tree;
  }

//	public static void main(String[] args) {
//		JFrame frame = new JFrame();
//		Container content = frame.getContentPane();
//		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//
//		content.add(new TreeLayoutDemo());
//		frame.pack();
//		frame.setVisible(true);
//	}
}
