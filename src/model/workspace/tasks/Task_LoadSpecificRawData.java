package model.workspace.tasks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.util.Pair;
import database.DBManagement;
import model.LDAConfiguration;
import model.workspace.Dataset;
import model.workspace.Workspace;
import model.workspace.TaskType;

/**
 * Loads datasets from specified directory.
 */
public class Task_LoadSpecificRawData extends WorkspaceTask
{
	/**
	 * Keyword/probability pairings.
	 * Key: Topic configuration, value: keyword/probability values.
	 */
	private Map<Pair<Integer, Integer>, ArrayList<Pair<String, Double>>> keywordProbabilities;
	
	/**
	 * Collections of topic configurations used.
	 */
	private ArrayList<Pair<Integer, Integer>> topicConfigurations;
	
	/**
	 * Number of keywords to load.
	 */
	private int numberOfKeywords;
	
	/**
	 * 
	 * @param workspace
	 * @param workspaceAction
	 * @param additionalOptionSet
	 * @param topicConfigurations List of topics for which keyword/probability data should be loaded.
	 */
	public Task_LoadSpecificRawData(Workspace workspace, TaskType workspaceAction, final Map<String, Integer> additionalOptionSet,
									ArrayList<Pair<Integer, Integer>> topicConfigurations, int numberOfKeywords)
	{
		super(workspace, workspaceAction, additionalOptionSet);
		
		this.topicConfigurations 	= topicConfigurations;
		this.numberOfKeywords		= numberOfKeywords;
	}

	/**
	 * Loads datasets from specified directory. Uses the value
	 * specified in @Workspace#directory.
	 * @return The number of found datasets.
	 */
	@Override
	protected Integer call() throws Exception
	{
		// Open connection to database.
		DBManagement db	= workspace.getDatabaseManagement();

		// Update task progress.
		updateProgress(0,  1);
		
		// Load n most relevant keyword/probability pairs.
		keywordProbabilities = new LinkedHashMap<Pair<Integer, Integer>, ArrayList<Pair<String, Double>>>(topicConfigurations.size());
		for (Pair<Integer, Integer> topicConfig : this.topicConfigurations) {
			keywordProbabilities.put( topicConfig, db.getRawDataForTopic(topicConfig.getKey(), topicConfig.getValue(), numberOfKeywords) );	
		}
		
		// Update task progress.
		updateProgress(1,  1);
		
		return 1;
	}

	public Map<Pair<Integer, Integer>, ArrayList<Pair<String, Double>>> getKeywordProbabilities()
	{
		return keywordProbabilities;
	}
}
