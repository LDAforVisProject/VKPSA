package model.workspace.tasks;

import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import model.LDAConfiguration;
import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

public class Task_GenerateParameterList extends WorkspaceTask
{
	public Task_GenerateParameterList(Workspace workspace, WorkspaceAction workspaceAction, final Map<String, Integer> additionalOptionSet)
	{
		super(workspace, workspaceAction, additionalOptionSet);
	}
	
	@Override
	protected Integer call() throws Exception
	{
		ArrayList<LDAConfiguration> configurationsToGenerate = workspace.getConfigurationsToGenerate();
		
		try {
			String path			= Paths.get(workspace.getDirectory(), Workspace.FILENAME_TOGENERATE).toString();
			PrintWriter writer	= new PrintWriter(path, "UTF-8");
			
			// Init task progress.
			updateProgress(0, 1);
			
			// Create LDA
			for (int i = 0; i < configurationsToGenerate.size(); i++) {
				writer.write(configurationsToGenerate.get(i).toString() + "\n");
				
				// Update task progress.
				updateProgress(i, configurationsToGenerate.size());
			}
			
			// Update task progress.
			updateProgress(1, 1);
			
			// Close file writer.
			writer.close();	
		}
		
		catch (Exception e) {
			
		}
		
		return null;
	}

}
