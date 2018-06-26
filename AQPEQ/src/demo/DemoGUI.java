package demo;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Shape;

import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JCheckBox;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import aqpeq.utilities.StringPoolUtility;
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ParallelEdgeShapeFunction;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.NodeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Context;
import graphInfra.GraphInfraReaderArray;
import graphInfra.NodeInfra;
import graphInfra.RelationshipInfra;
import neo4jBasedKWS.ResultNode;
import neo4jBasedKWS.ResultTree;
import queryExpansion.CostAndNodesOfAnswersPair;

import javax.swing.border.LineBorder;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.function.Function;
import java.awt.Paint;

import com.google.common.graph.Network;

import javax.swing.JRadioButton;
import javax.swing.JSpinner;

public class DemoGUI {

    private JFrame frame;
    private JTextField txtPleaseTypeKeywords;

    Configuration config = new Configuration();
    KWSHandler kwsHandler;
    JButton btnSearch;
    JButton btnExplore;
    JButton btnRollBack;
    private JTextField textCostOfResultTree;
    private JTextField txtX;
    private JTextField txtX_1;

    // config panel
    JComboBox<String> comboBox_KWS;
    JSpinner SPAnswerSize;
    JSpinner SPExpanded;
    JSlider sliderExploration;
    JSpinner spinCost;
    JSpinner spinLambda;
    JCheckBox chckbxTfidf;
    JCheckBox chckbxImportance;

    // result tree panel
    JPanel pSuggestKW;
    JPanel panelResult;

    Stack<Configuration> states = new Stack<Configuration>();
    boolean firstRollBack = true;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    DemoGUI window = new DemoGUI();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public DemoGUI() throws Exception {

        config.readTheDataset();
        initialize();
        // states.push(config);
    }

