package control;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import model.workspace.WorkspaceAction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

public class LoadController extends Controller
{
	private @FXML TextField textfield_directory;
	private @FXML Button button_browse;
	private @FXML Button button_load;
	private @FXML ProgressIndicator progressIndicator_load;
	
	private DirectoryChooser directoryChooser;
	private String currentPath;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_LoadController.");
		
		// Init file chooser.
		directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose Workspace");
		
		currentPath = "";
	}
	
	@FXML
	public void openFileBrowser(ActionEvent event)
	{
		currentPath = directoryChooser.showDialog(null).getAbsolutePath();
		textfield_directory.setText(currentPath);
	}
	
	@FXML
	public void loadData(ActionEvent event)
	{
		if ( Files.exists(Paths.get(currentPath)) ) {
			// Load data from new directory.
			if (currentPath != workspace.getDirectory()) {
				// Reset workspace.
				workspace.executeWorkspaceAction(WorkspaceAction.RESET);
				// Set new path.
				workspace.setDirectory(currentPath);
				// Load data, refresh visualizations.
				workspace.executeWorkspaceAction(WorkspaceAction.LOAD_MDS_COORDINATES);
			}
			
			// Specified is current directory.
			else {
				
			}
		}
		
		// Directory doesn't exist.
		else {
			
		}
	}
	
	/**
	 * Resets variables to fit current settings.
	 */
	public void reset()
	{
		currentPath = workspace.getDirectory(); 
	}
}
