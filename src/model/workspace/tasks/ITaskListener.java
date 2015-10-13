package model.workspace.tasks;

import model.workspace.WorkspaceAction;

public interface ITaskListener
{
	/**
	 * Is called once the specified task has been completed.
	 * @param workspaceAction
	 */
	void notifyOfTaskCompleted(final WorkspaceAction workspaceAction);
}