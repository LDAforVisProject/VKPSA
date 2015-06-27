package model.workspace.tasks;

import java.util.Map;

import database.DBManagement;
import model.LDAConfiguration;
import model.workspace.Dataset;
import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

/**
 * Loads datasets from specified directory.
 */
public class Task_LoadRawData extends WorkspaceTask
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
		// Open connection to database.
		DBManagement db										= workspace.getDatabaseManagement();

		// Update task progress.
		updateProgress(0,  1);
		
		// Collection of datasets including lists of topics where each topic includes a complete 
		// dictionary containing words and their probabilities for the respective words for this topic.
		// Get all keyword/probability pairs for each topic in each dataset/LDA configuration.
		Map<LDAConfiguration, Dataset> datasetMap			= db.loadRawData(this);;
		
		// Update task progress.
		updateProgress(1,  1);
		
		// Update workspace variables.
		workspace.setDatasetMap(datasetMap);
		
		// Tell workspace that raw topic data was loaded.
		workspace.setRawDataLoaded(true);
		
		return datasetMap.size();
	}
}


// File-based version:
/*
 	
 		Map<LDAConfiguration, Dataset> datasetMap			= new HashMap<LDAConfiguration, Dataset>();
		final ArrayList<LDAConfiguration> ldaConfigurations	= workspace.getLDAConfigurations();

		// Get all keyword/probability pairs for each topic in each dataset/LDA configuration.
		db.loadRawData(datasetMap);

		
		final String[] filenames							= new File(workspace.getDirectory()).list();
		int numberOfDatasets								= 0;
		
		// Get number of .csv files in directory (for progress indicators).
		int csvCount										= workspace.getNumberOfDatasetsInWS();
		
		db.closeConnection();
		
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
 
 
*/
