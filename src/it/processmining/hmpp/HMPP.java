package it.processmining.hmpp;

import it.processmining.hmpp.models.HMPPHeuristicsNet;
import it.processmining.hmpp.models.HMPPParameters;
import it.processmining.hmpp.ui.HMPPPreferencesPanel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.util.PluginDocumentationLoader;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.geneticmining.fitness.duplicates.DTContinuousSemanticsFitness;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;


/**
 * This is the main class for the HeuristicsMiner++ Algorithm
 * 
 * @author Andrea Burattin
 * @version 0.2
 */
public class HMPP implements MiningPlugin {

	/* The plugin name... */
	private final String PLUGIN_NAME = "HeuristicsMiner++";
	private HMPPParameters parameters;

	
	/* The events log */
	private LogEvents events;
//	/* Log with only one event type per event */
//	private LogEvents eventsFiltered;
	/* An array list with all the observed events (just one entry for each
	 * event, without considering the cardinality and the event type) */
	private ArrayList<String> transitions;
	/* The number of atomic events (for caching purpose) */
	private int transitionsSize;
	private int eventsSize;
	/* Matrix with the counts of parallel observations. Indexes referred to
	 * logAtomicEvents */
//	private int[][] observationParallel;
	/* Matrix with the counts of sequential observations. Indexes referred to
	 * logAtomicEvents */
//	private int[][] observationSequence;
	/* ===================== DATA FROM HEURISTICS MINER ===================== */
	/* Support matrices for the start and finish event detection */
	private DoubleMatrix1D startCount;
	private DoubleMatrix1D endCount;
	/* Matrix with the direct dependency measures */
//	private DoubleMatrix2D dependencyMeasures;	
	private DoubleMatrix2D longRangeSuccessionCount;
//	private DoubleMatrix2D causalSuccession;
	/* Information about the longrange dependecy relation */
	private DoubleMatrix2D longRangeDependencyMeasures;
	private DoubleMatrix2D dependencyMeasuresAccepted;
	/* Counts the total wrong dependency observations in the log */
	private DoubleMatrix2D noiseCounters;
	
	private DoubleMatrix1D L1LdependencyMeasuresAll;
	private boolean[] L1Lrelation;
	private DoubleMatrix2D L2LdependencyMeasuresAll;
	private int[] L2Lrelation;
	private DoubleMatrix2D ABdependencyMeasuresAll;
	private boolean[] alwaysVisited;
	
	private DoubleMatrix2D andInMeasuresAll;
	private DoubleMatrix2D andOutMeasuresAll;
	
	private DoubleMatrix2D directSuccessionCount;
	private DoubleMatrix2D succession2Count;
	private DoubleMatrix2D parallelCount;
	
	private DoubleMatrix1D totalActivityCounter;
	private DoubleMatrix1D totalActivityTime;
	private DoubleMatrix2D totalOverlappingTime;
	
	double[] bestInputMeasure;
	double[] bestOutputMeasure;
	int[] bestInputEvent;
	int[] bestOutputEvent;
	
