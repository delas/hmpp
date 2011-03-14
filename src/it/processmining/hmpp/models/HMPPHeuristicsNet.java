package it.processmining.hmpp.models;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.processmining.framework.log.LogEvents;
import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;

import cern.colt.matrix.DoubleMatrix2D;


/**
 * This class models the particular Heuristic Net (called HeuristicsNet++) 
 * returned by the HeuristicMiner++ algorithm.
 * 
 * @author Andrea Burattin
 */
public class HMPPHeuristicsNet extends HeuristicsNet {

	
	private String helpString;
	private DoubleMatrix2D dependencyMeasures;
	private DoubleMatrix2D directSuccessionCount;

	
	/**
	 * The class constructor
	 *  
	 * @param events the log events object
	 * @param dependencyMeasures matrix with all the dependency measures
	 * @param directSuccessionCount matrix with all the direct succession count
	 */
	public HMPPHeuristicsNet(LogEvents events, 
			DoubleMatrix2D dependencyMeasures,
			DoubleMatrix2D directSuccessionCount) {
		super(events);
		this.dependencyMeasures = dependencyMeasures;
		this.directSuccessionCount = directSuccessionCount;
	}

	
	@Override
	public String toStringWithEvents() {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < size(); i++) {
			sb.append("\n\n").append(WME_HEADER).append(" ").
				append(WME_NAME_DELIMITER).append(getLogEvents().
					getEvent(getDuplicatesMapping()[i]).getModelElementName()).
				append(" (").append(getLogEvents().
						getEvent(getDuplicatesMapping()[i]).getEventType()).
				append(")").append(WME_NAME_DELIMITER).append(":");

			//building IN part....
			sb.append("\n").append(INPUT_SETS_HEADER).append(": ");

			sb.append("[ ");
			buildVisualPresentation(sb, getInputSets()[i], getLogEvents());
			sb.append(" ]");

			//building OUT part....
			sb.append("\n").append(OUTPUT_SETS_HEADER).append(": ");
			sb.append("[ ");
			buildVisualPresentation(sb, getOutputSets()[i], getLogEvents());
			sb.append(" ]");
		}
		return sb.toString();
	}

	
	/**
	 * New declaration of the visual presentation builder
	 * 
	 * @param sb the string buffer
	 * @param set the set
	 * @param events the events
	 */
	private void buildVisualPresentation(StringBuffer sb, HNSet set, 
			LogEvents events) {

		HNSubSet subset = null;
		int element = 0;

		if (set != null) {

			for (int i = 0; i < set.size(); i++) {
				subset = set.get(i);
				sb.append("[");
				for (int j = 0; j < subset.size(); j++) {
					element = getDuplicatesMapping()[subset.get(j)];
					sb.append(" ").append(WME_NAME_DELIMITER).
					   append(events.getEvent(element).getModelElementName()).
					   append(" (").
					   append(events.getEvent(element).getEventType()).
					   append(")").append(WME_NAME_DELIMITER);
				}
				sb.append(" ]");
			}
		} else {
			sb.append("null");
		}
	}

	
	@Override
	public void writeToDotWithoutSplitJoinSemantics(Writer bw) 
		throws IOException {
		
		//correcting individual for visual presentation
		//two individuals with different genotype can have the same phenotype
		//HeuristicsNet phenotype = 
		//	MethodsOverIndividuals.
		//		removeDanglingElementReferences((HeuristicsNet)this.clone());

		DecimalFormat dec = new DecimalFormat("#.###");
		HashMap<String, String> activityClusters = new HashMap<String,String>();

		bw.write("digraph G {\n");
		bw.write("  size=\"6,10\"; fontname=\"Verdana\"; fontsize=\"13\";\n");
		bw.write("  node [shape=\"Mrecord\",fontname=\"Verdana\"," +
				"fontsize=\"13\",style=\"filled,,setlinewidth(2)\"," +
				"fillcolor=\"lightgoldenrod\"];\n");

		// write nodes
		for (int i = 0; i < size(); i++) {
			// write cluster information
			if (getLogEvents().getEvent(i).getEventType().equals("start")) {
				String current = "E" + i;
				activityClusters.put(getLogEvents().getEvent(i).
						getModelElementName(), current);
			} else if (getLogEvents().getEvent(i).
					getEventType().equals("complete")) {
				String current = activityClusters.get(getLogEvents().
						getEvent(i).getModelElementName());
				current = current + " ; E" + i;
				activityClusters.put(getLogEvents().getEvent(i).
						getModelElementName(), current);
			}
			
			helpString = "E" + i + " [label=\"";
			helpString = helpString + getLogEvents().getEvent(i).
							getModelElementName().replace('"', '\'');
			helpString = helpString + "\\n" +
						 getLogEvents().getEvent(i).getEventType().
						 replace('"', '\''); // event type
			helpString = helpString + "\\n" + getLogEvents().getEvent(i).
							getOccurrenceCount(); //# occurrence
			helpString = helpString + "\"];\n";
			bw.write(helpString);
		}
		
		// write clusters
		for (String val : activityClusters.keySet()) {
			helpString = "subgraph \"cluster_" + val + 
				"\" {style=\"filled,rounded,setlinewidth(5)\"; " +
				"fontname=\"Verdana\"; color=\"white\" fillcolor=\"red2\"; " + 
				activityClusters.get(val) + "}\n";
			bw.write(helpString);
		}

		// write edges
		for (int from = 0; from < size(); from++) {
			//Iterator set = phenotype.getAllElementsOutputSet(from).iterator();
			HNSubSet set = getAllElementsOutputSet(from);
			for (int iSet = 0; iSet < set.size(); iSet++) {
				int to = set.get(iSet);
				int ds = (int) directSuccessionCount.get(from, to);
				if (ds == 0) {
					helpString = "E" + from + " -> E" + to + " " +
							"[style=\"filled,setlinewidth(2)\", label=\" \"]\n";
				} else {
					helpString = "E" + from + " -> E" + to + " " +
							"[style=\"filled,setlinewidth(2)\", " +
							"fontname=\"Verdana\", label=\"";
					helpString = helpString + "\\n" + dec.
									format(dependencyMeasures.get(from, to));
					helpString = helpString + "\\n" + ds;
					helpString = helpString + "\"];\n";
				}
				bw.write(helpString);
			}
		}

		bw.write("}\n");
	}

	
	@Override
	public void writeToDotWithSplitJoinSemantics(Writer bw) throws IOException {
		
		HashMap<String, String> activityClusters = new HashMap<String,String>();
		
		//correcting individual for visual presentation
		//two individuals with different genotype can have the same phenotype
		//HeuristicsNet phenotype = MethodsOverIndividuals.
		//	removeDanglingElementReferences((HeuristicsNet)this.clone());

		bw.write("digraph G {\n");
		bw.write("  size=\"6,10\"; fontname=\"Verdana\"; fontsize=\"13\";\n");
		bw.write("  node [shape=\"record\" fontname=\"Verdana\"," +
				"fontsize=\"13\",style=\"filled,,setlinewidth(2)\"," +
				"fillcolor=\"lightgoldenrod\"];\n");
		
		// write nodes
		for (int i = 0; i < size(); i++) {
			// write cluster information
			if (getLogEvents().getEvent(i).getEventType().equals("start")) {
				String current = "E" + i;
				activityClusters.put(getLogEvents().getEvent(i).
						getModelElementName(), current);
			} else if (getLogEvents().getEvent(i).
					getEventType().equals("complete")) {
				String current = activityClusters.get(getLogEvents().
						getEvent(i).getModelElementName());
				current = current + " ; E" + i;
				activityClusters.put(getLogEvents().getEvent(i).
						getModelElementName(), current);
			}
			
			if ((getInputSet(i).size() > 0) || (getOutputSet(i).size() > 0)) {
				bw.write("E" + i + " [label=\"{");
				if (getInputSet(i).size() > 0) {
					bw.write("{");
					for (int j = 0; j < getInputSet(i).size(); j++) {
						if (j > 0) {
							bw.write(" | and | ");
						}
						bw.write("<" + toInputDotName(getInputSet(i).
								get(j).toString()) + ">  XOR ");
					}
					bw.write("} | ");
				}
				bw.write(getLogEvents().getEvent(getDuplicatesMapping()[i]).
						getModelElementName().replace('"', '\'') + "\\n" +
						getLogEvents().getEvent(getDuplicatesMapping()[i]).
						getEventType().replace('"', '\'') + "\\n" + 
						getDuplicatesActualFiring()[i]);
				
				if (getOutputSet(i).size() > 0) {
					bw.write(" | {");
					for (int j = 0; j < getOutputSet(i).size(); j++) {
						if (j > 0) {
							bw.write(" | and | ");
						}
						bw.write("<" + toOutputDotName(getOutputSet(i).
								get(j).toString()) + "> XOR ");
					}
					bw.write("}");
				}

				bw.write("}}\"];\n");
			}
		}
		
		// write clusters
		for (String val : activityClusters.keySet()) {
			helpString = "subgraph \"cluster_" + val + 
				"\" {style=\"filled,rounded,setlinewidth(5)\"; " +
				"fontname=\"Verdana\"; color=\"white\" fillcolor=\"red2\"; " + 
				activityClusters.get(val) + "}\n";
			bw.write(helpString);
		}

		// write edges
		DecimalFormat dec = new DecimalFormat("#.###");

		for (int from = 0; from < size(); from++) {
			//Iterator set = phenotype.getAllElementsOutputSet(from).iterator();
			
			for (int outSubsetIndex = 0; 
				outSubsetIndex < getOutputSet(from).size(); outSubsetIndex++) {
				
				HNSubSet outSubset = getOutputSet(from).get(outSubsetIndex);
				
				for (int outSubsetElementIndex = 0; 
					 outSubsetElementIndex < outSubset.size(); 
					 outSubsetElementIndex++) {
					
					int to = outSubset.get(outSubsetElementIndex);
					HNSet inputSubsetsElementWithFrom = 
						getInputSetsWithElement(to, from);
					
					for (int k = 0; k<inputSubsetsElementWithFrom.size(); k++) {
						int ds = (int) directSuccessionCount.get(from, to);
						if (ds == 0) {
							bw.write("E" + from + " -> E" + to + 
									" [style=\"filled,setlinewidth(2)\", " +
									"label=\" \" ]\n");
						} else {
							bw.write("E" + from + ":" + 
									toOutputDotName(outSubset.toString()) +
									" -> E" + to + ":" +
									toInputDotName(inputSubsetsElementWithFrom.
											get(k).toString()) +
									" [style=\"filled,setlinewidth(2)\", " +
									"fontname=\"Verdana\", label=\"  \\n" + 
									dec.format(dependencyMeasures.get(from,to))+
									"\\n" + (int) getArcUsage().get(from, to) + 
									"\"];\n");
						}
					}
				}
			}
		}
		bw.write("}\n");
	}
}
