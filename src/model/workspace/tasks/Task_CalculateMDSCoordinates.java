package model.workspace.tasks;

import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import mdsj.Data;
import mdsj.MDSJ;
import model.LDAConfiguration;
import model.workspace.Dataset;
import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

/**
 * Calculates MDS coordinates (requires loaded distance data).
 * Writes results to file (path as specified in @Workspace#directory + @Workspace#FILENAME_DISTANCES.
 * @author RM
 *
 */
public class Task_CalculateMDSCoordinates extends Task_WorkspaceTask
{

	public Task_CalculateMDSCoordinates(Workspace workspace, WorkspaceAction workspaceAction)
	{
		super(workspace, workspaceAction);
	}

	@Override
	protected Integer call() throws Exception
	{
		// @todo Check if distance data has already been loaded. If not, load it before MDS calculation. If so, use workspace.getDistances().
		
		// Get data from workspace.
		final ArrayList<LDAConfiguration> ldaConfigurations	= workspace.getLDAConfigurations();
		final double[][] distances							= workspace.getDistances();
		
		// Execute MDS on topic distance matrix.
		double[][] output									= MDSJ.classicalScaling(distances, 2);
		System.out.println(Data.format(output));

		try {
			String path			= Paths.get(workspace.getDirectory() + Workspace.FILENAME_MDSCOORDINATES).toString();
			PrintWriter writer	= new PrintWriter(path, "UTF-8");
			
			System.out.println("1: " + output.length);
			System.out.println("2: " + output[0].length);
			
			// Write LDA configurations above the respective columns.
			for (int j = 0; j < output[0].length; j++) {
				writer.print(ldaConfigurations.get(j) + " ");
			}
			
			for (int i = 0; i < output.length; i++) {
				
				for (int j = 0; j < output[i].length; j++) {
					writer.print(output[i][j] + " ");
				}
				
				if (i < output.length - 1)
					writer.println("");
			}
			
			// Update task progress.
			updateProgress(1, 1);
			
			// Update workspace variables.
			workspace.setMDSCoordinates(output);
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
