package control;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

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
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_LoadController.");
		
		// Init file chooser.
		directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose Workspace");
	}
	
	@FXML
	public void openFileBrowser(ActionEvent event)
	{
		File workspaceDirectory = directoryChooser.showDialog(null);
		textfield_directory.setText(workspaceDirectory.getAbsolutePath());
		
	}
	
}