	private boolean basicRelationsMade = false;

	
	/**
	 * Default plugin constructor
	 */
	public HMPP() {
		System.out.println("HMPP");
		parameters = new HMPPParameters();
	}
	
	
	@Override
	public String getName() {
		return PLUGIN_NAME;
	}

	
	@Override
	public JPanel getOptionsPanel(LogSummary summary) {
		return null;
	}
	
	
	/**
	 * WARNING: this method required modifications in the MiningSettings.java
	 *          file. Added lines 92, 93 and 94 
	 * 
	 * @param log
	 * @return
	 */
	public JPanel getOptionsPanel(LogReader log) {
		dataInitialization(log);
		HMPPPreferencesPanel panel = new HMPPPreferencesPanel(parameters, this, log);
		return panel;
	}
	
	
	@Override
	public String getHtmlDescription() {
		return PluginDocumentationLoader.load(this);
	}

	
	@Override
	public MiningResult mine(LogReader log) {
			
		/* ===================== SUPPORT DATA POPULATION ==================== */
//		dataInitialization(log);
//		makeBasicRelations(log, 0.8);
		
//		HMPPHeuristicsNet net;
//		net = makeHeuristicsRelations(log);
		
		/* =========================== DATA OUTPUT ========================== */
		return new HMPPResult(this, log, false, transitions);
	}
	
	
	/**
	 * This method to get the current activity time vector
	 * 
	 * @return the current activity time vector
	 */
	protected DoubleMatrix1D getActivityTime() {
		return totalActivityTime;
	}

	
	/**
	 * This method to get the current activity counter vector
	 * 
	 * @return the current activity counter vector
	 */
	protected DoubleMatrix1D getActivityCounter() {
		return totalActivityCounter;
	}

	
	/**
	 * This method to get the current overlapping time matrix
	 * 
	 * @return the current overlapping time matrix
	 */
	protected DoubleMatrix2D getOverlappingTime() {
		return totalOverlappingTime;
	}

	
	/**
	 * This method to get the current parallel count matrix
	 * 
	 * @return the current parallel count matrix
	 */
	protected DoubleMatrix2D getParallelCount() {
		return parallelCount;
	}

	
	/**
	 * This method to get the current algorithm parameters object
	 * 
	 * @return the current algorithm parameter object
	 */
	protected HMPPParameters getParameters() {
		return parameters;
	}
	
	
	public void setParameters(HMPPParameters parameters) {
		this.parameters = parameters;
	}
	
	
	/**
	 * This method builds the main object instances
	 * 
	 * @param log the log to analyse
	 */
	private void dataInitialization(LogReader log) {
		/* ====================== DATA INITIALIZATION ======================= */
		/* Build the single events array */
//		eventsFiltered = new LogEvents();
		transitions = new ArrayList<String>(
				Arrays.asList(log.getLogSummary().getModelElements()));
		transitionsSize = transitions.size();
		events = log.getLogSummary().getLogEvents();
		eventsSize = events.size();
		
		startCount = DoubleFactory1D.dense.make(eventsSize, 0.0);
		endCount = DoubleFactory1D.dense.make(eventsSize, 0.0);
		
		longRangeSuccessionCount = DoubleFactory2D.dense.make(eventsSize, eventsSize, 0);
		longRangeDependencyMeasures = DoubleFactory2D.dense.make(eventsSize, eventsSize, 0);
//		causalSuccession = DoubleFactory2D.dense.make(logAtomicEventsSize, logAtomicEventsSize, 0);
		longRangeSuccessionCount = DoubleFactory2D.dense.make(eventsSize, eventsSize, 0);
		dependencyMeasuresAccepted = DoubleFactory2D.sparse.make(eventsSize, eventsSize, 0.0);
		noiseCounters = DoubleFactory2D.sparse.make(events.size(), events.size(), 0);
		
		L1LdependencyMeasuresAll = DoubleFactory1D.sparse.make(eventsSize, 0);
		L2LdependencyMeasuresAll = DoubleFactory2D.sparse.make(eventsSize, eventsSize, 0);
		ABdependencyMeasuresAll = DoubleFactory2D.sparse.make(eventsSize, eventsSize, 0);
		
		andInMeasuresAll = DoubleFactory2D.sparse.make(eventsSize, eventsSize, 0);
		andOutMeasuresAll = DoubleFactory2D.sparse.make(eventsSize, eventsSize, 0);
		
		directSuccessionCount = DoubleFactory2D.dense.make(eventsSize, eventsSize, 0);
		succession2Count = DoubleFactory2D.dense.make(eventsSize, eventsSize, 0);
		/* This matrix considers just the parallel relations between activity,
		 * not between events (so between A and B instead of A-start, B-start,
		 * A-finish, B-finish) */
		parallelCount = DoubleFactory2D.dense.make(transitionsSize, transitionsSize, 0);
		
		totalActivityCounter = DoubleFactory1D.dense.make(transitionsSize, 0);
		totalActivityTime = DoubleFactory1D.dense.make(transitionsSize, 0);
		totalOverlappingTime = DoubleFactory2D.dense.make(transitionsSize, transitionsSize, 0);
	}
	
	
	/**
	 * This method builds all the basic relations, populating the long range
	 * succession count and invoking the calculateEventFrequencies for each
	 * process instance.
	 * 
	 * @param log
	 * @param causalityFall
	 */
	@SuppressWarnings("unchecked")
	public void makeBasicRelations(LogReader log, double causalityFall) {	
		/* Iterate through all log events */
		Iterator<ProcessInstance> it = log.instanceIterator();
		while (it.hasNext()) {
			/* Extract the current process and its activity list */
			ProcessInstance pi = it.next();
			AuditTrailEntryList atel = pi.getAuditTrailEntryList();
			
			/* Update the successors and parallels matrices */
			calculateEventsFrequencies(atel);
			
			int i = 0;
			boolean terminate = false;
			while (!terminate) {
				Iterator<AuditTrailEntry> it2 = atel.iterator();
				/* Skip the first i entries of the trace */
				for (int j = 0; j < i; j++) {
					it2.next();
				}
				/* The starting element */
				AuditTrailEntry begin = it2.next();
				LogEvent beginEvent = new LogEvent(begin.getElement(), begin.getType());
				/* Find the correct row of the matices */
				int row = events.indexOf(beginEvent);
				
				int distance = 0;
				boolean foundSelf = false;
				HNSubSet done = new HNSubSet();
				terminate = (!it2.hasNext());
				while (it2.hasNext() && (!foundSelf)) {
					/* The ending element */
					AuditTrailEntry end = it2.next();
					LogEvent endEvent = new LogEvent(end.getElement(), end.getType());
					/* Find the correct column of the matrices */
					int column = events.indexOf(endEvent);
					/* Is it the same? */
					foundSelf = (row == column);
					distance++;
					
					if (done.contains(column)) {
						continue;
					}
					done.add(column);
					
					/* Update long range matrix */
					longRangeSuccessionCount.set(row, column, longRangeSuccessionCount.get(row, column) + 1);

					/* Update causal matrix */
//					causalSuccession.set(row, column, causalSuccession.get(row, column) + Math.pow(causalityFall, distance - 1));
				}
				i++;
			}
		}

		// calculate longRangeDependencyMeasures
		for (int i = 0; i < longRangeDependencyMeasures.rows(); i++) {
			for (int j = 0; j < longRangeDependencyMeasures.columns(); j++) {
				if (events.getEvent(i).getOccurrenceCount() == 0) {
					continue;
				}
				longRangeDependencyMeasures.set(i, j, calculateLongDistanceDependencyMeasure(i, j));
			}

		}
	}
	
	
	/**
	 * This method returns a map to track the number of connection for each
	 * possible positive observations threshold
	 * 
	 * @return a hash map where the key is the possible threshold and the value
	 * is the number of connection for that threshold (counting also all the 
	 * lower values)
	 */
	public HashMap<Double, Integer> getPositiveObsThresholdsValues() {
		HashMap<Double, Integer> toret = new HashMap<Double, Integer>();
		Double key;
		Integer val;
		/* extracts the exact count for each threshold */
		for (int i = 0; i < directSuccessionCount.columns(); i++) {
			for (int j = 0; j < directSuccessionCount.rows(); j++) {
				key = directSuccessionCount.get(i, j);
				if (key > 0) {
					val = toret.get(key);
					if (val == null)
						val = new Integer(0);
					toret.put(key, val + 1);
				}
			}
		}
		
		/* sums all bigger thresholds value */
		Object[] keys = toret.keySet().toArray();
		Arrays.sort(keys);
		Integer curTot = 0;
		for (int i = keys.length - 1; i >= 0; i--) {
			curTot += toret.get(keys[i]);
			toret.put((Double) keys[i], curTot);
		}
		
		return toret;
	}
	
	
	/**
	 * This method returns an array to track the possible values for the
	 * relative to best parameter
	 * 
	 * @return a set with a discretization of all possible relative to best
	 * values (for which there are changes in the output)
	 */
	public String[] getRelativeToBestValues() {
		HashSet<Double> temp = new HashSet<Double>();
		DecimalFormat f = new DecimalFormat("#.#######");
		calculateBestRelations();
		double measure;
		for (int i = 0; i < eventsSize; i++) {
			for (int j = 0; j < eventsSize; j++) {
				measure = calculateDependencyMeasure(i, j);
				temp.add(bestOutputMeasure[i] - measure);
			}
		}
		
		Double[] a = temp.toArray(new Double[temp.size()]);
		Arrays.sort(a);
		String[] toret = new String[a.length];
		for (int i = 0; i < a.length; i++) {
			toret[i] = f.format(a[i]);
		}
		return toret;
	}
	
	
	/**
	 * This method returns a map to track the number of connection for each
	 * possible dependency threshold 
	 * 
	 * @return a hash map where the key is the possible threshold and the value
	 * is the number of connection for that threshold (counting also all the 
	 * lower values)
	 */
	public HashMap<Double, Integer> getDependencyThresholdValues() {
		HashMap<Double, Integer> toret = new HashMap<Double, Integer>();
		Double key;
		Integer val;
		/* extracts the exact count for each threshold */
		for (int i = 0; i < directSuccessionCount.columns(); i++) {
			for (int j = 0; j < directSuccessionCount.rows(); j++) {
				boolean sameEvent = events.get(i).getModelElementName().equals(events.get(j).getModelElementName());
				key =  calculateDependencyMeasure(i, j);
				if (key > 0 && !sameEvent) {
					val = toret.get(key);
					if (val == null)
						val = new Integer(0);
					toret.put(key, val + 1);
				}
			}
		}
		
		/* sums all bigger thresholds value */
		Object[] keys = toret.keySet().toArray();
		Arrays.sort(keys);
		Integer curTot = 0;
		for (int i = keys.length - 1; i >= 0; i--) {
			curTot += toret.get(keys[i]);
			toret.put((Double) keys[i], curTot);
		}
		
		return toret;
	}
	
	
	private void calculateBestRelations() {
		bestInputMeasure = new double[eventsSize];
		bestOutputMeasure = new double[eventsSize];
		bestInputEvent = new int[eventsSize];
		bestOutputEvent = new int[eventsSize];
		double measure;
		
		for (int i = 0; i < eventsSize; i++) {
			bestInputMeasure[i] = -10.0;
			bestOutputMeasure[i] = -10.0;
			bestInputEvent[i] = -1;
			bestOutputEvent[i] = -1;
		}
		/* Search the beste ones */
		for (int i = 0; i < eventsSize; i++) {
			for (int j = 0; j < eventsSize; j++) {
				if (i != j) {
					measure = calculateDependencyMeasure(i, j);
//					measure = dependencyMeasuresAccepted.get(i, j);
//					dependencyMeasuresAccepted.set(i, j, measure);
					ABdependencyMeasuresAll.set(i, j, measure);

					if (measure > bestOutputMeasure[i]) {
						bestOutputMeasure[i] = measure;
						bestOutputEvent[i] = j;
					}
					if (measure > bestInputMeasure[j]) {
						bestInputMeasure[j] = measure;
						bestInputEvent[j] = i;
					}
				}
			}
		}
	}
	
	
	/**
	 * This method extracts information on the parameter instance, calculating
	 * the direct successions matrix and the parallel events matrix
	 * 
	 * @param atel the process instance's activities
	 */
	@SuppressWarnings("unchecked")
	private void calculateEventsFrequencies(AuditTrailEntryList atel) {

		HashMap<String, Integer> finishedActivities = new HashMap<String, Integer>();
		HashMap<String, Integer> finishedActivitiesEver = new HashMap<String, Integer>();
		HashMap<String, Long[]> startedNotFinishedActivities = new HashMap<String, Long[]>();
		
		/* Starting and ending elements for this process instance */
		int startElement = -1;
		int endElement = -1;
		
		/* We have to iterate throughout the process instance */
		Iterator<AuditTrailEntry> i = atel.iterator();
		/* We need to remember if the last activity was a finish so if we have
		 * no other direct successors */
		boolean previousEventWasComplete = false;

		while (i.hasNext()) {	
			AuditTrailEntry ate = i.next();
			LogEvent le = new LogEvent(ate.getElement(), ate.getType());
			String leName = ate.getName();
			String leType = ate.getType();
			
			int indexOfAct = events.indexOf(le);
			int indexOfTransition = transitions.indexOf(leName);
			
			if (leType.equals("start")) {
				
				/* If required, update the starting activity
				 */
				if (startElement == -1) {
					startElement = indexOfAct;
				}
				
				/* This is the start of a new activity, all the activities
				 * started but not finished are overlapped with this one and all
				 * the activities already finished are before this one.
				 */
				/* Set up the activity direct successors */
				for (String act : finishedActivities.keySet()) {
					int indexOfCurrAct = events.indexOf(new LogEvent(act, "complete"));
					double old = directSuccessionCount.get(indexOfCurrAct, indexOfAct);
					directSuccessionCount.set(indexOfCurrAct, indexOfAct, old + 1);
//					System.out.println("   Added "+ act +" => "+ leName);
				}
				
				/* Set up the activity successors */
//				for (String act : finishedActivitiesEver.keySet()) {
//					int indexOfCurrAct = events.indexOf(new LogEvent(act, "complete"));
//					double old = succession2Count.get(indexOfCurrAct, indexOfAct);
//					succession2Count.set(transitions.indexOf(act), indexOfAct, old + 1);
//					System.out.println("   Added "+ act +" ===> "+ leName);
//				}
				
				/* Overlapped activities */
				for (String act : startedNotFinishedActivities.keySet()) {
					double old = parallelCount.get(transitions.indexOf(act), indexOfTransition);
					parallelCount.set(transitions.indexOf(act), indexOfTransition, old + 1);
					parallelCount.set(indexOfTransition, transitions.indexOf(act), old + 1);
//					System.out.println("   Added "+ act +" || "+ leName);
				}
				
				/* Started not finished increment */
				Long[] val_started_not_finished = {1L, ate.getTimestamp().getTime()};
				if (startedNotFinishedActivities.containsKey(leName)) {
					val_started_not_finished[0] += startedNotFinishedActivities.get(leName)[0];
				}
				startedNotFinishedActivities.put(leName, val_started_not_finished);
				
				previousEventWasComplete = false;
				
			} else if (leType.equals("complete")) {
				
				/* Update the current end activity  */
				endElement = indexOfAct;
				
				/* Update the activity counter and the total activity time */
				double oldOccur = totalActivityCounter.get(indexOfTransition);
				totalActivityCounter.set(indexOfTransition, oldOccur+1);
				oldOccur = totalActivityTime.get(indexOfTransition);

				/* We have to clean this because we want to keep only the DIRECT
				 * successors of the activity, just if there are no other
				 * acrivity ended before */
				if (!previousEventWasComplete) {
					finishedActivities.clear();
				}
				
				/* This is the finish of an activity, I have just to terminate
				 * the start.  
				 */
				/* Eventual started but not finished removal */
				if (startedNotFinishedActivities.containsKey(leName))
				{
					Long[] val_started_not_finished = startedNotFinishedActivities.get(leName);
					/* Update the total activity time */
					double time = totalActivityTime.get(transitions.indexOf(leName));
					time += ((ate.getTimestamp().getTime() - val_started_not_finished[1]) / 1000);
					totalActivityTime.set(transitions.indexOf(leName), time);

					/* Update the started not finished map */
					long val = val_started_not_finished[0];
					if (val == 1) {
						startedNotFinishedActivities.remove(leName);
					} else {
						val_started_not_finished[0] = val - 1L;
						startedNotFinishedActivities.put(leName, val_started_not_finished);
					}
					
					/* Update the total overlapping time */
					for (String act : startedNotFinishedActivities.keySet()) {
						/* Update the overlapping time only for the activities
						 * different from the current one */
						if (!act.equals(leName)) {
							int indexOfCurrAct = transitions.indexOf(act);
							time = ate.getTimestamp().getTime() - startedNotFinishedActivities.get(act)[1];
							time /= 1000;
							time += totalOverlappingTime.get(indexOfTransition, indexOfCurrAct);
							totalOverlappingTime.set(indexOfTransition, indexOfCurrAct, time);
							totalOverlappingTime.set(indexOfCurrAct, indexOfTransition, time);
						}
					}
				}
				
				/* Finished activities increment */
				int val_finished = 1;
				int val_finished_ever = 1;
				if (finishedActivities.containsKey(leName)) {
					val_finished += finishedActivities.get(leName);
				}
				finishedActivities.put(leName, val_finished);
				
				if (finishedActivitiesEver.containsKey(leName)) {
					val_finished_ever += finishedActivitiesEver.get(leName);
				}
				finishedActivitiesEver.put(leName, val_finished_ever);
				
				previousEventWasComplete = true;
				
			}
		}
		/* Update the start / finish process counter */
		if (startElement >= 0) {
			startCount.set(startElement, startCount.get(startElement) + 1);
		}
		if (endElement >= 0) {
			endCount.set(endElement, endCount.get(endElement) + 1);
		}
	}


