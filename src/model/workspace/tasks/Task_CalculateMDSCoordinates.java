package model.workspace.tasks;

import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import mdsj.ClassicalScaling;
import mdsj.Data;
import mdsj.MDSJ;
import model.LDAConfiguration;
import model.workspace.Dataset;
import model.workspace.Workspace;
import model.workspace.TaskType;

/**
 * Calculates MDS coordinates (requires loaded distance data).
 * Writes results to file (path as specified in @Workspace#directory + @Workspace#FILENAME_DISTANCES.
 * @author RM
 *
 */
public class Task_CalculateMDSCoordinates extends WorkspaceTask
{

	public Task_CalculateMDSCoordinates(Workspace workspace, TaskType workspaceAction, final Map<String, Integer> additionalOptionSet)
	{
		super(workspace, workspaceAction, additionalOptionSet);
	}

	@Override
	protected Integer call() throws Exception
	{
		// Get data from workspace.
		final ArrayList<LDAConfiguration> ldaConfigurations	= workspace.getLDAConfigurations();
		final double[][] distances							= workspace.getDistances();
		
		// Create copy of distances, since MDSJ seems to change the input matrix.
		double[][] distancesCopy							= new double[distances.length][distances.length];
		for (int i = 0; i < distances.length; i++)
			for (int j = 0; j < distances.length; j++)
				distancesCopy[i][j] = distances[i][j];
		
		// Apply MDS on topic distance matrix.
		double[][] output									= new double[2][distances.length]; 
		
		ClassicalScaling.fullmds(distancesCopy, output);
		//MDSJ.classicalScaling(distances, 2);
	
		double delta = 0;
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances[i].length; j++) {
				delta += Math.abs(distances[i][j] - distances[j][i]);
			}	
		}
		System.out.println("delta = " + delta);
			
		try {
			String path			= Paths.get(workspace.getDirectory(), Workspace.FILENAME_MDSCOORDINATES).toString();
			PrintWriter writer	= new PrintWriter(path, "UTF-8");
			
			// Write LDA configurations above the respective columns.
			for (int j = 0; j < output[0].length; j++) {
				writer.print(ldaConfigurations.get(j) + " ");
			}
			
			writer.println("");
			
			// Output MDS coordinates.
			for (int i = 0; i < output.length; i++) {
				for (int j = 0; j < output[i].length; j++) {
					writer.print(output[i][j] + " ");
					
					// Update task progress.
					updateProgress(i * output[0].length + j, output.length * output[0].length);
				}
				
				if (i < output.length - 1)
					writer.println("");
			}
			
			// Update task progress.
			updateProgress(1, 1);
			
			// Transfer MDS coordinate data to workspace intance.
			workspace.setMDSCoordinates(output);
			
			// Signal that MDS data is available.
			workspace.setMDSDataLoaded(true);
			
			// Close file writer.
			writer.close();	
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return 1;
	}

}
