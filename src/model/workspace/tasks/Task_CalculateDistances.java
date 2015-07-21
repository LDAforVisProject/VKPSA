package model.workspace.tasks;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
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
		final Map<LDAConfiguration, Dataset> datasetMap		= workspace.getDatasetMap();
		final ArrayList<LDAConfiguration> ldaConfigurations	= workspace.getLDAConfigurations();
		
		// Calculated distances.
		double distances[][]								= new double[datasetMap.size()][datasetMap.size()];
		
		// n: Auxiliary variable used for calculating the total number of connections.
		int n												= ldaConfigurations.size() - 1;
		// Stores number of calculated distances to date.
		int numberOfCalculatedDistances						= 0;
		// Total number of distances to calculate.
		int totalNumberOfDistances							= (n * n + n) / 2;
		// Stores the factor needed to get all distances to >= 1 (workaround needed for MDSJ to work correctly).
		int normalizationFactor								= 0;
		
		// Write calculated distances to file.
		String path = Paths.get(workspace.getDirectory(), Workspace.FILENAME_DISTANCES).toString();
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "utf-8"))) 
		{
			// Update task progress.
			updateProgress(0, 1);
			
			/*
			 * Compare all datasets with each other, calculate distances.
			 */

			for (int i = 0; i < ldaConfigurations.size(); i++) {
				distances[i][i] = 0;
				
				// Calculate other distances.
				for (int j = i + 1; j < ldaConfigurations.size(); j++) {
					// Update task progress.
					updateProgress(numberOfCalculatedDistances, totalNumberOfDistances);
				
					// Assume symmetric distance calculations is done in .calculateDatasetDistance().
					distances[i][j] = (float)datasetMap.get(ldaConfigurations.get(i)).calculateDatasetDistance(datasetMap.get(ldaConfigurations.get(j)), DatasetDistance.HausdorffDistance);
					distances[j][i] = distances[i][j];
					
					int tempNormalizationFactor = 1;
					//for (; distances[i][j] * tempNormalizationFactor < 10; tempNormalizationFactor *= 10);
					normalizationFactor = tempNormalizationFactor > normalizationFactor ? tempNormalizationFactor : normalizationFactor;
					
					numberOfCalculatedDistances++;
				}
			}
			
			// Write first (configuration) line.
			writer.write("\t\t\t\t\t\t\t");
			for (int i = 0; i < ldaConfigurations.size(); i++) {
				writer.write(ldaConfigurations.get(i).toString() + "\t");
			}
			writer.write("\n");
			
			/*
			 * Normalize values and write them to file.
			 */
			
			for (int i = 0; i < ldaConfigurations.size(); i++) {
				// Output LDA configuration at line start.
				String fileOutput = ldaConfigurations.get(i).toString() + "\t\t";
				
				// Fill in with already calculated distances.
				for (int k = 0; k < i + 1; k++) {
					fileOutput += (float)distances[i][k] + " ";
				}

				for (int j = i + 1; j < ldaConfigurations.size(); j++) {
					distances[i][j] *= normalizationFactor;
					distances[j][i]  = distances[i][j];
					
					// Append newly calculated distance.
					fileOutput += (float)distances[i][j] + " ";
				}

				// Start next line in file.
				if (i < ldaConfigurations.size() - 1)
					fileOutput += "\n";

				// Write to file.
				writer.write(fileOutput);
			}
			
			// Update task progress.
			updateProgress(1, 1);
			
			// Transfer distance data to workspace instance.
			workspace.setDistances(distances);
			
			// Notify workspace that distance data is loaded.
			workspace.setDistanceDataLoaded(true);
			
			// Clear raw data.
			workspace.getDatasetMap().clear();
			
			// Close file writer.
			writer.close();
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return 1;
	}

}
