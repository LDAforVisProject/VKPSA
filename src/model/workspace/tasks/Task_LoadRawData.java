package model.workspace.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.util.Pair;
import model.LDAConfiguration;
import model.topic.Topic;
import model.topic.TopicKeywordAlignment;
import model.workspace.Dataset;
import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

/**
 * Loads datasets from specified directory.
 */
public class Task_LoadRawData extends Task_WorkspaceTask
{

	public Task_LoadRawData(Workspace workspace, WorkspaceAction workspaceAction)
	{
		super(workspace, workspaceAction);
	}

	/**
	 * Loads datasets from specified directory. Uses the value
	 * specified in @Workspace#directory.
	 * @return The number of found datasets.
	 */
	@Override
	protected Integer call() throws Exception
	{
		Map<LDAConfiguration, Dataset> datasetMap			= new HashMap<LDAConfiguration, Dataset>();
		final ArrayList<LDAConfiguration> ldaConfigurations	= workspace.getLDAConfigurations();
		
		final String[] filenames							= new File(workspace.getDirectory()).list();
		int numberOfDatasets								= 0;
		
		// Get number of .csv files in directory (for progress indicators).
		int csvCount = workspace.getNumberOfDatasetsInWS();
		
		// Iterate through files.
		for (String filename : filenames) {
			// If .csv: Process next file.
			try {
				if (filename.endsWith(".csv")) {
					numberOfDatasets++;
					
					// Generate topic instance from file data.
					Pair<LDAConfiguration, ArrayList<Topic>> topicsetTuple = Topic.generateTopicsFromFile(workspace.getDirectory(), filename, TopicKeywordAlignment.HORIZONTAL);
					
					// Add dataset to collections.
					if (!ldaConfigurations.contains(topicsetTuple.getKey()))
						System.out.println("### ERROR ### LDAConfiguration " + topicsetTuple.getKey() + " not found.");
					
					datasetMap.put( topicsetTuple.getKey(), new Dataset(topicsetTuple.getKey(), topicsetTuple.getValue()) );
					
					// Update task progress. 
					updateProgress(numberOfDatasets, csvCount);
				}
			}
			
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Update workspace variables.
		workspace.setDatasetMap(datasetMap);
		
		// Tell workspace that raw topic data was loaded.
		workspace.setRawDataLoaded(true);
		
		return numberOfDatasets;
	}
}
