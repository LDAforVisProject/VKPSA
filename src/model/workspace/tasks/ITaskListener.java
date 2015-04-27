package model.workspace.tasks;

import model.workspace.WorkspaceAction;

public interface ITaskListener
{
	void notifyOfTaskCompleted(final WorkspaceAction workspaceAction);
}