package it.processmining.hmpp.ui;

import it.processmining.hmpp.models.HMPPHeuristicsNet;
import it.processmining.hmpp.ui.widget.HMPPOverlapMatrix;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import org.deckfour.gantzgraf.ui.GGGraphView;
import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;


/**
 * This is the HM++ result panel, that contains all the algorithm output
 * 
 * @author Andrea Burattin
 */
public class HMPPResultPanel extends GradientPanel implements Provider, 
	ActionListener {

	
	private static final long serialVersionUID = -4360079329722737772L;
	
	private HMPPHeuristicsNet net;
	private ArrayList<String> transitions;
	private DoubleMatrix1D totalActivityTime;
	private DoubleMatrix1D totalActivityCounter;
	private DoubleMatrix2D totalOverlappingTime;
	private DoubleMatrix2D parallelCount;
	private DoubleMatrix2D overlapMeasureTime;
	private DoubleMatrix2D overlapMeasureCardinality;
	private boolean showSplitJoinSemantics = false;
	
	protected Color background = new Color(100, 100, 100);
	private JCheckBoxMenuItem checkMenu;
	private JPanel graphPanel = null;
	protected GGGraphView graphView;
	private JPanel timePanel = null;
	private JPanel overlapPanel = null;
	
	private ButtonGroup matrixTypeGroup = null;
	private JRadioButton matrixTypeTime = null;
	private JRadioButton matrixTypeCardinality = null;
	private JPanel descriptionPanelTime = null;
	private JPanel descriptionPanelCardinality = null;
	
	private HMPPOverlapMatrix matrixTime = null;
	private HMPPOverlapMatrix matrixCardinality = null;
	
	
	/* the graph renderer */
	private GrappaAdapter grappaAdapter = new GrappaAdapter() {
		/**
		 * The method is called when a mouse press occurs on a displayed subgraph. The
		 * returned menu is added to the end of the default right-click menu
		 *
		 * @param subg displayed subgraph where action occurred
		 * @param elem subgraph element in which action occurred
		 * @param pt the point where the action occurred (graph coordinates)
		 * @param modifiers mouse modifiers in effect
		 * @param panel specific panel where the action occurred
		 */
		protected JMenuItem getCustomMenu(Subgraph subg, Element elem, GrappaPoint pt,
				int modifiers,
				GrappaPanel panel) {
			return checkMenu;
		}
	};


	/**
	 * Object contructor
	 * 
	 * @param net the constructed net
	 * @param transitions array with all the transitions
	 * @param totalActivityTime vector with all the activity times
	 * @param totalActivityCounter vector with all the activity counter
	 * @param totalOverlappingTime matrix with all the overlapping time between
	 *                             two activities
	 * @param parallelCount matrix with all the parallel counter between two
	 *                      activities
	 * @param overlapMeasure matrix with the overlap measure (each value must be
	 *                       between 0 and 1)
	 */
	public HMPPResultPanel(HMPPHeuristicsNet net,
					 ArrayList<String> transitions,
					 DoubleMatrix1D totalActivityTime,
					 DoubleMatrix1D totalActivityCounter,
					 DoubleMatrix2D totalOverlappingTime,
					 DoubleMatrix2D parallelCount,
					 DoubleMatrix2D overlapMeasure,
					 DoubleMatrix2D overlapMeasureCardinality) {
		super(new Color(60, 60, 60), new Color(20, 20, 20));
		this.net = net;
		this.transitions = transitions;
		this.totalActivityTime = totalActivityTime;
		this.totalActivityCounter = totalActivityCounter;
		this.totalOverlappingTime = totalOverlappingTime;
		this.parallelCount = parallelCount;
		this.overlapMeasureTime = overlapMeasure;
		this.overlapMeasureCardinality = overlapMeasureCardinality;
		
		buildPanel();
		showIndividuals();
		setupUI();
	}


	/**
	 * This method prepares the GUI
	 */
	protected void setupUI() {
		// setup panel basics
		this.setDoubleBuffered(true);
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder());
		FlatTabbedPane tabPane = new FlatTabbedPane("", 
				new Color(240, 240, 240, 230), new Color(100, 100, 100), 
				new Color(220, 220, 220, 150));

		tabPane.addTab("Events Graph", graphPanel);
		tabPane.addTab("Times summary", timePanel);
		tabPane.addTab("Overlapping graph", overlapPanel);

		// assemble UI
		JPanel centerPanel = new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		centerPanel.add(tabPane, BorderLayout.CENTER);
		this.add(centerPanel, BorderLayout.CENTER);
		HeaderBar header = new HeaderBar("HeuristicsMiner++");
		header.setHeight(40);
		this.add(header, BorderLayout.NORTH);
	}


	/**
	 * This method prepares the panels
	 */
	private void buildPanel() {
		
		Color colorEnclosureBg = new Color(40, 40, 40);
		Color colorListSelectionFg = new Color(240, 240, 240);
		Color colorListBg = new Color(60, 60, 60);
		Color colorListFg = new Color(180, 180, 180);
		Color colorNonFocus = new Color(70, 70, 70);
		
		JTable instancesList;
		JTable overlapList;
		
		/* =========================== GRAPH PANEL ========================== */
		graphPanel = new JPanel(new BorderLayout());
		graphPanel.setOpaque(false);
		graphPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.Y_AXIS));
		
		checkMenu = new JCheckBoxMenuItem("Display split/join semantics");
		checkMenu.setSelected(showSplitJoinSemantics);

		checkMenu.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				showSplitJoinSemantics = 
					(e.getStateChange() == ItemEvent.SELECTED);
				showIndividuals();
			}
		});
		
		/* =========================== TIME PANEL =========================== */
		DecimalFormat f = new DecimalFormat("###.###");
		/* populate data */
		String[] instancesColumnData = {"Activity", "Total duration", 
				"Average duration"};
		Object[][] instancesData = new Object[transitions.size()][3];
		for (int i = 0; i < transitions.size(); i++) {
			instancesData[i][0] = transitions.get(i);
			instancesData[i][1] = f.format(totalActivityTime.get(i));
			instancesData[i][2] = f.format(totalActivityTime.get(i) / 
					totalActivityCounter.get(i));
		}
		
		ListModel lm = new AbstractListModel() {
			private static final long serialVersionUID = 6229638720959782801L;
			public int getSize() { 
				return transitions.size();
			}
			public Object getElementAt(int index) {
				String to_return = " " + transitions.get(index) + " ";
				return to_return;
			}
		};

		DefaultTableModel overlapModel = 
			new DefaultTableModel(transitions.toArray(), transitions.size());
		overlapList = new JTable(overlapModel) {
			private static final long serialVersionUID = -6630663209258046839L;
			public void changeSelection(int row, int column, boolean toggle, 
					boolean extend)
			{
				return;
			}
			public boolean isCellEditable(int row, int column){
				return false;
			}
		};
		JList rowHeader = new JList(lm);
		RowHeaderRenderer rhr = new RowHeaderRenderer(overlapList);
		rhr.setFont(overlapList.getFont().deriveFont(java.awt.Font.BOLD));
		rhr.setBackground(colorListFg);
		rhr.setForeground(colorEnclosureBg);
		rowHeader.setCellRenderer(rhr);
		rowHeader.setBackground(colorEnclosureBg);
		
		for (int i = 0; i < transitions.size(); i++) {
			for (int j = 0; j < transitions.size(); j++) {
				double o = 0;
				if (parallelCount.get(i, j) > 0) {
					o = totalOverlappingTime.get(i, j) / parallelCount.get(i,j);
				}
				overlapModel.setValueAt(f.format(o), i, j);
			}
		}
		
		overlapList.setBackground(colorListBg);
		overlapList.setForeground(colorListFg);
		overlapList.setFont(overlapList.getFont().deriveFont(13f));
		overlapList.setGridColor(colorEnclosureBg);
		overlapList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JTableHeader instancesHeaderOverlap = overlapList.getTableHeader();
		instancesHeaderOverlap.setFont(instancesHeaderOverlap.getFont().
				deriveFont(13f).deriveFont(java.awt.Font.BOLD));
		instancesHeaderOverlap.setBackground(colorListFg);
		instancesHeaderOverlap.setForeground(colorEnclosureBg);
		
		timePanel = new JPanel(new BorderLayout());
		timePanel.setOpaque(false);
		timePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
	
		instancesList = new JTable(instancesData, instancesColumnData) {
			private static final long serialVersionUID = 990260647413054778L;
			public void changeSelection(int row, int column, boolean toggle,
					boolean extend)
			{
				return;
			}
			public boolean isCellEditable(int row, int column){
				return false;
			}
		};
		instancesList.setBackground(colorListBg);
		instancesList.setForeground(colorListFg);
		instancesList.setFont(instancesList.getFont().deriveFont(13f));
		instancesList.setGridColor(colorEnclosureBg);
		JTableHeader instancesHeader = instancesList.getTableHeader();
		instancesHeader.setFont(instancesHeader.getFont().deriveFont(13f).
				deriveFont(java.awt.Font.BOLD));
		instancesHeader.setBackground(colorListFg);
		instancesHeader.setForeground(colorEnclosureBg);
		
		
		JLabel instancesListLabel = new JLabel("Event durations");
		instancesListLabel.setOpaque(false);
		instancesListLabel.setForeground(colorListSelectionFg);
		instancesListLabel.setFont(instancesListLabel.getFont().
				deriveFont(13f));
		instancesListLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		instancesListLabel.setHorizontalAlignment(JLabel.CENTER);
		instancesListLabel.setHorizontalTextPosition(JLabel.CENTER);
		
		JLabel overlapListLabel = new JLabel("Average overlapping times");
		overlapListLabel.setOpaque(false);
		overlapListLabel.setForeground(colorListSelectionFg);
		overlapListLabel.setFont(instancesListLabel.getFont().deriveFont(13f));
		overlapListLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		overlapListLabel.setHorizontalAlignment(JLabel.CENTER);
		overlapListLabel.setHorizontalTextPosition(JLabel.CENTER);
		rowHeader.setFixedCellHeight(overlapList.getRowHeight());
		
		JScrollPane instancesScrollPane = new JScrollPane(instancesList);
		instancesScrollPane.setOpaque(false);
		instancesScrollPane.getViewport().setOpaque(false);
		instancesScrollPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		instancesScrollPane.setVerticalScrollBarPolicy(JScrollPane.
				VERTICAL_SCROLLBAR_AS_NEEDED);
		instancesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.
				HORIZONTAL_SCROLLBAR_NEVER);
		JScrollBar vBar = instancesScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(160, 160, 160), colorNonFocus, 4, 12));
		vBar.setOpaque(false);
		
		JScrollPane overlapScrollPane = new JScrollPane(overlapList);
		overlapScrollPane.setOpaque(false);
		overlapScrollPane.getViewport().setOpaque(false);
		overlapScrollPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		overlapScrollPane.setVerticalScrollBarPolicy(JScrollPane.
				VERTICAL_SCROLLBAR_AS_NEEDED);
		overlapScrollPane.setHorizontalScrollBarPolicy(JScrollPane.
				HORIZONTAL_SCROLLBAR_AS_NEEDED);
		overlapScrollPane.setRowHeaderView(rowHeader);
		JScrollBar vBarOverlap = overlapScrollPane.getVerticalScrollBar();
		vBarOverlap.setUI(new SlickerScrollBarUI(vBarOverlap, 
				new Color(0, 0, 0, 0), new Color(160, 160, 160), 
				colorNonFocus, 4, 12));
		vBarOverlap.setOpaque(false);
		JScrollBar vBarHOverlap = overlapScrollPane.getHorizontalScrollBar();
		vBarHOverlap.setUI(new SlickerScrollBarUI(vBarHOverlap, 
				new Color(0, 0, 0, 0), new Color(160, 160, 160), 
				colorNonFocus, 4, 12));
		vBarHOverlap.setOpaque(false);
		
		RoundedPanel instancesPanel = new RoundedPanel(10, 5, 0);
		instancesPanel.setBackground(colorEnclosureBg);
		instancesPanel.setLayout(new BoxLayout(instancesPanel, 
				BoxLayout.Y_AXIS));
		instancesPanel.add(instancesListLabel);
		instancesPanel.add(Box.createVerticalStrut(8));
		instancesPanel.add(instancesScrollPane);
		
		RoundedPanel overlapsPanel = new RoundedPanel(10, 5, 0);
		overlapsPanel.setBackground(colorEnclosureBg);
		overlapsPanel.setLayout(new BoxLayout(overlapsPanel, BoxLayout.Y_AXIS));
		overlapsPanel.add(overlapListLabel);
		overlapsPanel.add(Box.createVerticalStrut(8));
		overlapsPanel.add(overlapScrollPane);
		
		timePanel.add(instancesPanel);
		timePanel.add(overlapsPanel);
		
		
		/* ========================== OVERLAP GRAPH ========================= */
		overlapPanel = new JPanel(new BorderLayout());
		overlapPanel.setOpaque(false);
		overlapPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		overlapPanel.setLayout(new BoxLayout(overlapPanel, BoxLayout.X_AXIS));
		
		matrixTypeTime = new JRadioButton("Time matrix", true);
		matrixTypeTime.setUI(new org.deckfour.slickerbox.ui.SlickerRadioButtonUI());
		matrixTypeTime.addActionListener(this);
		matrixTypeTime.setActionCommand("time");
		
		matrixTypeCardinality = new JRadioButton("Cardinality matrix");
		matrixTypeCardinality.setUI(new org.deckfour.slickerbox.ui.SlickerRadioButtonUI());
		matrixTypeCardinality.addActionListener(this);
		matrixTypeCardinality.setActionCommand("cardinality");
		
		matrixTypeGroup = new ButtonGroup();
		matrixTypeGroup.add(matrixTypeTime);
		matrixTypeGroup.add(matrixTypeCardinality);
		
		descriptionPanelTime = new JPanel(new BorderLayout());
		descriptionPanelTime.setOpaque(false);
		descriptionPanelCardinality = new JPanel(new BorderLayout());
		descriptionPanelCardinality.setOpaque(false);

		String welcomeStringTime = "<html>This matrix (activity x activity) is a " +
		"representation of the overlaps between activities. The more a " +
		"cell is red, the more the activities are overlapped. Each " +
		"overlap value is between 0 an 1 and is calculate as:" +
		"<center>" +
		"<i>avgOverlapTime</i><sub>ij</sub><sup>2</sup> &nbsp;&frasl;<br>" +
		"(<i>avgTime</i><sub>i</sub> * <i>avgTime</i><sub>j</sub>)" +
		"</center>" +
		"</html>";
		descriptionPanelTime.add(new JLabel(welcomeStringTime));
	
		String welcomeStringCard = "<html>This matrix (activity x activity) is a " +
		"representation of the overlaps between activities. The more a " +
		"cell is red, the more the activities appear overlapped. Each " +
		"overlap value is between 0 an 1 and is calculate as:" +
		"<center>" +
		"<i>overlapCount</i><sub>ij</sub><sup>2</sup> &nbsp;&frasl;<br>" +
		"(<i>count</i><sub>i</sub> * <i>count</i><sub>j</sub>)" +
		"</center>" +
		"</html>";
		descriptionPanelCardinality.add(new JLabel(welcomeStringCard));
		
		matrixTime = new HMPPOverlapMatrix(overlapMeasureTime, transitions);
		matrixCardinality = new HMPPOverlapMatrix(overlapMeasureCardinality, transitions);
		
		showMatrix(matrixTime, descriptionPanelTime);
	}
	
	
	/**
	 * Matrix to switch between a matrix and the other
	 * 
	 * @param matrix the matrix to show
	 * @param descriptionPanel the description panel to show
	 */
	private void showMatrix(HMPPOverlapMatrix matrix, JPanel descriptionPanel) {
		JPanel content = new JPanel(new BorderLayout());
		content.setOpaque(false);
		content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		content.setLayout(new BoxLayout(content,
				BoxLayout.Y_AXIS));
		content.setMaximumSize(new Dimension(500, 1000));
		content.setPreferredSize(new Dimension(200, 1000));
		
		JLabel title = new JLabel("Matrix type");
		title.setOpaque(false);
		title.setFont(title.getFont().deriveFont(15f));
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setHorizontalTextPosition(JLabel.CENTER);
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		
		content.add(title);
		content.add(Box.createVerticalStrut(7));
		content.add(matrixTypeTime);
		content.add(matrixTypeCardinality);
		content.add(Box.createVerticalStrut(15));
		content.add(descriptionPanel);
		content.add(Box.createVerticalGlue());
		
		RoundedPanel overlapMatrixPanel = new RoundedPanel(10, 5, 0);
		overlapMatrixPanel.setBackground(Color.BLACK);
		overlapMatrixPanel.setLayout(new BoxLayout(overlapMatrixPanel, 
				BoxLayout.Y_AXIS));
		overlapMatrixPanel.add(matrix);
		
		overlapPanel.removeAll();
		overlapPanel.add(content);
		overlapPanel.add(Box.createHorizontalStrut(10));
		overlapPanel.add(overlapMatrixPanel);
		overlapPanel.updateUI();
	}


	/**
	 * This method prepares the graph data
	 */
	private void showIndividuals() {
		ModelGraphPanel gp = null;

		if (this.showSplitJoinSemantics) {
			gp = net.getGrappaVisualizationWithSplitJoinSemantics();

		} else {
			gp = net.getGrappaVisualization();
		}
		gp.addGrappaListener(grappaAdapter);
		gp.setOpaque(false);
				
		JSplitPane mainPane = (JSplitPane)gp.getComponent(0);
		JSplitPane secondPane = (JSplitPane)((JPanel)mainPane.
				getBottomComponent()).getComponent(0);
		JScrollPane mainDraw = ((JScrollPane)mainPane.getLeftComponent());
		GrappaPanel thumbnailDraw =((GrappaPanel)secondPane.getLeftComponent());
		mainPane.setDividerSize(0);
		mainPane.setOpaque(false);
		mainDraw.setOpaque(false);
		secondPane.setDividerSize(0);
		secondPane.setDividerLocation(130);
		secondPane.setOpaque(false);
		
		RoundedPanel mdp = new RoundedPanel(10, 5, 0);
		mdp.setBackground(new Color(255, 255, 255));
		mdp.setLayout(new BoxLayout(mdp, BoxLayout.Y_AXIS));
		mdp.add(mainDraw);
		mainPane.setLeftComponent(mdp);
		
		RoundedPanel tnp = new RoundedPanel(10, 5, 0);
		tnp.setBackground(new Color(255, 255, 255));
		tnp.setLayout(new BoxLayout(tnp, BoxLayout.Y_AXIS));
		tnp.add(thumbnailDraw);
		secondPane.setTopComponent(tnp);
		thumbnailDraw.setBackground(new Color(255, 255, 255));
		
		/* zoom label container */
		((JPanel)gp.getZoomSlider().getParent().getParent()).setOpaque(false);
		
		/* zoom slider */
		JSlider sBox = gp.getZoomSlider();
		sBox.setUI(new org.deckfour.slickerbox.ui.SlickerSliderUI(sBox));
		sBox.setOpaque(false);
		/* graph scroll bars */
		JScrollBar vBar = gp.getScrollPane().getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0), 
				new Color(0, 0, 0), new Color(70, 70, 70), 4, 12));
		vBar.setOpaque(false);
		vBar = gp.getScrollPane().getHorizontalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0), 
				new Color(0, 0, 0), new Color(70, 70, 70), 4, 12));
		vBar.setOpaque(false);
		
		graphPanel.removeAll();
		graphPanel.add(gp, BorderLayout.CENTER);
	}
	

	@Override
	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] {new ProvidedObject("Heuristics net", 
				new Object[] {net})};
	}
	
	
	/**
	 * This method builds a message label correctly styled
	 * 
	 * @param message the message for the label
	 * @return the JLabel object
	 */
	public static JLabel createMessageLabel(String message) {
		JLabel messageLabel = new JLabel(message);
		messageLabel.setFont(messageLabel.getFont().deriveFont(10f));
		messageLabel.setHorizontalAlignment(JLabel.CENTER);
		messageLabel.setHorizontalTextPosition(JLabel.LEFT);
		messageLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		return messageLabel;
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		/* TODO correggere metodo */
		if (arg0.getActionCommand().equals("time")) {
			showMatrix(matrixTime, descriptionPanelTime);
		} else {
			showMatrix(matrixCardinality, descriptionPanelCardinality);
		}
	}

}


/**
 * This class is required for to show the first column as header 
 * 
 * @author Andrea Burattin
 */
class RowHeaderRenderer extends JLabel implements ListCellRenderer {


	private static final long serialVersionUID = 8358493340483811792L;

	
	/**
	 * Class constructor
	 * 
	 * @param table the reference table
	 */
	RowHeaderRenderer(JTable table) {
		JTableHeader header = table.getTableHeader();
		setOpaque(true);
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		setHorizontalAlignment(CENTER);
		setForeground(header.getForeground());
		setBackground(header.getBackground());
		setFont(header.getFont());
	}

	
	@Override
	public Component getListCellRendererComponent(JList list,  Object value, int index, boolean isSelected, boolean cellHasFocus) {
		setText((value == null) ? "" : value.toString());
		return this;
	}
}
