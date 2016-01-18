package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javafx.scene.chart.XYChart;
import javafx.util.Pair;
import control.analysisView.AnalysisController;

/**
 * Provides abstraction for data used in AnalysisController.
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
	private Set<Integer> availableIndices;
	/**
	 * Stores filterd coordinates.
	 */
	private double availableCoordinates[][];
	/**
	 * Stores filtered distances.
	 */
	private double availableDistances[][];
	/**
	 * Stores average cohesive distances.
	 */
	private double averageDistances[];
	
	/**
	 * List of filtered LDA configurations.
	 */
	private ArrayList<LDAConfiguration> availableLDAConfigurations;
	
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
	 * Derived, third-level data: Inactive indices, coordinates and distances.
	 */
	
	/**
	 * Set of all datasets matching the currently defined thresholds and selection.
	 */
	private Set<Integer> inactiveIndices;
	/**
	 * Stores filtered distances - without distances from and to selected datapoints.
	 */
	private double inactiveDistances[][];
	/**
	 * Stores inactive LDA configurations.
	 */
	private ArrayList<LDAConfiguration> inactiveLDAConfigurations;
	/**
	 * Stores inactive coordinates.
	 */
	private double inactiveCoordinates[][];
	
	/*
	 * Derived, third-level data: Selected indices, coordinates and distances.
	 */
	
	/**
	 * Set of all datasets matching the currently defined thresholds and selection.
	 */
	private Set<Integer> activeIndices;
	/**
	 * Stores active distances.
	 */
	private double activeDistances[][];
	/**
	 * Stores active LDA configurations.
	 */
	private ArrayList<LDAConfiguration> activeLDAConfigurations;
	/**
	 * Stores selected coordinates.
	 */
	private double activeCoordinates[][];

	
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
		
		// Init collections.
		availableIndices	= new LinkedHashSet<Integer>();
		activeIndices		= new LinkedHashSet<Integer>();
		inactiveIndices		= new LinkedHashSet<Integer>();
		discardedIndices	= new LinkedHashSet<Integer>();
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
	public static Map<String, Pair<Double, Double>> identifyLDAParameterExtrema(ArrayList<LDAConfiguration> ldaConfigurations)
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
	 * Identify maxima and minima in set of loaded LDA parameters.
	 * @param ldaConfigurations
	 * @return
	 */
	public static Map<String, Pair<Double, Double>> identifyLDAParameterExtrema(ArrayList<LDAConfiguration> ldaConfigurations, String param)
	{
		Map<String, Pair<Double, Double>> parameterExtrema = new HashMap<String, Pair<Double, Double>>(LDAConfiguration.SUPPORTED_PARAMETERS.length);
		
		// Init parameter extrema collection.
		parameterExtrema.put(param, new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE));
		
		// Search for extrema in all LDA configurations.
		for (LDAConfiguration ldaConfig : ldaConfigurations) {
			// For all supported parameters:
			double value	= ldaConfig.getParameter(param);
			double min		= value < parameterExtrema.get(param).getKey()		? value : parameterExtrema.get(param).getKey();
			double max 		= value > parameterExtrema.get(param).getValue() 	? value : parameterExtrema.get(param).getValue();
			
			parameterExtrema.put(param, new Pair<Double, Double>(min, max));
		}
		
		return parameterExtrema;
	}
	
	/**
	 * Update dataspace after user selected new datapoints.
	 * @param selectedIndices
	 */
	public void updateAfterSelection(Set<Integer> selectedIndices)
	{
		// Update active values.
		updateActiveIndexSet(selectedIndices == null ? this.activeIndices : selectedIndices);
		updateActiveDistanceMatrix();
		updateActiveLDAConfigurations();
		updateActiveCoordinateMatrix();
		
		// Update inactive values.
		updateInactiveIndexSet(selectedIndices == null ? this.activeIndices : selectedIndices);
		updateInactiveDistanceMatrix();
		updateInactiveLDAConfigurations();
		updateInactiveCoordinateMatrix();
		
		// Update list of average distances.
		averageDistances = calculateAverageDistances();
	}
	
	/**
	 * Updates set of active indices.
	 * @param selectedIndices
	 */
	private void updateActiveIndexSet(Set<Integer> selectedIndices)
	{
		this.activeIndices = createActiveIndexSet(this.availableIndices, selectedIndices);
	}
	
	/**
	 * Updates set of inactive indices.
	 * @param selectedIndices
	 */
	private void updateInactiveIndexSet(Set<Integer> selectedIndices)
	{
		this.inactiveIndices = createInactiveIndexSet(this.availableIndices, selectedIndices);
	}
	
	/**
	 * Creates set of active indices.
	 * @param availableIndices
	 * @param selectedIndices
	 * @return
	 */
	public Set<Integer> createActiveIndexSet(Set<Integer> availableIndices, Set<Integer> selectedIndices)
	{
		Set<Integer> activeIndices = new HashSet<Integer>(selectedIndices.size());
		
		for (int selectedIndex : selectedIndices) {
			if (availableIndices.contains(selectedIndex)) {
				activeIndices.add(selectedIndex);
			}
		}
		
		return activeIndices;
	}
	
	/**
	 * Creates set of inactive indices.
	 * @param availableIndices
	 * @param selectedIndices
	 * @return
	 */
	public Set<Integer> createInactiveIndexSet(Set<Integer> availableIndices, Set<Integer> selectedIndices)
	{
		// Copy all available indices.
		Set<Integer> inactiveIndices = new HashSet<Integer>(availableIndices);
		
		// Remove all selected indices.
		inactiveIndices.removeAll(selectedIndices);
		
		return inactiveIndices;
	}
	
	/**
	 * Updtes set of discarded indices.
	 * @param availableIndices
	 */
	private void updateDiscardedIndexSet()
	{
		this.discardedIndices = createDiscardedIndexSet(this.availableIndices);
	}
	
	/**
	 * Creates set of discarded indices.
	 * Picks every index from 0 to the number of LDA configurations that's not in 
	 * the set of filtered indices.
	 * @param availableIndices
	 * @return
	 */
	public Set<Integer> createDiscardedIndexSet(Set<Integer> availableIndices)
	{
		Set<Integer> discardedIndices = new HashSet<Integer>();
		
		for (int i = 0; i < ldaConfigurations.size(); i++) {
			if (!availableIndices.contains(i))
				discardedIndices.add(i);
		}
		
		return discardedIndices;
	}

	/**
	 * Updates matrix of discarded coordinate.
	 * @param discardedIndices
	 */
	private void updateDiscardedCoordinateMatrix(Set<Integer> discardedIndices)
	{
		this.discardedCoordinates = createCoordinateMatrix(discardedIndices);
	}
	
	/**
	 * Updates selected LDA configuration set.
	 * @param activeIndices
	 */
	private void updateActiveLDAConfigurations()
	{
		this.activeLDAConfigurations = createLDAConfigurations(this.activeIndices);
	}
	
	private void updateInactiveLDAConfigurations()
	{
		this.inactiveLDAConfigurations = createLDAConfigurations(this.inactiveIndices);
	}
	
	/**
	 * Creates list of LDA configurations out of index set. 
	 * @param indices
	 * @return
	 */
	public ArrayList<LDAConfiguration> createLDAConfigurations(Set<Integer> indices)
	{
		ArrayList<LDAConfiguration> ldaConfigurations = new ArrayList<LDAConfiguration>(indices.size());
		
		for (int index : indices) {
			ldaConfigurations.add(this.ldaConfigurations.get(index));
		}
		
		return ldaConfigurations;
	}
	
	/**
	 * Updates discarded LDA configuration set.
	 * @param discardedIndices
	 */
	private void updateDiscardedLDAConfigurations(Set<Integer> discardedIndices)
	{
		this.discardedLDAConfigurations = createLDAConfigurations(discardedIndices);
	}
	
	/**
	 * Updates selected distance matrix.
	 * @param activeIndices
	 */
	private void updateInactiveDistanceMatrix()
	{
		this.inactiveDistances = createDistanceMatrix(this.inactiveIndices);
	}
	
	/**
	 * Updates active distance matrix.
	 * @param activeIndices
	 */
	private void updateActiveDistanceMatrix()
	{
		this.activeDistances = createDistanceMatrix(this.activeIndices);
	}
	
	/**
	 * Creates distance matrix out of sets of indices. 
	 * @param availableIndices
	 * @param indices
	 * @return
	 */
	public double[][] createDistanceMatrix(Set<Integer> indices)
	{
		// Copy actual distance data in array.
		double[][] distances = new double[indices.size()][indices.size()];
		
		int count = 0;
		for (int index : indices) {
			int innerCount = 0;
			for (int innerIndex : indices) {
				distances[count][innerCount] = this.distances[index][innerIndex];
				innerCount++;
			}
			
			count++;
		}
		
		return distances;
	}
	
	/**
	 * Creates distance matrix for all datapoints part of the filtered, but not of the selected dataset.
	 * @param filteredIndices
	 * @param selectedIndices
	 * @return
	 */