	/**
	 * This method uses the support data to build the heuristics relations.
	 * These are the main steps of this procedure:
	 *   - Best start and end activities calculation
	 *   - Build dependency measures
	 *   - Given the InputSets and OutputSets build OR-subsets
	 *   - Build the HeuristicsNetwork as output
	 * 
	 * @param log the current log
	 * @return the heuristics net from the log
	 */
	public HMPPHeuristicsNet makeHeuristicsRelations(LogReader log) {
		
		/* Step 0 =========================================================== */
		/* Data initialization */
		dependencyMeasuresAccepted = DoubleFactory2D.sparse.make(eventsSize, eventsSize, 0.0);
		
		/* The net we are going to build... */
//		DependencyHeuristicsNet result = new DependencyHeuristicsNet(eventsFiltered,
//				dependencyMeasuresAccepted, directSuccessionCount);
		HMPPHeuristicsNet result = new HMPPHeuristicsNet(events,
				dependencyMeasuresAccepted, directSuccessionCount);
//		HeuristicsNet result = new DependencyHeuristicsNet()

		L1Lrelation = new boolean[eventsSize];
		L2Lrelation = new int[eventsSize];
		
		HNSubSet[] inputSet = new HNSubSet[eventsSize];
		HNSubSet[] outputSet = new HNSubSet[eventsSize];
		
		for (int i = 0; i < eventsSize; i++) {
			inputSet[i] = new HNSubSet();
			outputSet[i] = new HNSubSet();
			L1Lrelation[i] = false;
			L2Lrelation[i] = -10;
		}
		
		/* Step 1 =========================================================== */
		/* Best start and end activities calculation */
		int bestStart = 0;
		int bestEnd = 0;
		for (int i = 0; i < eventsSize; i++) {
			if (startCount.get(i) > startCount.get(bestStart)) {
				bestStart = i;
			}
			if (endCount.get(i) > endCount.get(bestEnd)) {
				bestEnd = i;
			}
		}
		/* Set the start task */
		HNSubSet startTask = new HNSubSet();
		startTask.add(bestStart);
		result.setStartTasks(startTask);
		/* Set the end task */
		HNSubSet endTask = new HNSubSet();
		endTask.add(bestEnd);
		result.setEndTasks(endTask);
		/* Update noiseCounters */
		noiseCounters.set(bestStart, 0, log.getLogSummary().getNumberOfProcessInstances() - startCount.get(bestStart));
		noiseCounters.set(0, bestEnd, log.getLogSummary().getNumberOfProcessInstances() - endCount.get(bestEnd));
		
		/* Step 2 =========================================================== */
		/* Build dependency measures */
		double measure = 0.0;
		
		/* Step 2.1 - L1L loops ............................................. */
		for (int i = 0; i < eventsSize; i++) {
			measure = calculateL1LDependencyMeasure(i);
			L1LdependencyMeasuresAll.set(i, measure);
			if (measure >= parameters.getL1lThreshold() &&
					directSuccessionCount.get(i, i) >= parameters.getPositiveObservationsThreshold()) {
				dependencyMeasuresAccepted.set(i, i, measure);
				L1Lrelation[i] = true;
				inputSet[i].add(i);
				outputSet[i].add(i);
			}
		}
		
		/* Step 2.2 - L2L loops ............................................. */
		for (int i = 0; i < eventsSize; i++) {
			for (int j = 0; j < eventsSize; j++) {
				measure = calculateL2LDependencyMeasure(i, j);
				L2LdependencyMeasuresAll.set(i, j, measure);
				L2LdependencyMeasuresAll.set(j, i, measure);
				
				if ((i != j) && (measure >= parameters.getL2lThreshold()) && 
						((succession2Count.get(i, j) + succession2Count.get(j,i)) >= parameters.getPositiveObservationsThreshold())) {
					dependencyMeasuresAccepted.set(i, j, measure);
					dependencyMeasuresAccepted.set(j, i, measure);
					L2Lrelation[i] = j;
					L2Lrelation[j] = i;
					inputSet[i].add(j);
					outputSet[j].add(i);
					inputSet[j].add(i);
					outputSet[i].add(j);
				}
			}
		}
		
		/* Step 2.3 - Normal dependecy measure .............................. */
		/* Independent of any threshold search the best input and output
		 * connection */
//		int[] bestInputEvent = new int[eventsSize];
//		int[] bestOutputEvent = new int[eventsSize];
//		for (int i = 0; i < eventsSize; i++) {
//			bestInputMeasure[i] = -10.0;
//			bestOutputMeasure[i] = -10.0;
//			bestInputEvent[i] = -1;
//			bestOutputEvent[i] = -1;
//		}
//		/* Search the beste ones */
//		for (int i = 0; i < eventsSize; i++) {
//			for (int j = 0; j < eventsSize; j++) {
//				if (i != j) {
//					measure = calculateDependencyMeasure(i, j);
////					measure = dependencyMeasuresAccepted.get(i, j);
////					dependencyMeasuresAccepted.set(i, j, measure);
//					ABdependencyMeasuresAll.set(i, j, measure);
//
//					if (measure > bestOutputMeasure[i]) {
//						bestOutputMeasure[i] = measure;
//						bestOutputEvent[i] = j;
//					}
//					if (measure > bestInputMeasure[j]) {
//						bestInputMeasure[j] = measure;
//						bestInputEvent[j] = i;
//					}
//				}
//			}
//		}
		calculateBestRelations();
		/* Extra check for best compared with L2L-loops */
		for (int i = 0; i < eventsSize; i++) {
			if ((i!=bestStart) && (i!=bestEnd)) {
				for (int j = 0; j < eventsSize; j++) {
					measure = calculateL2LDependencyMeasure(i, j);
					
					if (measure > bestInputMeasure[i]) {
						dependencyMeasuresAccepted.set(i, j, measure);
						dependencyMeasuresAccepted.set(j, i, measure);
						L2Lrelation[i] = j;
						L2Lrelation[j] = i;
						inputSet[i].add(j);
						outputSet[j].add(i);
						inputSet[j].add(i);
						outputSet[i].add(j);
					}
				}
			}
		}
		/* Update the dependencyMeasuresAccepted matrix, the inputSet, outputSet
		 * arrays and the noiseCounters matrix */
		if (parameters.useAllConnectedHeuristics) {
			for (int i = 0; i < eventsSize; i++) {
				/* consider each case */
				int j = L2Lrelation[i];
				if (i != bestStart) {
					if ((j > -1) && (bestInputMeasure[j] > bestInputMeasure[i])) {
						/* i is in a L2L relation with j but j has a stronger
						 * input connection do nothing */
					} else {
						dependencyMeasuresAccepted.set(bestInputEvent[i], i, bestInputMeasure[i]);
						inputSet[i].add(bestInputEvent[i]);
						outputSet[bestInputEvent[i]].add(i);
						noiseCounters.set(bestInputEvent[i], i, 
							directSuccessionCount.get(i, bestInputEvent[i]));
					}
				}
				if (i != bestEnd) {
					if ((j > -1) && (bestOutputMeasure[j] > bestOutputMeasure[i])) {
						/* i is in a L2L relation with j but j has a stronger
						 * input connection do nothing */
					} else {
						dependencyMeasuresAccepted.set(i, bestOutputEvent[i], bestOutputMeasure[i]);
						inputSet[bestOutputEvent[i]].add(i);
						outputSet[i].add(bestOutputEvent[i]);
						noiseCounters.set(i, bestOutputEvent[i],
							directSuccessionCount.get(bestOutputEvent[i], i));
					}
				}
			}
		} else {
			/* Connect all starts with the relative finish */
			for (int i = 0; i < eventsSize; i++) {
				for (int j = 0; j < eventsSize; j++) {
					LogEvent leI = events.get(i);
					LogEvent leJ = events.get(j);
					boolean sameEvent = leI.getModelElementName().equals(leJ.getModelElementName());
					boolean isIStart = leI.getEventType().equals("start");
					boolean isIFinish = leI.getEventType().equals("complete");
					boolean isJStart = leJ.getEventType().equals("start");
					boolean isJFinish = leJ.getEventType().equals("complete");
					if (sameEvent /*&& isIStart && isJFinish*/) {
						if (isIStart && isJFinish) {
							outputSet[i].add(j);
							inputSet[j].add(i);
						} else if (isJStart && isIFinish) {
							outputSet[j].add(i);
							inputSet[i].add(j);
						}
					}
				}
			}
		}
		/* Search for other connections that fulfill all the thresholds */
		for (int i = 0; i < eventsSize; i++) {
			for (int j = 0; j < eventsSize; j++) {
				if (dependencyMeasuresAccepted.get(i, j) <= 0.0001) {
					measure = calculateDependencyMeasure(i, j);
					if (((bestOutputMeasure[i] - measure) <= parameters.getRelativeToBestThreshold()) &&
							(directSuccessionCount.get(i, j) >= parameters.getPositiveObservationsThreshold()) &&
							(measure >= parameters.getDependencyThreshold())) {
						dependencyMeasuresAccepted.set(i, j, measure);
						inputSet[j].add(i);
						outputSet[i].add(j);
						noiseCounters.set(i, j, directSuccessionCount.get(j, i));
					}
				}
			}
		}
		
		/* Step 3 =========================================================== */
		/* Given the InputSets and OutputSets build OR-subsets */
		double score;
		alwaysVisited = new boolean[eventsSize];
		for (int i = 0; i < eventsSize; i++) {
			result.setInputSet(i, buildOrInputSets(i, inputSet[i]));
			result.setOutputSet(i, buildOrOutputSets(i, outputSet[i]));
		}
//		System.out.println(andInMeasuresAll);
//		System.out.println(andOutMeasuresAll);
		/* Update the HeuristicsNet with non binairy dependecy relations */
		/* Search for always visited activities */
		if (parameters.useLongDistanceDependency) {
			alwaysVisited[bestStart] = false;
			for (int i = 1; i < eventsSize; i++) {
				BitSet h = new BitSet();
				if (escapeToEndPossibleF(bestStart, i, h, result)) {
					alwaysVisited[i] = false;
				} else {
					alwaysVisited[i] = true;
				}
			}
//		/* Why close the if and than re-open it? :-/ */
//		}
//		if (USE_LONG_DISTANCE_CONNECTIONS) {
//		if (parameters.useLongDistanceDependency) {
			for (int i = (eventsSize - 1); i >= 0; i--) {
				for (int j = (eventsSize - 1); j >= 0; j--) {
					if ((i == j) || (alwaysVisited[j] && (j != bestEnd))) {
						continue;
					}
					score = calculateLongDistanceDependencyMeasure(i, j);
					if (score > parameters.getLDThreshold()) {
						BitSet h = new BitSet();
						if (escapeToEndPossibleF(i, j, h, result)) {
							// HNlongRangeFollowingChance.set(i, j, hnc);
							dependencyMeasuresAccepted.set(i, j, score);

							// update heuristicsNet
							HNSubSet helpSubSet = new HNSubSet();
							HNSet helpSet = new HNSet();

							helpSubSet.add(j);
							helpSet = result.getOutputSet(i);
							helpSet.add(helpSubSet);
							result.setOutputSet(i, helpSet);

							helpSubSet = new HNSubSet();
							helpSet = new HNSet();

							helpSubSet.add(i);
							helpSet = result.getInputSet(j);
							helpSet.add(helpSubSet);
							result.setInputSet(j, helpSet);
						}
					}
				}
			}
		}
		int numberOfConnections = 0;
		for (int i = 0; i < dependencyMeasuresAccepted.rows(); i++) {
			for (int j = 0; j < dependencyMeasuresAccepted.columns(); j++) {
				if (dependencyMeasuresAccepted.get(i, j) > 0.01) {
					numberOfConnections = numberOfConnections + 1;
				}
			}
		}
		int noiseTotal = 0;
		for (int i = 0; i < noiseCounters.rows(); i++) {
			for (int j = 0; j < noiseCounters.columns(); j++) {
				noiseTotal = noiseTotal + (int) noiseCounters.get(i, j);
			}
		}

		/* Step 4 =========================================================== */
		/* Building the output */
		HMPPHeuristicsNet[] population = new HMPPHeuristicsNet[1];
		population[0] = result;
		
//		System.out.println("Input-output set, before disconnection:");
//		for (int i = 0; i < eventsSize; i++) {
//			System.out.println(events.get(i) +"  in = "+ result.getInputSet(i));
//			System.out.println(events.get(i) +" out = "+ result.getOutputSet(i));
//			System.out.println();
//		}
		
		DTContinuousSemanticsFitness fitness1 = new DTContinuousSemanticsFitness(log);
		fitness1.calculate(population);
//		DTImprovedContinuousSemanticsFitness fitness2 = new DTImprovedContinuousSemanticsFitness(log);
//		fitness2.calculate(population);
		
//		population[0].disconnectUnusedElements();
		
//		for (int i = 0; i < dependencyMeasuresAccepted.rows(); i++) {
//			for (int j = 0; j < dependencyMeasuresAccepted.columns(); j++) {
//				System.out.print(dependencyMeasuresAccepted.get(i, j) + " ");
//			}
//		}
//		System.out.println("");
		
		return population[0];
	}
	

