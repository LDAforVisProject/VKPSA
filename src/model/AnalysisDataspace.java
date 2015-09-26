package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;
import control.analysisView.AnalysisController;

/**
 * @todo Provides abstraction for data used in AnalysisController.
 * Encompasses all relevant data and methods used for tasks in AnalysisController.
 * @author RM
 *
 */
public class AnalysisDataspace
{
	// -----------------------------------------------
	// 				Data.
	// -----------------------------------------------
	
	/*
	 * First-level data: Coordinates, distances and LDA configurations. 
	 */
	
	/**
	 * MDS coordinates of all datasets in workspace / currently loaded.
	 */
	private double[][] coordinates;
	/**
	 * Distances of all datasets in workspace / currently loaded.
	 */
	private double[][] distances;
	/**
	 * LDA configurations all datasets in workspace / currently loaded.
	 */
	private ArrayList<LDAConfiguration> ldaConfigurations;
	
	/*
	 * Derived, second-level data: Filtered (as opposed to discarded) indices, coordinates and distances.
	 */
	
	/**
	 * Set of all datasets matching the currently defined thresholds.
	 */
	private Set<Integer> filteredIndices;
	/**
	 * Stores filterd coordinates.
	 */
	private double filteredCoordinates[][];
	/**
	 * Stores filtered distances.
	 */
	private double filteredDistances[][];
	/**
	 * List of filtered LDA configurations.
	 */
	private ArrayList<LDAConfiguration> filteredLDAConfigurations;
	
	/*
	 * Derived, second-level data: Discarded (as opposed to filtered) indices, coordinates and distances.
	 */
	
	/**
	 * Set of all datasets matching the currently defined thresholds and selection.
	 */
	private Set<Integer> discardedIndices;
	/**
	 * Stores discarded coordinates.
	 */
	private double discardedCoordinates[][];
	/**
	 * Stores filtered and selecte distances.
	 */
	private double discardedDistances[][];
	/**
	 * Stores filtered and selected LDA configurations.
	 */
	private ArrayList<LDAConfiguration> discardedLDAConfigurations;
	
	/*
	 * Derived, third-level data: Selected indices, coordinates and distances.
	 */
	
	/**
	 * Set of all datasets matching the currently defined thresholds and selection.
	 */
	private Set<Integer> selectedFilteredIndices;
	/**
	 * Stores filtered and selecte distances.
	 */
	private double selectedFilteredDistances[][];
	/**
	 * Stores filtered and selected LDA configurations.
	 */
	private ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations;
	
	/*
	 * Miscanellous data.
	 */
	
	/**
	 * Flag indicating whether or not the global extrema were already identified.
	 */
	private boolean globalExtremaIdentified;
	
	/**
	 * Reference to instance of AnalysisController.
	 */
	private AnalysisController controller; 
	
	
	// -----------------------------------------------
	// 			Initialization procedures.
	// -----------------------------------------------
	
	public AnalysisDataspace(AnalysisController controller)
	{
		this.controller = controller;
		
		// Init collection of filtered/selected indices.
		filteredIndices						= new LinkedHashSet<Integer>();
		selectedFilteredIndices				= new LinkedHashSet<Integer>();
		discardedIndices					= new LinkedHashSet<Integer>();
	}
	
	/**
	 * Sets references to first-level data.
	 */
	public void setDataReferences(ArrayList<LDAConfiguration> ldaConfigurations, double[][] coordinates, double[][] distances)
	{
		// Get LDA configurations. Important: Integrity/consistency checks ensure that
		// workspace.ldaConfigurations and coordinates/distances are in the same order. 
		this.ldaConfigurations 	= ldaConfigurations;
		// Load current MDS data from workspace.
		this.coordinates		= coordinates;
		// Load current distance data from workspace.
		this.distances			= distances;
	}
	
	public void parseData()
	{
		
	}
		
