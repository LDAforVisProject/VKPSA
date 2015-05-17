package control;

import java.net.URL;
import java.util.ResourceBundle;

import model.workspace.WorkspaceAction;
import model.workspace.tasks.Task_LoadRawData;
import model.workspace.tasks.Task_WorkspaceTask;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;

public class PreprocessingController extends Controller
{
	// Buttons.
	
	@FXML Button button_calculateDistances;
	@FXML Button button_calculateMDSCoordinates;
	
	// Checkboxes.
	
	@FXML CheckBox checkbox_appendToDistanceMatrix;
	@FXML CheckBox checkbox_appendToMDSCoordinateMatrix;

	@FXML CheckBox checkbox_bhattacharyya;
	@FXML CheckBox checkbox_hellinger;
	@FXML CheckBox checkbox_jensenshannon;
	@FXML CheckBox checkbox_kullbackleibler;
	@FXML CheckBox checkbox_l2;
	
	@FXML CheckBox checkbox_hausdorff;
	@FXML CheckBox checkbox_avgmin;
	
	// Comboboxes.
	
	@FXML ComboBox<String> combobox_scalingAlgorithm;
	
	// Labels.
	
	@FXML Label label_numberOfDatasets;
	@FXML Label label_numberOfDistanceCalculations;
	
	// Progress indicators.
	
	@FXML ProgressIndicator progressIndicator_calculateMDSCoordinates;
	@FXML ProgressIndicator progressIndicator_distanceCalculation;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_PreprocessingController.");
	}
	
	@FXML
	public void calculate(ActionEvent event)
	{
		if (!(event.getSource() instanceof Button))
			return;
		
        // Get event source.
		Button source = (Button) event.getSource();
		
		switch (source.getId()) {
			case "button_calculateDistances":
				// Check if raw data has already been loaded. If not: Load it.
				if (!workspace.isRawDataLoaded()) {
					System.out.println("Loading raw data.");
					workspace.executeWorkspaceAction(WorkspaceAction.LOAD_RAW_DATA, progressIndicator_distanceCalculation.progressProperty(), this);
				}
				
				// Raw data has already been loaded: Calculate distances.
				else {
					System.out.println("Calculating distances");
					workspace.executeWorkspaceAction(WorkspaceAction.CALCULATE_DISTANCES, progressIndicator_distanceCalculation.progressProperty(), this);
				}
				
			break;
			
			case "button_calculateMDSCoordinates":
				// If distance is already loaded: Calculate MDS coordinates.
				if (workspace.isDistanceDataLoaded()) {
					System.out.println("Calculating MDS coordinates");
					workspace.executeWorkspaceAction(WorkspaceAction.CALCULATE_MDS_COORDINATES, progressIndicator_calculateMDSCoordinates.progressProperty(), this);
				}
				
				// Otherwise: Load distance data, then calculate MDS coordinates.
				else {
					// If .dis file exists: Load it, then calculate MDS data.
					if (workspace.containsDISFile()) {
						System.out.println("Loading distance data.");
						workspace.executeWorkspaceAction(WorkspaceAction.LOAD_DISTANCES, progressIndicator_calculateMDSCoordinates.progressProperty(), this);
					}
					
					// Otherwise: Prompt user to calculate distance data first.
					else {
						System.out.println("### WARNING ### Distance data is required, but not yet calculated.");
					}
				}
			break;			
		}
	}
	
	@Override
	public void notifyOfTaskCompleted(final WorkspaceAction workspaceAction)
	{
		switch (workspaceAction) {
			case LOAD_RAW_DATA:
				System.out.println("Finished loading raw data. Calculating distances.");
				workspace.executeWorkspaceAction(WorkspaceAction.CALCULATE_DISTANCES, progressIndicator_distanceCalculation.progressProperty(), this);
			break;
			
			case LOAD_DISTANCES:
				System.out.println("Finished loading distance data. Calculating MDS coordinates.");
				//workspace.executeWorkspaceAction(WorkspaceAction.CALCULATE_MDS_COORDINATES, progressIndicator_distanceCalculation.progressProperty(), this);
			break;
			
			case CALCULATE_DISTANCES:
				System.out.println("Finished calculating distances.");
			break;
			
			case CALCULATE_MDS_COORDINATES:
				System.out.println("Finished calculating MDS coordinates.");
			break;			
		}
	}
}