	/**
	 * This method calculates the long distance dependency measure between two
	 * activities
	 *  
	 * @param i the first activity index
	 * @param j the second activity index
	 * @return the dependency measure
	 */
	private double calculateLongDistanceDependencyMeasure(int i, int j) {
		return ((double) longRangeSuccessionCount.get(i, j) / (events.getEvent(i).getOccurrenceCount() + parameters.getDependencyDivisor())) -
				(5.0 * (Math.abs(events.getEvent(i).getOccurrenceCount() - events.getEvent(j).getOccurrenceCount())) / events.getEvent(i).getOccurrenceCount());

	}
	
	
	/**
	 * This method calculates the length one loop between two activities
	 *  
	 * @param i the activity index
	 * @return the dependency measure
	 */
	private double calculateL1LDependencyMeasure(int i) {
		return ((double) directSuccessionCount.get(i, i)) /
				(directSuccessionCount.get(i, i) + parameters.getDependencyDivisor());
	}
	
	

	/**
	 * This method calculates the length two loop distance dependency measure
	 * between two activities
	 *  
	 * @param i the first activity index
	 * @param j the second activity index
	 * @return the dependency measure
	 */
	private double calculateL2LDependencyMeasure(int i, int j) {
		/* Problem if, for instance, we have a A -> A loop in parallel with B
		 * the |A > B > A|-value can be high without a L2L-loop
		 */
		if ((L1Lrelation[i] && succession2Count.get(i, j) >= parameters.getPositiveObservationsThreshold()) ||
			(L1Lrelation[j] && succession2Count.get(j, i) >= parameters.getPositiveObservationsThreshold())) {
			return 0.0;
		} else {
//			LogEvent leI = events.get(i);
//			LogEvent leJ = events.get(j);
//			int transitionIndexI = transitions.indexOf(leI.getModelElementName());
//			int transitionIndexJ = transitions.indexOf(leJ.getModelElementName());
			return ((double) succession2Count.get(i, j) + succession2Count.get(j, i)) /
					(succession2Count.get(i, j) + 
					 succession2Count.get(j, i) + 
					 /*(parallelCount.get(transitionIndexI, transitionIndexJ) * parameters.getIntervalsOverlapMultiplier()) +*/ 
					 parameters.getDependencyDivisor());
		}
	}
	

