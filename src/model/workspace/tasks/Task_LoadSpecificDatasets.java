package model.workspace.tasks;

import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

public class Task_LoadSpecificDatasets extends WorkspaceTask
{

	public Task_LoadSpecificDatasets(Workspace workspace,
			WorkspaceAction workspaceAction)
	{
		super(workspace, workspaceAction);
	}

	@Override
	protected Integer call() throws Exception
	{
		System.out.println("Executing Task_LoadSpecificDatasets.");
		return null;
	}

}
