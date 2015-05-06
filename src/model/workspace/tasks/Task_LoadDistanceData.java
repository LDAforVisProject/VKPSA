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

public class Task_LoadDistanceData extends Task_WorkspaceTask
{

	public Task_LoadDistanceData(Workspace workspace, WorkspaceAction workspaceAction)
	{
		super(workspace, workspaceAction);
	}

	@Override
	protected Integer call() throws Exception
	{
		ArrayList<LDAConfiguration> ldaConfigurations	= new ArrayList<LDAConfiguration>();
		int n											= -1;
		double distances[][]							= null;
		
		// File data.
		Path path										= Paths.get(workspace.getDirectory(), Workspace.FILENAME_DISTANCES);
	    Charset charset									= Charset.forName("UTF-8");
	    
	    // Auxiliary variables.
	    int totalItems					= 1;
	    int processedItems				= 0;
	    int numberOfDatasetsInDISFile	= 0;
	    
	    updateProgress(0, Double.MAX_VALUE);
	    
	    // Process file.
	    if (Files.exists(path)) {
			try {
				List<String> lines = Files.readAllLines(path, charset);
				
				String[] ldaConfigStrings = lines.get(0).split("\t");
				for (String ldaConfigString : ldaConfigStrings) {
					if (ldaConfigString.length() > 1) {
						ldaConfigurations.add(LDAConfiguration.generateLDAConfiguration(ldaConfigString));
					}
				}
				
				// Init variables.
				n			= ldaConfigurations.size();
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
								
								System.out.println("\t\tHERE 4-4");
							}
						}
							
					}
					
					fromIndex++;
				}
				
				// Wrap up.
				for (int i = 0; i < distances.length; i++) {
					for (int j = 0; j < distances[i].length; j++) {
						System.out.print(distances[i][j] + " ");
					}
					System.out.println();
				}
				
				// Update workspace variables.
				workspace.setNumberOfDatasetsInDISFile(numberOfDatasetsInDISFile);
				workspace.setLDAConfigurations(ldaConfigurations);
				workspace.setDistances(distances);
				
				// Tell workspace that distance data was loaded.
				workspace.setDistanceDataLoaded(true);
			} 
			
			catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
	    // File doesn't exist.
	    else {
	    	return -1;
	    }
	    
		return numberOfDatasetsInDISFile;
	}

}
