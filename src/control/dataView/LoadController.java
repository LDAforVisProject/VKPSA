package control.dataView;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import model.workspace.WorkspaceAction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.DirectoryChooser;

public class LoadController extends DataSubViewController
{
	private @FXML TextField textfield_directory;
	private @FXML Button button_browse;
	private @FXML ProgressIndicator progressIndicator_load;
	
	private @FXML Label label_datasetNumber;
	private @FXML Label label_numberDISdatasets;
	private @FXML Label label_numberMDSdatasets;
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
		
		// Hide progress indicator.
		progressIndicator_load.setVisible(false);
		
		currentPath = "";
	}
	
	public void setDataViewController(DataViewController dataViewController)
	{
		this.dataViewController = dataViewController;
	}
	
	@FXML
	public void openFileBrowser(ActionEvent event)
	{
		// Hide workspace chooser popup.
		dataViewController.toggleWorkspaceChooserStatus(false);
		
		// Get current path from file dialog.
		currentPath = directoryChooser.showDialog(null).getAbsolutePath();
		textfield_directory.setText(currentPath);

		// Show workspace chooser popup.
		dataViewController.toggleWorkspaceChooserStatus(false);
	
		// Show progress indicator.
		progressIndicator_load.setVisible(true);
		
		// Load workspace.
		loadData(event);
	}
	
	/**
	 * Loads file metadata.
	 * @param event
	 */
	@FXML
	public void loadData(ActionEvent event)
	{
		// Change path, reset directory, load everything anew.
		if ( Files.exists(Paths.get(currentPath)) ) {
			// Load data from new directory.
			if (currentPath != workspace.getDirectory()) {
				// 	Reset workspace.
				workspace.executeWorkspaceAction(WorkspaceAction.SWITCH, null, this);
				
				// Set new path.
				workspace.setDirectory(currentPath);
				
				// Collect file metadata. Execute other actions once file metadata reading is complete.
				workspace.executeWorkspaceAction(WorkspaceAction.COLLECT_FILE_METADATA, progressIndicator_load.progressProperty(), this);
			}
			
			// Specified is current directory.
			else {
				System.out.println("### WARNING ### Specified is current directory. Nothing to do here.");
			}
		}
		
		// Directory doesn't exist.
		else {
			System.out.println("### ERROR ### Directory " + currentPath + " doesn't exist.");			
		}
	}
	
	
	/** 
	 * Gets called once workspace action/task has been completed.
	 * @param workspaceAction
	 */
	@Override
	public void notifyOfTaskCompleted(final WorkspaceAction workspaceAction)
	{
		switch (workspaceAction) {
			case COLLECT_FILE_METADATA:
				System.out.println("Collected file metadata.");
				
				// If .dis exists: Load it.
				if (workspace.containsDISFile()) {
					// Load distance data.
					workspace.executeWorkspaceAction(WorkspaceAction.LOAD_DISTANCES, progressIndicator_load.progressProperty(), this);
				}
				
				// Otherwise: Execute and display integrity check, set summary as tooltip.
				else {
					// Load MDS coordinates.
					workspace.executeWorkspaceAction(WorkspaceAction.LOAD_MDS_COORDINATES, progressIndicator_load.progressProperty(), this);
				}
			break;
			
			case LOAD_DISTANCES:
				System.out.println("Loaded distance data.");
				
				// Once distance data is loaded: Check if .mds file exists. If so: load it.
				if (workspace.containsMDSFile()) {
					// Load MDS coordinates.
					workspace.executeWorkspaceAction(WorkspaceAction.LOAD_MDS_COORDINATES, progressIndicator_load.progressProperty(), this);					
				}
				
				// Otherwise: Execute and display integrity check, set summary as tooltip.
				else {
					showIntegrityCheckTooltip(displayIntegrityCheck());
				}
			break;
			
			case LOAD_MDS_COORDINATES:
				System.out.println("Loaded MDS coordinates.");
				
				// Once MDS coordinates are loaded: Execute and display integrity check.
				showIntegrityCheckTooltip(displayIntegrityCheck());
				dataViewController.setDataLoaded(true);
				
				// Check if all data is available.
				dataViewController.checkOnDataAvailability();
			break;

			default:
			break;
		}
	}
	
	private void showIntegrityCheckTooltip(String tooltipMessage)
	{
		Tooltip tooltip = new Tooltip(tooltipMessage);
		tooltip.setMaxWidth(250);
		tooltip.setAutoHide(false);
		tooltip.setWrapText(true);
		
        Tooltip.install(shape_integrity, tooltip);
	}
	
	private String displayIntegrityCheck()
	{
		boolean mdsFileExists	= workspace.containsMDSFile();
		boolean disFileExists	= workspace.containsDISFile();
		String message			= ""; 
		
		label_datasetNumber.setText(String.valueOf(workspace.getNumberOfDatasetsInWS()));
		label_numberDISdatasets.setText(String.valueOf(workspace.getNumberOfDatasetsInDISFile()));
		label_numberMDSdatasets.setText(String.valueOf(workspace.getNumberOfDatasetsInMDSFile()));
		label_mdsFound.setText(mdsFileExists ? "Yes" : "No");
		label_disFound.setText(disFileExists ? "Yes" : "No");
		
		// Check workspace integrity. Cross-check with .dis file to see if datasets in
		// directory and referenced datasets in .dis are the same.
		// Workspace may be consistent (green), incomplete (orange to yellow) or corrupted (red). 
		
		// Worskpace consistent, but neither .mds nor .dis exists:
		if (!mdsFileExists && !disFileExists) {
			label_consistency.setText("-");
			shape_integrity.setFill(Color.ORANGE);
			
			if (workspace.getNumberOfDatasetsInWS() > 0) {
				message = "Warning: Neither .mds or .dis file exist in this workspace. Run preprocessing on "
						+ "the raw topic data in this workspace.";
			}
			
			else {
				message = "Warning: Empty workspace. Generate data.";
			}
		}
		
		// Workspace consistent and one of both metadata file exists (and is consistent): Yellow.
		else if ( (mdsFileExists && !disFileExists) || (!mdsFileExists && disFileExists)) {
			label_consistency.setText("-");
			shape_integrity.setFill(Color.YELLOW);
			message = "Warning: .mds or .dis file doesn't exist in this workspace. Run preprocessing on "
					+ "the raw topic data in this workspace.";
		}
		
		// Both .mds and .dis exist. Conistent?
		else if (mdsFileExists && disFileExists) {
			// Workspace not consistent: .mds and .dis files don't fit each other or the actual datasets.
			if (!workspace.checkIntegrity()) {
				label_consistency.setText("No");
				shape_integrity.setFill(Color.RED);
				message = "Error: Workspace is not consistent. Check if .dis and .mds files match each "
						+ "other and datasets in this directory or preprocess raw topic data again.";
			}

			// Workspace consistent and complete:			
			else {
				label_consistency.setText("Yes");
				shape_integrity.setFill(Color.GREEN);
				message = "Success: Workspace is consistent and complete.";
			}
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

	@Override
	public void freezeOptionControls()
	{
	}

	@Override
	public void unfreezeOptionControls()
	{	
	}
}
