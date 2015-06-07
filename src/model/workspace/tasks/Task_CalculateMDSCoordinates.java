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
		// Get data from workspace.
		final ArrayList<LDAConfiguration> ldaConfigurations	= workspace.getLDAConfigurations();
		final double[][] distances							= workspace.getDistances();
		
		// Apply MDS on topic distance matrix.
		double[][] output									= MDSJ.classicalScaling(distances, 2);
		
		double delta = 0;
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances[i].length; j++) {
				delta += Math.abs(distances[i][j]) - Math.abs(distances[j][i]);
			}	
		}
		System.out.println("delta = " + delta);
//		System.out.println(Data.format(distances));
//		System.out.println("------");
		System.out.println(Data.format(output));
//
//		double[][] input={        // input dissimilarity matrix
//			    {0.00,2.04,1.92,2.35,2.06,2.12,2.27,2.34,2.57,2.43,1.90,2.41},
//			    {2.04,0.00,2.10,2.00,2.23,2.04,2.38,2.36,2.23,2.36,2.57,2.34},
//			    {1.92,2.10,0.00,1.95,2.21,2.23,2.32,2.46,1.87,1.88,2.41,1.97},
//			    {2.35,2.00,1.95,0.00,2.05,1.78,2.08,2.27,2.14,2.14,2.38,2.17},
//			    {2.06,2.23,2.21,2.05,0.00,2.35,2.23,2.18,2.30,1.98,1.74,2.06},
//			    {2.12,2.04,2.23,1.78,2.35,0.00,2.21,2.12,2.21,2.12,2.17,2.23},
//			    {2.27,2.38,2.32,2.08,2.23,2.21,0.00,2.04,2.44,2.19,1.74,2.13},
//			    {2.34,2.36,2.46,2.27,2.18,2.12,2.04,0.00,2.19,2.09,1.71,2.17},
//			    {2.57,2.23,1.87,2.14,2.30,2.21,2.44,2.19,0.00,1.81,2.53,1.98},
//			    {2.43,2.36,1.88,2.14,1.98,2.12,2.19,2.09,1.81,0.00,2.00,1.52},
//			    {1.90,2.57,2.41,2.38,1.74,2.17,1.74,1.71,2.53,2.00,0.00,2.33},
//			    {2.41,2.34,1.97,2.17,2.06,2.23,2.13,2.17,1.98,1.52,2.33,0.00}
//		        };
//			int n=input[0].length;    // number of data objects
//			double[][] outputx=MDSJ.classicalScaling(input, 2); // apply MDS
//			for(int i=0; i<n; i++) {  // output all coordinates
//			    System.out.println(outputx[0][i]+"\t\t"+outputx[1][i]);
//			}
//			
			
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
