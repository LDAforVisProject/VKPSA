package model.workspace.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javafx.util.Pair;
import database.DBManagement;
import mdsj.Data;
import model.LDAConfiguration;
import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

public class Task_LoadTopicDistancesForSelection extends WorkspaceTask
{
	/*
	 * --------------------------------------------------------------
	 * 							Data
	 * --------------------------------------------------------------
	 */
	
	/**
	 * Stores loaded topic distances.
	 */
	private double[][] topicDistances;
	
	/**
	 * Map translating the combination of LDA configuration ID/topic ID to spatial IDs (e.g. row and column number in topic distance matrix).
	 */
	private Map<Pair<Integer, Integer>, Integer> spatialIDsForLDATopicConfiguration;
	
	/**
	 * Set of LDA configurations to load.
	 */
	private ArrayList<LDAConfiguration> ldaConfigurationsToLoad;
	
	/**
	 * Filter thresholds for all currently supported parameter. 
	 */
	Map<String, Pair<Double, Double>> parameterFilterThresholds;
	

	/*
	 * --------------------------------------------------------------
	 * 							Methods
	 * --------------------------------------------------------------
	 */
	
	public Task_LoadTopicDistancesForSelection(	Workspace workspace, WorkspaceAction workspaceAction, final Map<String, Integer> additionalOptionSet,
												ArrayList<LDAConfiguration> ldaConfigurationsToLoad, Map<String, Pair<Double, Double>> parameterFilterThresholds)
	{
		super(workspace, workspaceAction, additionalOptionSet);
		
		this.ldaConfigurationsToLoad	= ldaConfigurationsToLoad;
		this.parameterFilterThresholds	= parameterFilterThresholds;
	}

	@Override
	protected Integer call() throws Exception
	{
		// Get DB.
		DBManagement db = workspace.getDatabaseManagement();
		
		// Load topic distances. 
		Pair<Map<Pair<Integer, Integer>, Integer>, double[][]> topicDistancesDataset = db.loadTopicDistances(ldaConfigurationsToLoad);
		
		// Fetch translation LDA config ID/topic ID to spatial ID.
		spatialIDsForLDATopicConfiguration		= topicDistancesDataset.getKey(); 
		// Fetch topic distance matrix.
		this.topicDistances						= topicDistancesDataset.getValue();

		return 1;
	}
}