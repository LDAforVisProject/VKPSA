package model.workspace.tasks;

import java.util.ArrayList;

import model.workspace.Workspace;
import model.workspace.WorkspaceAction;
import javafx.concurrent.Task;

public abstract class Task_WorkspaceTask extends Task<Integer>
{
	protected Workspace workspace;
	protected WorkspaceAction workspaceAction;
	protected ArrayList<ITaskListener> listeners;
	
	public Task_WorkspaceTask(Workspace workspace, WorkspaceAction workspaceAction)
	{
		this.workspace			= workspace;
		this.workspaceAction	= workspaceAction;
		this.listeners			= new ArrayList<ITaskListener>();
	}

	public void addListener(ITaskListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(ITaskListener listener)
	{
		listeners.remove(listener);
	}
}
