package model.workspace.tasks;

import model.workspace.TaskType;

public interface ITaskListener
{
	/**
	 * Is called once the specified task has been completed.
	 * @param taskType
	 */
	void notifyOfTaskCompleted(final TaskType taskType);
}