    /**
     * Initialize the contents of the frame.
     */
    public void initialize() throws Exception {
        frame = new JFrame();
        frame.getContentPane().setBackground(Color.WHITE);
        frame.setBounds(100, 0, 1194, 850);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JPanel panelSuggest = new JPanel();
        panelSuggest.setBounds(6, 6, 354, 816);
        panelSuggest.setBackground(Color.WHITE);
        frame.getContentPane().add(panelSuggest);
        panelSuggest.setLayout(null);

        JTextPane txtpnSuggestedKeywords = new JTextPane();
        txtpnSuggestedKeywords.setEditable(false);
        txtpnSuggestedKeywords.setText("Suggested Keywords");
        txtpnSuggestedKeywords.setForeground(Color.BLACK);
        txtpnSuggestedKeywords.setFont(new Font("Lucida Grande", Font.BOLD, 15));
        txtpnSuggestedKeywords.setBounds(6, 40, 168, 20);
        panelSuggest.add(txtpnSuggestedKeywords);

        JScrollPane pSuggestKWS = new JScrollPane();
        pSuggestKWS.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pSuggestKWS.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pSuggestKWS.setBorder(new LineBorder(Color.LIGHT_GRAY));
        pSuggestKWS.setBackground(Color.WHITE);
        pSuggestKWS.setBounds(6, 60, 340, 750);
        panelSuggest.add(pSuggestKWS);

        pSuggestKW = new JPanel();
        pSuggestKW.setBackground(Color.WHITE);
        // pSuggestKW.setBounds(0, 0, 335, 748);
        pSuggestKW.setLayout(null);
        pSuggestKWS.setViewportView(pSuggestKW);

        JScrollPane panelResultS = new JScrollPane();
        panelResultS.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panelResultS.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panelResultS.setBorder(new LineBorder(Color.LIGHT_GRAY));
        panelResultS.setBackground(Color.WHITE);
        panelResultS.setBounds(365, 308, 816, 514);
        frame.getContentPane().add(panelResultS);

        panelResult = new JPanel();
        panelResult.setBorder(new LineBorder(Color.LIGHT_GRAY));
        panelResult.setBackground(Color.WHITE);
        panelResult.setBounds(1, 1, 814, 512);
        // panelResult.setBounds(218, 308, 816, 514);
        // frame.getContentPane().add(panelResult);
        panelResult.setLayout(new GridLayout(1, 0, 1, 1));
        panelResultS.setViewportView(panelResult);

        JPanel panelConfig = new JPanel();
        panelConfig.setBorder(new LineBorder(Color.LIGHT_GRAY));
        panelConfig.setBackground(Color.WHITE);
        panelConfig.setBounds(365, 7, 816, 226);
        frame.getContentPane().add(panelConfig);
        panelConfig.setLayout(null);

        JPanel panel_4 = new JPanel();
        panel_4.setBackground(Color.WHITE);
        panel_4.setBounds(6, 102, 235, 27);
        panelConfig.add(panel_4);
        panel_4.setLayout(null);

        JPanel panel_11 = new JPanel();
        panel_11.setBackground(Color.WHITE);
        panel_11.setBounds(614, 70, 192, 145);
        panelConfig.add(panel_11);
        panel_11.setLayout(null);

        JTextPane txtpnRelevancyFunction = new JTextPane();
        txtpnRelevancyFunction.setEditable(false);
        txtpnRelevancyFunction.setText("Relevancy function");
        txtpnRelevancyFunction.setForeground(Color.BLACK);
        txtpnRelevancyFunction.setFont(new Font("Lucida Grande", Font.BOLD, 15));
        txtpnRelevancyFunction.setBounds(6, 6, 168, 19);
        panel_11.add(txtpnRelevancyFunction);

        chckbxTfidf = new JCheckBox("TF-IDF");
        chckbxTfidf.setBounds(6, 37, 128, 23);
        // chckbxTfidf.addActionListener(new ActionListener() {
        //
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // config.relevanceFunction.add("TFIDF");
        // System.out.println("choose importance");
        // }
        // });
        panel_11.add(chckbxTfidf);

        chckbxImportance = new JCheckBox("Importance");
        // chckbxImportance.addActionListener(new ActionListener() {
        //
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // config.relevanceFunction.add("IMPORTANCE");
        // System.out.println("choose importance");
        // }
        // });
        chckbxImportance.setBounds(6, 61, 128, 23);
        panel_11.add(chckbxImportance);

        JPanel panelSearch = new JPanel();
        panelSearch.setBorder(new LineBorder(Color.LIGHT_GRAY));
        panelSearch.setBackground(Color.WHITE);
        panelSearch.setBounds(365, 245, 816, 57);
        frame.getContentPane().add(panelSearch);
        panelSearch.setLayout(null);

        txtPleaseTypeKeywords = new JTextField();
        txtPleaseTypeKeywords.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        txtPleaseTypeKeywords.setForeground(Color.GRAY);
        txtPleaseTypeKeywords.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
        txtPleaseTypeKeywords.setText("Please type keywords");
        txtPleaseTypeKeywords.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!txtPleaseTypeKeywords.getText().contains(",")) {
                    txtPleaseTypeKeywords.setText("");
                    txtPleaseTypeKeywords.setForeground(Color.BLACK);
                }
            }
        });
        // txtPleaseTypeKeywords.setColumns(10);
        // txtPleaseTypeKeywords.setBounds(6, 6, 523, 45);
        // panelSearch.add(txtPleaseTypeKeywords);
        // spPleaseTypeKeywords.setViewportView(txtPleaseTypeKeywords);

        JScrollPane spPleaseTypeKeywords = new JScrollPane(txtPleaseTypeKeywords);
        spPleaseTypeKeywords.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        spPleaseTypeKeywords.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        spPleaseTypeKeywords.setBackground(Color.WHITE);
        spPleaseTypeKeywords.setBounds(6, 6, 470, 45);
        spPleaseTypeKeywords.setBorder(null);
        panelSearch.add(spPleaseTypeKeywords);

        btnSearch = new JButton("Search");

        btnSearch.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {


                if (!btnSearch.isEnabled()) {
                    return;
                }

                if (txtPleaseTypeKeywords.getText() == "Please type keywords") {
                    System.out.println("Please type Keywords");
                }

                if (config.keywords.isEmpty()) {
                    System.out.println("First searching.");
                    HashSet<String> keywords = new HashSet<String>();
                    String[] keywordTem = txtPleaseTypeKeywords.getText().split(",");
                    for (String str : keywordTem) {
                        keywords.add(str.toLowerCase().trim());
                    }
                    config.keywords = keywords;
                    System.out.println("keywords = " + config.keywords);
                    try {
                        kwsHandler = new KWSHandler(config);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else {
                    kwsHandler.config.searchInc = true;
                    System.out.println("inc searching.");
                    HashSet<String> keywords = config.keywords;
                    String[] keywordTem = txtPleaseTypeKeywords.getText().split(",");
                    for (String str : keywordTem) {
                        keywords.add(str.toLowerCase());
                    }
                    config.keywords = keywords;
//					System.out.println("keywords = " + config.keywords);
                }

                LinkedList<DemoResultTree> results = new LinkedList<DemoResultTree>();
                try {
                    results = kwsHandler.Search();
                    config.resultTrees = results;
                } catch (Exception e5) {
                    // TODO Auto-generated catch block
                    e5.printStackTrace();
                }

                config.choseResults = new HashSet<ResultTree>();
                // results = kwsHandler.getResultTrees();

                Collections.sort(results, new Comparator<DemoResultTree>() {
                    @Override
                    public int compare(DemoResultTree o1, DemoResultTree o2) {
                        return Double.compare(o1.tree.cost, o2.tree.cost);
                    }
                });

                if (!results.isEmpty()) {
                    // continue searching
                    btnSearch.setEnabled(false);
                    if (config.originalNodes.isEmpty()) {
                        HashSet<NodeInfra> originalNodes = new HashSet<NodeInfra>();
                        HashSet<RelationshipInfra> originalEdges = new HashSet<RelationshipInfra>();
                        for (DemoResultTree rt : results) {
                            for (ResultNode node : rt.tree.anOutputTree.vertexSet()) {
                                originalNodes.add(node.node);
                            }
                            for (RelationshipInfra edge : rt.tree.anOutputTree.edgeSet()) {
                                originalEdges.add(edge);
                            }
                        }
                        config.originalNodes = originalNodes;
                        config.originalEdges = originalEdges;
                    }

                    try {
                        // TODO: debug
                        // Configuration state = config.copy(config);
                        Configuration copy;// = new Configuration();
                        copy = (Configuration) config.clone();
                        states.push(copy);
//						firstRollBack = true;
                        if (config.searchInc && !states.isEmpty()) {
                            btnRollBack.setEnabled(true);
                        }

                    } catch (Exception e4) {
                        // TODO Auto-generated catch block
                        e4.printStackTrace();
                    }
                    // Draw result tree
                    LinkedList<DemoResultTree> finalResults = new LinkedList<>();
                    if (results.size() > config.answerSize) {
                        finalResults.addAll(results.subList(0, config.answerSize));
                    } else {
                        finalResults = results;
                    }

                    drawResultTree(finalResults);
                } else {
                    // search again
                    JOptionPane.showMessageDialog(frame, "Cannot find results, please try other keywords. ",
                            "Inane error", JOptionPane.ERROR_MESSAGE);
                    try {
                        txtPleaseTypeKeywords.setText("Please type keywords");
                        config.keywords = new HashSet<String>();
                        config.searchInc = false;
                        kwsHandler = new KWSHandler(config);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

            }
        });
        btnSearch.setForeground(Color.BLACK);
        btnSearch.setBackground(new Color(0, 153, 255));
        btnSearch.setBounds(480, 16, 78, 29);
        panelSearch.add(btnSearch);

        btnExplore = new JButton("Explore");
        btnExplore.setEnabled(false);
        btnExplore.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {

                if (!btnExplore.isEnabled()) {
                    return;
                }

                config.relevanceFunction = new HashSet<String>();

                if (chckbxTfidf.isSelected()) {
                    config.relevanceFunction.add("TFIDF");
                }
                if (chckbxImportance.isSelected()) {
                    config.relevanceFunction.add("IMPORTANCE");
                }
                try {
                    System.out.println("Relevance function = " + config.relevanceFunction);
                    kwsHandler.Explore();
                } catch (Exception e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>> suggestGroup = config.groupedSuggestions;
                System.out.println("suggestGroup contains " + suggestGroup.size());

                if (suggestGroup.size() > 0) {

                    if (!states.isEmpty())
                        states.peek().groupedSuggestions = suggestGroup;

                    // draw suggestion panel
                    drawSuggestionPanel(suggestGroup);
                } else {
                    // show no more suggestions
                    JOptionPane.showMessageDialog(frame, "No more suggested keywords. Please click Reset button",
                            "Inane warning", JOptionPane.WARNING_MESSAGE);
                }

            }
        });
        btnExplore.setBounds(560, 16, 78, 29);
        panelSearch.add(btnExplore);

        JTextPane txtpnDataset = new JTextPane();
        txtpnDataset.setEditable(false);
        txtpnDataset.setBounds(110, 3, 66, 19);
        panel_4.add(txtpnDataset);
        txtpnDataset.setFont(new Font("Lucida Grande", Font.BOLD, 15));
        txtpnDataset.setText("Dataset");

        JPanel panel_5 = new JPanel();
        panel_5.setBackground(Color.WHITE);
        panel_5.setBounds(6, 131, 235, 27);
        panelConfig.add(panel_5);
        panel_5.setLayout(null);

        comboBox_KWS = new JComboBox<String>();
        comboBox_KWS.setBounds(0, 0, 110, 27);
        comboBox_KWS.addItem("DR");
        comboBox_KWS.addItem("ST");
        comboBox_KWS.addItem("SG");
        comboBox_KWS.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    resetAction(false);
                    config.KWS = comboBox_KWS.getSelectedItem().toString();
                    config.keywords.clear();
                    config.searchInc = false;
                    config.KWChoose = "";
                    config.choseResults = new HashSet<ResultTree>();
                    config.originalNodes = new HashSet<NodeInfra>();
                    config.originalEdges = new HashSet<RelationshipInfra>();
                    config.estimatedWeightOfSuggestedKeywordMap = new HashMap<Integer, CostAndNodesOfAnswersPair>();
                    config.groupedSuggestions = new HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>>();

                    try {
                        kwsHandler = new KWSHandler(config);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    System.out.println("KWS = " + config.KWS);
                }
            }
        });
        panel_5.add(comboBox_KWS);

        JTextPane txtpnKwsClass = new JTextPane();
        txtpnKwsClass.setEditable(false);
        txtpnKwsClass.setBounds(110, 2, 81, 19);
        panel_5.add(txtpnKwsClass);
        txtpnKwsClass.setFont(new Font("Lucida Grande", Font.BOLD, 15));
        txtpnKwsClass.setText("KWS class");

        JPanel panel_6 = new JPanel();
        panel_6.setBackground(Color.WHITE);
        panel_6.setBounds(6, 70, 235, 32);
        panelConfig.add(panel_6);
        panel_6.setLayout(null);

        JTextPane txtpnConfigurationPanel = new JTextPane();
        txtpnConfigurationPanel.setEditable(false);
        txtpnConfigurationPanel.setForeground(Color.GRAY);
        txtpnConfigurationPanel.setFont(new Font("Lucida Grande", Font.BOLD, 15));
        txtpnConfigurationPanel.setText("Configuration Panel");
        txtpnConfigurationPanel.setBounds(6, 6, 168, 19);
        panel_6.add(txtpnConfigurationPanel);

        JPanel panel_7 = new JPanel();
        panel_7.setBackground(Color.WHITE);
        panel_7.setLayout(null);
        panel_7.setBounds(6, 162, 235, 27);
        panelConfig.add(panel_7);

        JTextPane txtpnAnswers = new JTextPane();
        txtpnAnswers.setEditable(false);
        txtpnAnswers.setText("Answers");
        txtpnAnswers.setFont(new Font("Lucida Grande", Font.BOLD, 15));
        txtpnAnswers.setBounds(110, 3, 72, 19);
        panel_7.add(txtpnAnswers);

        SPAnswerSize = new JSpinner();
        SPAnswerSize.setValue(config.DEFAULT_ANSWERS_SIZE);
        SPAnswerSize.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                config.answerSize = (int) SPAnswerSize.getValue();
            }
        });
        SPAnswerSize.setBounds(0, 0, 106, 27);
        panel_7.add(SPAnswerSize);

        JPanel panel_8 = new JPanel();
        panel_8.setBackground(Color.WHITE);
        panel_8.setLayout(null);
        panel_8.setBounds(6, 193, 235, 27);
        panelConfig.add(panel_8);

        JTextPane txtpnExpandedQueries = new JTextPane();
        txtpnExpandedQueries.setEditable(false);
        txtpnExpandedQueries.setText("Expanded queries");
        txtpnExpandedQueries.setFont(new Font("Lucida Grande", Font.BOLD, 15));
        txtpnExpandedQueries.setBounds(110, 3, 132, 19);
        panel_8.add(txtpnExpandedQueries);

        SPExpanded = new JSpinner();
        SPExpanded.setValue(config.DEFAULT_EXPANSION_SIZE);
        SPExpanded.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                config.expandedQueriess = (int) SPExpanded.getValue();
            }
        });
        SPExpanded.setBounds(0, 0, 106, 27);
        panel_8.add(SPExpanded);

        JPanel panel_9 = new JPanel();
        panel_9.setBackground(Color.WHITE);
        panel_9.setBounds(253, 70, 334, 51);
        panelConfig.add(panel_9);
        panel_9.setLayout(null);

        JTextPane txtpnExplorationRange = new JTextPane();
        txtpnExplorationRange.setEditable(false);
        txtpnExplorationRange.setText("Exploration");
        txtpnExplorationRange.setFont(new Font("Lucida Grande", Font.BOLD, 15));
        txtpnExplorationRange.setBounds(6, 6, 132, 19);
        panel_9.add(txtpnExplorationRange);

        sliderExploration = new JSlider(1, 6, 2);
        sliderExploration.setBounds(138, 6, 190, 39);
        // sliderExploration.setMinimum(1);
        // sliderExploration.setMaximum(6);
        sliderExploration.setMajorTickSpacing(1);
        // sliderExploration.set
        sliderExploration.setPaintLabels(true);
        sliderExploration.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int explorRange = sliderExploration.getValue();
                config.explorationRange = explorRange;
                System.out.println("explorate range = " + explorRange);
            }
        });
        panel_9.add(sliderExploration);

        JTextPane txtpnRange = new JTextPane();
        txtpnRange.setEditable(false);
        txtpnRange.setText("Range");
        txtpnRange.setFont(new Font("Lucida Grande", Font.BOLD, 15));
        txtpnRange.setBounds(20, 26, 53, 19);
        panel_9.add(txtpnRange);

        JPanel panel_10 = new JPanel();
        panel_10.setBackground(Color.WHITE);
        panel_10.setLayout(null);
        panel_10.setBounds(253, 171, 334, 43);
        panelConfig.add(panel_10);

        JTextPane txtpnCostBound = new JTextPane();
        txtpnCostBound.setEditable(false);
        txtpnCostBound.setText("Cost bound");
        txtpnCostBound.setFont(new Font("Lucida Grande", Font.BOLD, 15));
        txtpnCostBound.setBounds(6, 11, 132, 19);
        panel_10.add(txtpnCostBound);

        Double current = new Double(1.00);
        Double min = new Double(0.00);
        Double max = new Double(10.00);
        Double step = new Double(0.25);
        SpinnerNumberModel m_numberSpinnerModel = new SpinnerNumberModel(current, min, max, step);
        spinCost = new JSpinner(m_numberSpinnerModel);
        spinCost.setToolTipText("Delta");
        spinCost.setBounds(175, 8, 40, 28);
        spinCost.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                config.costBound = (double) spinCost.getValue();
            }
        });
        panel_10.add(spinCost);

        txtX = new JTextField();
        txtX.setText("(1+");
        txtX.setEditable(false);
        txtX.setColumns(10);
        txtX.setBounds(150, 8, 25, 28);
        txtX.setBorder(null);
        panel_10.add(txtX);

        txtX_1 = new JTextField();
        txtX_1.setText(") •");
        txtX_1.setEditable(false);
        txtX_1.setColumns(10);
        txtX_1.setBounds(220, 8, 20, 28);
        txtX_1.setBorder(null);
        panel_10.add(txtX_1);

        textCostOfResultTree = new JTextField();
        textCostOfResultTree.setToolTipText("");
        textCostOfResultTree.setBounds(243, 8, 49, 28);
        // textCostOfResultTree.setBorder(null);
        panel_10.add(textCostOfResultTree);
        textCostOfResultTree.setEditable(false);
        textCostOfResultTree.setHorizontalAlignment(JTextField.RIGHT);
        textCostOfResultTree.setColumns(10);

        JPanel panel_12 = new JPanel();
        panel_12.setBackground(new Color(152, 30, 50));
        panel_12.setBounds(6, 6, 800, 62);
        panelConfig.add(panel_12);
        panel_12.setLayout(null);

        JTextPane txtpnGexp = new JTextPane();
        txtpnGexp.setEditable(false);
        txtpnGexp.setForeground(Color.WHITE);
        txtpnGexp.setBackground(new Color(152, 30, 50));
        txtpnGexp.setFont(new Font("Lucida Grande", Font.BOLD, 28));
        txtpnGexp.setText("GExp");
        txtpnGexp.setBounds(6, 6, 96, 40);
        panel_12.add(txtpnGexp);

        JTextPane txtpnCostawareGraphExploration = new JTextPane();
        txtpnCostawareGraphExploration.setEditable(false);
        txtpnCostawareGraphExploration.setForeground(Color.WHITE);
        txtpnCostawareGraphExploration.setBackground(new Color(152, 30, 50));
        txtpnCostawareGraphExploration.setText("Cost-aware Graph Exploration with Keywords");
        txtpnCostawareGraphExploration.setFont(new Font("Lucida Grande", Font.PLAIN, 26));
        txtpnCostawareGraphExploration.setBounds(106, 9, 688, 40);
        panel_12.add(txtpnCostawareGraphExploration);

        JPanel panel_Lambda = new JPanel();
        panel_Lambda.setLayout(null);
        panel_Lambda.setBackground(Color.WHITE);
        panel_Lambda.setBounds(253, 131, 334, 43);
        panelConfig.add(panel_Lambda);

        JTextPane txtpnLambda = new JTextPane();
        txtpnLambda.setText("Diversity");
        txtpnLambda.setFont(new Font("Lucida Grande", Font.BOLD, 15));
        txtpnLambda.setEditable(false);
        txtpnLambda.setBounds(6, 11, 132, 19);
        panel_Lambda.add(txtpnLambda);

        Double currentLambda = new Double(0.5);
        Double minLambda = new Double(0.0);
        Double maxLambda = new Double(1.0);
        Double stepLambda = new Double(0.1);
        SpinnerNumberModel lambda_numberSpinnerModel = new SpinnerNumberModel(currentLambda, minLambda, maxLambda,
                stepLambda);
        spinLambda = new JSpinner(lambda_numberSpinnerModel);
        spinLambda.setBounds(150, 8, 97, 28);
        spinLambda.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                config.lambda = (double) spinLambda.getValue();
            }
        });
        panel_Lambda.add(spinLambda);

        // JPanel panelPerformance = new JPanel();
        // panelPerformance.setBounds(1038, 6, 229, 816);

        JComboBox<String> comboBoxDataset = new JComboBox<String>();
        comboBoxDataset.setToolTipText("Choose a Dataset");
        comboBoxDataset.setBounds(0, 0, 110, 27);
        //comboBoxDataset.addItem("sample");
        comboBoxDataset.addItem("DBPKW");
        comboBoxDataset.addItem("DBPFun");
        comboBoxDataset.addItem("DBPPolitic");
        comboBoxDataset.addItem("IMDB1");
        //comboBoxDataset.addItem("IMDB2");
        comboBoxDataset.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    config.DBName = (comboBoxDataset.getSelectedItem().toString());
                    try {
                        resetAction(true);
                        config = new Configuration(config.DBName);
                        config.readTheDataset();
                        frame.repaint();

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    System.out.println("DBName = " + config.DBName);
                }
            }
        });
        panel_4.add(comboBoxDataset);

        JButton btnReset = new JButton("Reset");
        btnReset.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {

                resetAction(true);

                config.KWS = "DR";
                config.keywords = new HashSet<String>();
                config.answerSize = config.DEFAULT_ANSWERS_SIZE;
                config.expandedQueriess = config.DEFAULT_EXPANSION_SIZE;
                config.explorationRange = config.DEFAULT_EXPLORAITON_RANGE;
                config.costBound = config.DEFAULT_COST_BOUND;
                config.relevanceFunction = new HashSet<String>();
                config.lambda = config.DEFAULT_LAMBDA;
                config.searchInc = false;
                config.KWChoose = "";
                config.choseResults = new HashSet<ResultTree>();
                config.originalNodes = new HashSet<NodeInfra>();
                config.originalEdges = new HashSet<RelationshipInfra>();
                config.estimatedWeightOfSuggestedKeywordMap = new HashMap<Integer, CostAndNodesOfAnswersPair>();
                config.groupedSuggestions = new HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>>();
                frame.repaint();
            }
        });
        btnReset.setBackground(new Color(0, 153, 255));
        btnReset.setBounds(735, 16, 78, 29);
        panelSearch.add(btnReset);

        btnRollBack = new JButton("Roll Back");

        btnRollBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!btnRollBack.isEnabled()) {
                    return;
                }
