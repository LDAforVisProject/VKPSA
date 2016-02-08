package model.workspace.tasks;

import java.util.Map;

import database.DBManagement;
import model.workspace.Workspace;
import model.workspace.TaskType;

public class Task_LoadDistanceData extends WorkspaceTask
{
	public Task_LoadDistanceData(Workspace workspace, TaskType workspaceAction, final Map<String, Integer> additionalOptionSet)
	{
		super(workspace, workspaceAction, additionalOptionSet);
	}

	@Override
	protected Integer call() throws Exception
	{
		// Open connection to database.
		DBManagement db										= workspace.getDatabaseManagement();
		
		// Assume integrity check was executed and LDA configurations are consistent with data in file.
		double distances[][]								= null;
		
	    updateProgress(0, Double.MAX_VALUE);
	    
	    // Process distance data.
	    distances											= db.loadDistances(workspace.getLDAConfigurations(), this);
	    
		// Update number of datasets in .dis file in workspace reference.
//		workspace.setNumberOfDatasetsInDISTable(distances.length);
		
		// Transfer distances between datasets.
		workspace.setDistances(distances);
		
		// Tell workspace that distance data was loaded.
		workspace.setDistanceDataLoaded(true);
		
		// Update task progress.
		updateProgress(1, 1);
	    
		return distances.length;
	}

}

// File-based version:
// Process file.
//if (Files.exists(path)) {
//	try {
//		List<String> lines = Files.readAllLines(path, charset);
//		
//		// Init variables.
//		n			= lines.size() - 1;//ldaConfigurations.size();
//		distances	= new double[n][n];
//		totalItems	= (n * n - n) / 2;
//		
//		// Parse distance data.
//		int fromIndex = 0;
//		for (String line : lines.subList(1, lines.size())) {
//
//			// Ignore LDA configuration string at the beginning of the line; process data following it.
//			for (String subLine : line.split("\t")) {
//
//				// Make sure we get only numbers (and not the config string at the beginning of the line).
//				if (!subLine.isEmpty() && subLine.indexOf("=") == -1) {
//
//					int toIndex = 0;
//					for (String value : subLine.split(" ")) {
//						distances[fromIndex][toIndex] = Double.valueOf(value);
//						distances[toIndex][fromIndex] = distances[fromIndex][toIndex];
//						
//						// Update task progress.
//						updateProgress(processedItems, totalItems);
//						
//						toIndex++;
//						processedItems++;
//					}
//				}
//			}
//			
//			fromIndex++;
//		}
//		
//		// Store number of datasets in this .dis file.
//		numberOfDatasetsInDISFile = fromIndex;
//		
//		// Update number of datasets in .dis file in workspace reference.
//		workspace.setNumberOfDatasetsInDISTable(numberOfDatasetsInDISFile);
//		
//		// Transfer distances between datasets.
//		workspace.setDistances(distances);
//		
//		// Tell workspace that distance data was loaded.
//		workspace.setDistanceDataLoaded(true);
//		
//		// Update task progress.
//		updateProgress(1, 1);
//	} 
//	
//	catch (IOException e) {
//		e.printStackTrace();
//	}
//}

// File doesn't exist.
//else {
//	// Update progress.
//	updateProgress(1, 1);
//	
//	return -1;
//}