	// -----------------------------------------------
	// 				Analytic methods.
	// -----------------------------------------------
	
	
	/**
	 * Identify maxima and minima in set of loaded LDA parameters.
	 * @param ldaConfigurations
	 * @return
	 */
	public Map<String, Pair<Double, Double>> identifyLDAParameterExtrema(ArrayList<LDAConfiguration> ldaConfigurations)
	{
		Map<String, Pair<Double, Double>> parameterExtrema = new HashMap<String, Pair<Double, Double>>(LDAConfiguration.SUPPORTED_PARAMETERS.length);
		
		// Init parameter extrema collection.
		for (String param : LDAConfiguration.SUPPORTED_PARAMETERS) {
			parameterExtrema.put(param, new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE));
		}
		
		// Search for extrema in all LDA configurations.
		for (LDAConfiguration ldaConfig : ldaConfigurations) {
			// For all supported parameters:
			for (String param : LDAConfiguration.SUPPORTED_PARAMETERS) {
				double value	= ldaConfig.getParameter(param);
				double min		= value < parameterExtrema.get(param).getKey()		? value : parameterExtrema.get(param).getKey();
				double max 		= value > parameterExtrema.get(param).getValue() 	? value : parameterExtrema.get(param).getValue();
				
				parameterExtrema.put(param, new Pair<Double, Double>(min, max));
			}
		}
		