	/**
	 * This method calculates the dependency measure between two activities
	 *  
	 * @param i the first activity index
	 * @param j the second activity index
	 * @return the dependency measure
	 */
	private double calculateDependencyMeasure(int i, int j) {
		LogEvent leI = events.get(i);
		LogEvent leJ = events.get(j);
		boolean sameEvent = leI.getModelElementName().equals(leJ.getModelElementName());
		boolean isIStart = leI.getEventType().equals("start");
		boolean isIFinish = leI.getEventType().equals("complete");
		boolean isJStart = leJ.getEventType().equals("start");
		boolean isJFinish = leJ.getEventType().equals("complete");
		if (sameEvent && isIStart && isJFinish) {
			return 1.0;
		} else if ((!sameEvent) && isIFinish && isJStart) {
			int transitionIndexI = transitions.indexOf(leI.getModelElementName());
			int transitionIndexJ = transitions.indexOf(leJ.getModelElementName());
			double calc;
			/* TODO Check the use of direct succession or simply succession */
			calc = (directSuccessionCount.get(i, j) - 
					directSuccessionCount.get(j, i)) / 
				   (directSuccessionCount.get(i, j) + 
					directSuccessionCount.get(j, i) + 
					(parallelCount.get(transitionIndexI, transitionIndexJ) * parameters.getIntervalsOverlapMultiplier()) + 
					parameters.getDependencyDivisor());
			return calc;
		} else {
			return 0.0;
		}
	}

	
	/**
	 * This method builds the or input set for the event
	 * 
	 * @param ownerE the current event index
	 * @param inputSet the input events set
	 * @return the corrent input set
	 */
	private HNSet buildOrInputSets(int ownerE, HNSubSet inputSet) {
		HNSet h = new HNSet();
		int currentE;
		// using the welcome method,
		// distribute elements of TreeSet inputSet over the elements of HashSet h
		boolean minimalOneOrWelcome;
		//setE = null;
		//Iterator hI = h.iterator();
		HNSubSet helpTreeSet;
		for (int isetE = 0; isetE < inputSet.size(); isetE++) {
			currentE = inputSet.get(isetE);
			minimalOneOrWelcome = false;
			for (int ihI = 0; ihI < h.size(); ihI++) {
				helpTreeSet = h.get(ihI);
				if (xorInWelcome(ownerE, currentE, helpTreeSet)) {
					minimalOneOrWelcome = true;
					helpTreeSet.add(currentE);
				}
			}
			if (!minimalOneOrWelcome) {
				helpTreeSet = new HNSubSet();
				helpTreeSet.add(currentE);
				h.add(helpTreeSet);
			}
		}

		// look to the (A v B) & (B v C) example with B A C in the inputSet;
		// result is [AB] [C]
		// repeat to get [AB] [BC]

		for (int isetE = 0; isetE < inputSet.size(); isetE++) {
			currentE = inputSet.get(isetE);
			for (int ihI = 0; ihI < h.size(); ihI++) {
				helpTreeSet = h.get(ihI);
				if (xorInWelcome(ownerE, currentE, helpTreeSet)) {
					helpTreeSet.add(currentE);
				}
			}
		}
		return h;
	}


