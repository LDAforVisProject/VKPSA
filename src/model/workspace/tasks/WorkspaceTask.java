package model.workspace.tasks;

import java.util.ArrayList;
import java.util.Map;

import model.workspace.Workspace;
import model.workspace.WorkspaceAction;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public abstract class WorkspaceTask extends Task<Integer>
{
	protected Workspace workspace;
	protected WorkspaceAction workspaceAction;
	protected ArrayList<ITaskListener> listeners;
	protected Map<String, Integer> additionalOptionSet;
	
	/**
	 * Initializes task with reference to the workspace, the desired WorkspaceAction and an additional (non-mandatory) option set. 
	 * @param workspace
	 * @param workspaceAction
	 * @param additionalOptionSet
	 */
	public WorkspaceTask(Workspace workspace, WorkspaceAction workspaceAction, final Map<String, Integer> additionalOptionSet)
	{
		this.workspace				= workspace;
		this.workspaceAction		= workspaceAction;
		this.additionalOptionSet	= additionalOptionSet;
		this.listeners				= new ArrayList<ITaskListener>();
		
		// Set notification handle for successful and failed exit.
		this.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
	        @Override
	        public void handle(WorkerStateEvent t)
	        {
	        	// Notify all listeners.
	            for (ITaskListener listener : listeners) {
	            	listener.notifyOfTaskCompleted(workspaceAction);
	            }
	        }
		});
	}

	public void addListener(ITaskListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(ITaskListener listener)
	{
		listeners.remove(listener);
	}
	
	public void updateTaskProgress(long workDone, long max)
	{
		updateProgress(workDone, max);
	}
}
