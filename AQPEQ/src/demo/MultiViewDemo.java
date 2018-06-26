/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package demo;

import com.google.common.graph.Network;

import demo.Demo.Rings;
import edu.uci.ics.jung.graph.CTreeNetwork;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.RadialTreeLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.TreeLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.model.PolarPoint;
import edu.uci.ics.jung.samples.util.ControlHelpers;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.LayoutScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ViewScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction;
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithmTransition;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.NodeLabel.Position;
import graphInfra.RelationshipInfra;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import javax.swing.*;

import org.jgrapht.graph.ListenableUndirectedGraph;

/**
 * Demonstrates 3 views of one graph in one model with one layout. Each view
 * uses a different scaling graph mouse.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class MultiViewDemo extends JPanel {

	Dimension preferredSize = new Dimension(420, 350);

	final String messageOne = "The mouse wheel will scale the model's layout when activated"
			+ " in View 1. Since all three views share the same layout Function, all three views will"
			+ " show the same scaling of the layout.";

	final String messageTwo = "The mouse wheel will scale the view when activated in"
			+ " View 2. Since all three views share the same view Function, all three views will be affected.";

	final String messageThree = "   The mouse wheel uses a 'crossover' feature in View 3."
			+ " When the combined layout and view scale is greater than '1', the model's layout will be scaled."
			+ " Since all three views share the same layout Function, all three views will show the same "
			+ " scaling of the layout.\n   When the combined scale is less than '1', the scaling function"
			+ " crosses over to the view, and then, since all three views share the same view Function,"
			+ " all three views will show the same scaling.";

	JTextArea textArea;
	JScrollPane scrollPane;

	/**
	 * create an instance of a simple graph in two views with controls to demo
	 * the zoom features.
	 */
	public MultiViewDemo(LinkedList<ResultTree> fullOutputsQueueTemp) {

		setLayout(new BorderLayout());
		
		JPanel panel = new JPanel(new GridLayout(1, 0));
		
		int cnt=1;
		for (ResultTree resultTree : fullOutputsQueueTemp) {
			// create a simple graph for the demo
			/** the graph */
			Network<String, Integer> graph = createTree(resultTree);
			
			// create one layout for the graph
			FRLayoutAlgorithm<String> layoutAlgorithm = new FRLayoutAlgorithm<>();
			layoutAlgorithm.setMaxIterations(1000);

			TreeLayoutAlgorithm<String> treeLayoutAlgorithm = new TreeLayoutAlgorithm<>();

			RadialTreeLayoutAlgorithm<String> radialLayoutAlgorithm = new RadialTreeLayoutAlgorithm<>();
			
			// create one model that all views will share
			VisualizationModel<String, Integer> visualizationModel = new BaseVisualizationModel<>(graph, treeLayoutAlgorithm,
					preferredSize);
			/** the visual components and renderers for the graph */
			VisualizationViewer<String, Integer> vv = new VisualizationViewer<String, Integer>(graph, treeLayoutAlgorithm, preferredSize);
			vv.setBackground(Color.white);
			vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
			vv.getRenderContext().setNodeLabelFunction(Object::toString);
			// add a listener for ToolTips
			vv.setNodeToolTipFunction(Object::toString);
			vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);
			
			// create one pick support for all 3 views to share
			NetworkElementAccessor<String, Integer> pickSupport = new ShapePickSupport<>(vv);
			vv.setPickSupport(pickSupport);
			// create one picked state for all 3 views to share
			PickedState<Integer> pes = new MultiPickedState<>();
			PickedState<String> pvs = new MultiPickedState<>();
			vv.setPickedNodeState(pvs);
			vv.setPickedEdgeState(pes);
			
			// set an edge paint function that shows picked edges
			vv.getRenderContext().setEdgeDrawPaintFunction(new PickableEdgePaintFunction<>(pes, Color.black, Color.red));
			vv.getRenderContext().setNodeFillPaintFunction(new PickableNodePaintFunction<>(pvs, Color.red, Color.yellow));
			
			// add default listener for ToolTips
			vv.setNodeToolTipFunction(Object::toString);
			
			final JPanel p1 = new JPanel(new BorderLayout());
			
			p1.add(new GraphZoomScrollPane(vv));
			
			JButton h1 = new JButton("?");
			
			String viewName = "Result Tree " + String.valueOf(cnt);
			h1.addActionListener(e -> {
				textArea.setText(messageOne);
				JOptionPane.showMessageDialog(p1, scrollPane, viewName, JOptionPane.PLAIN_MESSAGE);
			});
			
			// create a GraphMouse for each view
			// each one has a different scaling plugin
			DefaultModalGraphMouse<String, Integer> gm1 = new DefaultModalGraphMouse<String, Integer>() {
				protected void loadPlugins() {
					pickingPlugin = new PickingGraphMousePlugin<String, Integer>();
					animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<String, Integer>();
					translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
					scalingPlugin = new ScalingGraphMousePlugin(new LayoutScalingControl(), 0);
					rotatingPlugin = new RotatingGraphMousePlugin();
					shearingPlugin = new ShearingGraphMousePlugin();

					add(scalingPlugin);
					setMode(Mode.TRANSFORMING);
				}
			};
			
			vv.setGraphMouse(gm1);
			
//			vv.setToolTipText("<html><center>MouseWheel Scales Layout</center></html>");
			vv.addPostRenderPaintable(new BannerLabel(vv, viewName));

			textArea = new JTextArea(6, 30);
			scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setEditable(false);
			
		    // the regular graph mouse for the normal view
		    AbstractModalGraphMouse graphMouseP = new DefaultModalGraphMouse<>();

		    vv.addKeyListener(graphMouseP.getModeKeyListener());
			
		    JMenuBar menubarP = new JMenuBar();
		    menubarP.add(graphMouseP.getModeMenu());
		    JComboBox<Position> cb = new JComboBox<>();
		    cb.addItem(Renderer.NodeLabel.Position.N);
		    cb.addItem(Renderer.NodeLabel.Position.NE);
		    cb.addItem(Renderer.NodeLabel.Position.E);
		    cb.addItem(Renderer.NodeLabel.Position.SE);
		    cb.addItem(Renderer.NodeLabel.Position.S);
		    cb.addItem(Renderer.NodeLabel.Position.SW);
		    cb.addItem(Renderer.NodeLabel.Position.W);
		    cb.addItem(Renderer.NodeLabel.Position.NW);
		    cb.addItem(Renderer.NodeLabel.Position.N);
		    cb.addItem(Renderer.NodeLabel.Position.CNTR);
		    cb.addItem(Renderer.NodeLabel.Position.AUTO);
		    cb.addItemListener(
		        e -> {
		          Renderer.NodeLabel.Position position = (Renderer.NodeLabel.Position) e.getItem();
		          vv.getRenderer().getNodeLabelRenderer().setPosition(position);
		          vv.repaint();
		        });

		    cb.setSelectedItem(Renderer.NodeLabel.Position.SE);

			JPanel flow = new JPanel();
			flow.add(h1);
			flow.add(gm1.getModeComboBox());
			flow.add(cb);
			p1.add(flow, BorderLayout.SOUTH);

			panel.add(p1);	
			cnt++;
		}
		add(panel);
	}

	class BannerLabel implements VisualizationViewer.Paintable {
		int x;
		int y;
		Font font;
		FontMetrics metrics;
		int swidth;
		int sheight;
		String str;
		VisualizationViewer<String, Integer> vv;

		public BannerLabel(VisualizationViewer<String, Integer> vv, String label) {
			this.vv = vv;
			this.str = label;
		}

		public void paint(Graphics g) {
			Dimension d = vv.getSize();
			if (font == null) {
				font = new Font(g.getFont().getName(), Font.BOLD, 30);
				metrics = g.getFontMetrics(font);
				swidth = metrics.stringWidth(str);
				sheight = metrics.getMaxAscent() + metrics.getMaxDescent();
				x = (3 * d.width / 2 - swidth) / 2;
				y = d.height - sheight;
			}
			g.setFont(font);
			Color oldColor = g.getColor();
			g.setColor(Color.gray);
			g.drawString(str, x, y);
			g.setColor(oldColor);
		}

		public boolean useTransform() {
			return false;
		}
	}

	private CTreeNetwork<String, Integer> createTree(ResultTree resultTree) {

		ListenableUndirectedGraph<ResultNode, RelationshipInfra> anOutputTree = resultTree.anOutputTree;

		Set<ResultNode> vertices = anOutputTree.vertexSet();

		// Set<RelationshipInfra> edges = resultTree.anOutputTree.edgeSet();

		MutableCTreeNetwork<String, Integer> tree = TreeNetworkBuilder.builder().expectedNodeCount(vertices.size())
				.build();

		int rootId = resultTree.rootNode.nodeId;
		tree.addNode(String.valueOf(rootId));

		HashMap<Integer, HashSet<String>> edgeMap = new HashMap<Integer, HashSet<String>>();
		for (RelationshipInfra e : anOutputTree.edgeSet()) {
			int src = anOutputTree.getEdgeSource(e).nodeId;
			int des = anOutputTree.getEdgeTarget(e).nodeId;
			// System.out.println("edge" + e.relId + ": " + src + " -> " + des);
			if (!edgeMap.containsKey(des)) {
				HashSet<String> srcS = new HashSet<String>();
				srcS.add(String.valueOf(src) + "," + String.valueOf(e.relId));
				edgeMap.put(des, srcS);
			} else {
				// System.out.println("Before = " + edgeMap.get(des));
				edgeMap.get(des).add(String.valueOf(src) + "," + String.valueOf(e.relId));
				// System.out.println("After = " + edgeMap.get(des));
			}
		}

		PriorityQueue<Integer> queue = new PriorityQueue<Integer>();
		queue.add(rootId);

		while (!queue.isEmpty()) {
			int curId = queue.poll();
			HashSet<String> srcS = edgeMap.get(curId);
			if (srcS != null) {
				for (String str : srcS) {
					String[] strTem = str.split(",");
					tree.addEdge(String.valueOf(curId), strTem[0], Integer.parseInt(strTem[1]));
					queue.add(Integer.parseInt(strTem[0]));
				}
			}
		}

		return tree;
	}

	// public static void main(String[] args) {
	// JFrame f = new JFrame();
	// f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	// f.getContentPane().add(new MultiViewDemo());
	// f.pack();
	// f.setVisible(true);
	// }
}
