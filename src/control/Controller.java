package control;

import model.workspace.Workspace;
import model.workspace.WorkspaceAction;
import model.workspace.tasks.ITaskListener;
import model.workspace.tasks.Task_WorkspaceTask;
import javafx.fxml.Initializable;

public abstract class Controller implements Initializable, ITaskListener
{
	// Holds and administrates data contained in one (specified) directory. 
	protected Workspace workspace;
	
	public void setWorkspace(Workspace workspace)
	{
		this.workspace = workspace;
	}
	
	@Override
	public void notifyOfTaskCompleted(final WorkspaceAction workspaceAction)
	{
	}
}
