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
	    
	    // Update progress.
	    updateProgress(0, 1);
	    
	    // Process file.
	    if (Files.exists(path)) {
			try {
				List<String> lines = Files.readAllLines(path, charset);
						
				// Skip configuration line, then process coordinates.
				for (String line : lines.subList(1, lines.size())) {
					System.out.println("### " + line);
					String[] coordinates		= line.split(" ");
					numberOfMDSCoordinatesInWS	= 0;
					
					// Estimate number of items to process.
					if (totalItems == -1) {
						totalItems = (lines.size() - 1) * coordinates.length;
					}
					
					// Allocate memory
					if (mdsCoordinates == null)
						mdsCoordinates = new double[2][coordinates.length];
					
					for (String coordinate : coordinates) {
						mdsCoordinates[lineCount][numberOfMDSCoordinatesInWS] = Double.parseDouble(coordinate);
						
						System.out.println("coordinate = " + coordinate);
						
						processedItems++;
						numberOfMDSCoordinatesInWS++;
						
						// Update task progress.
						updateProgress(processedItems, totalItems);
					}
					
					lineCount++;
				}
				
			} 
			
			catch (Exception e) {
				e.printStackTrace();
			}
			
			// Update number of MDS coordinates in file.
			workspace.setNumberOfDatasetsInMDSFile(numberOfMDSCoordinatesInWS);
			
			// Transfer MDS coordinate data to workspace instance.
			workspace.setMDSCoordinates(mdsCoordinates);
			
			// Signal that MDS data is available.
			workspace.setMDSDataLoaded(true);
			
			// Update progress.
			updateProgress(1, 1);
	    }
	    
	    // File doesn't exist.
	    else {
			// Update progress.
			updateProgress(1, 1);
			
	    	return -1;
	    }
	    
		return numberOfMDSCoordinatesInWS;
	}

}