	/**
	 * This method builds the or output set for the event
	 * 
	 * @param ownerE the current event index
	 * @param outputSEt the output events set
	 * @return the corrent output set
	 */
	private HNSet buildOrOutputSets(int ownerE, HNSubSet outputSet) {
		HNSet h = new HNSet();
		int currentE;

		// using the welcome method,
		// distribute elements of TreeSet inputSet over the elements of HashSet h
		boolean minimalOneOrWelcome;
		//setE = null;
		HNSubSet helpTreeSet;
		for (int isetE = 0; isetE < outputSet.size(); isetE++) {
			currentE = outputSet.get(isetE);
			minimalOneOrWelcome = false;
			for (int ihI = 0; ihI < h.size(); ihI++) {
				helpTreeSet = h.get(ihI);
				if (xorOutWelcome(ownerE, currentE, helpTreeSet)) {
					minimalOneOrWelcome = true;
					helpTreeSet.add(currentE);
				}
			}
			if (!minimalOneOrWelcome) {
				helpTreeSet = new HNSubSet();
				helpTreeSet.add(currentE);
				h.add(helpTreeSet);
			}
		}

		// look to the (A v B) & (B v C) example with B A C in the inputSet;
		// result is [AB] [C]
		// repeat to get [AB] [BC]
		for (int isetE = 0; isetE < outputSet.size(); isetE++) {
			currentE = outputSet.get(isetE);
			for (int ihI = 0; ihI < h.size(); ihI++) {
				helpTreeSet = h.get(ihI);
				if (xorOutWelcome(ownerE, currentE, helpTreeSet)) {
					helpTreeSet.add(currentE);
				}
			}
		}

		return h;
	}


