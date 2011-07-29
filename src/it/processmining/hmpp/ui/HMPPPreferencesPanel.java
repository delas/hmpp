package it.processmining.hmpp.ui;

import it.processmining.hmpp.HMPP;
import it.processmining.hmpp.models.HMPPHeuristicsNet;
import it.processmining.hmpp.models.HMPPParameters;
import it.processmining.hmpp.ui.widget.HMPPHistogram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.ui.SlickerCheckBoxUI;
import org.processmining.exporting.heuristicsNet.HnExport;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.WaitDialog;


/**
 * This is the class for the the management of the algorithm parameters
 * @author Andrea Burattin
 */
public class HMPPPreferencesPanel extends JPanel 
	implements FocusListener, ChangeListener, MouseListener, 
		ListSelectionListener {


	private static final long serialVersionUID = 7336170449014439824L;
	private HMPPParameters parameters;
	protected LogReader log;
	protected HMPP algorithm;
	DecimalFormat dec = new DecimalFormat("#.###");
	
	/* gui objects */
	JTextField relativeToBestThresholdText;
	JTextField positiveObservationsThresholdText;
	JTextField dependencyThresholdText;
	JTextField l1lThresholdText;
	JTextField l2lThresholdText;
	JTextField LDThresholdText;
	JTextField dependencyDivisorText;
	JTextField andThresholdText;
	JCheckBox useAllConnectedHeuristics;
	JCheckBox useLongDistanceDependency;
	JButton exportAllThePossibleNetwork;
	
	JList relativeToBestList;
	HMPPHistogram positiveObsHisto;
	HMPPHistogram dependencyThresholdsHisto;
	

	/**
	 * Class constructors
	 * 
	 * @param parameters the algorithm parameters object
	 */
	public HMPPPreferencesPanel(HMPPParameters parameters, HMPP alg, 
			LogReader l) {
		this.parameters = parameters;
		this.algorithm = alg;
		this.log = l;
		
		/* add the waiting gui */
//		final Thread t = new Thread() {
//			public void run() {
				WaitDialog dialog = new WaitDialog(MainUI.getInstance(), "Preprocessing log...", "Please wait, \npreprocessing log...");
				dialog.setVisible(true);
				if (!algorithm.getBasicRelationsMade())
				{
					algorithm.makeBasicRelations(log, 0.8);
					algorithm.setBasicRelationsMade(true);
				}
				dialog.setVisible(false);
				setupUI();
				updateUI();
//			}
//		};
//		this.addAncestorListener(new AncestorListener() {
//			protected boolean hasRun = false;
//			public synchronized void ancestorAdded(AncestorEvent event) {
//				if(hasRun == false) {
//					hasRun = true;
//					t.start();
//				}
//			}
//			public void ancestorMoved(AncestorEvent event) {
//				// ignore
//			}
//			public void ancestorRemoved(AncestorEvent event) {
//				// ignore
//			}
//		});
	}
	
	
	/**
	 * This method builds the main GUI objects
	 */
	private void setupUI() {
		
		/* parameters panel */
		JLabel relativeToBestThresholdLabel = new JLabel(HMPPParameters.
				RELATIVE_TO_BEST_THRESHOLD_L);
		JLabel positiveObservationsThresholdLabel = new JLabel(HMPPParameters.
				POSITIVE_OBSERVATIONS_THRESHOLD_L);
		JLabel dependencyThresholdLabel = new JLabel(HMPPParameters.
				DEPENDENCY_THRESHOLD_L);
		JLabel l1lThresholdLabel = new JLabel(HMPPParameters.
				L1L_THRESHOLD_L);
		JLabel l2lThresholdLabel = new JLabel(HMPPParameters.
				L2L_THRESHOLD_L);
		JLabel LDThresholdLabel = new JLabel(HMPPParameters.
				LONG_DISTANCE_THRESHOLD_L);
		JLabel dependencyDivisorLabel = new JLabel(HMPPParameters.
				DEPENDENCY_DIVISOR_L);
		JLabel andThresholdLabel = new JLabel(HMPPParameters.
				AND_THRESHOLD_L);
		JLabel useAllConnectedHeuristicsLabel = new JLabel();
		JLabel useLongDistanceDependencyLabel = new JLabel();
		
		relativeToBestThresholdText = buildTextField();
		positiveObservationsThresholdText = buildTextField();
		dependencyThresholdText = buildTextField();
		l1lThresholdText = buildTextField();
		l2lThresholdText = buildTextField();
		LDThresholdText = buildTextField();
		dependencyDivisorText = buildTextField();
		andThresholdText = buildTextField();
		useAllConnectedHeuristics = new JCheckBox();
		useLongDistanceDependency = new JCheckBox();
		exportAllThePossibleNetwork = new SlickerButton("Export processes");
		exportAllThePossibleNetwork.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (!algorithm.getBasicRelationsMade()) {
					algorithm.makeBasicRelations(log, 0.8);
				}
				
				final Thread saverThread = new Thread() {
					public void run() {
						// select destination directory
						JFileChooser fc = new JFileChooser();
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						if(fc.showSaveDialog(HMPPPreferencesPanel.this) != JFileChooser.APPROVE_OPTION){
							return;
						}
						String saveDir = fc.getSelectedFile().getAbsolutePath();
						
						// export all the possible DIFFERENT processes
						HashSet<HeuristicsNet> processes = new HashSet<HeuristicsNet>();
						String[] relativeToBest = algorithm.getRelativeToBestValues();
						Set<Double> positiveObs = algorithm.getPositiveObsThresholdsValues().keySet();
						Set<Double> depThreshold = algorithm.getDependencyThresholdValues().keySet();
						
						WaitDialog dialog = new WaitDialog(MainUI.getInstance(), "Exporting net...", "Please wait, \nexporting net models...");
						dialog.setVisible(true);
						
						FileOutputStream os;
						HnExport export = new HnExport();
						
						StringBuilder CSV = new StringBuilder("ID;Relative to best;Positive observation;Dependency thr;\n");
						
						// iterate through all parameter configuration
						int i = 0;
						for (String rtb : relativeToBest) {
							for (Double po : positiveObs) {
								for (Double dt : depThreshold) {
		
									// prepare the configuration
									HMPPParameters para = new HMPPParameters();
									para.setRelativeToBestThreshold(Double.parseDouble(rtb));
									para.setPositiveObservationsThreshold(po.intValue());
									para.setDependencyThreshold(dt);
									algorithm.setParameters(para);
									HMPPHeuristicsNet net = algorithm.makeHeuristicsRelations(log);
									
									if (!processes.contains(net)) {
										processes.add(net);
										
										CSV.append(""+ i +";"+ rtb +";"+ po.intValue() +";"+ dt +"\n");
										
										// build the network and save it to the file
										try {
											os = new FileOutputStream(saveDir + File.separator + i + ".hn");
											export.export(new ProvidedObject("HeuristicsNet", net), os);
										} catch (FileNotFoundException ex) {
											ex.printStackTrace();
										} catch (IOException ex) {
											ex.printStackTrace();
										}
									}
									
									i++;
									
								}
							}
						}
						
						// write the information file
						try {
							os = new FileOutputStream(saveDir + File.separator + "info.csv");
							FileWriter fw = new FileWriter(saveDir + File.separator + "info.csv");
							fw.write(CSV.toString());
						} catch (FileNotFoundException ex) {
							ex.printStackTrace();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						
						dialog.setVisible(false);
					}
				};
				
				saverThread.start();
				
				
				
			}
		});
		
		relativeToBestThresholdText.addFocusListener(this);
		positiveObservationsThresholdText.addFocusListener(this);
		dependencyThresholdText.addFocusListener(this);
		l1lThresholdText.addFocusListener(this);
		l2lThresholdText.addFocusListener(this);
		LDThresholdText.addFocusListener(this);
		dependencyDivisorText.addFocusListener(this);
		andThresholdText.addFocusListener(this);
		useAllConnectedHeuristics.addFocusListener(this);
		useLongDistanceDependency.addFocusListener(this);
		
		useAllConnectedHeuristics.setSelected(true);
		useAllConnectedHeuristics.
			setText("Use all-activities-connected heuristic");
		useLongDistanceDependency.setSelected(false);
		useLongDistanceDependency.
			setText("Use long distance dependency heuristics");
		useAllConnectedHeuristics.setUI(new SlickerCheckBoxUI());
		useLongDistanceDependency.setUI(new SlickerCheckBoxUI());
		
		setParameters(parameters);
		
		JPanel parametersPanel = new JPanel();
		parametersPanel.setOpaque(false);
		parametersPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		parametersPanel.setLayout(new GridLayout(11, 2, 1, 1));
		
		//1: relativeToBestThreshold
		parametersPanel.add(relativeToBestThresholdLabel, null);
		parametersPanel.add(relativeToBestThresholdText, null);

		//2: positiveObservationsThreshold
		parametersPanel.add(positiveObservationsThresholdLabel, null);
		parametersPanel.add(positiveObservationsThresholdText, null);

		//3: dependencyThreshold
		parametersPanel.add(dependencyThresholdLabel, null);
		parametersPanel.add(dependencyThresholdText, null);

		//4: l1lThreshold
		parametersPanel.add(l1lThresholdLabel, null);
		parametersPanel.add(l1lThresholdText, null);

		//5: l2lThreshold
		parametersPanel.add(l2lThresholdLabel, null);
		parametersPanel.add(l2lThresholdText, null);

		//5b: LDThreshold
		parametersPanel.add(LDThresholdLabel, null);
		parametersPanel.add(LDThresholdText, null);

		//6: dependencyDivisor
		parametersPanel.add(dependencyDivisorLabel, null);
		parametersPanel.add(dependencyDivisorText, null);

		//7: andThreshold
		parametersPanel.add(andThresholdLabel, null);
		parametersPanel.add(andThresholdText, null);

		//8: intervalOverlap
		
		//9: extraInfo
		parametersPanel.add(useAllConnectedHeuristicsLabel, null);
		parametersPanel.add(useAllConnectedHeuristics, null);
		useAllConnectedHeuristics.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					parameters.setUseAllConnectedHeuristics(true);
				}
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					parameters.setUseAllConnectedHeuristics(false);
				}
			}
		});
		
		//10: longDistanceDependencyHeuristics
		parametersPanel.add(useLongDistanceDependencyLabel, null);
		parametersPanel.add(useLongDistanceDependency, null);
		useLongDistanceDependency.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					parameters.setUseLongDistanceDependency(true);
				}
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					parameters.setUseLongDistanceDependency(false);
				}
			}
		});
		

		/* configuration panel */
		JPanel configurationPanel = new JPanel();
		configurationPanel.setOpaque(false);
		configurationPanel.setBorder(BorderFactory.
				createEmptyBorder(10, 10, 10, 10));
		configurationPanel.setLayout(new BoxLayout(configurationPanel,
				BoxLayout.PAGE_AXIS));
		
		/* HISTOGRAMS ======================================================= */
		
		/* relative to best */
		String[] relativeToBestValues = algorithm.getRelativeToBestValues();
		relativeToBestList = new JList(relativeToBestValues);
		relativeToBestList.setBackground(Color.BLACK);
		relativeToBestList.setForeground(Color.WHITE);
		relativeToBestList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		relativeToBestList.setVisibleRowCount(2);
		relativeToBestList.setAlignmentX(LEFT_ALIGNMENT);
		relativeToBestList.addListSelectionListener(this);
		javax.swing.JScrollPane rtbScroll = new javax.swing.JScrollPane(relativeToBestList);
		rtbScroll.setBorder(BorderFactory.createEmptyBorder());
		rtbScroll.setAlignmentX(LEFT_ALIGNMENT);
		
		RoundedPanel relativeToBestContainer = new RoundedPanel(10, 5, 0);
		relativeToBestContainer.setBackground(Color.BLACK);
		relativeToBestContainer.setLayout(new BoxLayout(relativeToBestContainer, BoxLayout.Y_AXIS));
		relativeToBestContainer.add(rtbScroll);
		configurationPanel.add(new JLabel("Relative-to-best chooser"));
		configurationPanel.add(Box.createVerticalStrut(7));
		configurationPanel.add(relativeToBestContainer);
		configurationPanel.add(Box.createVerticalStrut(15));
		
		
		
		/* positive observations histogram */
		ArrayList<Integer> posObsThrVal = new ArrayList<Integer>();
		ArrayList<Double> posObsThrLbl = new ArrayList<Double>();
		HashMap<Double, Integer> posObsHistoMap =  algorithm.getPositiveObsThresholdsValues();
		Object[] posObsKeys = posObsHistoMap.keySet().toArray();
		Arrays.sort(posObsKeys);
		for (int i = 0; i < posObsKeys.length; i ++) {
			posObsThrVal.add(posObsHistoMap.get(posObsKeys[i]));
			posObsThrLbl.add((Double)posObsKeys[i]);
		}
		positiveObsHisto = new HMPPHistogram(posObsThrVal, posObsThrLbl);
		positiveObsHisto.addMouseListener(this);
		
		RoundedPanel positiveObsHistoContainer = new RoundedPanel(10, 5, 0);
		positiveObsHistoContainer.setBackground(Color.BLACK);
		positiveObsHistoContainer.setLayout(new BoxLayout(positiveObsHistoContainer, BoxLayout.Y_AXIS));
		positiveObsHistoContainer.add(positiveObsHisto);
		positiveObsHistoContainer.setMinimumSize(new java.awt.Dimension(10, 200));
		
		configurationPanel.add(new JLabel("Positive observations chooser"));
		configurationPanel.add(Box.createVerticalStrut(7));
		configurationPanel.add(positiveObsHistoContainer);
		configurationPanel.add(Box.createVerticalStrut(15));
		
		/* dependency thresholds histogram */
		ArrayList<Integer> depThrVal = new ArrayList<Integer>();
		ArrayList<Double> depThrLbl = new ArrayList<Double>();
		HashMap<Double, Integer> depThrHistoMap =  algorithm.getDependencyThresholdValues();
		Object[] depThrKeys = depThrHistoMap.keySet().toArray();
		Arrays.sort(depThrKeys);
		for (int i = 0; i < depThrKeys.length; i ++) {
			depThrVal.add(depThrHistoMap.get(depThrKeys[i]));
			depThrLbl.add((Double)depThrKeys[i]);
		}
		dependencyThresholdsHisto = new HMPPHistogram(depThrVal, depThrLbl);
		dependencyThresholdsHisto.addMouseListener(this);
		
		RoundedPanel dependencyThrHistoContainer = new RoundedPanel(10, 5, 0);
		dependencyThrHistoContainer.setBackground(Color.BLACK);
		dependencyThrHistoContainer.setLayout(new BoxLayout(dependencyThrHistoContainer, BoxLayout.Y_AXIS));
		dependencyThrHistoContainer.add(dependencyThresholdsHisto);
		configurationPanel.add(new JLabel("Dependency thresholds chooser"));
		configurationPanel.add(Box.createVerticalStrut(7));
		configurationPanel.add(dependencyThrHistoContainer);
		
		
		/* export panel */
		JPanel exportNetworkPanel = new JPanel();
		exportNetworkPanel.setOpaque(false);
		exportNetworkPanel.setBorder(BorderFactory.
				createEmptyBorder(10, 10, 10, 10));
		exportNetworkPanel.setLayout(new BoxLayout(exportNetworkPanel,
				BoxLayout.PAGE_AXIS));
		exportNetworkPanel.add(exportAllThePossibleNetwork);
		
		
		
		/* overall window */
		GradientPanel back = new GradientPanel(new Color(80, 80, 80), 
				new Color(40, 40, 40));
		back.setLayout(new BorderLayout());
		back.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		FlatTabbedPane tabs = new FlatTabbedPane("Configuration", 
				new Color(20, 20, 20, 230), new Color(160, 160, 160, 180), 
				new Color(220, 220, 220, 180));
		tabs.addTab("Parameters setup guidance", configurationPanel);
		tabs.addTab("Parameter details", parametersPanel);
		tabs.addTab("Export processes", exportNetworkPanel);
		back.add(tabs, BorderLayout.CENTER);
		HeaderBar header = new HeaderBar("HeuristicsMiner++");
		header.setHeight(40);
		
