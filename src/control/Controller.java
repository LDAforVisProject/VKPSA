package control;

import java.util.Map;

import model.workspace.Workspace;
import model.workspace.WorkspaceAction;
import model.workspace.tasks.ITaskListener;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;

public abstract class Controller implements Initializable, ITaskListener
{
	// -----------------------------------------------------
	//						Data
	// -----------------------------------------------------
	
	/**  
	 * Holds and administrates data contained in one (specified) directory.
	 */
	protected Workspace workspace;
	
	// Protocol components (should be accessible for every controller for logging reasons).
	
	/**
	 * Protocol pane's ProgressIndicator.
	 */
	protected ProgressIndicator log_protocol_progressindicator;
	/**
	 * Protocol pane's TextArea.
	 */
	protected TextArea log_protocol_textarea;
	
	// -----------------------------------------------------
	//					Methods
	// -----------------------------------------------------
	
	/**
	 * Set reference to workspace.
	 * @param workspace
	 */
	public void setWorkspace(Workspace workspace)
	{
		this.workspace = workspace;
	}
	
	/**
	 * Set references to protocol UI elements.
	 * @param log_protocol_progressindicator
	 * @param log_protocol_textarea
	 */
	public void setProtocolElements(ProgressIndicator log_protocol_progressindicator, TextArea log_protocol_textarea)
	{
		this.log_protocol_progressindicator = log_protocol_progressindicator;
		this.log_protocol_textarea			= log_protocol_textarea;
	}
	
	/**
	 * Adds specified line to the protocol panel.
	 * @param additionalLogString
	 */
	protected void log(String additionalLogString)
	{
		log_protocol_textarea.setText(log_protocol_textarea.getText() + "\n" + additionalLogString);
	}
	
	/**
	 * Sets indicator to busy/idle.
	 */
	protected void setProgressStatus(boolean isBusy)
	{
		log_protocol_progressindicator.setVisible(isBusy);		
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
	 * Provides an option set holding all relevant information for workspace in order to execute
	 * workspace actions intitated by the controller. 
	 * @return
	 */
	protected abstract Map<String, Integer> prepareOptionSet();
	
	/**
	 * Resizes one specific element of this view. Implementation is optional.
	 * @param node
	 * @param width
	 * @param height
	 */
	protected void resizeElement(Node node, double width, double height)
	{
	}

	public ProgressIndicator getLogProtocolProgressindicator()
	{
		return log_protocol_progressindicator;
	}

	public TextArea getLogProtocolTextarea()
	{
		return log_protocol_textarea;
	}
}
