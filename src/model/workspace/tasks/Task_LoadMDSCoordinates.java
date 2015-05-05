package model.workspace.tasks;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import model.workspace.Workspace;
import model.workspace.WorkspaceAction;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class Task_LoadMDSCoordinates extends Task_WorkspaceTask
{
	public Task_LoadMDSCoordinates(Workspace workspace, WorkspaceAction workspaceAction)
	{
		super(workspace, workspaceAction);
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	protected Integer call() throws Exception
	{
		double[][] mdsCoordinates	= null;
		Path path					= Paths.get(workspace.getDirectory(), Workspace.FILENAME_MDSCOORDINATES);
	    Charset charset				= Charset.forName("UTF-8");
	    
	    int lineCount					= 0;
	    int totalItems					= -1;
	    int processedItems				= 0;
	    int numberOfMDSCoordinatesInWS	= 0;
	    
	    updateProgress(0, Double.MAX_VALUE);
	    
	    // Process file.
	    if (Files.exists(path)) {
			try {
				List<String> lines = Files.readAllLines(path, charset);
						
				for (String line : lines) {
					String[] coordinates		= line.split(" ");
					numberOfMDSCoordinatesInWS	= 0;
					
					// Estimate number of items to process.
					if (totalItems == -1) {
						totalItems = lines.size() * coordinates.length;
					}
					
					// Allocate memory
					if (mdsCoordinates == null)
						mdsCoordinates = new double[2][coordinates.length];
					
					for (String coordinate : coordinates) {
						mdsCoordinates[lineCount][numberOfMDSCoordinatesInWS] = Double.parseDouble(coordinate);
						
						processedItems++;
						numberOfMDSCoordinatesInWS++;
						
						// Update task progress.
						updateProgress(processedItems, totalItems);
					}
					
					lineCount++;
				}
				
			} 
			
			catch (IOException e) {
				e.printStackTrace();
			}
			
			// Update workspace variables.
			workspace.setNumberOfMDSCoordinatesInWS(numberOfMDSCoordinatesInWS);
			workspace.setMDSCoordinates(mdsCoordinates);
			
			// Tell workspace that MDS data was loaded.
			workspace.setMDSDataLoaded(true);
	    }
	    
	    // File doesn't exist.
	    else {
	    	return -1;
	    }
	    
		return numberOfMDSCoordinatesInWS;
	}

}
