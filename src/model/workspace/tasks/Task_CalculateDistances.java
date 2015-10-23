package model.workspace.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;
import database.DBManagement;
import mdsj.Data;
import model.LDAConfiguration;
import model.workspace.Dataset;
import model.workspace.DatasetDistance;
import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

/**
 * Calculates distances (requires loaded raw topic data).
 * Writes results to file (path as specified in @Workspace#directory + @Workspace#FILENAME_DISTANCES.
 * @author RM
 *
 */
public class Task_CalculateDistances extends WorkspaceTask
{

	public Task_CalculateDistances(Workspace workspace, WorkspaceAction workspaceAction, final Map<String, Integer> additionalOptionSet)
	{
		super(workspace, workspaceAction, additionalOptionSet);
	}

	@Override
	protected Integer call() throws Exception
	{
		// Open connection to database.
		DBManagement db										= workspace.getDatabaseManagement();
		// Get current dataset map.
		final Map<LDAConfiguration, Dataset> datasetMap		= workspace.getDatasetMap();
		// Get current LDA configuration list.
		final ArrayList<LDAConfiguration> ldaConfigurations	= workspace.getLDAConfigurations();
		
		// Calculated distances.
		double distances[][]								= new double[datasetMap.size()][datasetMap.size()];
		
		// n: Auxiliary variable used for calculating the total number of connections.
		int n												= ldaConfigurations.size() - 1;
		// Stores number of calculated distances to date.
		int numberOfCalculatedDistances						= 0;
		// Total number of distances to calculate (+ n since the distance of the topics of one model to itself have to be calculated also).
		int totalNumberOfDistances							= (n * n + n) / 2 + n;
		
		// Holds distances between two topics of different datasets.
		Map<Pair<LDAConfiguration, LDAConfiguration>, double[][]> topicDistances = new HashMap<Pair<LDAConfiguration,LDAConfiguration>, double[][]>();
		
		// Update task progress.
		updateProgress(0, 1);
		
		// Fetch configuration IDs of all LDA configurations for which distances have not been calculated yet.
		Set<Integer> listOfLDAConfigsWithoutDistances 		= db.loadLDAConfigIDsWithoutDistanceMatrixEntries();
		
		// Determine whether all distances should be calculated.
		boolean calculateAllDistances = additionalOptionSet == null	|| 
										additionalOptionSet != null && (additionalOptionSet.get("forceDistanceRecalculation") == 1);
		
	
//		for (int i : listOfLDAConfigsWithoutDistances)
//			System.out.println("\tblub - " + i);
		
		/*
		 * Compare all datasets with each other, calculate distances.
		 */
		try {	
			double delta = 0;
			for (int i = 0; i < ldaConfigurations.size(); i++) {
				distances[i][i] = 0;
				
				// Get dataset 1.
				Dataset dataset1 = datasetMap.get(ldaConfigurations.get(i));
				
				// Calculate other distances.
				for (int j = i; j < ldaConfigurations.size(); j++) {
					// Update task progress.
					updateProgress(numberOfCalculatedDistances++, totalNumberOfDistances);
					
					// Adaptive distance calculation:
					// Check if distances have to be calculated for this pairing of topic models.
					if (	calculateAllDistances	||
							(
								listOfLDAConfigsWithoutDistances.contains(ldaConfigurations.get(i).getConfigurationID()) ||
								listOfLDAConfigsWithoutDistances.contains(ldaConfigurations.get(j).getConfigurationID())
							)
					) {
						System.out.println("Calculating " + ldaConfigurations.get(i).getConfigurationID() + " to " + ldaConfigurations.get(j).getConfigurationID());
						// Get dataset 2.
						Dataset dataset2 = datasetMap.get(ldaConfigurations.get(j));
						
						// Allocate topic distance matrix.
						double currTopicDistances[][] = new double[dataset1.getTopics().size()][dataset2.getTopics().size()];
						
						// Assume symmetric distance calculations is done in .calculateDatasetDistance().
						distances[i][j] = (float)dataset1.calculateDatasetDistance(dataset2, DatasetDistance.HausdorffDistance, currTopicDistances);
						distances[j][i] = distances[i][j];
						delta += Math.abs(distances[i][j] - distances[j][i]); 
						
						// Store topic distance matrix for the current two datasets/topic models in map.
						Pair<LDAConfiguration, LDAConfiguration> topicDistanceKey = new Pair<LDAConfiguration, LDAConfiguration>(ldaConfigurations.get(i),  ldaConfigurations.get(j));
						topicDistances.put(topicDistanceKey, currTopicDistances);
					}
				}
			}
			
			if (delta > 0)
				System.out.println("delta_source = " + delta);
		
			// Save topic distances to database.
			db.saveTopicDistances(topicDistances, false, this);

			// Save dataset distances to database.
			db.saveDatasetDistances(ldaConfigurations, distances, calculateAllDistances, listOfLDAConfigsWithoutDistances, this);
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}

		
		// Update task progress.
		updateProgress(1, 1);
		
		// Transfer distance data to workspace instance.
		workspace.setDistances(distances);
		
		// Notify workspace that distance data is loaded (if all distances were calculated).
		workspace.setDistanceDataLoaded(calculateAllDistances);
		
		// Clear raw data (don't wanna use memory without a good reason to).
		workspace.getDatasetMap().clear();
		
		return 1;
	}

}

// File-based version:
//// Write first (configuration) line.
//writer.write("\t\t\t\t\t\t\t");
//for (int i = 0; i < ldaConfigurations.size(); i++) {
//	writer.write(ldaConfigurations.get(i).toString() + "\t");
//}
//writer.write("\n");
//
///*
// * Normalize values and write them to file.
// */
//
//for (int i = 0; i < ldaConfigurations.size(); i++) {
//	// Output LDA configuration at line start.
//	String fileOutput = ldaConfigurations.get(i).toString() + "\t\t";
//	
//	// Fill in with already calculated distances.
//	for (int k = 0; k < i + 1; k++) {
//		fileOutput += (float)distances[i][k] + " ";
//	}
//
//	for (int j = i + 1; j < ldaConfigurations.size(); j++) {
//		distances[i][j] *= normalizationFactor;
//		distances[j][i]  = distances[i][j];
//		
//		// Append newly calculated distance.
//		fileOutput += (float)distances[i][j] + " ";
//	}
//
//	// Start next line in file.
//	if (i < ldaConfigurations.size() - 1)
//		fileOutput += "\n";
//
//	// Write to file.
//	writer.write(fileOutput);
//}