package model.workspace.tasks;

import java.util.ArrayList;
import java.util.Map;

import database.DBManagement;
import model.LDAConfiguration;
import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

public class Task_LoadTopicDistancesForSelection extends WorkspaceTask
{
	/**
	 * Stores loaded topic distances.
	 */
	private double[][] topicDistances;
	
	/**
	 * Map translating row/column number to the corresponding LDA configuration; the actual topic distance matrix.
	 */
	private Map<Integer, LDAConfiguration> topicToLDAConfigurationIDMap;
	
	/**
	 * Set of LDA configurations to load.
	 */
	private ArrayList<LDAConfiguration> ldaConfigurationsToLoad;
	
	public Task_LoadTopicDistancesForSelection(	Workspace workspace, WorkspaceAction workspaceAction, 
												final Map<String, Integer> additionalOptionSet,
												ArrayList<LDAConfiguration> ldaConfigurationsToLoad)
	{
		super(workspace, workspaceAction, additionalOptionSet);
		
		this.ldaConfigurationsToLoad = ldaConfigurationsToLoad;
	}

	@Override
	protected Integer call() throws Exception
	{
		// Get DB.
		DBManagement db = workspace.getDatabaseManagement();
		
		// Load topic distances.
		db.loadTopicDistances(ldaConfigurationsToLoad);
		
		return 1;
	}

}