	/**
	 * This method determines if two elements are in a XOR split
	 * 
	 * @param ownerE first element
	 * @param newE second element
	 * @param h the elements subset
	 * @return true if the elements are in a XOR splir
	 */
	private boolean xorInWelcome(int ownerE, int newE, HNSubSet h) {
		boolean welcome = true;
		int oldE;
		double andValue;

		for (int ihI = 0; ihI < h.size(); ihI++) {
			oldE = h.get(ihI);
			andValue = andInMeasureF(ownerE, oldE, newE);
			if (newE != oldE) {
				andInMeasuresAll.set(newE, oldE, andValue);
			}
			if (andValue > parameters.getAndThreshold()) {
				welcome = false;
			}
		}
		return welcome;
	}

	
	/**
	 * This method determines if two elements are in a XOR join
	 * 
	 * @param ownerE first element
	 * @param newE second element
	 * @param h the elements subset
	 * @return true if the elements are in a XOR splir
	 */
	private boolean xorOutWelcome(int ownerE, int newE, HNSubSet h) {
		boolean welcome = true;
		int oldE;
		double andValue;

		for (int ihI = 0; ihI < h.size(); ihI++) {
			oldE = h.get(ihI);
			andValue = andOutMeasureF(ownerE, oldE, newE);
			if (newE != oldE) {
				andOutMeasuresAll.set(newE, oldE, andValue);
			}
			if (andValue > parameters.getAndThreshold()) {
				welcome = false;
			}
		}
		return welcome;
	}
	
	
	/**
	 * This method determines if two elements are in a AND split
	 * 
	 */
	private double andInMeasureF(int ownerE, int oldE, int newE) {
		double toret = 0.0;
		if (ownerE == newE) {
			toret = 0.;
		/* TODO: verify if it's correct to not consider this case */
//		} else if ((directSuccessionCount.get(oldE, newE) < parameters.getPositiveObservationsThreshold()) ||
//				(directSuccessionCount.get(newE, oldE) < parameters.getPositiveObservationsThreshold())) {
//			toret = 0.;
		} else {
			int pcIndexNewE = transitions.indexOf(events.get(newE).getModelElementName());
			int pcIndexOldE = transitions.indexOf(events.get(oldE).getModelElementName());
			toret = ((double) directSuccessionCount.get(oldE, newE) + 
					         directSuccessionCount.get(newE, oldE) + 
					         (parallelCount.get(pcIndexNewE, pcIndexOldE) * parameters.getIntervalsOverlapMultiplier())) /
					// relevantInObservations;
					(directSuccessionCount.get(newE, ownerE) + 
					 directSuccessionCount.get(oldE, ownerE) + 1);
		}
		return toret;
	}

	
	/**
	 * This method determines if two elements are in a AND join
	 * 
	 */
	private double andOutMeasureF(int ownerE, int oldE, int newE) {
		double toret = 0.0;
		if (ownerE == newE) {
			toret = 0.;
		/* TODO: verify if it's correct to not consider this case */
//		} else if ((directSuccessionCount.get(oldE, newE) < parameters.getPositiveObservationsThreshold()) ||
//				(directSuccessionCount.get(newE, oldE) < parameters.getPositiveObservationsThreshold())) {
//			toret = 0.;
		} else {
			int pcIndexNewE = transitions.indexOf(events.get(newE).getModelElementName());
			int pcIndexOldE = transitions.indexOf(events.get(oldE).getModelElementName());
			toret = ((double) directSuccessionCount.get(oldE, newE) + 
					         directSuccessionCount.get(newE, oldE) + 
					         (parallelCount.get(pcIndexNewE, pcIndexOldE) * parameters.getIntervalsOverlapMultiplier())) /
					// relevantOutObservations;
					(directSuccessionCount.get(ownerE, newE) + 
					 directSuccessionCount.get(ownerE, oldE) + 1);
		}
		return toret;
	}
	
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param alreadyVisit
	 * @param result
	 * @return
	 */
	private boolean escapeToEndPossibleF(int x, int y, BitSet alreadyVisit,
			HMPPHeuristicsNet result) {
		HNSet outputSetX, outputSetY = new HNSet();
		//double max, min, minh;
		boolean escapeToEndPossible;
		int minNum;

		//          [A B]
		// X        [C]     ---> Y
		//          [D B F]

		// build subset h = [A B C D E F] of all elements of outputSetX
		// search for minNum of elements of min subset with X=B as element: [A B] , minNum = 2

		outputSetX = result.getOutputSet(x);
		outputSetY = result.getOutputSet(y);

		HNSubSet h = new HNSubSet();
		minNum = 1000;
		for (int i = 0; i < outputSetX.size(); i++) {
			HNSubSet outputSubSetX = new HNSubSet();
			outputSubSetX = outputSetX.get(i);
			if ((outputSubSetX.contains(y)) && (outputSubSetX.size() < minNum)) {
				minNum = outputSubSetX.size();
			}
			for (int j = 0; j < outputSubSetX.size(); j++) {
				h.add(outputSubSetX.get(j));
			}
		}

		if (alreadyVisit.get(x)) {
			return false;
		} else if (x == y) {
			return false;
		} else if (outputSetY.size() < 0) {
			// y is an eEe element
			return false;
		} else if (h.size() == 0) {
			// x is an eEe element
			return true;
		} else if (h.contains(y) && (minNum == 1)) {
			// x is unique connected with y
			return false;
		} else {
			// iteration over OR-subsets in outputSetX
			for (int i = 0; i < outputSetX.size(); i++) {
				HNSubSet outputSubSetX = new HNSubSet();
				outputSubSetX = outputSetX.get(i);
				escapeToEndPossible = false;
				for (int j = 0; j < outputSubSetX.size(); j++) {
					int element = outputSubSetX.get(j);
					BitSet hulpAV = (BitSet) alreadyVisit.clone();
					hulpAV.set(x);
					if (escapeToEndPossibleF(element, y, hulpAV, result)) {
						escapeToEndPossible = true;
					}

				}
				if (!escapeToEndPossible) {
					return false;
				}
			}
			return true;
		}
	}
	
	
	/**
	 * Method to know if it has already build the basic relations
	 * 
	 * @return 
	 */
	public boolean getBasicRelationsMade() {
		return basicRelationsMade;
	}
	
	
	/**
	 * Method to set if it has already build the basic relations
	 * 
	 * @param val the new value
	 */
	public void setBasicRelationsMade(boolean val) {
		basicRelationsMade = val;
	}
}