//	public double[][] createInactiveDistanceMatrix(Set<Integer> filteredIndices, Set<Integer> selectedIndices)
//	{
//		// Copy actual distance data in array.
//		final int size							= filteredIndices.size() - selectedIndices.size();
//		double[][] reductiveFilteredDistances 	= new double[size][size];
//		
//		int count = 0;
//		for (int index : filteredIndices) {
//			if (!selectedIndices.contains(index)) {
//				int innerCount = 0;
//				for (int innerIndex : filteredIndices) {
//					if (!selectedIndices.contains(innerIndex)) {
//						reductiveFilteredDistances[count][innerCount] = distances[index][innerIndex];
//						innerCount++;
//					}
//				}
//				
//				count++;
//			}
//		}
//		
//		return reductiveFilteredDistances;
//	}
	
	/**
	 * Update active coordinate matrix.
	 */
	private void updateActiveCoordinateMatrix()
	{
		this.activeCoordinates = createCoordinateMatrix(this.activeIndices);
	}
	
	/**
	 * Update inactive coordinate matrix.
	 */
	private void updateInactiveCoordinateMatrix()
	{
		this.inactiveCoordinates = createCoordinateMatrix(this.inactiveIndices);
	}
	
	
	/**
	 * Creates matrix of coordinates of datapoints.
	 * @param indices
	 * @return
	 */
	public double[][] createCoordinateMatrix(Set<Integer> indices)
	{
		double[][] coordinates = new double[this.coordinates.length][indices.size()];
		
		int count = 0;
		for (int index : indices) {
			// Copy MDS coordinates.
			for (int column = 0; column < this.coordinates.length; column++) {
				coordinates[column][count] = this.coordinates[column][index];
			}
			
			count++;
		}
		
		return coordinates;
	}

	/**
	 * Updates discarded distance matrix.
	 * @param discardedIndices
	 * @return
	 */
	private void updateDiscardedDistanceMatrix(Set<Integer> discardedIndices)
	{
		this.discardedDistances = createDistanceMatrix(discardedIndices);
	}
	
	/**
	 * Filter data using the given parameter thresholds.
	 * @param parameterBoundaries
	 */
	public void filterIndices(final Map<String, Pair<Double, Double>> parameterBoundaries)
	{
		// Make sure that derived attributes are calculated.
		if (averageDistances == null)
			averageDistances = calculateAverageDistances();
		
		// Iterate through all LDA configurations in workspace.
		for (int i = 0; i < ldaConfigurations.size(); i++) {
			LDAConfiguration ldaConfig			= ldaConfigurations.get(i);
			boolean fitsBoundaries				= true;
			
			// Check if this particular LDA configuration is in bounds of all specified parameter thresholds.
			for (Map.Entry<String, Pair<Double, Double>> entry : parameterBoundaries.entrySet()) {
				double value	= -1;
				
				// If primitive attribute: Access value directly through LDA object.
				if (LDAConfiguration.supports(entry.getKey())) {
					value = ldaConfig.getParameter(entry.getKey());	
				}
				// Otherwise (derived attribute): Check parameter, get custom information.
				else if (entry.getKey().equals("distance")) {
					value = averageDistances[i];
				}
				
				double min		= entry.getValue().getKey();
				double max		= entry.getValue().getValue();
				
				// Exclude LDA configuration if limits are exceeded.
				if (value < min || value > max) {
					fitsBoundaries = false;
					
					// Stop loop.
					break;
				}
			}
			
			// If in boundaries and not contained in selection:
			if (fitsBoundaries) {
				// Remove from set of discarded indices, if in there.
				if (discardedIndices.contains(i))
					discardedIndices.remove(i);
				
				// Add to set of filtered indices, if not already in there.
				if (!availableIndices.contains(i))
					availableIndices.add(i);
			}
			
			// Else if not in boundaries and contained in selection:
			else if (!fitsBoundaries) {
				// Add to set of discarded indices, if not already in there.
				if (!discardedIndices.contains(i))
					discardedIndices.add(i);
				
				// Remove from set of filtered indices, if in there.
				if (availableIndices.contains(i))
					availableIndices.remove(i);
			}
		}
		
		// Determine set of discarded indices.
		discardedIndices	= createDiscardedIndexSet(availableIndices);
		// Determine set of active indices.
		activeIndices		= createActiveIndexSet(availableIndices, activeIndices);
		// Determine set of inactive indices.
		inactiveIndices		= createInactiveIndexSet(availableIndices, activeIndices);
	}
	
	/**
	 * Filters data.
	 */
	public void filterData()
	{
		/*
		 * Update data collections for filtered datasets. 
		 */
		
		// Use AnalysisController.filteredIndices to filter out data in desired parameter boundaries.
		availableCoordinates		= new double[coordinates.length][availableIndices.size()];
		availableDistances			= new double[availableIndices.size()][availableIndices.size()];
		availableLDAConfigurations	= new ArrayList<LDAConfiguration>(availableIndices.size());
		
		// Copy data corresponding to chosen LDA configurations in new arrays.
		int count = 0;
		for (int filteredIndex : availableIndices) {
			// Copy MDS coordinates.
			for (int column = 0; column < coordinates.length; column++) {
				availableCoordinates[column][count] = coordinates[column][filteredIndex];
			}
			
			// Copy distances.
			int innerCount = 0;
			for (int filteredInnerIndex : availableIndices) {
				availableDistances[count][innerCount] = distances[filteredIndex][filteredInnerIndex];
				innerCount++;
			}
			
			// Copy LDA configurations.
			availableLDAConfigurations.add(ldaConfigurations.get(filteredIndex));
			
			count++;
		}
		
		/*
		 * Update data collections for discarded (not filtered) datasets. 
		 */

		// Update set of discarded values.
		discardedCoordinates				= createCoordinateMatrix(discardedIndices);
		discardedDistances					= createDistanceMatrix(discardedIndices);
		discardedLDAConfigurations			= createLDAConfigurations(discardedIndices);
		
		/*
		 * Update data collections for available subsets (inactive and active datapoints). 
		 */
		
		// Update set of active values.
		activeCoordinates					= createCoordinateMatrix(activeIndices);
		activeDistances						= createDistanceMatrix(activeIndices);
		activeLDAConfigurations				= createLDAConfigurations(activeIndices);
		// Update set of inactive values.		
		inactiveCoordinates					= createCoordinateMatrix(inactiveIndices);
		inactiveDistances					= createDistanceMatrix(inactiveIndices);
		inactiveLDAConfigurations			= createLDAConfigurations(inactiveIndices);
		
		// Update list of average cohesive distances.
		averageDistances					= calculateAverageDistances();
	}
	
	/**
	 * Integrates selection (of LDA configuration IDs) into dataspace.
	 * @param newlySelectedLDAConfigIDs
	 * @param isAddition
	 */
	public boolean integrateSelection(Set<Integer> newlySelectedLDAConfigIDs, final boolean isAddition)
	{
		// Check if there is any change in the set of selected datasets.
		boolean changeDetected = false;
		
		// 1. 	Check which elements are to be added/removed from current selection by comparing 
		// 		with set of filtered and selected datasets.
		
		// 1.a.	Selection should be added:
		if (isAddition) {
			// Check if any of the newly selected IDs are not contained in global selection yet.
			// If so: Add them.
			for (LDAConfiguration selectedLDAConfiguration : this.activeLDAConfigurations) {
				final int alreadySelectedLDAConfigID = selectedLDAConfiguration.getConfigurationID(); 
				
				// If newly selected set already contained in existing selection: Remove from addition set.
				if (newlySelectedLDAConfigIDs.contains(alreadySelectedLDAConfigID)) {
					newlySelectedLDAConfigIDs.remove(alreadySelectedLDAConfigID);
				}
			}
			
			// If set of newly selected indices still contains elements: Change detected.
			if (newlySelectedLDAConfigIDs.size() > 0) {
				// Update flag.
				changeDetected = true;
				
				// Add missing LDA configurations to collection.
				for (final int ldaConfigIndex : getInactiveIndices()) {
					// Get LDA configuration for this index.
					final LDAConfiguration ldaConfiguration = this.ldaConfigurations.get(ldaConfigIndex);
					
					// Check if this LDA configuration is part of the set of newly selected LDA configurations. 
					if ( newlySelectedLDAConfigIDs.contains(ldaConfiguration.getConfigurationID()) )
						this.activeIndices.add(ldaConfigIndex);
				}
			}
		}
		
		// 1.b.	Selection should be removed:
		else {
			System.out.println("no addition");
			// Set of dataset indices (instead of configuration IDs) to delete.
			Set<Integer> indicesToDeleteFromSelection = new HashSet<Integer>();
			
			// Check if any of the newly selected IDs are contained in global selection.
			// If so: Remove them.
			for (final int ldaConfigIndex : this.activeIndices) {
				// Get LDA configuration for this index.
				final LDAConfiguration ldaConfiguration = this.ldaConfigurations.get(ldaConfigIndex); 
				
				// If currently examine LDAConfiguration is in set of newly selected indices:
				// Remove LDAConfiguration from set of selected indices.
				if (newlySelectedLDAConfigIDs.contains(ldaConfiguration.getConfigurationID())) {
					// Update flag.
					changeDetected = true;
					
					// Add dataset index to collection of indices to remove from selection.
					indicesToDeleteFromSelection.add(ldaConfigIndex);
				}
			}
			System.out.println("indices to remove: " + indicesToDeleteFromSelection);
			// Remove set of indices to delete from set of selected indices.
			this.activeIndices.removeAll(indicesToDeleteFromSelection);
		}
		
		// 2. Return dirty bit.
		return changeDetected;
	}
	
	/**
	 * Calculates average cohesive distances within each data series (active, inactive, discarded).
	 * @return
	 */
	private double[] calculateAverageDistances()
	{
		double[] averageDistances = new double[ldaConfigurations.size()];
		
		// Calculate distances of active datasets.
		for (int i : activeIndices) {
			double distanceSum = 0;
			for (int j = 0; j < distances.length; j++) {
				distanceSum += distances[i][j];
			}
			averageDistances[i] = distanceSum / (distances.length - 1);
		}
		
		// Calculate distances of inactive datasets.
		for (int i : inactiveIndices) {
			double distanceSum = 0;
			for (int j = 0; j < distances.length; j++) {
				distanceSum += distances[i][j];
			}
			averageDistances[i] = distanceSum / (distances.length - 1);
		}
		
		// Calculate distances of discarded datasets.
		for (int i : discardedIndices) {
			double distanceSum = 0;
			for (int j = 0; j < distances.length; j++) {
				distanceSum += distances[i][j];
			}
			averageDistances[i] = distanceSum / (distances.length - 1);
		}		
		
		return averageDistances;
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

	public Set<Integer> getInactiveIndices()
	{
		return availableIndices;
	}

	public double[][] getAvailableCoordinates()
	{
		return availableCoordinates;
	}

	public double[][] getAvaibleDistances()
	{
		return availableDistances;
	}

	public ArrayList<LDAConfiguration> getAvailableLDAConfigurations()
	{
		return availableLDAConfigurations;
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

	public Set<Integer> getActiveIndices()
	{
		return activeIndices;
	}

	public double[][] getActiveDistances()
	{
		return activeDistances;
	}

	public double[][] getInactiveDistances()
	{
		return inactiveDistances;
	}
	
	public ArrayList<LDAConfiguration> getInactiveLDAConfigurations()
	{
		return inactiveLDAConfigurations;
	}
	
	public ArrayList<LDAConfiguration> getActiveLDAConfigurations()
	{
		return activeLDAConfigurations;
	}

	public double[][] getActiveCoordinates()
	{
		return activeCoordinates;
	}

	public boolean isGlobalExtremaIdentified()
	{
		return globalExtremaIdentified;
	}

	public AnalysisController getController()
	{
		return controller;
	}

	public double[] getAverageDistances()
	{
		return averageDistances;
	}
}
