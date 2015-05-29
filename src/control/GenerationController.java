 package control;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import org.controlsfx.control.RangeSlider;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class GenerationController extends Controller
{
	@FXML
	private GridPane parameterConfiguration_gridPane;
	
	@FXML
	private BarChart<String, Integer> alpha_barchart;
	@FXML
	private BarChart<String, Integer> eta_barchart;
	@FXML
	private BarChart<String, Integer> kappa_barchart;
	
	@FXML
	private TextField alpha_min_textfield;
	@FXML
	private TextField alpha_max_textfield;
	@FXML
	private TextField eta_min_textfield;
	@FXML
	private TextField eta_max_textfield;
	@FXML
	private TextField kappa_min_textfield;
	@FXML
	private TextField kappa_max_textfield;
	
	@FXML
	private TextField numberOfDatasets_textfield;
	@FXML
	private TextField numberOfDivisions_textfield;
	
	@FXML
	private Button parameterCoupling_button;
	
	@FXML
	private ComboBox<String> sampling_combobox;

	private Map<String, RangeSlider> rangeSliders;
	private Map<String, VBox> vBoxes;
	
	private int numberOfDatasetsToGenerate;
	private int numberOfDivisions;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing SII_GenerationController.");
		
		//spinners		= new HashMap<String, Spinner<Float>>();
		rangeSliders	= new HashMap<String, RangeSlider>();
		vBoxes			= new HashMap<String, VBox>();
		
		// Init UI elements.
		initUIElements();
	}
	
	/**
	 * Updates UI as created with SceneBuilder with better fitting controls.
	 * @return
	 */
	private void initUIElements() 
	{
		// Parse pre-configured textfield valus.
		initTextFields();
		
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
			rs.setMax(100);
			rs.setMajorTickUnit(5);
			rs.setSnapToTicks(false);
			rs.setShowTickMarks(true);
			rs.setLowValue(0);
			rs.setHighValue(100);
			
			addEventHandlerToRangeSlider(rs, entry.getKey());
		}
		
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
            	updateTextFields(rs, parameter);
            	// Re-generate parameter values.
            	generateParameterValues();
            };
        });
		
		// Add listener to determine position during mouse drag.
		rs.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	updateTextFields(rs, parameter);
            };
        });
	}
	
	private void updateTextFields(RangeSlider rs, String parameter)
	{
    	if (!parameterCoupling_button.isDefaultButton()) {
        	switch (parameter) 
        	{
        		case "alpha":
        			alpha_min_textfield.setText(String.valueOf(rs.getLowValue()));
                	alpha_max_textfield.setText(String.valueOf(rs.getHighValue()));
        		break;
        		
        		case "eta":
        	       	eta_min_textfield.setText(String.valueOf(rs.getLowValue()));
                	eta_max_textfield.setText(String.valueOf(rs.getHighValue()));
            	break;
            		
        		case "kappa":
        			kappa_min_textfield.setText(String.valueOf(rs.getLowValue()));
                	kappa_max_textfield.setText(String.valueOf(rs.getHighValue()));
            	break;	
        	}            		
    	}
    	
    	else {
			alpha_min_textfield.setText(String.valueOf(rs.getLowValue()));
        	alpha_max_textfield.setText(String.valueOf(rs.getHighValue()));
		 	eta_min_textfield.setText(String.valueOf(rs.getLowValue()));
        	eta_max_textfield.setText(String.valueOf(rs.getHighValue()));
    		kappa_min_textfield.setText(String.valueOf(rs.getLowValue()));
        	kappa_max_textfield.setText(String.valueOf(rs.getHighValue()));
        	
        	for (RangeSlider rangeSlider : rangeSliders.values()) {
        		rangeSlider.setLowValue(rs.getLowValue());
        		rangeSlider.setHighValue(rs.getHighValue());
        	}
    	}
	}
	
	@FXML
	public void changeParameterCouplingMode(ActionEvent e)
	{
		parameterCoupling_button.setDefaultButton(!parameterCoupling_button.isDefaultButton());
		
		if (parameterCoupling_button.isDefaultButton())
			System.out.println("Enabled parameter coupling.");
		
		else
			System.out.println("Disabled parameter coupling.");
	}
	
	@FXML
	private void updateNumberOfDatasets(ActionEvent e)
	{
		try {
			numberOfDatasetsToGenerate = Integer.parseInt(numberOfDatasets_textfield.getText());
			System.out.println("New number of datasets to generate: " + numberOfDatasetsToGenerate);
			
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
			System.out.println("New number of divisions: " + numberOfDivisions);
			
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
		/**
		 * Initialize variables for storage of the actual parameter data and prepare the histogram bins aggregating them.  
		 */
		
		// Init storage.
		Map<String, ArrayList<Double>> parameterValues = new HashMap<String, ArrayList<Double>>();
		parameterValues.put("alpha", new ArrayList<Double>(numberOfDivisions));
		parameterValues.put("eta", new ArrayList<Double>(numberOfDivisions));
		parameterValues.put("kappa", new ArrayList<Double>(numberOfDivisions));
		
		// Init bins.
		final int numberOfBins 					= 100;
		Map<String, int[]> parameterBinLists	= new HashMap<String, int[]>();
		parameterBinLists.put("alpha", new int[numberOfBins]);
		parameterBinLists.put("eta", new int[numberOfBins]);
		parameterBinLists.put("kappa", new int[numberOfBins]);
		
		/**
		 * Generate numberOfDivisions values for each parameter.
		 */
		
		// Init random number generator.
		 Random randomGenerator = new Random();
		// Generated values are allowed to be up to rangeSlider.getHighValue() and as low as rangeSlider.getLowValue().
		double intervalAlpha 	= rangeSliders.get("alpha").getHighValue() - rangeSliders.get("alpha").getLowValue();
		double intervalEta		= rangeSliders.get("eta").getHighValue() - rangeSliders.get("eta").getLowValue();
		double intervalKappa	= rangeSliders.get("kappa").getHighValue() - rangeSliders.get("kappa").getLowValue();

		// Generate random parameter values.		
		for (int i = 0; i < numberOfDivisions; i++) {
			parameterValues.get("alpha").add(rangeSliders.get("alpha").getLowValue() + randomGenerator.nextFloat() * intervalAlpha);
//			System.out.println(parameterValues.get("alpha").get(parameterValues.get("alpha").size() - 1));
			parameterValues.get("eta").add(rangeSliders.get("eta").getLowValue() + randomGenerator.nextFloat() * intervalEta);
			parameterValues.get("kappa").add(rangeSliders.get("kappa").getLowValue() + randomGenerator.nextFloat() * intervalKappa);
		}
		
		/**
		 * @todo Bin data for use in histograms/scented widgets.
		 */
		// @todo Use .getHighValue() instead of .getMax()? What's more appropriate?
		double binInterval		= (rangeSliders.get("alpha").getMax() - rangeSliders.get("alpha").getMin()) / numberOfBins;
		// Bin data.
		for (Map.Entry<String, ArrayList<Double>> entry : parameterValues.entrySet()) {
			// Check every value and assign it to the correct bin.
			for (double value : entry.getValue()) {
				// Increment content of corresponding bin.
				parameterBinLists.get(entry.getKey())[(int) (value / binInterval)]++;
			}
		}
		
		for (int x : parameterBinLists.get("alpha")) {
			System.out.println(x);
		}
		System.out.println();
		/**
		 * Transfer data to scented widgets.
		 */
		
		// Clear old data.
		alpha_barchart.getData().clear();
		
		// @todo Find way to prevent BarChart from dynamically adjusting bar height per unit.
		// @todo Move BarCharts slightly to the left.
		
		// Add data to barcharts.
		
		
		// Add data series to barcharts.
		alpha_barchart.getData().add(generateParameterHistogramDataSeries("alpha", parameterBinLists));
	}
	
	private XYChart.Series<String, Integer> generateParameterHistogramDataSeries(String key, Map<String, int[]> parameterBinLists)
	{
		final XYChart.Series<String, Integer> data_series = new XYChart.Series<String, Integer>();
		System.out.println(rangeSliders.get(key).getMin() + " - " + rangeSliders.get(key).getMax());
		data_series.getData().add(new XYChart.Data<String, Integer>("-1", 10));
		for (int i = (int) rangeSliders.get(key).getMin(); i < rangeSliders.get(key).getMax(); i++) {
			int binContent = parameterBinLists.get(key)[i];
			System.out.println(binContent);
			data_series.getData().add(new XYChart.Data<String, Integer>(String.valueOf(i), binContent ));
			// If in defined range: Output parameter counts.
//			if (i >= rangeSliders.get(key).getLowValue() && i <= rangeSliders.get(key).getHighValue()) {
//				data_series.getData().add(new XYChart.Data<String, Float>(String.valueOf(i), (float)(i * 10) ));
//				
//			}
			// Otherwise: Output 0.
//			else {
//				data_series.getData().add(new XYChart.Data<String, Float>(String.valueOf(i), (float)0 ));
//			}
		}
		
		return data_series;
	}
}
