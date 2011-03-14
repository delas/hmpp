package it.processmining.hmpp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import it.processmining.hmpp.models.HMPPHeuristicsNet;
import it.processmining.hmpp.ui.HMPPCoreResultPanel;
import it.processmining.hmpp.ui.HMPPResultPanel;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.deckfour.slickerbox.components.HeaderBar;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.mining.MiningResult;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;


/**
 * This is the main result graphic interface
 * 
 * @author Andrea Burattin
 */
public class HMPPResult extends JPanel implements MiningResult, Provider {
	
	private static final long serialVersionUID = -8328627635514384093L;
	protected HMPPHeuristicsNet net;
	protected LogReader log;
	
	protected HMPP algorithm;
	protected ArrayList<String> transitions;
	protected DoubleMatrix1D totalActivityTime;
	protected DoubleMatrix1D totalActivityCounter;
	protected DoubleMatrix2D totalOverlappingTime;
	protected DoubleMatrix2D parallelCount;
	protected DoubleMatrix2D overlapTimeMeasure;
	protected DoubleMatrix2D overlapCardinalityMeasure;

	/* gui local variables */
	protected HMPPCoreResultPanel corePanel;
	protected HMPPResultPanel resultPanel;
	
	
	/**
	 * The class constructor
	 * 
	 * @param alg the current algorithm class (the mining algorithm is called
	 *            from this class, after the construction)
	 * @param l the current log reader
	 * @param showSplitJoinSemantics if the split/join semantics has to be shown
	 * @param trans the transitions array
	 */
	public HMPPResult(HMPP alg, LogReader l, 
					  boolean showSplitJoinSemantics,
					  ArrayList<String> trans) {
		
		this.algorithm = alg;
		this.log = l;
		this.transitions = trans;
		
		/* add the waiting gui */
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder());
		HeaderBar waitHeader = new HeaderBar("HeuristicsMiner++");
		waitHeader.setHeight(40);
		this.add(waitHeader, BorderLayout.NORTH);
		final ProgressPanel progress = new ProgressPanel("Processing...");
		this.add(progress.getPanel(), BorderLayout.CENTER);
		revalidate();
		
		final Thread minerThread = new Thread() {
			public void run() {
				if (!algorithm.getBasicRelationsMade())
				{
					algorithm.makeBasicRelations(log, 0.8);
				}
				net = algorithm.makeHeuristicsRelations(log);
				
				totalActivityTime = algorithm.getActivityTime();
				totalActivityCounter = algorithm.getActivityCounter();
				totalOverlappingTime = algorithm.getOverlappingTime();
				parallelCount = algorithm.getParallelCount();
				
				/* buld the overlap measure matrix */
				int transitionSize = transitions.size();
				overlapTimeMeasure = cern.colt.matrix.DoubleFactory2D.
					dense.make(transitionSize, transitionSize);
				overlapCardinalityMeasure = cern.colt.matrix.DoubleFactory2D.
				dense.make(transitionSize, transitionSize);
				for (int i = 0; i < transitionSize; i++) {
					for (int j = 0; j < transitionSize; j++) {
						double om = 0;
						double omc = 0;
						if (parallelCount.get(i, j) > 0) {
							/* time */
							double avgTimeA = (totalActivityTime.get(i) / 
									totalActivityCounter.get(i));
							if (avgTimeA == 0) 
								avgTimeA = 0.00001;
							
							double avgTimeB = (totalActivityTime.get(j) / 
									totalActivityCounter.get(j));
							if (avgTimeB == 0) 
								avgTimeB = 0.00001;
							
							double avgOverlap = (totalOverlappingTime.get(i, j)/ 
									parallelCount.get(i, j));
							double omA = (avgOverlap > avgTimeA)? 1 : 
								(avgOverlap / avgTimeA);
							double omB = (avgOverlap > avgTimeB)? 1 : 
								(avgOverlap / avgTimeB);
							om = omA * omB;
							
							/* cardinality */
							omc = (parallelCount.get(i, j) * parallelCount.get(i, j)) / 
							(totalActivityCounter.get(i) * totalActivityCounter.get(j));
						}
						overlapTimeMeasure.set(i, j, om);
						overlapCardinalityMeasure.set(i, j, omc);
					}
				}
				
				initializeGUI();
				revalidate();
			}
		};
		
		this.addAncestorListener(new AncestorListener() {
			protected boolean hasRun = false;
			public synchronized void ancestorAdded(AncestorEvent event) {
				if(hasRun == false) {
					hasRun = true;
					minerThread.start();
				}
			}
			public void ancestorMoved(AncestorEvent event) {
				// ignore
			}
			public void ancestorRemoved(AncestorEvent event) {
				// ignore
			}
		});
	}
	
	
	/**
	 * This method initialize the GUI with the calculated results
	 * (HMPPResultPanel). To be called when all the required objects (net,
	 * transitions, totalActivityTime, totalActivityCounter, 
	 * totalOverlappingTime, parallelCount, overlapMeasure) are correctly
	 * initialized.
	 */
	protected void initializeGUI() {
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(100, 100, 100));
		resultPanel = new HMPPResultPanel(net, transitions, totalActivityTime, 
				totalActivityCounter, totalOverlappingTime, parallelCount,
				overlapTimeMeasure, overlapCardinalityMeasure);
		this.removeAll();
		this.add(resultPanel, BorderLayout.CENTER);
	}
	
	
	@Override
	public LogReader getLogReader() {
		return log;
	}

	
	@Override
	public JComponent getVisualization() {
		return this;
	}

	
	@Override
	public ProvidedObject[] getProvidedObjects() {
		if(resultPanel != null) {
			return resultPanel.getProvidedObjects();
		} else {
			return new ProvidedObject[] {};
		}
	}

}
