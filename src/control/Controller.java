package control;

import model.workspace.Workspace;
import model.workspace.WorkspaceAction;
import model.workspace.tasks.ITaskListener;
import model.workspace.tasks.WorkspaceTask;
import javafx.fxml.Initializable;
import javafx.scene.Node;

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
	
	/**
	 * Resizes/repositions controller content manually.
	 * One of the two arguments is always zero, signalling that there is no change on this axis. 
	 * @param width
	 * @param height
	 */
	public abstract void resizeContent(double width, double height);
	
	/**
	 * Resizes one specific element of this view. Implementation is optional.
	 * @param node
	 * @param width
	 * @param height
	 */
	protected void resizeElement(Node node, double width, double height)
	{
	}
}
