package control.dataView;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import model.workspace.WorkspaceAction;
import model.workspace.tasks.Task_LoadRawData;
import model.workspace.tasks.WorkspaceTask;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;

public class PostprocessingController extends DataSubViewController
{
	// Buttons.
	
	private @FXML Button button_calculateDistances;
	private @FXML Button button_calculateMDSCoordinates;
	
	// Checkboxes.
	
	private @FXML CheckBox checkbox_appendToDistanceMatrix;
	private @FXML CheckBox checkbox_appendToMDSCoordinateMatrix;

	private @FXML CheckBox checkbox_bhattacharyya;
	private @FXML CheckBox checkbox_hellinger;
	private @FXML CheckBox checkbox_jensenshannon;
	private @FXML CheckBox checkbox_kullbackleibler;
	private @FXML CheckBox checkbox_l2;
	
	private @FXML CheckBox checkbox_hausdorff;
	private @FXML CheckBox checkbox_avgmin;
	
	// Comboboxes.
	
	private @FXML ComboBox<String> combobox_scalingAlgorithm;
	
	// Progress indicators.
	
	private @FXML ProgressIndicator progressIndicator_calculateMDSCoordinates;
	private @FXML ProgressIndicator progressIndicator_distanceCalculation;
	
	// Option set.
	private Map<String, Integer> optionSet;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_PostprocessingController.");
	}
	
	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		Map<String, Integer> optionSet = new HashMap<String, Integer>();
		
		// Add option for appending to existing distance matrix.
		optionSet.put("forceDistanceRecalculation", checkbox_appendToDistanceMatrix.isSelected() ? 0 : 1);
		// Add option for appending for loading only topics without an existing distance matrix.
		optionSet.put("loadOnlyDataNecessaryForDistanceCalculation", optionSet.get("forceDistanceRecalculation"));
		
		return optionSet;
	}
	
	@FXML
	public void calculate(ActionEvent event)
	{
		if (!(event.getSource() instanceof Button))
			return;
		
        // Get event source.
		Button source = (Button) event.getSource();
		
		// Prepare option set.
		optionSet = prepareOptionSet();
		
		// Act according to used button.
		switch (source.getId()) {
			case "button_calculateDistances":
				// Check if raw data has already been loaded. If not: Load it.
				if (!workspace.isRawDataLoaded()) {
					System.out.println("Loading raw data.");
					log("Loading raw data.");
					
					workspace.executeWorkspaceAction(WorkspaceAction.LOAD_RAW_DATA, progressIndicator_distanceCalculation.progressProperty(), this, optionSet);
				}
				
				// Raw data has already been loaded: Calculate distances.
				else {
					System.out.println("Calculating distances.");
					log("Calculating distances.");
					
					workspace.executeWorkspaceAction(WorkspaceAction.CALCULATE_DISTANCES, progressIndicator_distanceCalculation.progressProperty(), this, optionSet);
				}
				
			break;
			
			case "button_calculateMDSCoordinates":
				// If distance is already loaded: Calculate MDS coordinates.
				if (workspace.isDistanceDataLoaded()) {
					System.out.println("Calculating MDS coordinates.");
					log("Calculating MDS coordinates.");
					
					workspace.executeWorkspaceAction(WorkspaceAction.CALCULATE_MDS_COORDINATES, progressIndicator_calculateMDSCoordinates.progressProperty(), this, null);
				}
				
				// Otherwise: Load distance data, then calculate MDS coordinates.
				else {
					// If .dis data exists: Load it, then calculate MDS data.
					if (workspace.readNumberOfDatasetsInDISTable() > 0 &&
						workspace.getNumberOfDatasetsInDISTable() == workspace.getNumberOfDatasetsInWS()) {
						System.out.println("Loading distance data.");
						log("Loading distance data.");
						
						workspace.executeWorkspaceAction(WorkspaceAction.LOAD_DISTANCES, progressIndicator_calculateMDSCoordinates.progressProperty(), this, null);
					}
					
					// Otherwise: Prompt user to calculate distance data first.
					else {
						System.out.println("### WARNING ### Distance data is required, but not consistent or not existent. " + workspace.getNumberOfDatasetsInDISTable() + "; " + workspace.readNumberOfDatasetsInDISTable()  + ", " + workspace.getNumberOfDatasetsInWS());
					}
				}
			break;			
		}
	}
	
	@FXML
	public void generateData(ActionEvent e)
	{
		System.out.println("Generating Data");
	}
	
	@Override
	public void notifyOfTaskCompleted(final WorkspaceAction workspaceAction)
	{
		switch (workspaceAction) {
			case LOAD_RAW_DATA:
				System.out.println("Finished loading raw data. Calculating distances.");
				log("Finished loading raw data. Calculating distances.");
				
				workspace.executeWorkspaceAction(WorkspaceAction.CALCULATE_DISTANCES, progressIndicator_distanceCalculation.progressProperty(), this, optionSet);
			break;
			
			case LOAD_DISTANCES:
				System.out.println("Finished loading distance data. Calculating MDS coordinates.");
				log("Finished loading distance data. Calculating MDS coordinates.");
				
				workspace.executeWorkspaceAction(WorkspaceAction.CALCULATE_MDS_COORDINATES, progressIndicator_distanceCalculation.progressProperty(), this, null);
			break;
			
			case CALCULATE_DISTANCES:
				System.out.println("Finished calculating distances.");
				log("Finished calculating distances.");
				
				// Check if all data is avaible and analysis view may be used.
				dataViewController.checkOnDataAvailability();
			break;
			
			case CALCULATE_MDS_COORDINATES:
				System.out.println("Finished calculating MDS coordinates.");
				log("Finished calculating MDS coordinates.");
				
				// Check if all data is avaible and analysis view may be used.
				dataViewController.checkOnDataAvailability();
			break;			
		}
	}

	@Override
	public void freezeOptionControls()
	{
		button_calculateDistances.setDisable(true);
		button_calculateMDSCoordinates.setDisable(true);
		
		checkbox_appendToDistanceMatrix.setDisable(true);
		checkbox_appendToMDSCoordinateMatrix.setDisable(true);

		 checkbox_bhattacharyya.setDisable(true);
		 checkbox_hellinger.setDisable(true);
		 checkbox_jensenshannon.setDisable(true);
		 checkbox_kullbackleibler.setDisable(true);
		 checkbox_l2.setDisable(true);
		
		 checkbox_hausdorff.setDisable(true);
		 checkbox_avgmin.setDisable(true);
		 
		 combobox_scalingAlgorithm.setDisable(true);
	}

	@Override
	public void unfreezeOptionControls()
	{
		button_calculateDistances.setDisable(false);
		button_calculateMDSCoordinates.setDisable(false);
		
		checkbox_appendToDistanceMatrix.setDisable(false);
//		checkbox_appendToMDSCoordinateMatrix.setDisable(false);

//		 checkbox_bhattacharyya.setDisable(false);
//		 checkbox_hellinger.setDisable(false);
//		 checkbox_jensenshannon.setDisable(false);
//		 checkbox_kullbackleibler.setDisable(false);
//		 checkbox_l2.setDisable(false);
		
//		 checkbox_hausdorff.setDisable(false);
//		 checkbox_avgmin.setDisable(false);
		 
//		 combobox_scalingAlgorithm.setDisable(false);
	}


	@Override
	public void resizeContent(double width, double height)
	{
	}
	
	public ProgressIndicator getProgressIndicator_calculateMDSCoordinates()
	{
		return progressIndicator_calculateMDSCoordinates;
	}

	public ProgressIndicator getProgressIndicator_distanceCalculation()
	{
		return progressIndicator_distanceCalculation;
	}
}