////				System.out.println("Before roll back: " + states.size());
//					if (firstRollBack) {
//						states.pop();
//					}
////					System.out.println("After roll back: " + states.size());
//					firstRollBack = false;
//					if (states.size() == 1) {
//						Configuration state = states.peek();
//						rollBackAction(state);
//					} else {
//						Configuration state = states.pop();
//						rollBackAction(state);
//					}
                states.pop();
                Configuration state = states.pop();
                rollBackAction(state);

            }
        });
        btnRollBack.setEnabled(false);
        btnRollBack.setBounds(645, 16, 85, 29);
        panelSearch.add(btnRollBack);

        // frame.getContentPane().add(panelPerformance);
    }

    public void rollBackAction(Configuration state) {
        // update keyword panel
        String[] tem = txtPleaseTypeKeywords.getText().split(",");
        String updateKW = "";

        int cnt = 1;
        for (int i = 0; i < tem.length - 1; i++) {
            if (cnt < tem.length - 1) {
                updateKW += tem[i] + ",";
            } else {
                updateKW += tem[i];
            }
            cnt++;
        }
        txtPleaseTypeKeywords.setText(updateKW);

        // update cost panel
        textCostOfResultTree.setText("");
        textCostOfResultTree.setToolTipText("");

        // update suggestion panel
        HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>> suggestGroup = state.groupedSuggestions;
        drawSuggestionPanel(suggestGroup);

        // update result tree panel
        LinkedList<DemoResultTree> results = state.resultTrees;
        drawResultTree(results);

        if (states.isEmpty()) {
            btnRollBack.setEnabled(false);
        }

        Configuration copy;// = new Configuration();
        copy = (Configuration) state.clone();
        states.push(copy);

    }

    public void resetAction(boolean resetKWSCombo) {
        txtPleaseTypeKeywords.setForeground(Color.GRAY);
        txtPleaseTypeKeywords.setText("Please type keywords");
        panelResult.removeAll();
        pSuggestKW.removeAll();

        if (resetKWSCombo)
            comboBox_KWS.setSelectedIndex(0);

        // comboBoxAnswerSize.setSelectedIndex(0);
        SPAnswerSize.setValue(config.DEFAULT_ANSWERS_SIZE);
        SPExpanded.setValue(config.DEFAULT_EXPANSION_SIZE);
        sliderExploration.setValue(config.DEFAULT_EXPLORAITON_RANGE);
        // sliderCost.setValue(6);
        spinCost.setValue(config.DEFAULT_COST_BOUND);
        spinLambda.setValue(config.DEFAULT_LAMBDA);
        chckbxTfidf.setSelected(false);
        chckbxImportance.setSelected(false);
        // config = new Configuration();
        btnSearch.setEnabled(true);
        btnExplore.setEnabled(false);
        btnRollBack.setEnabled(false);

        textCostOfResultTree.setText("");
        textCostOfResultTree.setToolTipText("");

        states.clear();


        panelResult.repaint();
        pSuggestKW.repaint();

    }

    private void suggestionTableSorting(ArrayList<CostAndNodesOfAnswersPair> suggestedKW) {
        if (chckbxTfidf.isSelected() && chckbxImportance.isSelected()) {
            Collections.sort(suggestedKW, new Comparator<CostAndNodesOfAnswersPair>() {
                @Override
                public int compare(CostAndNodesOfAnswersPair o1, CostAndNodesOfAnswersPair o2) {
                    if (o1.getTfIdf() == o2.getTfIdf()) {
                        return Double.compare(o2.getImportance(), o1.getImportance());
                    }
                    return Double.compare(o2.getTfIdf(), o1.getTfIdf());
                }
            });
        } else if (chckbxTfidf.isSelected()) {
            Collections.sort(suggestedKW, new Comparator<CostAndNodesOfAnswersPair>() {
                @Override
                public int compare(CostAndNodesOfAnswersPair o1, CostAndNodesOfAnswersPair o2) {
                    if (o1.getTfIdf() == o2.getTfIdf()) {
                        return Double.compare(o1.cost, o2.cost);
                    }
                    return Double.compare(o2.getTfIdf(), o1.getTfIdf());
                }
            });
        } else if (chckbxImportance.isSelected()) {
            Collections.sort(suggestedKW, new Comparator<CostAndNodesOfAnswersPair>() {
                @Override
                public int compare(CostAndNodesOfAnswersPair o1, CostAndNodesOfAnswersPair o2) {
                    if (o1.getImportance() == o2.getImportance()) {
                        return Double.compare(o1.cost, o2.cost);
                    }
                    return Double.compare(o2.getImportance(), o1.getImportance());
                }
            });
        }
    }

    public void drawSuggestionPanel(HashMap<Integer, ArrayList<CostAndNodesOfAnswersPair>> suggestGroup) {

        String keywordInput = txtPleaseTypeKeywords.getText();
        pSuggestKW.removeAll();

        int y = 0;
        ButtonGroup group = new ButtonGroup();
        ArrayList<Integer> tablesHeight = new ArrayList<Integer>();
        for (int labelId : suggestGroup.keySet()) {
            try {
                String label = StringPoolUtility.getStringOfId(labelId);
                ArrayList<CostAndNodesOfAnswersPair> suggestedKW = suggestGroup.get(labelId);

                suggestionTableSorting(suggestedKW);

                System.out.println(label + ", url size = " + suggestedKW.size());

                String[] columnNames = {"Suggestion", "Cost", "TFIDF", "Importance"};
                Object[][] data = new Object[suggestedKW.size()][4];

                int i = 0;
                for (CostAndNodesOfAnswersPair kw : suggestedKW) {

                    JRadioButton button = new JRadioButton(StringPoolUtility.getStringOfId(kw.keywordIndex));
                    button.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            config.KWChoose = button.getText();
                            String cur = keywordInput;
                            cur += "," + button.getText();
                            txtPleaseTypeKeywords.setText(cur);
                            System.out.println("User chooses " + config.KWChoose + " to be added keyword.");
                            btnSearch.setEnabled(true);
                            btnExplore.setEnabled(false);
                            btnRollBack.setEnabled(false);

                            group.clearSelection();
                            for (int m = 0; m < data.length; m++) {
                                JRadioButton tempRadioBtn = (JRadioButton) data[m][0];
                                tempRadioBtn.setSelected(false);
                                tempRadioBtn.repaint();
                                tempRadioBtn.setSelected(false);
                                tempRadioBtn.repaint();
                                System.out.println(tempRadioBtn.getText() + " selected? " + tempRadioBtn.isSelected());

                            }
                            button.setSelected(true);



                        }
                    });
                    data[i][0] = button;
                    data[i][1] = kw.cost;
                    double tfidf = kw.getTfIdf() * 100;
                    tfidf = Math.round(tfidf);
                    tfidf = tfidf / 100;
                    double importance = kw.getImportance() * 100;
                    importance = Math.round(importance);
                    importance = importance / 100;
                    // data[i][2] = FScore;
                    data[i][2] = tfidf;
                    data[i][3] = importance;

                    i++;
                }

                JTable table = new JTable(data, columnNames);

                for (int r = 0; r < suggestedKW.size(); r++) {
                    group.add((JRadioButton) table.getValueAt(r, 0));
                }

                table.getColumn("Suggestion").setCellRenderer(new RadioButtonRenderer());
                table.getColumn("Suggestion").setCellEditor(new RadioButtonEditor(new JCheckBox()));

                TableColumn column = null;
                for (int c = 0; c < columnNames.length; c++) {
                    column = table.getColumnModel().getColumn(c);
                    if (c == 0) {
                        column.setPreferredWidth(100);
                    } else if (c == 3) {
                        column.setPreferredWidth(40);
                    } else {
                        column.setPreferredWidth(20);
                    }

                }

                int sum = tablesHeight.stream().mapToInt(t -> t.intValue()).sum();

                int tableHeight = table.getRowHeight();
                //
                tableHeight = tableHeight * (suggestedKW.size() + 1);

                if (tableHeight > 100) {
                    tableHeight = 100;
                }

                JTextPane txtpnTableName = new JTextPane();
                txtpnTableName.setEditable(false);
                txtpnTableName.setText(label);
                txtpnTableName.setForeground(Color.BLACK);
                txtpnTableName.setFont(new Font("Lucida Grande", Font.BOLD, 15));
                txtpnTableName.setBounds(0, 20 * y + sum + (10 * y), 168, 20);

                JScrollPane spTable = new JScrollPane(table);
                spTable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                spTable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                spTable.setBorder(new LineBorder(Color.LIGHT_GRAY));
                spTable.setBackground(Color.WHITE);
                spTable.setBounds(0, 20 * y + sum + (10 * y) + 20, 330, tableHeight);

                table.setFillsViewportHeight(true);

                pSuggestKW.add(txtpnTableName);
                pSuggestKW.add(spTable);

                tablesHeight.add(tableHeight);

                y++;

            } catch (Exception e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

        }
        int height = 20 * y + (10 * y) + 20 + tablesHeight.stream().mapToInt(t -> t.intValue()).sum();
        pSuggestKW.setPreferredSize(new Dimension(350, height));
        pSuggestKW.revalidate();
        pSuggestKW.repaint();
    }

    public void drawResultTree(LinkedList<DemoResultTree> results) {

        // Draw result tree
        System.out.println("Draw Result Tree");
        panelResult.removeAll();
        // remove suggestion
        pSuggestKW.removeAll();
        pSuggestKW.repaint();

        textCostOfResultTree.setText("");
        textCostOfResultTree.setToolTipText("");

        for (DemoResultTree resultTree : results) {
            HashSet<NodeInfra> newNodes = resultTree.newNodes;
            HashSet<RelationshipInfra> newEdges = resultTree.newEdges;
//			System.out.println("result size = " + resultTree.tree.anOutputTree.vertexSet().size());
            // create a simple graph for the demo
            /** the graph */
            Network<NodeInfra, RelationshipInfra> graph;
            try {

                Dimension preferredSize = new Dimension(500, 300);

                graph = kwsHandler.createTree(resultTree);

                LayoutAlgorithm<NodeInfra> layoutAlgorithm = new CircleLayoutAlgorithm<>();
                VisualizationViewer<NodeInfra, RelationshipInfra> vv = new VisualizationViewer<>(graph, layoutAlgorithm,
                        preferredSize);
                vv.setBackground(Color.white);

                // NodeLabelRenderer nodeLabelRenderer =
                // vv.getRenderContext().getNodeLabelRenderer();
                //
                // EdgeLabelRenderer edgeLabelRenderer =
                // vv.getRenderContext().getEdgeLabelRenderer();

                ScalingControl scaler = new CrossoverScalingControl();

                Function<RelationshipInfra, String> stringer = e1 -> e1.toString();

                vv.getRenderContext().setEdgeLabelFunction(stringer);

                PickedState<RelationshipInfra> picked_state_edge = vv.getPickedEdgeState();
                EdgeFillColor<RelationshipInfra> edgeFillColor = new EdgeFillColor<RelationshipInfra>(picked_state_edge,
                        newEdges, config.originalEdges);
                vv.getRenderContext().setEdgeDrawPaintFunction(edgeFillColor);

                Function<NodeInfra, String> nodeStringer = n1 -> n1.toString();
                vv.getRenderContext().setNodeLabelFunction(nodeStringer);

                PickedState<NodeInfra> picked_state = vv.getPickedNodeState();

                SeedFillColor<NodeInfra> seedFillColor = new SeedFillColor<NodeInfra>(picked_state, newNodes,
                        config.originalNodes);
                vv.getRenderContext().setNodeFillPaintFunction(seedFillColor);

                vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.AUTO);

                // add my listener for ToolTips
                Function<NodeInfra, String> nodeStringer2 = n1 -> n1.toStringToolTips();
                vv.setNodeToolTipFunction(nodeStringer2);

                GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);

                DefaultModalGraphMouse<Integer, Number> graphMouse = new DefaultModalGraphMouse<>();
                vv.setGraphMouse(graphMouse);

                JButton plus = new JButton("+");
                plus.addActionListener(e1 -> scaler.scale(vv, 1.1f, vv.getCenter()));

                JButton minus = new JButton("-");
                minus.addActionListener(e1 -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

                graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

                EdgeClosenessUpdater edgeClosenessUpdater = new EdgeClosenessUpdater(vv);
                JSlider closenessSlider = new JSlider(edgeClosenessUpdater.rangeModel) {
                    public Dimension getPreferredSize() {
                        Dimension d = super.getPreferredSize();
                        d.width /= 2;
                        return d;
                    }
                };

                JSlider edgeOffsetSlider = new JSlider(0, 50) {
                    public Dimension getPreferredSize() {
                        Dimension d = super.getPreferredSize();
                        d.width /= 2;
                        return d;
                    }
                };
                edgeOffsetSlider.addChangeListener(e2 -> {
                    JSlider s = (JSlider) e2.getSource();
                    Function<Context<Network<NodeInfra, RelationshipInfra>, RelationshipInfra>, Shape> edgeShapeFunction = vv
                            .getRenderContext().getEdgeShapeFunction();
                    if (edgeShapeFunction instanceof ParallelEdgeShapeFunction) {
                        ((ParallelEdgeShapeFunction) edgeShapeFunction).setControlOffsetIncrement(s.getValue());
                        vv.repaint();
                    }
                });

                JPanel controls = new JPanel(new GridLayout(1, 0));
                controls.setBounds(0, 251, 340, 110);

                JPanel zoomPanel = new JPanel(new GridLayout(1, 0));
                zoomPanel.setBorder(BorderFactory.createTitledBorder("Scale"));
                zoomPanel.setBounds(0, 0, 5, 100);
                zoomPanel.add(plus);
                zoomPanel.add(minus);

                JPanel choosePanel = new JPanel(new GridLayout(0, 1));
                choosePanel.setBorder(BorderFactory.createTitledBorder("choose Answer"));

                JCheckBox choose = new JCheckBox("Cost = " + resultTree.tree.cost);
                choose.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        chooseAction(choose, resultTree);
                    }
                });

                choosePanel.add(choose);

                controls.add(zoomPanel);
                // controls.add(labelPanel);
                // controls.add(modePanel);
                controls.add(choosePanel);
                panel.add(controls, BorderLayout.SOUTH);

                panelResult.add(panel);
                panelResult.revalidate();
                panelResult.repaint();

            } catch (Exception e3) {
                // TODO Auto-generated catch block
                e3.printStackTrace();
            }
        }
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

    /**
     * subclassed to hold two BoundedRangeModel instances that are used by
     * JSliders to move the edge label positions
     *
     * @author Tom Nelson
     */
    class EdgeClosenessUpdater {
        BoundedRangeModel rangeModel;

        public EdgeClosenessUpdater(VisualizationViewer<NodeInfra, graphInfra.RelationshipInfra> vv) { // double
            // undirected,
            // double
            // directed)
            // {
            int initialValue = ((int) vv.getRenderContext().getEdgeLabelCloseness() * 10) / 10;
            this.rangeModel = new DefaultBoundedRangeModel(initialValue, 0, 0, 10);

            rangeModel.addChangeListener(e -> {
                vv.getRenderContext().setEdgeLabelCloseness(rangeModel.getValue() / 10f);
                vv.repaint();
            });
        }
    }

    private final class SeedFillColor<N> implements Function<N, Paint> {
        protected PickedInfo<N> pi;
        // boolean newNode;
        HashSet<NodeInfra> newNodes;
        HashSet<NodeInfra> originalNodes;

        public SeedFillColor(PickedInfo<N> pi, HashSet<NodeInfra> newNodes, HashSet<NodeInfra> originalNodes) {
            this.pi = pi;
            // this.newNode = newNode;
            this.newNodes = newNodes;
            this.originalNodes = originalNodes;
        }

        public Paint apply(N v) {
            if (pi.isPicked(v)) {
                return Color.YELLOW;
            } else {
                if (originalNodes.contains(v)) {
                    return Color.RED;
                } else if (newNodes.contains(v)) {
                    return Color.GREEN;
                } else {
                    return Color.BLUE;
                }
                // return Color.RED;
            }
        }
    }

    private final class EdgeFillColor<RelationshipInfra> implements Function<RelationshipInfra, Paint> {
        protected PickedInfo<RelationshipInfra> pi;
        // boolean newNode;
        HashSet<RelationshipInfra> newEdges;
        HashSet<RelationshipInfra> originalEdges;

        public EdgeFillColor(PickedInfo<RelationshipInfra> pi, HashSet<RelationshipInfra> newEdges,
                             HashSet<RelationshipInfra> originalEdges) {
            this.pi = pi;
            // this.newNode = newNode;
            this.newEdges = newEdges;
            this.originalEdges = originalEdges;
        }

        public Paint apply(RelationshipInfra e) {
            if (pi.isPicked(e)) {
                return Color.YELLOW;
            } else {
                if (originalEdges.contains(e)) {
                    return Color.RED;
                } else if (newEdges.contains(e)) {
                    return Color.GREEN;
                } else {
                    return Color.BLUE;
                }
                // return Color.RED;
            }
        }
    }

    public void chooseAction(JCheckBox choose, DemoResultTree resultTree) {

        if (choose.isSelected()) {
            btnExplore.setEnabled(true);
            btnRollBack.setEnabled(false);
            ResultTree choseResultTree = resultTree.tree;
            config.choseResults.add(choseResultTree);
            double fQG = 0.0;
            String newCost = "";
            if (textCostOfResultTree.getText().isEmpty()) {
                newCost = String.valueOf(resultTree.tree.cost);
                fQG = resultTree.tree.cost;
            } else {
                fQG = Double.parseDouble(textCostOfResultTree.getText());
                newCost = textCostOfResultTree.getToolTipText();
                newCost += "+" + resultTree.tree.cost;
                fQG += resultTree.tree.cost;
            }
            textCostOfResultTree.setToolTipText(newCost);
            textCostOfResultTree.setText(String.valueOf(fQG));
            System.out.println("User choose " + config.choseResults.size() + " answers.");
        } else {
            config.choseResults.remove(resultTree.tree);
            String removedKW = String.valueOf(resultTree.tree.cost);
            String[] tem = textCostOfResultTree.getToolTipText().split("[+]");
            String newCost = "";
            int cnt = 0;
            boolean remove = true;
            for (int i = 0; i < tem.length; i++) {
                if ((tem[i].equals(removedKW)) && (remove)) {
                    remove = false;
                } else {
                    if (cnt < config.choseResults.size()) {
                        newCost += tem[i] + "+";
                    } else {
                        newCost += tem[i];
                    }
                }
                cnt++;
            }
            textCostOfResultTree.setToolTipText(newCost);

            try {
                double fQG = Double.parseDouble(textCostOfResultTree.getText()) - resultTree.tree.cost;
                textCostOfResultTree.setText(String.valueOf(fQG));
            } catch (Exception ex) {
                System.err.println(textCostOfResultTree.getText() + " is not a numeric value");
                textCostOfResultTree.setText("");
            }

            System.out.println("User choose " + config.choseResults.size() + " answers.");
        }

    }

}

class RadioButtonRenderer implements TableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (value == null)
            return null;
        return (Component) value;
    }
}

class RadioButtonEditor extends DefaultCellEditor implements ItemListener {
    private JRadioButton button;

    public RadioButtonEditor(JCheckBox checkBox) {
        super(checkBox);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value == null)
            return null;
        button = (JRadioButton) value;
        // button.addItemListener(this);
        return (Component) value;
    }

    public Object getCellEditorValue() {
        button.removeItemListener(this);
        return button;
    }

    public void itemStateChanged(ItemEvent e) {
        super.fireEditingStopped();
    }
}
