package model.workspace.tasks;

import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import model.LDAConfiguration;
import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

public class Task_GenerateData extends Task_WorkspaceTask
{
	public Task_GenerateData(Workspace workspace, WorkspaceAction workspaceAction)
	{
		super(workspace, workspaceAction);
	}
	
	@Override
	protected Integer call() throws Exception
	{
		ArrayList<LDAConfiguration> configurationsToGenerate = workspace.getConfigurationsToGenerate();
		
		try {
			String path			= Paths.get(workspace.getDirectory(), Workspace.FILENAME_TOGENERATE).toString();
			PrintWriter writer	= new PrintWriter(path, "UTF-8");
			
			// Create LDA
			for (LDAConfiguration ldaConfiguration : configurationsToGenerate) {
				writer.write(ldaConfiguration.toString() + "\n");
			}
			
			// @todo Here: Command python script to generate data. 
			
			// Update task progress.
			updateProgress(1, 1);
			
			// Clear workspace collection containing parameters to generate.
			configurationsToGenerate.clear();
			
			// Close file writer.
			writer.close();	
		}
		
		catch (Exception e) {
			
		}
		
		System.out.println("blub");
		
		return null;
	}

}
