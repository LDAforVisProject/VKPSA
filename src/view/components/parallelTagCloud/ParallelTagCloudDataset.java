package view.components.parallelTagCloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;
import model.LDAConfiguration;
import model.workspace.TaskType;
import model.workspace.Workspace;
import model.workspace.tasks.ITaskListener;
import view.components.VisualizationComponentDataset;
import model.workspace.tasks.Task_LoadSpecificRawData;;

/**
 * Dataset for parallel tag cloud component.
 * @author RM
 *
 */
public class ParallelTagCloudDataset extends VisualizationComponentDataset
{
	// Due to performance reasons: Limit number of keywords to load.
	final public static int NUMBER_OF_KEYWORDS_TO_LOAD = 100;
	
	/**
	 * Collections of topic configurations used.
	 */
	private ArrayList<Pair<Integer, Integer>> topicConfigurations;

	/**
	 * Keyword/probability pairings.
	 * Key: Topic configuration, value: keyword/probability values.
	 */
	private Map<Pair<Integer, Integer>, ArrayList<Pair<String, Double>>> keywordProbabilities;
	
	/**
	 * Number of keywords for each topic.
	 */
	private int numberOfKeywords; 
	
	/**
	 * Task used for loading keyword/probability pairings from database.
	 */
	private Task_LoadSpecificRawData keywordDataLoadingTask;
	
	
	/**
	 * Default constructor. Not used in production.
	 * @param allLDAConfigurations
	 */
	public ParallelTagCloudDataset(ArrayList<LDAConfiguration> allLDAConfigurations)
	{
		super(allLDAConfigurations);
	}

	/**
	 * Prepares data collection for numberOfKeywords most relevant keywords for each topic.
	 * @param topicConfigurations
	 * @param numberOfKeywords
	 */
	public ParallelTagCloudDataset(Set<Pair<Integer, Integer>> topicConfigurations)
	{
		super(null);
		
		// Sort topic configurations by LDA and topic configuration IDs.
		ArrayList<Pair<Integer, Integer>> tmpTopicConfigurations = new ArrayList<Pair<Integer,Integer>>();
		tmpTopicConfigurations.addAll(topicConfigurations);
		this.topicConfigurations = sortTopicConfigurations(tmpTopicConfigurations);	
	}
	
	/**
	 * Fetches numberOfKeywords most relevant keywords for each specified topic.
	 * @param listener Gets notified once data is loaded.
	 * @param workspace
	 */
	public void fetchTopicProbabilityData(ITaskListener listener, Workspace workspace)
	{
		// Load topic distance data for selection.
		// Load data for maximum number of keywords specified.
		keywordDataLoadingTask = new Task_LoadSpecificRawData(	workspace, TaskType.LOAD_SPECIFIC_RAW_DATA, null, 
																this.topicConfigurations, ParallelTagCloudDataset.NUMBER_OF_KEYWORDS_TO_LOAD);
		keywordDataLoadingTask.addListener(listener);
		
		// Start thread.
		(new Thread(keywordDataLoadingTask)).start();
	}
	
	/**
	 * Updates keyword/probability data by assuming values loaded
	 * from database by keywordDataLoadingTask.
	 */
	public void updateKeywordProbabilityData()
	{
		this.keywordProbabilities = keywordDataLoadingTask.getKeywordProbabilities();
	}
	
	/**
	 * Sort topic configurations by configuration and topic ID.
	 * @param topicConfigurationsToSort 
	 * @return Sorted list of topic configurations.
	 */
	private ArrayList<Pair<Integer, Integer>> sortTopicConfigurations(ArrayList<Pair<Integer, Integer>> topicConfigurationsToSort)
	{
		Collections.sort(topicConfigurationsToSort, new Comparator<Pair<Integer, Integer>>() {
	        @Override
	        public int compare(Pair<Integer, Integer> topicConfig1, Pair<Integer, Integer>  topicConfig2)
	        {	
	        	if (topicConfig1.getKey() < topicConfig2.getKey())
	        		return -1;
	        	
	        	else if (topicConfig1.getKey() > topicConfig2.getKey())
	        		return 1;
	        	
	        	else {
		        	if (topicConfig1.getValue() < topicConfig2.getValue())
		        		return -1;
		        	
		        	else if (topicConfig1.getValue() > topicConfig2.getValue())
		        		return 1;
		        	
		        	else
		        		return 0;
	        	}
	        }
	    });
		
		return topicConfigurationsToSort;
	}

	public Map<Pair<Integer, Integer>, ArrayList<Pair<String, Double>>> getKeywordProbabilities()
	{
		return keywordProbabilities;
	}

	public void setKeywordProbabilities(
			Map<Pair<Integer, Integer>, ArrayList<Pair<String, Double>>> keywordProbabilities)
	{
		this.keywordProbabilities = keywordProbabilities;
	}

	public ArrayList<Pair<Integer, Integer>> getTopicConfigurations()
	{
		return topicConfigurations;
	}

	public int getNumberOfKeywords()
	{
		return numberOfKeywords;
	}

	public Task_LoadSpecificRawData getKeywordDataLoadingTask()
	{
		return keywordDataLoadingTask;
	}
	
	/**
	 * Returns n-th entry in keyword probability map.
	 * Inefficient, but preferred due to it being less effort.
	 * @param n
	 * @return
	 */
	public Map.Entry<Pair<Integer, Integer>, ArrayList<Pair<String, Double>>> getNthKeywordProbabilityArray(int n)
	{
		int i = 0;
		for (Map.Entry<Pair<Integer, Integer>, ArrayList<Pair<String, Double>>> entry : keywordProbabilities.entrySet()) {
			if (i++ == n)
				return entry;
		}
		
		return null;
	}
}
