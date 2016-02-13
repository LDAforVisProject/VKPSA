package model.workspace.tasks;

import java.util.Map;

import model.workspace.Workspace;
import model.workspace.TaskType;

public class Task_LoadSpecificDatasets extends WorkspaceTask
{
	public Task_LoadSpecificDatasets(Workspace workspace, TaskType workspaceAction, final Map<String, Integer> additionalOptionSet)
	{
		super(workspace, workspaceAction, additionalOptionSet);
	}

	@Override
	protected Integer call() throws Exception
	{
		System.out.println("Executing Task_LoadSpecificDatasets.");
		return null;
	}

}
