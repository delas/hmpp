package it.processmining.hmpp.models;


/**
 * This class is the collector of the algorithm parameters (as in the Heuristics
 * Miner plugin)
 * 
 * @author Andrea Burattin
 */
public class HMPPParameters {
	
	
	public static final double RELATIVE_TO_BEST_THRESHOLD = 0.05;
	public static final int POSITIVE_OBSERVATIONS_THRESHOLD = 10;
	public static final double DEPENDENCY_THRESHOLD = 0.8;
	public static final double L1L_THRESHOLD = 0.8;
	public static final double L2L_THRESHOLD = 0.8;
	public static final double LONG_DISTANCE_THRESHOLD = 0.8;
	public static final int DEPENDENCY_DIVISOR = 1;
	public static final double AND_THRESHOLD = 0.2;
	public static final double INTERVALS_OVERLAP_MULTIPLIER = 2;


	public static final String RELATIVE_TO_BEST_THRESHOLD_L = "Relative-to-best threshold ";
	public static final String POSITIVE_OBSERVATIONS_THRESHOLD_L = "Positive observations ";
	public static final String DEPENDENCY_THRESHOLD_L = "Dependency threshold ";
	public static final String L1L_THRESHOLD_L = "Length-one-loops threshold ";
	public static final String L2L_THRESHOLD_L = "Length-two-loops threshold ";
	public static final String LONG_DISTANCE_THRESHOLD_L = "Long distance threshold ";
	public static final String DEPENDENCY_DIVISOR_L = "Dependency divisor ";
	public static final String AND_THRESHOLD_L = "AND threshold ";
	public static final String INTERVALS_OVERLAP_MULTIPLIER_L = "Intervals overlap multiplier ";


	private double relativeToBestThreshold = RELATIVE_TO_BEST_THRESHOLD;
	private int positiveObservationsThreshold = POSITIVE_OBSERVATIONS_THRESHOLD;
	private double dependencyThreshold = DEPENDENCY_THRESHOLD;
	private double l1lThreshold = L1L_THRESHOLD;
	private double l2lThreshold = L2L_THRESHOLD;
	private double LDThreshold = LONG_DISTANCE_THRESHOLD;
	private int dependencyDivisor = DEPENDENCY_DIVISOR;
	private double andThreshold = AND_THRESHOLD;
	public boolean useAllConnectedHeuristics = true;
	public boolean useLongDistanceDependency = false;
	private double intervalsOverlapMultiplier = INTERVALS_OVERLAP_MULTIPLIER;
	

	
	/**
	 * This methods returns the relative to best threshold
	 * 
	 * @return the relative to best threshold
	 */
	public double getRelativeToBestThreshold() {
		return relativeToBestThreshold;
	}

	
	/**
	 * This methods returns the positive observation threshold
	 * 
	 * @return the positive observation threshold
	 */
	public int getPositiveObservationsThreshold() {
		return positiveObservationsThreshold;
	}

	
	/**
	 * This methods returns the dependency threshold
	 * 
	 * @return the dependency threshold
	 */
	public double getDependencyThreshold() {
		return dependencyThreshold;
	}

	
	/**
	 * This methods returns the length one loop threshold
	 * 
	 * @return the lendth one loop threshold
	 */
	public double getL1lThreshold() {
		return l1lThreshold;
	}

	
	/**
	 * This methods returns the length two loop threshold
	 * 
	 * @return the length two loop threshold
	 */
	public double getL2lThreshold() {
		return l2lThreshold;
	}

	
	/**
	 * This methods returns the long distance threshold
	 * 
	 * @return the long distance threshold
	 */
	public double getLDThreshold() {
			return LDThreshold;
	}

	
	/**
	 * This methods returns the dependency divisor
	 * 
	 * @return the dependency divisor
	 */
	public int getDependencyDivisor() {
		return dependencyDivisor;
	}

	
	/**
	 * This methods returns the AND threshold
	 * 
	 * @return the AND threshold
	 */
	public double getAndThreshold() {
		return andThreshold;
	}

	
	/**
	 * This methods returns the intervals overlap multiplier
	 * 
	 * @return the intervals overlap multiplier
	 */
	public double getIntervalsOverlapMultiplier() {
		return intervalsOverlapMultiplier;
	}

	
	/**
	 * This methods sets the relative to best threshold
	 * 
	 * @param x the relative to best threshold
	 */
	public void setRelativeToBestThreshold(double x) {
		relativeToBestThreshold = x;
	}

	
	/**
	 * This methods sets the positive observation threshold
	 * 
	 * @param n the positive observation threshold
	 */
	public void setPositiveObservationsThreshold(int n) {
		positiveObservationsThreshold = n;
	}

	
	/**
	 * This methods sets the depdendency threshold
	 * 
	 * @param x the dependency threshold
	 */
	public void setDependencyThreshold(double x) {
		dependencyThreshold = x;
	}

	
	/**
	 * This methods sets the length one loop threshold
	 * 
	 * @param x the length one loop threshold
	 */
	public void setL1lThreshold(double x) {
		l1lThreshold = x;
	}

	
	/**
	 * This methods sets the length two loop threshold
	 * 
	 * @param x the length two loop threshold
	 */
	public void setL2lThreshold(double x) {
		l2lThreshold = x;
	}

	
	/**
	 * This methods sets the long distance threshold
	 * 
	 * @param x the long distance threshold
	 */
	public void setLDThreshold(double x) {
			LDThreshold = x;
	}

	
	/**
	 * This methods sets the dependency divisor
	 * 
	 * @param n the dependency divisor
	 */
	public void setDependencyDivisor(int n) {
		dependencyDivisor = n;
	}

	
	/**
	 * This methods sets the AND threshold
	 * 
	 * @param x the AND threshold
	 */
	public void setAndThreshold(double x) {
		andThreshold = x;
	}

	
	/**
	 * This methods sets the use all connected heuristics
	 * 
	 * @param x the use all connected heuristics
	 */
	public void setUseAllConnectedHeuristics(boolean x) {
		useAllConnectedHeuristics = x;
	}

	
	/**
	 * This methods sets the use long distance dependency
	 * 
	 * @param x the use long distance dependency
	 */
	public void setUseLongDistanceDependency(boolean x) {
		useLongDistanceDependency = x;
	}

	
	/**
	 * This methods sets the intervals overlap multiplier
	 * 
	 * @param n the intervals overlap multiplier
	 */
	public void setIntervalsOverlapMultiplier(double n) {
		intervalsOverlapMultiplier = n;
	}


	@Override
	public String toString() {
		String output = "THRESHOLDS:\n" +
			RELATIVE_TO_BEST_THRESHOLD_L + " " + relativeToBestThreshold + "\n"+
			POSITIVE_OBSERVATIONS_THRESHOLD_L+" "+positiveObservationsThreshold+
			"\n" +
			DEPENDENCY_THRESHOLD_L + " " + dependencyThreshold + "\n" +
			L1L_THRESHOLD_L + " " + l1lThreshold + "\n" +
			L2L_THRESHOLD_L + " " + l2lThreshold + "\n" +
			LONG_DISTANCE_THRESHOLD_L + " " + LDThreshold + "\n" +
			DEPENDENCY_DIVISOR_L + " " + dependencyDivisor + "\n" +
			AND_THRESHOLD_L + " " + andThreshold + "\n" +
			"Use all-events-connected-heuristic " +
			Boolean.toString(useAllConnectedHeuristics) + "\n" +
			"Use long distance dependency heuristics " +
			Boolean.toString(useLongDistanceDependency) + "\n";
		return output;
	}
}
