package control;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Observable;
import java.util.ResourceBundle;

import model.workspace.WorkspaceAction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.DirectoryChooser;

public class LoadController extends Controller
{
	private @FXML TextField textfield_directory;
	private @FXML Button button_browse;
	private @FXML Button button_load;
	private @FXML ProgressIndicator progressIndicator_load;
	
	private @FXML Label label_datasetNumber;
	private @FXML Label label_numberMDSCoordinates;
	private @FXML Label label_disFound;
	private @FXML Label label_mdsFound;
	private @FXML Label label_consistency;
	private @FXML Shape shape_integrity;
	
	
	private DirectoryChooser directoryChooser;
	private String currentPath;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_LoadController.");
		
		// Init file chooser.
		directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Choose Workspace");
		
		// Set progress value.
		progressIndicator_load.setProgress(0);
		
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
				// Set new path.
				workspace.setDirectory(currentPath);
				
				// Switch to new directory. Bind progressIndicator to this task.
				workspace.executeWorkspaceAction(WorkspaceAction.SWITCH_DIRECTORY, progressIndicator_load.progressProperty(), this);
			}
			
			// Specified is current directory.
			else {
			}
		}
		
		// Directory doesn't exist.
		else {
			
		}
	}
	
	
	// Check workspace integrity, display info message as tooltip.
	@Override
	public void notifyOfTaskCompleted(final WorkspaceAction workspaceAction)
	{
		switch (workspaceAction) {
			case LOAD_MDS_COORDINATES:
				displayIntegrityCheck();
			break;

			default:
			break;
		}
	}
	
	private String displayIntegrityCheck()
	{
		boolean mdsFileExists	= workspace.containsMDSFile();
		boolean disFileExists	= workspace.containsDISFile();
		String message			= ""; 
		
		label_datasetNumber.setText(String.valueOf(workspace.getNumberOfDatasetsInWS()));
		label_numberMDSCoordinates.setText(String.valueOf(workspace.getNumberOfMDSCoordinatesInWS()));
		label_mdsFound.setText(String.valueOf(mdsFileExists));
		label_disFound.setText(String.valueOf(disFileExists));
		
		// Check workspace integrity. @todo Cross-check with .dis file to see if datasets in
		// directory and referenced datasets in .dis are the same.
		// Workspace may be consistent (green), incomplete (orange to yellow) or corrupted (red). 
		
		// Workspace not consistent: .mds and .dis files don't fit each other or the actual datasets.
		if (!workspace.checkMetadataIntegrity()) {
			shape_integrity.setFill(Color.RED);
			message = "Error: Workspace is not consistent. Check if .dis and .mds files match each "
					+ "other and datasets in this directory or preprocess raw topic data again.";
		}
		
		else if (!mdsFileExists && !disFileExists) {
			shape_integrity.setFill(Color.ORANGE);
			message = "Warning: Neither .mds or .dis file exist in this workspace. Run preprocessing on "
					+ "the raw topic data in this workspace.";
		}
		
		// One of both metadata file exists (and is consistent): Yellow.
		else if ( (mdsFileExists && !disFileExists) || (!mdsFileExists && disFileExists)) {
			shape_integrity.setFill(Color.YELLOW);
			message = "Warning: .mds or .dis file doesn't exist in this workspace. Run preprocessing on "
					+ "the raw topic data in this workspace.";
		}
		
		else if (mdsFileExists && disFileExists) {
			shape_integrity.setFill(Color.GREEN);
			message = "Success: Workspace is consistent and complete.";
		}
		
		return message;
	}
	
	/**
	 * Resets variables to fit current settings.
	 */
	public void reset()
	{
		currentPath = workspace.getDirectory(); 
	}
}
