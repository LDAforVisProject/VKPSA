package model;

import java.util.ArrayList;
import java.util.Set;

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
	// 				Methods
	// -----------------------------------------------
	
	public AnalysisDataspace(AnalysisController controller)
	{
		this.controller = controller;
	}
}
