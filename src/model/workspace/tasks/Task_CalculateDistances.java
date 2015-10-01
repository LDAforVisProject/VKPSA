package model.workspace.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.util.Pair;
import database.DBManagement;
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

	public Task_CalculateDistances(Workspace workspace, WorkspaceAction workspaceAction)
	{
		super(workspace, workspaceAction);
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
		// Total number of distances to calculate.
		int totalNumberOfDistances							= (n * n + n) / 2; 
		// Stores the factor needed to get all distances to >= 1 (workaround needed for MDSJ to work correctly). @deprecated
		int normalizationFactor								= 0;
		
		// Holds distances between two topics of different datasets.
		Map<Pair<LDAConfiguration, LDAConfiguration>, double[][]> topicDistances = new HashMap<Pair<LDAConfiguration,LDAConfiguration>, double[][]>();
		
		// Update task progress.
		updateProgress(0, 1);
		
		System.out.println("Calculating dataset distances.");
		/*
		 * Compare all datasets with each other, calculate distances.
		 */

		for (int i = 0; i < ldaConfigurations.size(); i++) {
			distances[i][i] = 0;
			
			// Calculate other distances.
			for (int j = i + 1; j < ldaConfigurations.size(); j++) {
				// Update task progress.
				updateProgress(numberOfCalculatedDistances, totalNumberOfDistances * 1.25);
				
				// Get datasets.
				Dataset dataset1 = datasetMap.get(ldaConfigurations.get(i));
				Dataset dataset2 = datasetMap.get(ldaConfigurations.get(j));
				
				// Allocate topic distance matrix.
				double currTopicDistances[][] = new double[dataset1.getTopics().size()][dataset2.getTopics().size()];
				
				// Assume symmetric distance calculations is done in .calculateDatasetDistance().
				distances[i][j] = (float)dataset1.calculateDatasetDistance(dataset2, DatasetDistance.HausdorffDistance, currTopicDistances);
				distances[j][i] = distances[i][j];
				
				// Store topic distance matrix for the current two datasets/topic models in map.
				Pair<LDAConfiguration, LDAConfiguration> topicDistanceKey = new Pair<LDAConfiguration, LDAConfiguration>(ldaConfigurations.get(i),  ldaConfigurations.get(j));
				topicDistances.put(topicDistanceKey, currTopicDistances);
				
				int tempNormalizationFactor = 1;
				//for (; distances[i][j] * tempNormalizationFactor < 10; tempNormalizationFactor *= 10);
				normalizationFactor			= tempNormalizationFactor > normalizationFactor ? tempNormalizationFactor : normalizationFactor;
				
				numberOfCalculatedDistances++;
			}
		}
		
		// Save distances to database.
		db.saveDatasetDistances(ldaConfigurations, distances, false);
		
		// Update task progress.
		updateProgress(1, 1);
		
		// Transfer distance data to workspace instance.
		workspace.setDistances(distances);
		
		// Notify workspace that distance data is loaded.
		workspace.setDistanceDataLoaded(true);
		
		// Clear raw data.
		workspace.getDatasetMap().clear();
		
//		catch (Exception e) {
//			e.printStackTrace();
//		}
		
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