		return parameterExtrema;
	}
	
	/**
	 * Creates set of selected and filtered indices.
	 * @param filteredIndices
	 * @param selectedIndices
	 * @return
	 */
	public Set<Integer> createSelectedFilteredIndexSet(Set<Integer> filteredIndices, Set<Integer> selectedIndices)
	{
		Set<Integer> selectedFilteredIndices = new HashSet<Integer>(selectedIndices.size());
		
		for (int selectedIndex : selectedIndices) {
			if (filteredIndices.contains(selectedIndex)) {
				selectedFilteredIndices.add(selectedIndex);
			}
		}
		
		return selectedFilteredIndices;
	}
	
	/**
	 * Creates set of discarded indices.
	 * Picks every index from 0 to the number of LDA configurations that's not in 
	 * the set of filtered indices.
	 * @param filteredIndices
	 * @return
	 */
	public Set<Integer> createDiscardedIndexSet(Set<Integer> filteredIndices)
	{
		Set<Integer> discardedIndices = new HashSet<Integer>();
		
		for (int i = 0; i < ldaConfigurations.size(); i++) {
			if (!filteredIndices.contains(i))
				discardedIndices.add(i);
		}
		
		return discardedIndices;
	}

	/**
	 * Creates matrix of coordinates of discarded (non-filtered) datapoints.
	 * @param discardedIndices
	 * @return
	 */
	public double[][] createDiscardedCoordinateMatrix(Set<Integer> discardedIndices)
	{
		double[][] discardedCoordinates = new double[coordinates.length][discardedIndices.size()];
		
		int count = 0;
		for (int discardedIndex : discardedIndices) {
			// Copy MDS coordinates.
			for (int column = 0; column < coordinates.length; column++) {
				discardedCoordinates[column][count] = coordinates[column][discardedIndex];
			}
			
			count++;
		}
		
		return discardedCoordinates;
	}
	
	/**
	 * Creates list of LDA configurations out of sets of filtered and selected indices. 
	 * @param selectedFilteredIndices
	 * @return
	 */
	public ArrayList<LDAConfiguration> createSelectedLDAConfigurations(Set<Integer> selectedFilteredIndices)
	{
		ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations = new ArrayList<LDAConfiguration>(selectedFilteredIndices.size());
		
		for (int index : selectedFilteredIndices) {
			selectedFilteredLDAConfigurations.add(ldaConfigurations.get(index));
		}
		
		return selectedFilteredLDAConfigurations;
	}
	
	/**
	 * Creates list of LDA configurations out of sets of discarded indices. 
	 * @param discardedIndices
	 * @return
	 */
	public ArrayList<LDAConfiguration> createDiscardedLDAConfigurations(Set<Integer> discardedIndices)
	{
		ArrayList<LDAConfiguration> discardedLDAConfigurations = new ArrayList<LDAConfiguration>(discardedIndices.size());
		
		for (int index : discardedIndices) {
			discardedLDAConfigurations.add(ldaConfigurations.get(index));
		}
		
		return discardedLDAConfigurations;
	}	
	
	/**
	 * Creates distance matrix out of sets of filtered and selected indices. 
	 * @param filteredIndices
	 * @param selectedIndices
	 * @return
	 */
	public double[][] createSelectedDistanceMatrix(Set<Integer> selectedFilteredIndices)
	{
		// Copy actual distance data in array.
		double[][] filteredSelectedDistances = new double[selectedFilteredIndices.size()][selectedFilteredIndices.size()];
		
		int count = 0;
		for (int index : selectedFilteredIndices) {
			int innerCount = 0;
			for (int innerIndex : selectedFilteredIndices) {
				filteredSelectedDistances[count][innerCount] = distances[index][innerIndex];
				innerCount++;
			}
			
			count++;
		}
		
		return filteredSelectedDistances;
	}
	
	/**
	 * Creates matrix of coordinates of selected (non-filtered) datapoints.
	 * @param selectedIndices
	 * @return
	 */
	public double[][] createSelectedCoordinateMatrix(Set<Integer> selectedIndices)
	{
		double[][] selectedCoordinates = new double[coordinates.length][selectedIndices.size()];
		
		int count = 0;
		for (int selectedIndex : selectedIndices) {
			// Copy MDS coordinates.
			for (int column = 0; column < coordinates.length; column++) {
				selectedCoordinates[column][count] = coordinates[column][selectedIndex];
			}
			
			count++;
		}
		
		return selectedCoordinates;
	}
	
	/**
	 * Creates distance matrix out of sets of discarded indices.
	 * Contains distances between discarded datasets only.
	 * @param filteredIndices
	 * @param selectedIndices
	 * @return
	 */
	public double[][] createDiscardedDistanceMatrix(Set<Integer> discardedIndices)
	{
		// Copy actual distance data in array.
		double[][] discardedDistances = new double[discardedIndices.size()][discardedIndices.size()];
		
		int count = 0;
		for (int index : discardedIndices) {
			int innerCount = 0;
			for (int innerIndex : discardedIndices) {
				discardedDistances[count][innerCount] = distances[index][innerIndex];
				innerCount++;
			}
			
			count++;
		}
		
		return discardedDistances;
	}	
	
	// -----------------------------------------------
	// 				Getter and Setter
	// -----------------------------------------------

	public double[][] getCoordinates()
	{
		return coordinates;
	}

	public double[][] getDistances()
	{
		return distances;
	}

	public ArrayList<LDAConfiguration> getLDAConfigurations()
	{
		return ldaConfigurations;
	}

	public Set<Integer> getFilteredIndices()
	{
		return filteredIndices;
	}

	public double[][] getFilteredCoordinates()
	{
		return filteredCoordinates;
	}

	public double[][] getFilteredDistances()
	{
		return filteredDistances;
	}

	public ArrayList<LDAConfiguration> getFilteredLDAConfigurations()
	{
		return filteredLDAConfigurations;
	}

	public Set<Integer> getDiscardedIndices()
	{
		return discardedIndices;
	}

	public double[][] getDiscardedCoordinates()
	{
		return discardedCoordinates;
	}

	public double[][] getDiscardedDistances()
	{
		return discardedDistances;
	}

	public ArrayList<LDAConfiguration> getDiscardedLDAConfigurations()
	{
		return discardedLDAConfigurations;
	}

	public Set<Integer> getSelectedFilteredIndices()
	{
		return selectedFilteredIndices;
	}

	public double[][] getSelectedFilteredDistances()
	{
		return selectedFilteredDistances;
	}

	public ArrayList<LDAConfiguration> getSelectedFilteredLDAConfigurations()
	{
		return selectedFilteredLDAConfigurations;
	}

	public boolean isGlobalExtremaIdentified()
	{
		return globalExtremaIdentified;
	}

	public AnalysisController getController()
	{
		return controller;
	}
}