//		java.awt.Container a = this.getParent().getParent();
		java.awt.Container a = this;
		a.removeAll();
		a.setLayout(new BorderLayout());
		a.add(header, BorderLayout.NORTH);
//		a.add(tabs, BorderLayout.CENTER);
		a.add(back, BorderLayout.CENTER);
		a.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
//		this.getParent().getParent().setBackground(new Color(40, 40, 40));
//		this.getParent().setBackground(new Color(40, 40, 40));
		a.setBackground(new Color(40, 40, 40));
//		a.setOpaque(true);
	}

	
	/**
	 * This method correctly sets the parameters based on the GUI widget
	 * 
	 * @param parameters
	 */
	public void setParameters(HMPPParameters parameters) {
		parameters.setRelativeToBestThreshold(readRelativeToBestThreshold());
		parameters.setPositiveObservationsThreshold(
				readPositiveObservationsThreshold());
		parameters.setDependencyThreshold(readDependencyThreshold());
		parameters.setL1lThreshold(readL1lThreshold());
		parameters.setL2lThreshold(readL2lThreshold());
		parameters.setLDThreshold(readLDThreshold());
		parameters.setDependencyDivisor(readDependencyDivisor());
		parameters.setAndThreshold(readAndThreshold());
		parameters.setUseAllConnectedHeuristics(
				readUseAllConnectedHeuristics());
		parameters.setUseLongDistanceDependency(
				readUseLongDistanceDependency());
	}
	
	
	/**
	 * This method builds a message label correctly styled
	 * 
	 * @return the JLabel object
	 */
	private JTextField buildTextField() {
		JTextField tf = new JTextField(10);
		tf.setBackground(new Color(180, 180, 180));
		tf.setForeground(new Color(10, 10, 10));
		tf.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		return tf;
	}


	@Override
	public void focusGained(FocusEvent e) {
	}


	@Override
	public void focusLost(FocusEvent e) {
		/* remove suggestions */
		positiveObsHisto.suggestCountColumn(-1);
		dependencyThresholdsHisto.suggestCountColumn(-1);
		
		if (e.getSource() == relativeToBestThresholdText) { 
			parameters.setRelativeToBestThreshold(
					readRelativeToBestThreshold());
		} else if (e.getSource() == positiveObservationsThresholdText) {
			parameters.setPositiveObservationsThreshold(
					readPositiveObservationsThreshold());
			/* reset histogram value chooser */
			positiveObsHisto.setClickedColumn(-1);
		} else if (e.getSource() == dependencyThresholdText) {
			parameters.setDependencyThreshold(readDependencyThreshold());
			/* reset histogram value chooser */
			dependencyThresholdsHisto.setClickedColumn(-1);
		} else if (e.getSource() == l1lThresholdText) {
			parameters.setL1lThreshold(readL1lThreshold());
		} else if (e.getSource() == l2lThresholdText) {
			parameters.setL2lThreshold(readL2lThreshold());
		} else if (e.getSource() == LDThresholdText) {
			parameters.setLDThreshold(readLDThreshold());
		} else if (e.getSource() == dependencyDivisorText) {
			parameters.setDependencyDivisor(readDependencyDivisor());
		} else if (e.getSource() == andThresholdText) {
			parameters.setAndThreshold(readAndThreshold());
		}
	}


	/**
	 * This method read the relative-to-best threshold
	 * 
	 * @return the parameter value, as setteid in the widget
	 */
	private double readRelativeToBestThreshold() {
		try {
			if (Double.parseDouble(relativeToBestThresholdText.getText()) < 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			relativeToBestThresholdText.setText(Double.
					toString(HMPPParameters.RELATIVE_TO_BEST_THRESHOLD));
		}
		return Double.parseDouble(relativeToBestThresholdText.getText());
	}

	
	/**
	 * This method read the positive observations threshold
	 * 
	 * @return the parameter value, as setteid in the widget
	 */
	private int readPositiveObservationsThreshold() {
		try {
			if (Integer.parseInt(positiveObservationsThresholdText.
					getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			positiveObservationsThresholdText.setText(Integer.
					toString(HMPPParameters.POSITIVE_OBSERVATIONS_THRESHOLD));
		}
		return Integer.parseInt(positiveObservationsThresholdText.getText());
	}

	
	/**
	 * This method read the dependency threshold
	 * 
	 * @return the parameter value, as setteid in the widget
	 */
	private double readDependencyThreshold() {
		try {
			if (Double.parseDouble(dependencyThresholdText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			dependencyThresholdText.setText(Double.
					toString(HMPPParameters.DEPENDENCY_THRESHOLD));
		}
		return Double.parseDouble(dependencyThresholdText.getText());
	}

	
	/**
	 * This method read the length one loop threshold
	 * 
	 * @return the parameter value, as setteid in the widget
	 */
	private double readL1lThreshold() {
		try {
			if (Double.parseDouble(l1lThresholdText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			l1lThresholdText.setText(Double.
					toString(HMPPParameters.L1L_THRESHOLD));
		}
		return Double.parseDouble(l1lThresholdText.getText());
	}

	
	/**
	 * This method read the length two loop threshold
	 * 
	 * @return the parameter value, as setteid in the widget
	 */
	private double readL2lThreshold() {
		try {
			if (Double.parseDouble(l2lThresholdText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			l2lThresholdText.setText(Double.
					toString(HMPPParameters.L2L_THRESHOLD));
		}
		return Double.parseDouble(l2lThresholdText.getText());
	}

	
	/**
	 * This method read the long distance threshold
	 * 
	 * @return the parameter value, as setteid in the widget
	 */
	private double readLDThreshold() {
		try {
			if (Double.parseDouble(LDThresholdText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			LDThresholdText.setText(Double.
					toString(HMPPParameters.LONG_DISTANCE_THRESHOLD));
		}
		return Double.parseDouble(LDThresholdText.getText());
	}

	
	/**
	 * This method read the dependency divisor
	 * 
	 * @return the parameter value, as setteid in the widget
	 */
	private int readDependencyDivisor() {
		try {
			if (Integer.parseInt(dependencyDivisorText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			dependencyDivisorText.setText(Integer.
					toString(HMPPParameters.DEPENDENCY_DIVISOR));
		}
		return Integer.parseInt(dependencyDivisorText.getText());
	}

	
	/**
	 * This method read the AND threshold
	 * 
	 * @return the parameter value, as setteid in the widget
	 */
	private double readAndThreshold() {
		try {
			if (Double.parseDouble(andThresholdText.getText()) <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			andThresholdText.setText(Double.
					toString(HMPPParameters.AND_THRESHOLD));
		}
		return Double.parseDouble(andThresholdText.getText());
	}

	
	/**
	 * This method read the use-all-connected heuristic
	 * 
	 * @return the parameter value, as setteid in the widget
	 */
	private boolean readUseAllConnectedHeuristics() {
		return useAllConnectedHeuristics.isSelected();
	}

	
	/**
	 * This method read the use-long-distance dependency
	 * 
	 * @return the parameter value, as setteid in the widget
	 */
	private boolean readUseLongDistanceDependency() {
		return useLongDistanceDependency.isSelected();
	}


	@Override
	public void stateChanged(ChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		DecimalFormat f = new DecimalFormat("#.#######");
		if (e.getSource() == positiveObsHisto) {
			int c = positiveObsHisto.getColumn(e.getX());
			positiveObservationsThresholdText.setText(f.format(positiveObsHisto.getColumnValue(c)));
			positiveObsHisto.setClickedColumn(c);
			dependencyThresholdsHisto.suggestCount(positiveObsHisto.getColumnCount(c));
			positiveObsHisto.suggestCountColumn(-1);
		} else if (e.getSource() == dependencyThresholdsHisto) {
			int c = dependencyThresholdsHisto.getColumn(e.getX());
			dependencyThresholdText.setText(f.format(dependencyThresholdsHisto.getColumnValue(c)));
			dependencyThresholdsHisto.setClickedColumn(c);
			positiveObsHisto.suggestCount(dependencyThresholdsHisto.getColumnCount(c));
			dependencyThresholdsHisto.suggestCountColumn(-1);
		}
		dependencyThresholdsHisto.repaint();
		positiveObsHisto.repaint();
		setParameters(parameters);
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void valueChanged(ListSelectionEvent e) {
		relativeToBestThresholdText.setText((String) relativeToBestList.getSelectedValue());
		setParameters(parameters);
	}
}
