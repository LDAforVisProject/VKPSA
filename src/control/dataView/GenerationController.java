package control.dataView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import javax.xml.ws.handler.MessageContext.Scope;

import model.LDAConfiguration;
import model.workspace.WorkspaceAction;
import model.workspace.tasks.Task_CollectMetadata;

import org.controlsfx.control.RangeSlider;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class GenerationController extends DataSubViewController
{
	// -----------------------------------------------
	// 				UI elements.
	// -----------------------------------------------
	
	@FXML private GridPane parameterConfiguration_gridPane;
	
	@FXML private BarChart<String, Integer> alpha_barchart;
	@FXML private BarChart<String, Integer> eta_barchart;
	@FXML private BarChart<String, Integer> kappa_barchart;
	
	@FXML private TextField alpha_min_textfield;
	@FXML private TextField alpha_max_textfield;
	@FXML private TextField eta_min_textfield;
	@FXML private TextField eta_max_textfield;
	@FXML private TextField kappa_min_textfield;
	@FXML private TextField kappa_max_textfield;
	@FXML private TextField numberOfDatasets_textfield;
	@FXML private TextField numberOfDivisions_textfield;
	
	@FXML private Button generate_button;
	
	@FXML private CheckBox parameterCoupling_checkbox;
	@FXML private CheckBox includePreprocessing_checkbox;
	
	@FXML private ComboBox<String> sampling_combobox;

	@FXML private ScrollPane parameter_scrollPane;
	
	@FXML private ProgressIndicator generate_progressIndicator;
	
	private Map<String, RangeSlider> rangeSliders;
	private Map<String, VBox> vBoxes;
	
	// -----------------------------------------------
	// 				Content data.
	// -----------------------------------------------
	
	private Map<String, ArrayList<Double>> parameterValues;
	private int numberOfDatasetsToGenerate;
	private int numberOfDivisions;
	
	
	// -----------------------------------------------
	// 				Methods.
	// -----------------------------------------------
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing SII_GenerationController.");
		
		parameterValues = new HashMap<String, ArrayList<Double>>();
		
		// Init UI elements.
		initUIElements();
		
		// Generate initial parameter values (and histograms).
		generateParameterValues();
	}
	
	/**
	 * Updates UI as created with SceneBuilder with better fitting controls.
	 * @return
	 */
	private void initUIElements() 
	{
		// Init UI element collections.
		//spinners		= new HashMap<String, Spinner<Float>>();
		rangeSliders	= new HashMap<String, RangeSlider>();
		vBoxes			= new HashMap<String, VBox>();
		
		// Remove deprecated UI elements in parameter configuration grid pane.
		int vboxCount = 0;
		for (Object child : parameterConfiguration_gridPane.getChildren().toArray()) {
			Node node = (Node)child;
			
			//System.out.println(node.getClass().getName());
			switch (node.getClass().getName()) 
			{
	    		// Replace TextFields with numeric steppers / spinners.
	    		case "javafx.scene.control.TextField":
	    			// Set initial values.
	    			if (node.getId().contains("min"))
	    				((TextField)node).setText("0");
	    			else
	    				((TextField)node).setText("100");
	    		break;
	    		
	    		case "javafx.scene.layout.VBox":
	    			VBox currentVBox = ((VBox)node);
	    			// Look for Sliders in VBox nodes; replace with RangeSliders.
	    			for (Object vboxChild : currentVBox.getChildren().toArray()) {
	    				Node vboxNode = (Node)vboxChild;
	    				
	    				if (vboxNode.getClass().getName() == "javafx.scene.control.Slider") {
	    					currentVBox.getChildren().remove(vboxNode);
	    				}
	    			}
	    			
	    			// Store references to VBoxes.
	    			switch(vboxCount++)
	    			{
	    				case 0:
	    					vBoxes.put("alpha", currentVBox);
	    				break;
	    				
	    				case 1:
	    					vBoxes.put("eta", currentVBox);
	    				break;
	    				
	    				case 2:
	    					vBoxes.put("kappa", currentVBox);
	    				break;
	    			}
	    		break;
	    	}
	    }
		
		// Init range sliders.
		initRangeSliders();
		
		// Init textfields.
		initTextFields();
		
		// Init combobox.
		initComboBoxes();
	}
	
	/**
	 * Initialize ComboBox UI elements.
	 */
	private void initComboBoxes()
	{
		sampling_combobox.getItems().clear();
		sampling_combobox.getItems().add("Random");
		sampling_combobox.getItems().add("Cartesic");
		sampling_combobox.getItems().add("Latin Hypercube");
		sampling_combobox.setValue("Random");
	}
	
	/**
	 * Initialize TextField UI elements.
	 */
	private void initTextFields()
	{
		alpha_min_textfield.setText(String.valueOf(rangeSliders.get("alpha").getLowValue()));
		alpha_max_textfield.setText(String.valueOf(rangeSliders.get("alpha").getHighValue()));
		eta_min_textfield.setText(String.valueOf(rangeSliders.get("eta").getLowValue()));
		eta_max_textfield.setText(String.valueOf(rangeSliders.get("eta").getHighValue()));
		kappa_min_textfield.setText(String.valueOf(rangeSliders.get("kappa").getLowValue()));
		kappa_max_textfield.setText(String.valueOf(rangeSliders.get("kappa").getHighValue()));
		
		try {
			numberOfDatasetsToGenerate	= Integer.parseInt(numberOfDatasets_textfield.getText());
			numberOfDivisions			= Integer.parseInt(numberOfDivisions_textfield.getText());
			
			numberOfDatasets_textfield.setDisable(true);
		}
		
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialize RangeSlider UI elements.
	 */
	private void initRangeSliders()
	{
		rangeSliders.put("alpha", new RangeSlider());
		rangeSliders.put("eta", new RangeSlider());
		rangeSliders.put("kappa", new RangeSlider());
		
		for (Map.Entry<String, RangeSlider> entry : rangeSliders.entrySet()) {
			RangeSlider rs = entry.getValue();
			
			rs.setMaxWidth(360);
			rs.setMax(25);
			rs.setMajorTickUnit(5);
			rs.setMinorTickCount(29);
			rs.setSnapToTicks(true);
			rs.setShowTickLabels(true);
			rs.setShowTickMarks(true);
			rs.setLowValue(0);
			rs.setHighValue(25);
			
			// Get some distance between range sliders and bar charts.
			rs.setPadding(new Insets(10, 0, 0, 0));
			
			addEventHandlerToRangeSlider(rs, entry.getKey());
		}
		
		// Set variable-specific minima and maxima.
		rangeSliders.get("kappa").setMin(1);
		rangeSliders.get("kappa").setMax(50);
		rangeSliders.get("kappa").setHighValue(50);
		
		// Add to respective parents.
		vBoxes.get("alpha").getChildren().add(rangeSliders.get("alpha"));
		vBoxes.get("eta").getChildren().add(rangeSliders.get("eta"));
		vBoxes.get("kappa").getChildren().add(rangeSliders.get("kappa"));
	}

	private void addEventHandlerToRangeSlider(RangeSlider rs, String parameter)
	{
		// Add listener to determine position during after release.
		rs.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	// Update control values after slider values where changed.
            	updateControlValues(rs, parameter);
            	
            	// Re-generate parameter values.
            	generateParameterValues();
            };
        });
		
		// Add listener to determine position during mouse drag.
		rs.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	// Update control values after slider values where changed.
            	updateControlValues(rs, parameter);
            };
        });
	}
	
	/**
	 * Updates control values after slide event ended.
	 * @param rs
	 * @param parameter
	 */
	private void updateControlValues(RangeSlider rs, String parameter)
	{
		Map<String, Double> parameterValues_low		= new HashMap<String, Double>();
		Map<String, Double> parameterValues_high	= new HashMap<String, Double>();
		
		for (String param : rangeSliders.keySet()) {
			parameterValues_low.put(param, rs.getLowValue() >= rangeSliders.get(param).getMin() ? rs.getLowValue() : rangeSliders.get(param).getMin());
			parameterValues_high.put(param, rs.getHighValue() <= rangeSliders.get(param).getMax() ? rs.getHighValue() : rangeSliders.get(param).getMax());
		}
		
		if (!parameterCoupling_checkbox.isSelected()) {
        	switch (parameter) 
        	{
        		case "alpha":
        			alpha_min_textfield.setText(String.valueOf(parameterValues_low.get("alpha")));
                	alpha_max_textfield.setText(String.valueOf(parameterValues_high.get("alpha")));
        		break;
        		
        		case "eta":
        	       	eta_min_textfield.setText(String.valueOf(parameterValues_low.get("eta")));
                	eta_max_textfield.setText(String.valueOf(parameterValues_high.get("eta")));
            	break;
            		
        		case "kappa":
        			kappa_min_textfield.setText(String.valueOf(parameterValues_low.get("kappa")));
                	kappa_max_textfield.setText(String.valueOf(parameterValues_high.get("kappa")));
            	break;	
        	}            		
    	}
    	
    	else {
			alpha_min_textfield.setText(String.valueOf(parameterValues_low.get(parameter)));
        	alpha_max_textfield.setText(String.valueOf(parameterValues_high.get(parameter)));
		 	eta_min_textfield.setText(String.valueOf(parameterValues_low.get(parameter)));
        	eta_max_textfield.setText(String.valueOf(parameterValues_high.get(parameter)));
    		kappa_min_textfield.setText(String.valueOf(parameterValues_low.get(parameter)));
        	kappa_max_textfield.setText(String.valueOf(parameterValues_high.get(parameter)));
        	
        	for (RangeSlider rangeSlider : rangeSliders.values()) {
        		rangeSlider.setLowValue(parameterValues_low.get(parameter));
        		rangeSlider.setHighValue(parameterValues_high.get(parameter));
        	}
    	}
	}
	
	@FXML
	private void updateNumberOfDatasets(ActionEvent e)
	{
		try {
			numberOfDatasetsToGenerate = Integer.parseInt(numberOfDatasets_textfield.getText());
			
			switch(sampling_combobox.getValue())
			{
				// If sampling mode == random: Number of datasets to generate equals number of divisions for each parameter.
				case "Random":
					numberOfDivisions = numberOfDatasetsToGenerate;
					numberOfDivisions_textfield.setText(String.valueOf(numberOfDivisions));
				break;
				
				case "Cartesic":
				break;
				
				case "Hypercube":
				break;
			}
			
			// Update scented widget histograms.
			generateParameterValues();
		}
		
		catch (NumberFormatException ex) {
			System.out.println("Only numbers are accepted. Correct input.");
		}
	}
	
	@FXML
	private void updateNumberOfDivisions(ActionEvent e)
	{
		try {
			numberOfDivisions = Integer.parseInt(numberOfDivisions_textfield.getText());
			
			switch(sampling_combobox.getValue())
			{
				// If sampling mode == random: Number of datasets to generate equals number of divisions for each parameter.				
				case "Random":
					numberOfDatasetsToGenerate = numberOfDivisions;
					numberOfDatasets_textfield.setText(String.valueOf(numberOfDatasetsToGenerate));
				break;
				
				case "Cartesic":
				break;
				
				case "Hypercube":
				break;
			}
			
			// Update scented widget histograms.
			generateParameterValues();
		}
		
		catch (NumberFormatException ex) {
			System.out.println("Only numbers are accepted. Correct input.");
		}
	}
	
	/**
	 * Generate parameter values based on defined parameter intervals and the number of datasets to create.
	 */
	private void generateParameterValues()
	{
		/*
		 * Initialize variables for storage of the actual parameter data and prepare the histogram bins aggregating them.  
		 */
		
		// Init storage.
		parameterValues.clear();
		parameterValues.put("alpha", new ArrayList<Double>(numberOfDivisions));
		parameterValues.put("eta", new ArrayList<Double>(numberOfDivisions));
		parameterValues.put("kappa", new ArrayList<Double>(numberOfDivisions));
		
		// Init bins.
		final int numberOfBins 					= 50;
		Map<String, int[]> parameterBinLists	= new HashMap<String, int[]>();
		parameterBinLists.put("alpha", new int[numberOfBins]);
		parameterBinLists.put("eta", new int[numberOfBins]);
		parameterBinLists.put("kappa", new int[numberOfBins]);
		
		/*
		 * Generate numberOfDivisions values for each parameter.
		 */
		
		// Init random number generator.
		Random randomGenerator	= new Random();
		// Generated values are allowed to be up to rangeSlider.getHighValue() and as low as rangeSlider.getLowValue().
		double intervalAlpha 	= rangeSliders.get("alpha").getHighValue() - rangeSliders.get("alpha").getLowValue();
		double intervalEta		= rangeSliders.get("eta").getHighValue() - rangeSliders.get("eta").getLowValue();
		double intervalKappa	= rangeSliders.get("kappa").getHighValue() - rangeSliders.get("kappa").getLowValue();

		// Generate random parameter values.		
		for (int i = 0; i < numberOfDivisions; i++) {
			parameterValues.get("alpha").add(rangeSliders.get("alpha").getLowValue() + randomGenerator.nextFloat() * intervalAlpha);
			parameterValues.get("eta").add(rangeSliders.get("eta").getLowValue() + randomGenerator.nextFloat() * intervalEta);
			parameterValues.get("kappa").add(rangeSliders.get("kappa").getLowValue() + randomGenerator.nextFloat() * intervalKappa);
		}
		
		/*
		 * Bin data for use in histograms/scented widgets.
		 */
		
		// Bin data.
		for (Map.Entry<String, ArrayList<Double>> entry : parameterValues.entrySet()) {
			double binInterval = (rangeSliders.get(entry.getKey()).getMax() - rangeSliders.get(entry.getKey()).getMin()) / numberOfBins;
			
			// Check every value and assign it to the correct bin.
			for (double value : entry.getValue()) {
				int index_key = (int) ( (value - rangeSliders.get(entry.getKey()).getMin()) / binInterval);
				// Check if element is highest allowed entry.
				index_key = index_key < numberOfBins ? index_key : numberOfBins - 1;
				
				// Increment content of corresponding bin.
				parameterBinLists.get(entry.getKey())[index_key]++;	
			}
		}
		
		/*
		 * Transfer data to scented widgets.
		 */
		
		// Clear old data.
		alpha_barchart.getData().clear();
		eta_barchart.getData().clear();
		kappa_barchart.getData().clear();

		// Add data series to barcharts.
		alpha_barchart.getData().add(generateParameterHistogramDataSeries("alpha", parameterBinLists, numberOfBins));
		eta_barchart.getData().add(generateParameterHistogramDataSeries("eta", parameterBinLists, numberOfBins));
		kappa_barchart.getData().add(generateParameterHistogramDataSeries("kappa", parameterBinLists, numberOfBins));
	}
	
	private XYChart.Series<String, Integer> generateParameterHistogramDataSeries(String key, Map<String, int[]> parameterBinLists, final int numberOfBins)
	{
		final XYChart.Series<String, Integer> data_series = new XYChart.Series<String, Integer>();
		
		for (int i = 0; i < numberOfBins; i++) {
			int binContent = parameterBinLists.get(key)[i];
			data_series.getData().add(new XYChart.Data<String, Integer>(String.valueOf(i), binContent ));
		}
		
		return data_series;
	}
	
	@FXML
	private void generateData(ActionEvent e)
	{
		
		dataViewController.freezeControls();
		
		// Create LDA configurations out of parameter lists, transfer to workspace.
		workspace.setConfigurationsToGenerate(LDAConfiguration.generateLDAConfigurations( numberOfDivisions, numberOfDatasetsToGenerate, sampling_combobox.getValue(), parameterValues ));
		
		// Write list to file.
		workspace.executeWorkspaceAction(WorkspaceAction.GENERATE_PARAMETER_LIST, generate_progressIndicator.progressProperty(), this);
	}
	
	@Override
	public void notifyOfTaskCompleted(final WorkspaceAction workspaceAction)
	{
		switch (workspaceAction) 
		{
			case GENERATE_PARAMETER_LIST:
				System.out.println("Finished generating the parameter list.");

				// Call Python script and execute data.
				generate_progressIndicator.progressProperty().unbind();
				workspace.executeWorkspaceAction(WorkspaceAction.GENERATE_DATA, generate_progressIndicator.progressProperty(), this);
			break;
			
			case GENERATE_DATA:
				System.out.println("Generated topic data.");
				
				// Workaround to keep progress indicator from flipping back to 0%.
				generate_progressIndicator.progressProperty().unbind();
				generate_progressIndicator.progressProperty().set(1);

				// Reset workspace data.
				workspace.executeWorkspaceAction(WorkspaceAction.RESET, null, this);
				
				// Collect metadata from raw topic files.
				workspace.executeWorkspaceAction(WorkspaceAction.COLLECT_METADATA, generate_progressIndicator.progressProperty(), this);
			break;
			
			// After workspace variables are resetted and file metadata was parsed anew: Preprocess data if desired.
			case COLLECT_METADATA:
				System.out.println("[Post-generation] Collected file metadata.");
				
				// If "Include preprocessing" is enabled: Start preprocessing.
				if (includePreprocessing_checkbox.isSelected()) {
					// Load raw data.
					generate_progressIndicator.progressProperty().unbind();
					workspace.executeWorkspaceAction(WorkspaceAction.LOAD_RAW_DATA, generate_progressIndicator.progressProperty(), this);
				}
				
				// Else: Display warning (workspace is inconsistent). Refresh not necessary, since - apart from the already collected
				// topic file metadata - no new metadata was generated. 
				else {
					System.out.println("### WARNING ### Workspace is inconsistent - data has to be preprocessed. Apply preprocessing manually.");
					
					// Unfreeeze controls.
					dataViewController.unfreezeControls();
				}
			break;
			
			case LOAD_RAW_DATA:
				System.out.println("[Post-generation] Loaded raw topic data.");
				
				// Calculate distances.
				workspace.executeWorkspaceAction(WorkspaceAction.CALCULATE_DISTANCES, dataViewController.getProgressIndicator_distanceCalculation().progressProperty(), this);
			break;
			
			case CALCULATE_DISTANCES:
				System.out.println("[Post-generation] Calculated distance data.");
				
				// Calculate distances.
				workspace.executeWorkspaceAction(WorkspaceAction.CALCULATE_MDS_COORDINATES, dataViewController.getProgressIndicator_calculateMDSCoordinates().progressProperty(), this);
			break;
			
			case CALCULATE_MDS_COORDINATES:
				System.out.println("[Post-generation] Calculated MDS data.");
				
				// Data generation and preprocessing finished - unfreeze controls.
				dataViewController.unfreezeControls();
				
				// Check if all data is avaible and analysis view may be used.
				dataViewController.checkOnDataAvailability();
			break;
		}
	}
	
	@Override
	public void freezeOptionControls()
	{
		includePreprocessing_checkbox.setDisable(true);
		parameterConfiguration_gridPane.setDisable(true);
		
		for (RangeSlider rs : rangeSliders.values()) {
			rs.setDisable(true);
		}
		
		parameterCoupling_checkbox.setDisable(true);
		sampling_combobox.setDisable(true);
		numberOfDatasets_textfield.setDisable(true);
		numberOfDivisions_textfield.setDisable(true);
		
		generate_button.setDisable(true);
	}

	@Override
	public void unfreezeOptionControls()
	{
		includePreprocessing_checkbox.setDisable(false);
		parameterConfiguration_gridPane.setDisable(false);
		
		for (RangeSlider rs : rangeSliders.values()) {
			rs.setDisable(false);
		}
		
		parameterCoupling_checkbox.setDisable(false);
		sampling_combobox.setDisable(false);
		numberOfDatasets_textfield.setDisable(false);
		numberOfDivisions_textfield.setDisable(false);
		
		generate_button.setDisable(false);
	}

	@Override
	public void resizeContent(double width, double height)
	{
	}
}