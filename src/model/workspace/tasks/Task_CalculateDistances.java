package model.workspace.tasks;

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
 * Calculates distances.
 * @author RM
 *
 */
public class Task_CalculateDistances extends Task_WorkspaceTask
{

	public Task_CalculateDistances(Workspace workspace, WorkspaceAction workspaceAction)
	{
		super(workspace, workspaceAction);
	}

	@Override
	protected Integer call() throws Exception
	{
		// @todox Write distance calculation task.
		// @todox Then write raw data loading task.
		// @todo CURRENT: Then write results to file.
		// @todo Then: Test drive.
		final Map<LDAConfiguration, Dataset> datasetMap		= workspace.getDatasetMap();
		final ArrayList<LDAConfiguration> ldaConfigurations	= workspace.getLdaConfigurations();
		
		double distances[][]								= new double[datasetMap.size()][datasetMap.size()];
		
		// n: Auxiliary variable used for calculating the total number of connections.
		int n												= ldaConfigurations.size() - 1;
		// Stores number of calculated distances to date.
		int numberOfCalculatedDistances						= 0;
		// Total number of distances to calculate.
		int totalNumberOfDistances							= (n * n + n) / 2;  
		
		// Update task progress. .2: Arbitrary portion of progress indicator reserved for file loading.
		updateProgress(0.2, 1);
					
		// Compare all datasets with each other.
		for (int i = 0; i < ldaConfigurations.size(); i++) {
			distances[i][i] = 0;
			
			for (int j = i + 1; j < ldaConfigurations.size(); j++) {
				// Update task progress. .1: Arbitrary portion of progress indicator reserved for file loading.
				updateProgress(totalNumberOfDistances * 0.1 + numberOfCalculatedDistances * 0.9, totalNumberOfDistances);
				
				System.out.println(i + " to " + j);
				
				// Assume symmetric distance calculations is done in .calculateDatasetDistance().
				distances[i][j] = datasetMap.get(ldaConfigurations.get(i)).calculateDatasetDistance(datasetMap.get(ldaConfigurations.get(j)), DatasetDistance.HausdorffDistance);
				distances[j][i] = distances[i][j];
				
				numberOfCalculatedDistances++;
			}
		}

		// Update task progress. .2: Arbitrary portion of progress indicator reserved for file loading.
		updateProgress(1, 1);
		
		// Update workspace variables.
		workspace.setDistances(distances);
		
		return 1;
	}

}
