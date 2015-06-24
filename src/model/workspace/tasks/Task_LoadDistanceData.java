package model.workspace.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import mdsj.MDSJ;
import model.LDAConfiguration;
import model.topic.Topic;
import model.topic.TopicKeywordAlignment;
import model.workspace.Dataset;
import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

public class Task_LoadDistanceData extends WorkspaceTask
{
	public Task_LoadDistanceData(Workspace workspace, WorkspaceAction workspaceAction)
	{
		super(workspace, workspaceAction);
	}

	@Override
	protected Integer call() throws Exception
	{
		// Assume integrity check was executed and LDA configurations are consistent with data in file.
		
		int n												= -1;
		double distances[][]								= null;
		
		// File(-path) metadata.
		Path path											= Paths.get(workspace.getDirectory(), Workspace.FILENAME_DISTANCES);
	    Charset charset										= Charset.forName("UTF-8");
	    
	    // Auxiliary variables.
	    int numberOfDatasetsInDISFile						= 0;
	    int totalItems										= 1;
	    int processedItems									= 0;
	    
	    updateProgress(0, Double.MAX_VALUE);
	    
	    // Process file.
	    if (Files.exists(path)) {
	    	try {
				List<String> lines = Files.readAllLines(path, charset);
				
				// Init variables.
				n			= lines.size() - 1;//ldaConfigurations.size();
				distances	= new double[n][n];
				totalItems	= (n * n - n) / 2;
				
				// Parse distance data.
				int fromIndex = 0;
				for (String line : lines.subList(1, lines.size())) {

					// Ignore LDA configuration string at the beginning of the line; process data following it.
					for (String subLine : line.split("\t")) {

						// Make sure we get only numbers (and not the config string at the beginning of the line).
						if (!subLine.isEmpty() && subLine.indexOf("=") == -1) {

							int toIndex = 0;
							for (String value : subLine.split(" ")) {
								distances[fromIndex][toIndex] = Double.valueOf(value);
								distances[toIndex][fromIndex] = distances[fromIndex][toIndex];
								
								// Update task progress.
								updateProgress(processedItems, totalItems);
								
								toIndex++;
								processedItems++;
							}
						}
					}
					
					fromIndex++;
				}
				
				// Store number of datasets in this .dis file.
				numberOfDatasetsInDISFile = fromIndex;
				
				// Update number of datasets in .dis file in workspace reference.
				workspace.setNumberOfDatasetsInDISFile(numberOfDatasetsInDISFile);
				
				// Transfer distances between datasets.
				workspace.setDistances(distances);
				
				// Tell workspace that distance data was loaded.
				workspace.setDistanceDataLoaded(true);
				
				// Update task progress.
				updateProgress(1, 1);
			} 
			
			catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
	    // File doesn't exist.
	    else {
			// Update progress.
			updateProgress(1, 1);
			
	    	return -1;
	    }
	    
		return numberOfDatasetsInDISFile;
	}

}
