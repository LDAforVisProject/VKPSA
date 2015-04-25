package control;

import model.Workspace;
import javafx.fxml.Initializable;

public abstract class Controller implements Initializable
{
	// Holds and administrates data contained in one (specified) directory. 
	protected Workspace workspace;
	
	public void setWorkspace(Workspace workspace)
	{
		this.workspace = workspace;
	}
}
